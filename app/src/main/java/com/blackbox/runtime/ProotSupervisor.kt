package com.blackbox.runtime

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class ProotState {
    data object Stopped : ProotState()
    data object Starting : ProotState()
    data class Running(val session: String) : ProotState()
    data class Error(val message: String) : ProotState()
}

sealed class ShellResult {
    data class Ok(val output: String) : ShellResult()
    data class Err(val error: String) : ShellResult()
}

class ProotSupervisor(private val context: Context) {
    private val _state = MutableStateFlow<ProotState>(ProotState.Stopped)
    val state: StateFlow<ProotState> = _state

    fun start() {
        _state.value = ProotState.Starting
        // Real proot startup should bind to a local bridge port, e.g., 18923.
        // Stub transitions to Running after validation hook is wired.
        _state.value = ProotState.Running("stub-proot")
    }

    fun stop() {
        _state.value = ProotState.Stopped
    }

    fun healthCheck(): Boolean = _state.value is ProotState.Running

    suspend fun execute(command: String): ShellResult {
        val running = _state.value as? ProotState.Running
        return if (running != null) {
            ShellResult.Ok("stub-$command")
        } else {
            ShellResult.Err("proot not running")
        }
    }
}
