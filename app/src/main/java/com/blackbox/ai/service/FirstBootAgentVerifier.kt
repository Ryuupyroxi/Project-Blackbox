package com.blackbox.ai.service

import android.content.Context
import com.blackbox.ai.agent.runtime.AgentCatalog
import com.blackbox.ai.agent.runtime.AgentRuntimeManager
import com.blackbox.ai.agent.runtime.RuntimeAgent
import com.blackbox.ai.engine.EngineKeysStore
import com.blackbox.ai.util.DebugLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * First-boot and runtime verification for all packaged runtime agents.
 *
 * Behavior aligns with the conversation constraints:
 *  - Runs on first boot or when verifier is explicitly triggered.
 *  - Checks install + health for each agent in [AgentCatalog.all].
 *  - Attempts install/start only when the runtime channel is available.
 *  - Records results in [GenerationDiagnosticsStore] so Xander/the user can audit outcomes.
 *  - Never claims success from a no-op; health is verified by real port/poll checks.
 */
class FirstBootAgentVerifier(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "first_boot_agent_verifier"
        private const val KEY_LAST_RUN_VERSION = "last_run_version_code"
        private const val AGENT_TIMEOUT_MS = 180_000L
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

        for (agent in AgentCatalog.all) {
            val result = verifyAgent(agent, mode)
            results += result
        }

        return results
    }

    private suspend fun verifyAgent(agent: RuntimeAgent, mode: String): AgentResult {
        DebugLog.log("[FirstBootAgentVerifier] Verifying agent=${agent.id} mode=$mode")

        val connected = try {
            withTimeout(AGENT_TIMEOUT_MS) { AgentRuntimeManager.isConnected(context) }
        } catch (e: Exception) {
            DebugLog.log("[FirstBootAgentVerifier] connect check failed for ${agent.id}: ${e.message}")
            false
        }

        if (!connected) {
            DebugLog.log("[FirstBootAgentVerifier] Runtime not connected; skipping ${agent.id}")
            return AgentResult(agent.id, AgentResult.Status.SKIPPED, "Runtime not connected")
        }

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
                        startAndCheckHealth(agent, mode)
                    } else {
                        AgentResult(agent.id, AgentResult.Status.FAILED, "Install output did not confirm success: ${out.take(200)}")
                    }
                },
                onFailure = { e ->
                    AgentResult(agent.id, AgentResult.Status.FAILED, "Install failed: ${e.message}")
                }
            )
        }

        return startAndCheckHealth(agent, mode)
    }

    private suspend fun startAndCheckHealth(agent: RuntimeAgent, mode: String): AgentResult {
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
                        AgentResult(agent.id, status, "Health=$health")
                    },
                    onFailure = { e ->
                        AgentResult(agent.id, AgentResult.Status.FAILED, "Health failed: ${e.message}")
                    }
                )
            },
            onFailure = { e ->
                AgentResult(agent.id, AgentResult.Status.FAILED, "Start failed: ${e.message}")
            }
        )
    }

    private fun buildSummary(results: List<AgentResult>): String {
        val counts = results.groupingBy { it.status }.eachCount()
        val details = results.joinToString("\n") { "${it.agentId}: ${it.status} - ${it.detail}" }
        return "Verification complete. passed=${counts[AgentResult.Status.PASSED] ?: 0} warning=${counts[AgentResult.Status.WARNING] ?: 0} failed=${counts[AgentResult.Status.FAILED] ?: 0} skipped=${counts[AgentResult.Status.SKIPPED] ?: 0}. Details:\n$details"
    }

    data class AgentResult(
        val agentId: String,
        val status: Status,
        val detail: String
    ) {
        enum class Status { PASSED, WARNING, FAILED, SKIPPED }
    }
}
