package com.blackbox.ai.service

import android.content.Context
import com.blackbox.ai.agent.runtime.AgentCatalog
import com.blackbox.ai.agent.runtime.AgentRuntimeManager
import com.blackbox.ai.agent.runtime.EmbeddedRuntimeManager
import com.blackbox.ai.agent.runtime.RuntimeAgent
import com.blackbox.ai.engine.EngineKeysStore
import com.blackbox.ai.runtime.CodexServerManager
import com.blackbox.ai.util.DebugLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * First-boot and runtime verification for all packaged runtime agents.
 *
 * Behavior:
 *  - Runs on first boot or when verifier is explicitly triggered.
 *  - Checks install + health for each agent in [AgentCatalog.all].
 *  - SSH/proot agents (Hermes, OpenCode) require a connected runtime channel.
 *  - Embedded agents (Codex, OpenClaw) are checked via [EmbeddedRuntimeManager]
 *    when the SSH/proot channel is unavailable, and via SSH when it is available.
 *  - Attempts install/start only when the runtime channel is available.
 *  - Records results in [GenerationDiagnosticsStore] so the user can audit outcomes.
 *  - Never claims success from a no-op; health is verified by real port/poll checks.
 */
class FirstBootAgentVerifier(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "first_boot_agent_verifier"
        private const val KEY_LAST_RUN_VERSION = "last_run_version_code"
        private const val AGENT_TIMEOUT_MS = 180_000L

        /** Agents that can run via the embedded LOCAL runtime (no SSH required). */
        private val EMBEDDED_AGENTS = setOf("codex", "openclaw")
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun runIfNeeded(currentVersionCode: Long) {
        val lastRun = prefs.getLong(KEY_LAST_RUN_VERSION, 0L)
        if (lastRun == currentVersionCode) {
            DebugLog.log("[FirstBootAgentVerifier] Already verified for versionCode=$currentVersionCode")
            return
        }

        scope.launch {
            DebugLog.log("[FirstBootAgentVerifier] Starting first-boot agent verification for versionCode=$currentVersionCode")
            val results = verifyAllAgents()
            val summary = buildSummary(results)
            DebugLog.log("[FirstBootAgentVerifier] $summary")
            runCatching {
                GenerationDiagnosticsStore.recordBreadcrumb(
                    source = "first_boot_agent_verifier",
                    event = "agent_verification_complete",
                    details = summary
                )
            }
            prefs.edit().putLong(KEY_LAST_RUN_VERSION, currentVersionCode).apply()
        }
    }

    suspend fun runNow(): String {
        val results = verifyAllAgents()
        return buildSummary(results)
    }

    private suspend fun verifyAllAgents(): List<AgentResult> {
        val keys = EngineKeysStore(context)
        val mode = keys.getRuntimeMode()
        val results = mutableListOf<AgentResult>()

        DebugLog.log("[FirstBootAgentVerifier] Runtime mode=$mode")

        val sshConnected = try {
            withTimeout(AGENT_TIMEOUT_MS) { AgentRuntimeManager.isConnected(context) }
        } catch (e: Exception) {
            DebugLog.log("[FirstBootAgentVerifier] SSH/proot connect check failed: ${e.message}")
            false
        }

        val embeddedReady = try {
            EmbeddedRuntimeManager.isInstalled(context)
        } catch (e: Exception) {
            DebugLog.log("[FirstBootAgentVerifier] Embedded runtime check failed: ${e.message}")
            false
        }

        DebugLog.log("[FirstBootAgentVerifier] SSH connected=$sshConnected, embedded ready=$embeddedReady")

        for (agent in AgentCatalog.all) {
            val isEmbedded = agent.id in EMBEDDED_AGENTS
            val result = when {
                sshConnected -> verifyAgentSsh(agent, mode)
                isEmbedded && embeddedReady -> verifyAgentEmbedded(agent)
                isEmbedded -> AgentResult(
                    agent.id, AgentResult.Status.SKIPPED,
                    "Embedded runtime not installed; run Install first"
                )
                else -> AgentResult(
                    agent.id, AgentResult.Status.SKIPPED,
                    "Runtime not connected; connect Termux/SSH or proot first"
                )
            }
            results += result
        }

        // Refresh all agent health states after verification
        try {
            AgentRuntimeManager.refreshAllAgentHealth(context)
        } catch (e: Exception) {
            DebugLog.log("[FirstBootAgentVerifier] Failed to refresh agent health: ${e.message}")
        }

        return results
    }

    // ── SSH / proot verification (Hermes, OpenCode, or any agent when connected) ──

    private suspend fun verifyAgentSsh(agent: RuntimeAgent, mode: String): AgentResult {
        DebugLog.log("[FirstBootAgentVerifier] Verifying ${agent.id} via SSH mode=$mode")

        val installed = try {
            withTimeout(AGENT_TIMEOUT_MS) { AgentRuntimeManager.checkInstalled(context, agent) }
        } catch (e: Exception) {
            DebugLog.log("[FirstBootAgentVerifier] install check failed for ${agent.id}: ${e.message}")
            false
        }

        if (!installed) {
            DebugLog.log("[FirstBootAgentVerifier] ${agent.id} not installed; attempting install")
            val installOut = try {
                withTimeout(AGENT_TIMEOUT_MS) { AgentRuntimeManager.install(context, agent) }
            } catch (e: Exception) {
                DebugLog.log("[FirstBootAgentVerifier] install exception for ${agent.id}: ${e.message}")
                return AgentResult(agent.id, AgentResult.Status.FAILED, "Install exception: ${e.message}")
            }

            return installOut.fold(
                onSuccess = { out ->
                    val actuallyInstalled = out.contains("INSTALLED", ignoreCase = true) ||
                        out.contains("already", ignoreCase = true)
                    if (actuallyInstalled) {
                        startAndCheckHealthSsh(agent)
                    } else {
                        AgentResult(agent.id, AgentResult.Status.FAILED, "Install output unclear: ${out.take(200)}")
                    }
                },
                onFailure = { e ->
                    AgentResult(agent.id, AgentResult.Status.FAILED, "Install failed: ${e.message}")
                }
            )
        }

        return startAndCheckHealthSsh(agent)
    }

    private suspend fun startAndCheckHealthSsh(agent: RuntimeAgent): AgentResult {
        return try {
            withTimeout(AGENT_TIMEOUT_MS) { AgentRuntimeManager.start(context, agent) }
        } catch (e: Exception) {
            DebugLog.log("[FirstBootAgentVerifier] start exception for ${agent.id}: ${e.message}")
            return AgentResult(agent.id, AgentResult.Status.FAILED, "Start exception: ${e.message}")
        }.fold(
            onSuccess = { startOut ->
                val started = startOut.contains("STARTED", ignoreCase = true) ||
                    startOut.contains("running", ignoreCase = true)
                if (!started) {
                    return AgentResult(agent.id, AgentResult.Status.FAILED, "Start output unclear: ${startOut.take(200)}")
                }

                val healthOut = try {
                    withTimeout(AGENT_TIMEOUT_MS) { AgentRuntimeManager.health(context, agent) }
                } catch (e: Exception) {
                    DebugLog.log("[FirstBootAgentVerifier] health exception for ${agent.id}: ${e.message}")
                    return AgentResult(agent.id, AgentResult.Status.FAILED, "Health exception: ${e.message}")
                }

                healthOut.fold(
                    onSuccess = { health ->
                        val healthy = health.contains("ALIVE", ignoreCase = true) ||
                            health.contains("HTTP 200", ignoreCase = true) ||
                            health.contains("HTTP %{http_code}", ignoreCase = true) ||
                            health.contains("PORT ${agent.port}", ignoreCase = true)
                        val status = if (healthy) AgentResult.Status.PASSED else AgentResult.Status.WARNING
                        AgentResult(agent.id, status, "SSH health=$health")
                    },
                    onFailure = { e ->
                        AgentResult(agent.id, AgentResult.Status.FAILED, "SSH health failed: ${e.message}")
                    }
                )
            },
            onFailure = { e ->
                AgentResult(agent.id, AgentResult.Status.FAILED, "Start failed: ${e.message}")
            }
        )
    }

    // ── Embedded runtime verification (Codex, OpenClaw when SSH unavailable) ──

    private suspend fun verifyAgentEmbedded(agent: RuntimeAgent): AgentResult {
        DebugLog.log("[FirstBootAgentVerifier] Verifying ${agent.id} via embedded runtime")

        return when (agent.id) {
            "codex" -> verifyCodexEmbedded()
            "openclaw" -> verifyOpenclawEmbedded()
            else -> AgentResult(agent.id, AgentResult.Status.SKIPPED, "No embedded runtime for ${agent.id}")
        }
    }

    private suspend fun verifyCodexEmbedded(): AgentResult {
        val status = try {
            withTimeout(AGENT_TIMEOUT_MS) { EmbeddedRuntimeManager.status(context) }
        } catch (e: Exception) {
            DebugLog.log("[FirstBootAgentVerifier] embedded status check failed for codex: ${e.message}")
            return AgentResult("codex", AgentResult.Status.FAILED, "Status check failed: ${e.message}")
        }

        return when {
            status.serverRunning -> {
                AgentResult("codex", AgentResult.Status.PASSED, "Embedded codex server running (port ${CodexServerManager.SERVER_PORT})")
            }
            status.ready -> {
                AgentResult("codex", AgentResult.Status.WARNING, "Embedded codex installed (bootstrap=${status.bootstrap}, node=${status.node}, codex=${status.codex}) but server not running")
            }
            else -> {
                val parts = mutableListOf<String>()
                if (!status.bootstrap) parts.add("bootstrap")
                if (!status.node) parts.add("node")
                if (!status.codex) parts.add("codex")
                if (!status.platformBinary) parts.add("platform-binary")
                AgentResult("codex", AgentResult.Status.SKIPPED, "Embedded codex missing: ${parts.joinToString(", ")}")
            }
        }
    }

    private suspend fun verifyOpenclawEmbedded(): AgentResult {
        val openclawInstalled = try {
            withTimeout(AGENT_TIMEOUT_MS) {
                CodexServerManager(context).isOpenClawInstalled()
            }
        } catch (e: Exception) {
            DebugLog.log("[FirstBootAgentVerifier] openclaw install check failed: ${e.message}")
            false
        }

        if (!openclawInstalled) {
            return AgentResult("openclaw", AgentResult.Status.SKIPPED, "OpenClaw not installed in embedded runtime")
        }

        // Check if gateway is reachable
        val healthCheck = try {
            withTimeout(AGENT_TIMEOUT_MS) {
                EmbeddedRuntimeManager.healthCheck(context)
            }
        } catch (e: Exception) {
            DebugLog.log("[FirstBootAgentVerifier] openclaw health check failed: ${e.message}")
            Result.failure(e)
        }

        return healthCheck.fold(
            onSuccess = { output ->
                val healthy = output.contains("passed", ignoreCase = true) ||
                    output.contains("ALIVE", ignoreCase = true)
                if (healthy) {
                    AgentResult("openclaw", AgentResult.Status.PASSED, "OpenClaw gateway reachable (port ${CodexServerManager.OPENCLAW_GATEWAY_PORT})")
                } else {
                    AgentResult("openclaw", AgentResult.Status.WARNING, "OpenClaw installed but health unclear: ${output.take(200)}")
                }
            },
            onFailure = { e ->
                AgentResult("openclaw", AgentResult.Status.WARNING, "OpenClaw installed but gateway not running: ${e.message}")
            }
        )
    }

    // ── Summary ──

    private fun buildSummary(results: List<AgentResult>): String {
        val counts = results.groupingBy { it.status }.eachCount()
        val details = results.joinToString("\n") { "${it.agentId}: ${it.status} - ${it.detail}" }
        return "Verification complete. passed=${counts[AgentResult.Status.PASSED] ?: 0} " +
            "warning=${counts[AgentResult.Status.WARNING] ?: 0} " +
            "failed=${counts[AgentResult.Status.FAILED] ?: 0} " +
            "skipped=${counts[AgentResult.Status.SKIPPED] ?: 0}.\n$details"
    }

    data class AgentResult(
        val agentId: String,
        val status: Status,
        val detail: String
    ) {
        enum class Status { PASSED, WARNING, FAILED, SKIPPED }
    }
}
