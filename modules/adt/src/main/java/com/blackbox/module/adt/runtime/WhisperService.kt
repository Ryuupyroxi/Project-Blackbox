package com.blackbox.module.adt.runtime

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class WhisperService : Service() {
    companion object {
        const val CHANNEL_ID = "whisper_runtime"
        const val NOTIFICATION_ID = 9102
        const val EXTRA_AUDIO_PATH = "audio_path"
        const val EXTRA_LANGUAGE = "language"
        private val activeJobs = ConcurrentHashMap<Int, String>()
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var nativeLoader: AdtNativeLoader

    @Volatile
    private var whisperProcess: Process? = null
    @Volatile
    private var currentJobId: Int? = null

    override fun onCreate() {
        super.onCreate()
        nativeLoader = AdtNativeLoader(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Whisper ready"))
        extractFfmpegLibs()
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val audioPath = intent?.getStringExtra(EXTRA_AUDIO_PATH) ?: return START_NOT_STICKY
        val language = intent?.getStringExtra(EXTRA_LANGUAGE) ?: "auto"
        val jobId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        currentJobId = jobId
        serviceScope.launch { transcribe(jobId, audioPath, language) }
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        stopWhisperProcess()
    }

    private fun extractFfmpegLibs() {
        val ffDir = File(filesDir, "ffmpeg_libs")
        ffDir.mkdirs()
        // Best-effort extraction; actual extraction should be done via zip/apk
        val candidates = listOf(
            "libx264.so.164",
            "libwhisper.so.1",
            "libggml.so.0",
            "libggml-base.so.0",
            "libggml-cpu.so.0"
        )
        candidates.forEach { name ->
            val dest = File(ffDir, name)
            if (!dest.exists()) {
                runCatching {
                    val lib = File(nativeLoader.extractedDir, "lib$name.so")
                    if (lib.exists()) {
                        lib.copyTo(dest)
                        dest.setExecutable(true)
                    }
                }
            }
        }
    }

    private suspend fun transcribe(jobId: Int, audioPath: String, language: String) {
        val ffDir = File(filesDir, "ffmpeg_libs")
        val whisperLib = File(nativeLoader.extractedDir, "libwhisper.so")
        val ggmlLib = File(ffDir, "libggml.so.0")
        if (!whisperLib.exists() || !ggmlLib.exists()) {
            updateProgress(jobId, "error: native libs missing")
            return
        }
        updateProgress(jobId, "starting transcription")
        try {
            val cmd = mutableListOf(
                whisperLib.absolutePath,
                "-m", "ggml-base.bin",
                "-f", audioPath,
                "-l", language,
                "-otxt",
                "-of", "$filesDir/whisper_out_$jobId"
            )
            val pb = ProcessBuilder(cmd)
                .directory(ffDir)
                .redirectErrorStream(true)
            whisperProcess = pb.start()
            whisperProcess?.inputStream?.bufferedReader()?.useLines { lines ->
                lines.forEach { line ->
                    updateProgress(jobId, line.trim())
                }
            }
            val exit = whisperProcess?.waitFor()
            if (exit == 0) {
                val out = File("$filesDir/whisper_out_$jobId.txt")
                val text = out.takeIf { it.exists() }?.readText() ?: ""
                updateProgress(jobId, "completed|$text")
            } else {
                updateProgress(jobId, "error: process exited $exit")
            }
        } catch (e: Exception) {
            updateProgress(jobId, "error: ${e.message}")
        } finally {
            currentJobId = null
        }
    }

    fun updateProgress(jobId: Int, status: String) {
        activeJobs[jobId] = status
        val mgr = getSystemService(NotificationManager::class.java)
        mgr.notify(NOTIFICATION_ID, buildNotification("Whisper: $status"))
    }

    private fun stopWhisperProcess() {
        runCatching { whisperProcess?.destroy() }
        whisperProcess = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Whisper STT", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Blackbox Whisper")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .build()
    }
}
