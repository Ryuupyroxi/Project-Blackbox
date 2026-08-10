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
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

class ModelDownloadService : Service() {
    companion object {
        const val CHANNEL_ID = "model_download"
        const val NOTIFICATION_ID = 9103
        const val EXTRA_URL = "download_url"
        const val EXTRA_DEST = "destination"
        const val EXTRA_MODEL_ID = "model_id"
        private val activeDownloads = ConcurrentHashMap<String, Double>()
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var nativeLoader: AdtNativeLoader

    override fun onCreate() {
        super.onCreate()
        nativeLoader = AdtNativeLoader(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Model download idle"))
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra(EXTRA_URL) ?: return START_NOT_STICKY
        val dest = intent?.getStringExtra(EXTRA_DEST) ?: "$filesDir/models/"
        val modelId = intent?.getStringExtra(EXTRA_MODEL_ID) ?: "unknown"
        serviceScope.launch { downloadModel(modelId, url, dest) }
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private suspend fun downloadModel(modelId: String, url: String, dest: String) {
        val outFile = File(dest, modelId)
        outFile.parentFile?.mkdirs()
        var connection: HttpURLConnection? = null
        var input: java.io.InputStream? = null
        var output: FileOutputStream? = null
        try {
            connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            connection.connect()
            val totalSize = connection.contentLengthLong
            input = connection.inputStream
            output = FileOutputStream(outFile)
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var downloaded: Long = 0
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
                downloaded += read
                val progress = if (totalSize > 0) (downloaded * 100 / totalSize).toInt() else -1
                activeDownloads[modelId] = progress.toDouble()
                if (progress % 10 == 0) {
                    updateNotification("$modelId: $progress%")
                }
            }
            output.flush()
            activeDownloads[modelId] = 100.0
            updateNotification("$modelId: complete")
            sendBroadcast(Intent("com.blackbox.module.adt.MODEL_DOWNLOAD_COMPLETE").apply {
                `package` = this@ModelDownloadService.packageName
                putExtra("model_id", modelId)
                putExtra("path", outFile.absolutePath)
            })
        } catch (e: Exception) {
            updateNotification("$modelId: failed - ${e.message}")
            runCatching { outFile.delete() }
        } finally {
            runCatching { input?.close() }
            runCatching { output?.close() }
            runCatching { connection?.disconnect() }
            activeDownloads.remove(modelId)
        }
    }

    fun getProgress(modelId: String): Double = activeDownloads[modelId] ?: -1.0

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Model Downloads", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    private fun updateNotification(text: String) {
        val mgr = getSystemService(NotificationManager::class.java)
        mgr.notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Blackbox Model Download")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .build()
    }
}
