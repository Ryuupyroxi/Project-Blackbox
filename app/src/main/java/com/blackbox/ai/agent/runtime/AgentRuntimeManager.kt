package com.blackbox.ai.agent.runtime

import android.content.Context
import com.blackbox.ai.engine.EngineKeysStore
import com.blackbox.ai.service.SSHConfig
import com.blackbox.ai.service.SSHService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeout

/**
 * Drives the local Termux/Ubuntu runtime exactly like Blackbox does: an SSH channel
 * into the Termux-hosted environment (default 127.0.0.1:8025, user-configurable).
 * Runtime agents (Hermes, Codex CLI, OpenClaw) are installed, started, stopped,
 * and health-checked here using the termux-agents-hub command patterns.
 */
object AgentRuntimeManager {

    private const val INSTALL_TIMEOUT_MS = 180_000L
    private const val START_TIMEOUT_MS = 30_000L
    private const val MAX_CONSOLE = 60_000

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _console = MutableStateFlow("")
    val console: StateFlow<String> = _console.asStateFlow()

    private fun sshService(context: Context): SSHService = SSHService(context)

    private fun logFile(agentId: String): String = "\$HOME/.blackbox-agents/$agentId.log"
    private fun pidFile(agentId: String): String = "\$HOME/.blackbox-agents/$agentId.pid"

    suspend fun connect(context: Context): Result<String> {
        val keys = EngineKeysStore(context)
        val config = SSHConfig(
            host = keys.getTermuxHost(),
            port = keys.getTermuxPort(),
            user = keys.getTermuxUser(),
            password = keys.getTermuxPassword()
        )
        return sshService(context).connect(config)
            .map { "Connected to ${config.host}:${config.port} as ${config.user}" }
    }

    suspend fun disconnect(context: Context) {
        sshService(context).disconnect()
    }

    suspend fun checkInstalled(context: Context, agent: RuntimeAgent): Boolean {
        val result = sshService(context).executeCommand(agent.installCheckCommand)
        return result.getOrDefault("MISSING").contains("INSTALLED")
    }

    suspend fun install(context: Context, agent: RuntimeAgent): Result<String> {
        return runWithConsole(context, "Install ${agent.name}") {
            val ssh = sshService(context)
            if (!ssh.checkConnection()) {
                return@runWithConsole "SSH not connected. Open Agent Hub → connect to 127.0.0.1:8025 (Blackbox Ubuntu) or 8022 (Termux) first."
            }
            var lastOutput = "Install complete"
            var failed = false
            for (cmd in agent.installCommands) {
                if (failed) break
                appendConsole("> $cmd")
                val out = runCatching {
                    withTimeout(INSTALL_TIMEOUT_MS) {
                        sshService(context).executeCommand(cmd)
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
    }

    suspend fun start(context: Context, agent: RuntimeAgent): Result<String> {
        return runWithConsole(context, "Start ${agent.name}") {
            val ssh = sshService(context)
            if (!ssh.checkConnection()) {
                return@runWithConsole "SSH not connected. Open Agent Hub → connect to 127.0.0.1:8025 (Blackbox Ubuntu) or 8022 (Termux) first."
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
                withTimeout(START_TIMEOUT_MS) { sshService(context).executeCommand(cmd) }
            }.getOrElse { Result.failure(it) }.fold(
                onSuccess = { "Start: ${it.trim()}" },
                onFailure = { e -> "Start failed: ${e.message}" }
            )
        }
    }

    suspend fun stop(context: Context, agent: RuntimeAgent): Result<String> {
        return runWithConsole(context, "Stop ${agent.name}") {
            val pattern = bracketPattern(agent.stopPattern)
            val cmd = buildString {
                append("if [ -f ${pidFile(agent.id)} ]; then kill \$(cat ${pidFile(agent.id)}) 2>/dev/null; rm -f ${pidFile(agent.id)}; fi\n")
                append("pkill -f '$pattern' 2>/dev/null\n")
                append("echo STOPPED")
            }
            sshService(context).executeCommand(cmd).fold(
                onSuccess = { "Stop: ${it.trim()}" },
                onFailure = { e -> "Stop: ${e.message}" }
            )
        }
    }

    suspend fun health(context: Context, agent: RuntimeAgent): Result<String> {
        val cmd = buildString {
            append("if [ -f ${pidFile(agent.id)} ]; then\n")
            append("  if kill -0 \$(cat ${pidFile(agent.id)} 2>/dev/null) 2>/dev/null; then echo \"PID \$(cat ${pidFile(agent.id)}) ALIVE\"; else echo \"PID \$(cat ${pidFile(agent.id)}) DEAD\"; fi\n")
            append("else echo \"NOT RUNNING\"; fi\n")
            append("curl -s -o /dev/null -w \"PORT ${agent.port} HTTP %{http_code}\" --max-time 5 http://127.0.0.1:${agent.port} 2>/dev/null || echo \"PORT ${agent.port} CLOSED\"")
        }
        return sshService(context).executeCommand(cmd)
    }

    suspend fun logTail(context: Context, agent: RuntimeAgent, lines: Int = 80): Result<String> {
        val cmd = "tail -n $lines ${logFile(agent.id)} 2>&1 || echo 'No log yet'"
        return sshService(context).executeCommand(cmd)
    }

    suspend fun ensureWorkspaceFolder(context: Context, folder: String): Result<String> {
        val path = "/workspace/$folder"
        val cmd = "mkdir -p $path && echo OK:$path"
        return sshService(context).executeCommand(cmd)
    }

    /**
     * Turn "codex" into "[c]odex" so pkill -f does not match its own command line.
     */
    private fun bracketPattern(pattern: String): String {
        if (pattern.isEmpty()) return pattern
        return "[${pattern.first()}]${pattern.drop(1)}"
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
}
