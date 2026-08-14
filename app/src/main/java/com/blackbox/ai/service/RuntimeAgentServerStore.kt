package com.blackbox.ai.service

import android.content.Context
import com.blackbox.ai.agent.runtime.AgentCatalog
import com.blackbox.ai.agent.runtime.AgentRuntimeManager
import com.blackbox.ai.agent.runtime.EmbeddedRuntimeManager
import com.blackbox.ai.util.DebugLog.LogEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Observable state for runtime agent servers that have a web UI.
 * The AI Servers Hub observes this to render agent server cards alongside
 * the AI Studio servers.
 */
data class RuntimeAgentServerCard(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val port: Int,
    val webUrl: String?,
    val running: Boolean,
    val error: String?,
    val statusText: String,
    val logs: List<LogEntry> = emptyList(),
)

object RuntimeAgentServerStore {

    private val _cards = MutableStateFlow<List<RuntimeAgentServerCard>>(emptyList())
    val cards: StateFlow<List<RuntimeAgentServerCard>> = _cards.asStateFlow()

    private var refreshScope: CoroutineScope? = null

    /** Refresh card states from both runtime managers. */
    suspend fun refresh(context: Context) {
        val embeddedStatus = runCatching { EmbeddedRuntimeManager.status(context) }.getOrNull()
        val localReady = embeddedStatus?.serverRunning == true

        val cards = AgentCatalog.all.map { agent ->
            val isEmbedded = agent.id == "codex" || agent.id == "openclaw"
            val running: Boolean
            val error: String?
            val statusText: String

            if (isEmbedded) {
                running = localReady && embeddedStatus?.serverRunning == true
                error = if (running) null else "Not running"
                statusText = when {
                    running -> "Running (embedded LOCAL)"
                    embeddedStatus?.ready == true -> "Installed — not started"
                    embeddedStatus?.bootstrap == true -> "Partial install"
                    else -> "Not installed"
                }
            } else {
                val healthResult = runCatching { AgentRuntimeManager.health(context, agent) }.getOrNull()
                val healthText = healthResult?.getOrNull()?.trim() ?: "Unknown"
                running = healthText.contains("PID") && healthText.contains("ALIVE")
                error = if (running) null else healthText.takeIf { it != "Unknown" }
                statusText = healthText
            }

            RuntimeAgentServerCard(
                id = agent.id,
                name = agent.name,
                emoji = agent.emoji,
                description = agent.description,
                port = agent.port,
                webUrl = agent.webUrl,
                running = running,
                error = error,
                statusText = statusText,
            )
        }.filter { it.webUrl != null || it.running }

        _cards.value = cards
    }

    /** Start polling for live status updates. */
    fun startPolling(context: Context) {
        stopPolling()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        refreshScope = scope
        scope.launch {
            while (true) {
                refresh(context)
                kotlinx.coroutines.delay(5_000)
            }
        }
    }

    fun stopPolling() {
        refreshScope?.cancel()
        refreshScope = null
    }
}
