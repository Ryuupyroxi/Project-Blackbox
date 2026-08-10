package com.blackbox.module.anyclaw.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import com.blackbox.core.module.ModuleBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class DashboardCardState(
    val id: String,
    val title: String,
    val running: Boolean,
    val lastError: String? = null
)

class DashboardViewModel : ViewModel() {
    private val _cards = MutableStateFlow<List<DashboardCardState>>(emptyList())
    val cards: StateFlow<List<DashboardCardState>> = _cards

    init {
        _cards.value = listOf(
            DashboardCardState("openclaw", "OpenClaw", false),
            DashboardCardState("codex", "Codex", false),
            DashboardCardState("hermes", "Hermes", false),
            DashboardCardState("gateway", "Gateway", false)
        )
    }

    fun startHermesWebUiOnly() {
        ModuleBus.publish(ModuleEvent("anyclaw", "start_hermes"))
    }

    fun stopCodexWebLocalOnly() {
        ModuleBus.publish(ModuleEvent("anyclaw", "stop_codex"))
    }

    fun stopHermesWebUi() {
        ModuleBus.publish(ModuleEvent("anyclaw", "stop_hermes"))
    }
}
