package com.blackbox.core.module

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

data class ModuleEvent(
    val moduleId: String,
    val type: String,
    val payload: Map<String, String> = emptyMap(),
    val timestamp: Long = System.nanoTime()
)

object ModuleBus {
    private val _events = MutableSharedFlow<ModuleEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<ModuleEvent> = _events

    fun publish(event: ModuleEvent) {
        _events.tryEmit(event)
    }
}
