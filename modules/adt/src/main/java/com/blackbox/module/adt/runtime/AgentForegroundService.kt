package com.blackbox.module.adt.runtime

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class AgentForegroundService : Service() {
    companion object {
        const val CHANNEL_ID = "agent_runtime"
        const val NOTIFICATION_ID = 9104
        @Volatile
        var instance: AgentForegroundService? = null
        val runningCount = AtomicInteger(0)
        val recoveryEnabled = AtomicBoolean(false)
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var recoveryJob: Job? = null

    @Volatile
    private var wakeLock: PowerManager.WakeLock? = null
    @Volatile
    private var wifiLock: WifiManager.WifiLock? = null
    @Volatile
    private var running = false

    private val binder = Binder()

    inner class Binder : android.os.Binder() {
        fun startAgent() = startAgentInternal()
        fun stopAgent() = stopAgentInternal()
        fun isRunning(): Boolean = running
        fun requestRecovery() { recoveryEnabled.set(true) }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Agent runtime idle"))
        acquireLocks()
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAgentInternal()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        running = false
        serviceScope.cancel()
        recoveryJob?.cancel()
        releaseLocks()
        instance = null
    }

    fun startAgentInternal() {
        if (running) return
        running = true
        runningCount.incrementAndGet()
        startForeground(NOTIFICATION_ID, buildNotification("Agent running"))
        recoveryJob = serviceScope.launch { agentLoop() }
    }

    fun stopAgentInternal() {
        if (!running) return
        running = false
        recoveryJob?.cancel()
        releaseLocks()
        stopForeground(STOP_FOREGROUND_REMOVE)
        runningCount.decrementAndGet()
    }

    private suspend fun agentLoop() {
        while (running) {
            try {
                // Heartbeat / recovery loop for agent runtime
                if (recoveryEnabled.get()) {
                    recoverRuntime()
                    recoveryEnabled.set(false)
                }
                updateNotification("Agent heartbeat OK")
                delay(30_000)
            } catch (e: Exception) {
                if (running) {
                    updateNotification("Agent error: ${e.message}")
                    delay(5_000)
                }
            }
        }
    }

    private suspend fun recoverRuntime() {
        updateNotification("runtime_recovery_empty_stop")
        // Attempt to recover any stopped dependent services
        delay(2000)
        val catalog = AdtServiceCatalog.services
        catalog.forEach { def ->
            if (def.foregroundType != null) {
                val i = Intent().setClassName(packageName, def.className)
                try {
                    if (def.className == this@AgentForegroundService::class.java.name) return@forEach
                    startService(i)
                } catch (e: Exception) {
                    // ignore recoverable
                }
            }
        }
    }

    private fun acquireLocks() {
        val pm = getSystemService(PowerManager::class.java)
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Blackbox:AgentRuntime")
        wakeLock?.setReferenceCounted(false)

        val wm = getSystemService(WifiManager::class.java)
        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "Blackbox:AgentWifi")
        wifiLock?.setReferenceCounted(false)
        runCatching { wifiLock?.acquire() }
    }

    private fun releaseLocks() {
        runCatching { if (wakeLock?.isHeld == true) wakeLock?.release() }
        runCatching { if (wifiLock?.isHeld == true) wifiLock?.release() }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Agent Runtime", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    private fun updateNotification(text: String) {
        val mgr = getSystemService(NotificationManager::class.java)
        mgr.notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Blackbox Agent")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .build()
    }
}
