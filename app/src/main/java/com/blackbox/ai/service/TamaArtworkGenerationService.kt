package com.blackbox.ai.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.blackbox.ai.R
import com.blackbox.ai.data.SettingsRepository
import com.blackbox.ai.data.db.AppDatabase
import com.blackbox.ai.onnx.OnnxImageGenConfig
import com.blackbox.ai.onnx.OnnxImageGenMode
import com.blackbox.ai.onnx.OnnxRuntimeBackend
import com.blackbox.ai.onnx.OnnxRuntimeOptions
import com.blackbox.ai.onnx.OnnxTxt2ImgPipeline
import com.blackbox.ai.onnx.isOnnxTxt2ImgBundle
import com.blackbox.ai.service.UnifiedNotificationManager.TaskType
import com.blackbox.ai.tama.game.PetMapper
import com.blackbox.ai.tama.game.TamaAgentService
import com.blackbox.ai.tama.game.TamaDeepDreamRunCoordinator
import com.blackbox.ai.tama.data.TamaArtworkStatus
import com.blackbox.ai.tama.data.TamaPet
import com.blackbox.ai.tama.db.TamaArtworkEntity
import com.blackbox.ai.tama.db.TamaDatabase
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class TamaArtworkGenerationService : Service() {
    inner class LocalBinder : Binder() {
        fun getService(): TamaArtworkGenerationService = this@TamaArtworkGenerationService
    }

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var processingJob: Job? = null
    private var notificationTaskId: Int? = null

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_PROCESS_QUEUE && processingJob?.isActive != true) {
            processingJob = serviceScope.launch {
                processQueue()
                stopForeground(STOP_FOREGROUND_REMOVE)
                notificationTaskId?.let { UnifiedNotificationManager.dismissTask(it) }
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        notificationTaskId?.let { UnifiedNotificationManager.dismissTask(it) }
    }

    private suspend fun processQueue() {
        val tamaDatabase = TamaDatabase.getInstance(applicationContext)
        val appDatabase = AppDatabase.getDatabase(applicationContext)
        val tamaDao = tamaDatabase.tamaDao()
        val modelDao = appDatabase.modelDao()
        tamaDao.requeueGeneratingArtworks()
        tamaDao.getDeepDreamRunsByStatuses(
            listOf(
                TamaDeepDreamRunCoordinator.STATUS_QUEUED,
                TamaDeepDreamRunCoordinator.STATUS_ARTWORK_RUNNING
            )
        ).map { it.petId }
            .distinct()
            .forEach { petId ->
                TamaDeepDreamRunCoordinator.reconcileRunsForPet(
                    context = applicationContext,
                    database = tamaDatabase,
                    petId = petId,
                    startArtworkIfNeeded = false
                )
            }

        val firstArtwork = tamaDao.getNextQueuedArtwork() ?: return
        startForegroundOnce(titleForArtwork(firstArtwork))

        var artwork: TamaArtworkEntity? = firstArtwork
        while (artwork != null) {
            val currentArtwork = artwork ?: break
            val startedArtwork = currentArtwork.copy(
                status = TamaArtworkStatus.GENERATING.name,
                startedAt = System.currentTimeMillis(),
                errorMessage = null
            )
            tamaDao.saveArtwork(startedArtwork)
            if (startedArtwork.kind == com.blackbox.ai.tama.data.TamaArtworkKind.DAILY_DREAM.name) {
                startedArtwork.albumId?.let { albumId ->
                    TamaDeepDreamRunCoordinator.markArtworkRunningForAlbum(
                        database = tamaDatabase,
                        albumId = albumId
                    )
                }
            }
            updateForeground(statusTextForArtwork(startedArtwork))

            val model = modelDao.getModelByFilename(startedArtwork.modelFilename)
            if (model == null || !model.isOnnxTxt2ImgBundle()) {
                tamaDao.saveArtwork(
                    startedArtwork.copy(
                        status = TamaArtworkStatus.FAILED.name,
                        errorMessage = getString(R.string.tama_pic_gen_selected_model_missing),
                        completedAt = System.currentTimeMillis()
                    )
                )
                artwork = tamaDao.getNextQueuedArtwork()
                continue
            }

            val outputPath = startedArtwork.filePath ?: File(
                applicationContext.filesDir,
                "tama_gallery/${startedArtwork.petId}/${startedArtwork.id}.png"
            ).absolutePath

            runCatching {
                OnnxTxt2ImgPipeline().generate(
                    config = OnnxImageGenConfig(
                        modelPath = model.path,
                        modelName = startedArtwork.modelLabel,
                        mode = OnnxImageGenMode.TXT2IMG,
                        prompt = startedArtwork.prompt,
                        negativePrompt = startedArtwork.negativePrompt,
                        width = startedArtwork.width,
                        height = startedArtwork.height,
                        steps = startedArtwork.steps,
                        cfgScale = startedArtwork.cfgScale,
                        seed = -1L,
                        requestedWidth = startedArtwork.width,
                        requestedHeight = startedArtwork.height,
                        backend = OnnxRuntimeBackend.CPU,
                        runtimeOptions = OnnxRuntimeOptions(),
                        outputPath = outputPath
                    ),
                    onProgress = { _, status -> updateForeground(status) }
                )
            }.onSuccess { result ->
                tamaDao.saveArtwork(
                    startedArtwork.copy(
                        status = TamaArtworkStatus.COMPLETED.name,
                        seed = result.seedUsed,
                        filePath = result.outputFile.absolutePath,
                        errorMessage = result.warningMessage,
                        completedAt = System.currentTimeMillis()
                    )
                )
                reconcileDeepDreamAlbum(tamaDatabase, startedArtwork)
                maybeRefreshDeepDreamSummary(tamaDao, startedArtwork)
            }.onFailure { error ->
                runCatching {
                    startedArtwork.filePath?.let(::File)?.takeIf { it.exists() }?.delete()
                }
                tamaDao.saveArtwork(
                    startedArtwork.copy(
                        status = TamaArtworkStatus.FAILED.name,
                        errorMessage = error.message ?: getString(R.string.error_generic),
                        completedAt = System.currentTimeMillis()
                    )
                )
                reconcileDeepDreamAlbum(tamaDatabase, startedArtwork)
            }

            artwork = tamaDao.getNextQueuedArtwork()
        }
    }

    private suspend fun reconcileDeepDreamAlbum(
        tamaDatabase: TamaDatabase,
        artwork: TamaArtworkEntity
    ) {
        if (artwork.kind != com.blackbox.ai.tama.data.TamaArtworkKind.DAILY_DREAM.name) return
        val albumId = artwork.albumId ?: return
        val run = tamaDatabase.tamaDao().getDeepDreamRunByAlbumId(albumId) ?: return
        TamaDeepDreamRunCoordinator.reconcileAlbumState(
            context = applicationContext,
            database = tamaDatabase,
            run = run,
            startArtworkIfNeeded = false
        )
    }

    private suspend fun maybeRefreshDeepDreamSummary(
        tamaDao: com.blackbox.ai.tama.db.TamaDao,
        artwork: TamaArtworkEntity
    ) {
        if (artwork.kind != com.blackbox.ai.tama.data.TamaArtworkKind.DAILY_DREAM.name) return
        val albumId = artwork.albumId ?: return
        val albumArtworks = tamaDao.getArtworks(artwork.petId)
            .filter { it.albumId == albumId }
        if (albumArtworks.isEmpty()) return
        if (albumArtworks.any { it.status == TamaArtworkStatus.FAILED.name }) return
        if (albumArtworks.any { it.status != TamaArtworkStatus.COMPLETED.name }) return

        updateForeground(getString(R.string.tama_deep_dream_status_calling_summarizer))
        val petEntity = tamaDao.getPet(artwork.petId) ?: return
        val pet = PetMapper.toDomain(petEntity)
        val backgroundAgent = TamaAgentService(
            applicationContext,
            tamaDao,
            SettingsRepository(applicationContext),
            OllamaService(applicationContext),
            serviceScope
        )
        backgroundAgent.summarize(pet)
    }

    private fun startForegroundOnce(title: String) {
        if (notificationTaskId != null) return
        val (taskId, notification) = UnifiedNotificationManager.startTaskForForeground(
            TaskType.IMAGE_GEN,
            title
        )
        notificationTaskId = taskId
        startForeground(taskId, notification)
    }

    private fun updateForeground(status: String) {
        val taskId = notificationTaskId ?: return
        UnifiedNotificationManager.updateProgress(taskId, -1f, status)
    }

    private fun titleForArtwork(artwork: TamaArtworkEntity): String {
        return if (artwork.kind == com.blackbox.ai.tama.data.TamaArtworkKind.DAILY_DREAM.name) {
            getString(R.string.tama_deep_dream_service_title)
        } else {
            getString(R.string.tama_pic_gen_service_title)
        }
    }

    private fun statusTextForArtwork(artwork: TamaArtworkEntity): String {
        return if (artwork.kind == com.blackbox.ai.tama.data.TamaArtworkKind.DAILY_DREAM.name) {
            val albumIndex = artwork.albumIndex + 1
            val albumTotal = 4
            getString(R.string.tama_deep_dream_generating_slide, albumIndex, albumTotal)
        } else {
            artwork.title
        }
    }

    companion object {
        private const val ACTION_PROCESS_QUEUE = "com.blackbox.ai.action.PROCESS_TAMA_ARTWORK_QUEUE"

        fun createProcessQueueIntent(context: Context): Intent =
            Intent(context, TamaArtworkGenerationService::class.java).apply {
                action = ACTION_PROCESS_QUEUE
            }
    }
}
