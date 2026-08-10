package com.blackbox.module.anyclaw.proot

import android.content.Context
import com.blackbox.module.anyclaw.data.PreferencesManager
import com.blackbox.core.module.ModuleBus
import kotlinx.coroutines.*

class ProcessManager(private val context: Context, private val prefs: PreferencesManager) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startOpenCode() {
        ModuleBus.publish(ModuleEvent("anyclaw", "openclaw_start_requested"))
    }

    fun stopOpenCode() {
        ModuleBus.publish(ModuleEvent("anyclaw", "openclaw_stop_requested"))
    }

    fun startCodexWebLocal() {
        ModuleBus.publish(ModuleEvent("anyclaw", "codex_start_requested"))
    }

    fun stopCodexWebLocal() {
        ModuleBus.publish(ModuleEvent("anyclaw", "codex_stop_requested"))
    }

    fun startHermesWebUi() {
        ModuleBus.publish(ModuleEvent("anyclaw", "hermes_start_requested"))
    }

    fun stopHermesWebUi() {
        ModuleBus.publish(ModuleEvent("anyclaw", "hermes_stop_requested"))
    }

    fun startSshdInBackground() {
        ModuleBus.publish(ModuleEvent("anyclaw", "sshd_start_requested"))
    }

    fun stopSshd() {
        ModuleBus.publish(ModuleEvent("anyclaw", "sshd_stop_requested"))
    }

    fun approvePairing(requestId: String) {
        ModuleBus.publish(ModuleEvent("anyclaw", "pairing_approved", mapOf("requestId" to requestId)))
    }

    fun denyPairing(requestId: String) {
        ModuleBus.publish(ModuleEvent("anyclaw", "pairing_denied", mapOf("requestId" to requestId)))
    }

    fun listPairingRequests(): List<String> = emptyList()

    fun readProcessOutput(pid: Int): List<String> = emptyList()

    fun scheduleCodexuiLatestAutoUpdate() {
        // Auto-update scheduling stub.
    }

    fun updateCodexuiLatestInBackground() {
        scope.launch {
            // Background update stub.
        }
    }
}
