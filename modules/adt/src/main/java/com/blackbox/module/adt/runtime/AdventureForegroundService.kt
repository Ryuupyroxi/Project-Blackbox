package com.blackbox.module.adt.runtime

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdventureForegroundService : Service() {
    companion object {
        const val CHANNEL_ID = "adventure_runtime"
        const val NOTIFICATION_ID = 9105
        @Volatile
        var active: Boolean = false
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _stage = MutableStateFlow("idle")
    val stage: StateFlow<String> = _stage

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress

    private var wakeLock: PowerManager.WakeLock? = null
    @Volatile
    private var jobId: String? = null
    @Volatile
    private var world: String? = null

    override fun onCreate() {
        super.onCreate()
        wakeLock = (getSystemService(POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Blackbox:Adventure")
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val stageName = intent?.getStringExtra("stage") ?: "idle"
        val worldName = intent?.getStringExtra("world") ?: "default"
        startAdventure(stageName, worldName)
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        releaseWakeLock()
    }

    private fun startAdventure(stageName: String, worldName: String) {
        jobId = "adv_${System.currentTimeMillis()}"
        world = worldName
        active = true
        _stage.value = stageName
        serviceScope.launch { adventureLoop() }
    }

    fun stopAdventure() {
        active = false
        jobId = null
        _stage.value = "idle"
        releaseWakeLock()
        stopSelf()
    }

    fun recordDiagnostics(stage: String, world: String, lockHeld: Boolean, jobActive: Boolean) {
        try {
            android.util.Log.i("AdventureForegroundService", "[AdventureForegroundService] stage=$stage world=$world lockHeld=$lockHeld jobActive=$jobActive")
        } catch (e: Exception) {
            android.util.Log.e("AdventureForegroundService", "[AdventureForegroundService] Failed to record diagnostics: ${e.message}")
        }
    }

    private suspend fun adventureLoop() {
        while (active) {
            try {
                val currentWorld = world ?: "default"
                val currentStage = _stage.value
                recordDiagnostics(currentStage, currentWorld, isLockHeld(), active)
                _progress.value = (_progress.value + 1) % 101
                delay(2000)
            } catch (e: Exception) {
                if (active) delay(1000)
            }
        }
    }

    private fun isLockHeld(): Boolean = wakeLock?.isHeld == true

    private fun releaseWakeLock() {
        runCatching { if (wakeLock?.isHeld == true) wakeLock?.release() }
    }
}
