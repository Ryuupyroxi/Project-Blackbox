package com.blackbox.ai.agent.runtime

import android.content.Context
import com.blackbox.ai.data.proot.ProotManager
import com.blackbox.ai.engine.EngineKeysStore
import com.blackbox.ai.service.SSHConfig
import com.blackbox.ai.service.SSHService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeout

/**
 * Health state for a single runtime agent.
 */
data class AgentHealthState(
    val installed: Boolean = false,
    val running: Boolean = false,
    val healthy: Boolean = false,
    val lastCheck: Long = 0L,
    val detail: String = "Not checked"
) {
    val statusLabel: String
        get() = when {
            healthy -> "Running"
            running && !healthy -> "Running (unhealthy)"
            installed && !running -> "Installed"
            else -> "Stopped"
        }

    companion object {
        val UNKNOWN = AgentHealthState(detail = "Unknown")
    }
}

/**
 * Drives the local Termux/Ubuntu runtime exactly like Blackbox does: an SSH channel
 * into the Termux-hosted environment (default 127.0.0.1:8025, user-configurable).
 * Runtime agents (Hermes, Codex CLI, OpenClaw) are installed, started, stopped,
 * and health-checked here using the termux-agents-hub command patterns.
 *
 * Supports two runtime modes from EngineKeysStore:
 * - RUNTIME_MODE_TERMUX: existing SSH path
 * - RUNTIME_MODE_PROOT: ProotManager-based Linux proot environment
 */
object AgentRuntimeManager {

    private const val INSTALL_TIMEOUT_MS = 180_000L
    private const val START_TIMEOUT_MS = 30_000L
    private const val MAX_CONSOLE = 60_000

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _console = MutableStateFlow("")
    val console: StateFlow<String> = _console.asStateFlow()

    private val _agentHealth = MutableStateFlow<Map<String, AgentHealthState>>(emptyMap())
    val agentHealth: StateFlow<Map<String, AgentHealthState>> = _agentHealth.asStateFlow()

    fun getAgentHealth(agentId: String): AgentHealthState =
        _agentHealth.value[agentId] ?: AgentHealthState.UNKNOWN

    suspend fun refreshAgentHealth(context: Context, agent: RuntimeAgent) {
        val installed = checkInstalled(context, agent)
        val healthResult = runCatching { health(context, agent) }.getOrNull()
        val healthText = healthResult?.getOrNull() ?: ""

        val running = healthText.contains("ALIVE", ignoreCase = true) ||
            healthText.contains("HTTP 200", ignoreCase = true) ||
            healthText.contains("HTTP 2", ignoreCase = true)
        val healthy = running || (installed && healthText.contains("PORT ${agent.port}", ignoreCase = true) && !healthText.contains("CLOSED", ignoreCase = true))

        val detail = when {
            healthText.isNotBlank() -> healthText.trim().take(120)
            installed -> "Installed"
            else -> "Not installed"
        }

        val state = AgentHealthState(
            installed = installed,
            running = running,
            healthy = healthy,
            lastCheck = System.currentTimeMillis(),
            detail = detail
        )
        _agentHealth.value = _agentHealth.value + (agent.id to state)
    }

    suspend fun refreshAllAgentHealth(context: Context) {
        for (agent in AgentCatalog.all) {
            refreshAgentHealth(context, agent)
        }
    }

    private fun sshService(context: Context): SSHService = SSHService(context)
    private fun prootService(context: Context): ProotManager = ProotManager(context)

    private fun logFile(agentId: String): String = "\$HOME/.blackbox-agents/$agentId.log"
    private fun pidFile(agentId: String): String = "\$HOME/.blackbox-agents/$agentId.pid"

    fun runtimeMode(context: Context): String = EngineKeysStore(context).getRuntimeMode()

    suspend fun connect(context: Context): Result<String> {
        val keys = EngineKeysStore(context)
        return when (keys.getRuntimeMode()) {
            EngineKeysStore.RUNTIME_MODE_PROOT -> connectProot(context, keys)
            else -> connectTermux(context, keys)
        }
    }

