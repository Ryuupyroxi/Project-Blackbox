package com.blackbox.bridge

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class BridgeServer(private val root: File) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sessions = ConcurrentHashMap<String, BridgeSession>()
    private val _state = MutableStateFlow(BridgeState.Stopped)
    val state: StateFlow<BridgeState> = _state

    fun start() {
        root.mkdirs()
        _state.value = BridgeState.Running
        scope.launch { watchLoop() }
    }

    fun stop() {
        scope.cancel()
        sessions.values.forEach { it.close() }
        sessions.clear()
        _state.value = BridgeState.Stopped
    }

    private suspend fun watchLoop() {
        while (isActive) {
            val files = root.listFiles { f -> f.name.endsWith(".req") } ?: emptyArray()
            for (req in files) {
                handleRequest(req)
            }
            delay(80)
        }
    }

    private fun handleRequest(req: File) {
        try {
            val text = req.readText().trim()
            val id = extractId(req)
            val cmd = extractCmd(text) ?: "unknown"
            val resp = BridgeCommandHandler.handle(cmd, text)
            val respFile = File(req.parentFile, "cmd-${id}.resp")
            respFile.writeText(resp)
            req.delete()
            sessions[id] = BridgeSession(req)
        } catch (e: Exception) {
            req.delete()
        }
    }

    private fun extractId(file: File): String =
        file.name.removeSuffix(".req").removePrefix("cmd-").ifBlank { "0" }

    private fun extractCmd(text: String): String? =
        Regex("\"cmd\"\\s*:\\s*\"([^\"]+)\"").find(text)?.groupValues?.get(1)
}

sealed class BridgeState {
    data object Stopped : BridgeState()
    data object Running : BridgeState()
    data class Error(val message: String) : BridgeState()
}
