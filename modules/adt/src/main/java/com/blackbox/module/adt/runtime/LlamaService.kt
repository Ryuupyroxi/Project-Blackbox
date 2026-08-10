package com.blackbox.module.adt.runtime

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
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
import java.net.ServerSocket
import java.net.InetSocketAddress

class LlamaService : Service() {
    companion object {
        const val CHANNEL_ID = "llama_runtime"
        const val NOTIFICATION_ID = 9101
        const val DEFAULT_PORT = 8080
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var nativeLoader: AdtNativeLoader

    @Volatile
    private var boundPort: Int? = null
    @Volatile
    private var llamaProcess: Process? = null

    override fun onCreate() {
        super.onCreate()
        nativeLoader = AdtNativeLoader(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Llama runtime initializing..."))
        serviceScope.launch { ensureNativeLibsAndStart() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val port = intent?.getIntExtra("port", DEFAULT_PORT) ?: DEFAULT_PORT
        val model = intent?.getStringExtra("model") ?: "default"
        serviceScope.launch { startLlamaServer(port, model) }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        stopLlamaProcess()
    }

    private suspend fun ensureNativeLibsAndStart() {
        if (!nativeLoader.isReady()) {
            nativeLoader.extractFromApk()
        }
        if (nativeLoader.missingLibraries().isNotEmpty()) {
            updateNotification("Native libs missing; inference unavailable")
            return
        }
    }

    private suspend fun startLlamaServer(port: Int, model: String) {
        if (!isPortAvailable(port)) {
            cleanupStaleProcesses(port)
            if (!isPortAvailable(port)) {
                updateNotification("Port $port busy; cannot start Llama")
                return
            }
        }
        val libDir = nativeLoader.extractedDir
        val llamaLib = File(libDir, "libllama.so")
        if (!llamaLib.exists()) {
            updateNotification("libllama.so missing")
            return
        }
        stopLlamaProcess()
        try {
            val cmd = listOf(
                llamaLib.absolutePath,
                "--model", model,
                "--port", port.toString(),
                "--ctx-size", "2048",
                "--threads", Runtime.getRuntime().availableProcessors().toString()
            )
            val pb = ProcessBuilder(cmd)
                .directory(libDir)
                .redirectErrorStream(true)
            llamaProcess = pb.start()
            boundPort = port
            updateNotification("Llama server on port $port")
            llamaProcess?.waitFor()
        } catch (e: Exception) {
            updateNotification("Llama server failed: ${e.message}")
        }
    }

    private fun stopLlamaProcess() {
        runCatching { llamaProcess?.destroy() }
        llamaProcess = null
        boundPort = null
    }

    private fun isPortAvailable(port: Int): Boolean {
        return try {
            ServerSocket().use { socket ->
                socket.setReuseAddress(true)
                socket.bind(InetSocketAddress("127.0.0.1", port), 1)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun cleanupStaleProcesses(port: Int) {
        // Best-effort cleanup of stale llama-server processes
        runCatching {
            val procs = File("/proc").listFiles { f -> f.isDirectory && f.name.all { it.isDigit() } }
            procs?.forEach { dir ->
                val cmdline = File(dir, "cmdline").readText()
                if (cmdline.contains("llama") || cmdline.contains("litert")) {
                    dir.deleteRecursively()
                }
            }
        }
    }

    fun updateNotification(text: String) {
        val mgr = getSystemService(NotificationManager::class.java)
        mgr.notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Llama Runtime", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Blackbox Llama")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .build()
    }
}