    suspend fun disconnect(context: Context) {
        when (EngineKeysStore(context).getRuntimeMode()) {
            EngineKeysStore.RUNTIME_MODE_PROOT -> disconnectProot(context)
            else -> sshService(context).disconnect()
        }
    }

    suspend fun checkInstalled(context: Context, agent: RuntimeAgent): Boolean {
        return execute(context, agent.installCheckCommand)
            .map { it.contains("INSTALLED") }
            .getOrDefault(false)
    }

    suspend fun install(context: Context, agent: RuntimeAgent): Result<String> {
        val result = runWithConsole(context, "Install ${agent.name}") {
            if (!isConnected(context)) {
                return@runWithConsole "Not connected. Connect first, then retry Install."
            }
            var lastOutput = "Install complete"
            var failed = false
            for (cmd in agent.installCommands) {
                if (failed) break
                appendConsole("> $cmd")
                val out = runCatching {
                    withTimeout(INSTALL_TIMEOUT_MS) {
                        execute(context, cmd)
                    }
                }.getOrElse { Result.failure(it) }
                out.fold(
                    onSuccess = { output ->
                        appendConsole(output.trim().takeLast(1200))
                        lastOutput = "Install complete"
                    },
                    onFailure = { e ->
                        appendConsole("FAILED: ${e.message}")
                        lastOutput = "Install failed: ${e.message}"
                        failed = true
                    }
                )
            }
            lastOutput
        }
        result.onSuccess { refreshAgentHealth(context, agent) }
        return result
    }

    suspend fun start(context: Context, agent: RuntimeAgent): Result<String> {
        val result = runWithConsole(context, "Start ${agent.name}") {
            if (!isConnected(context)) {
                return@runWithConsole "Not connected. Connect first, then retry Start."
            }
            val cmd = buildString {
                append("mkdir -p \$HOME/.blackbox-agents\n")
                append("cd \$HOME\n")
                append("nohup bash -c '${agent.runCommand}' > ${logFile(agent.id)} 2>&1 &\n")
                append("echo \$! > ${pidFile(agent.id)}\n")
                append("sleep 2\n")
                append("if kill -0 \$(cat ${pidFile(agent.id)} 2>/dev/null) 2>/dev/null; then echo STARTED; else echo FAILED; fi")
            }
            appendConsole("> ${agent.runCommand}")
            runCatching {
                withTimeout(START_TIMEOUT_MS) { execute(context, cmd) }
            }.getOrElse { Result.failure(it) }.fold(
                onSuccess = { "Start: ${it.trim()}" },
                onFailure = { e -> "Start failed: ${e.message}" }
            )
        }
        result.onSuccess { refreshAgentHealth(context, agent) }
        return result
    }

    suspend fun stop(context: Context, agent: RuntimeAgent): Result<String> {
        val result = runWithConsole(context, "Stop ${agent.name}") {
            if (!isConnected(context)) {
                return@runWithConsole "Not connected. Connect first, then retry Stop."
            }
            val pattern = bracketPattern(agent.stopPattern)
            val cmd = buildString {
                append("if [ -f ${pidFile(agent.id)} ]; then kill \$(cat ${pidFile(agent.id)}) 2>/dev/null; rm -f ${pidFile(agent.id)}; fi\n")
                append("pkill -f '$pattern' 2>/dev/null\n")
                append("echo STOPPED")
            }
            execute(context, cmd).fold(
                onSuccess = { "Stop: ${it.trim()}" },
                onFailure = { e -> "Stop: ${e.message}" }
            )
        }
        result.onSuccess { refreshAgentHealth(context, agent) }
        return result
    }

