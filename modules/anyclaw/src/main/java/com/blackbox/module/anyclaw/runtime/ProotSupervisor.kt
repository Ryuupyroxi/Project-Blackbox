package com.blackbox.module.anyclaw.runtime

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProotSupervisor(
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow<ProotState>(ProotState.Stopped)
    val state: StateFlow<ProotState> = _state

    fun start() {
        _state.value = ProotState.Starting
        scope.launch(Dispatchers.IO) {
            runCatching {
                delay(120)
                _state.value = ProotState.Running("stub-proot")
            }.onFailure {
                _state.value = ProotState.Error(it.message ?: "proot start failed")
            }
        }
    }

    suspend fun execute(command: String): ShellResult {
        return ShellResult.Ok("stub-$command")
    }

    fun stop() {
        _state.value = ProotState.Stopped
    }
}

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