    suspend fun health(context: Context, agent: RuntimeAgent): Result<String> {
        val installed = checkInstalled(context, agent)
        val cmd = buildString {
            if (installed) {
                append("INSTALLED=YES\n")
            } else {
                append("INSTALLED=NO\n")
            }
            append("if [ -f ${pidFile(agent.id)} ]; then\n")
            append("  PID=\$(cat ${pidFile(agent.id)} 2>/dev/null)\n")
            append("  if kill -0 \$PID 2>/dev/null; then echo \"PID \$PID ALIVE\"; else echo \"PID \$PID DEAD\"; fi\n")
            append("else echo \"NOT RUNNING\"; fi\n")
            if (agent.port > 0) {
                append("curl -s -o /dev/null -w \"PORT ${agent.port} HTTP %{http_code}\" --max-time 5 http://127.0.0.1:${agent.port} 2>/dev/null || echo \"PORT ${agent.port} CLOSED\"")
            }
        }
        return execute(context, cmd)
    }

    suspend fun logTail(context: Context, agent: RuntimeAgent, lines: Int = 80): Result<String> {
        val cmd = "tail -n $lines ${logFile(agent.id)} 2>&1 || echo 'No log yet'"
        return execute(context, cmd)
    }

    suspend fun ensureWorkspaceFolder(context: Context, folder: String): Result<String> {
        val path = "/workspace/$folder"
        val cmd = "mkdir -p $path && echo OK:$path"
        return execute(context, cmd)
    }

    private suspend fun connectTermux(context: Context, keys: EngineKeysStore): Result<String> {
        val config = SSHConfig(
            host = keys.getTermuxHost(),
            port = keys.getTermuxPort(),
            user = keys.getTermuxUser(),
            password = keys.getTermuxPassword()
        )
        return sshService(context).connect(config)
            .map { "Connected to ${config.host}:${config.port} as ${config.user}" }
    }

    private suspend fun connectProot(context: Context, keys: EngineKeysStore): Result<String> {
        val pm = prootService(context)
        return runCatching {
            if (!pm.isRootfsReady()) {
                throw Exception("Linux proot rootfs is not ready yet. Install it first.")
            }
            keys.setProotInstalled(true)
            "Linux proot runtime ready at rootfs=${pm.rootfsDir.absolutePath}"
        }
    }

    private fun disconnectProot(context: Context) {
        val pm = prootService(context)
        runCatching { pm.stopRunningServers() }
    }

    private suspend fun execute(context: Context, command: String): Result<String> {
        return when (EngineKeysStore(context).getRuntimeMode()) {
            EngineKeysStore.RUNTIME_MODE_PROOT -> executeProot(context, command)
            else -> executeTermux(context, command)
        }
    }

    private suspend fun executeTermux(context: Context, command: String): Result<String> {
        return sshService(context).executeCommand(command)
    }

    private suspend fun executeProot(context: Context, command: String): Result<String> {
        val pm = prootService(context)
        return runCatching {
            val sb = StringBuilder()
            val code = pm.executeCommand(command, onOutput = { sb.appendLine(it) })
            if (code == 0) sb.toString() else throw Exception(sb.toString().ifBlank { "Proot command failed with code $code" })
        }
    }

    suspend fun isConnected(context: Context): Boolean {
        return when (EngineKeysStore(context).getRuntimeMode()) {
            EngineKeysStore.RUNTIME_MODE_PROOT -> {
                val pm = prootService(context)
                pm.isRootfsReady()
            }
            else -> SSHService.checkConnection()
        }
    }

    private suspend fun runWithConsole(
        context: Context,
        label: String,
        block: suspend () -> String
    ): Result<String> {
        _isBusy.value = true
        _console.value = "\n=== $label ===\n"
        val result = try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
        _console.value += "\n"
        _isBusy.value = false
        return result
    }

    private fun appendConsole(text: String) {
        val updated = _console.value + text + "\n"
        _console.value = if (updated.length > MAX_CONSOLE) updated.takeLast(MAX_CONSOLE) else updated
    }

    private fun bracketPattern(pattern: String): String {
        if (pattern.isEmpty()) return pattern
        return "[${pattern.first()}]${pattern.drop(1)}"
    }
}
