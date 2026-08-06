package com.blackbox.ai.agent.runtime

import android.content.Context
import com.blackbox.ai.engine.EngineKeysStore
import com.blackbox.ai.runtime.BootstrapInstaller
import com.blackbox.ai.runtime.CodexServerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * Zero-setup LOCAL channel: the embedded Termux bootstrap + Node.js + Codex CLI
 * inside the app sandbox (ported from AnyClaw/OpenClaw). No Termux install or
 * SSH server is required for this channel.
 *
 * WorkspaceChannel.LOCAL maps to this manager; WorkspaceChannel.SSH keeps
 * routing to [AgentRuntimeManager] (ADT's Termux-hosted Ubuntu proot).
 */
object EmbeddedRuntimeManager {

    private const val INSTALL_TIMEOUT_MS = 600_000L
    private const val START_TIMEOUT_MS = 120_000L
    private const val MAX_CONSOLE = 80_000

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _console = MutableStateFlow("")
    val console: StateFlow<String> = _console.asStateFlow()

    private fun manager(context: Context): CodexServerManager = CodexServerManager(context)

    suspend fun isInstalled(context: Context): Boolean = withContext(Dispatchers.IO) {
        BootstrapInstaller.isBootstrapInstalled(context)
    }

    data class RuntimeStatus(
        val bootstrap: Boolean = false,
        val node: Boolean = false,
        val proot: Boolean = false,
        val codex: Boolean = false,
        val platformBinary: Boolean = false,
        val serverRunning: Boolean = false,
        val loggedIn: Boolean = false,
    ) {
        val ready: Boolean
            get() = bootstrap && node && codex && platformBinary
    }

    suspend fun status(context: Context): RuntimeStatus = withContext(Dispatchers.IO) {
        val m = manager(context)
        RuntimeStatus(
            bootstrap = BootstrapInstaller.isBootstrapInstalled(context),
            node = m.isNodeInstalled(),
            proot = m.isProotInstalled(),
            codex = m.isCodexInstalled(),
            platformBinary = m.isPlatformBinaryInstalled(),
            serverRunning = m.isRunning,
            loggedIn = runCatching { m.isLoggedIn() }.getOrDefault(false),
        )
    }

    /**
     * Full LOCAL runtime install: bootstrap → Node → platform binary → Codex CLI.
     * Each step is bounded by a timeout and streamed to the console.
     */
    suspend fun install(context: Context): Result<String> = runWithConsole(context, "Install LOCAL runtime") {
        val m = manager(context)
        var last = "Install failed"
        val steps = listOf(
            "Extract embedded Termux bootstrap" to { progress: (String) -> Unit ->
                BootstrapInstaller.install(context) { progress(it) }
                true
            },
            "Install Node.js" to { progress: (String) -> Unit ->
                m.installNode { progress(it) }
            },
            "Install platform binary (Codex native)" to { progress: (String) -> Unit ->
                m.installPlatformBinary { progress(it) }
            },
            "Install Codex CLI" to { progress: (String) -> Unit ->
                m.installCodex { progress(it) }
            },
        )
        var failed = false
        for ((label, step) in steps) {
            if (failed) break
            appendConsole("\n[$label]")
            val ok = runCatching {
                withTimeout(INSTALL_TIMEOUT_MS) {
                    withContext(Dispatchers.IO) {
                        step { text -> appendConsole(text) }
                    }
                }
            }.getOrElse { e ->
                appendConsole("FAILED: ${e.message}")
                last = "Install failed at $label: ${e.message}"
                failed = true
                false
            }
            if (!failed && !ok) {
                last = "Install failed at $label (step reported failure)"
                failed = true
            }
        }
        if (!failed) {
            m.ensureDefaultWorkspace()
            m.ensureFullAccessConfig()
            last = "LOCAL runtime installed"
        }
        last
    }

    suspend fun startServer(context: Context): Result<String> = runWithConsole(context, "Start LOCAL server") {
        val m = manager(context)
        if (!BootstrapInstaller.isBootstrapInstalled(context)) {
            return@runWithConsole "LOCAL runtime not installed yet"
        }
        val ok = runCatching {
            withTimeout(START_TIMEOUT_MS) {
                withContext(Dispatchers.IO) {
                    m.startProxy()
                    m.startServer()
                }
            }
        }.getOrElse { e ->
            appendConsole("FAILED: ${e.message}")
            return@runWithConsole "Start failed: ${e.message}"
        }
        val ready = runCatching { m.waitForServer(45_000) }.getOrDefault(false)
        if (ok && ready) "Server running on http://127.0.0.1:${CodexServerManager.SERVER_PORT}" else "Server process started (health check pending)"
    }

    suspend fun stop(context: Context): Result<String> = runWithConsole(context, "Stop LOCAL server") {
        withContext(Dispatchers.IO) { manager(context).stopServer() }
        "LOCAL server stopped"
    }

    /**
     * Pipe an API key into `codex login --with-api-key` inside the prefix.
     */
    suspend fun login(context: Context, apiKey: String): Result<String> = runWithConsole(context, "Codex login") {
        if (apiKey.isBlank()) return@runWithConsole "No API key provided"
        val ok = runCatching {
            withTimeout(START_TIMEOUT_MS) {
                withContext(Dispatchers.IO) { manager(context).loginWithApiKey(apiKey) }
            }
        }.getOrDefault(false)
        if (ok) "Logged in" else "Login failed"
    }

    suspend fun healthCheck(context: Context): Result<String> = runWithConsole(context, "LOCAL health check") {
        val ok = runCatching {
            withTimeout(START_TIMEOUT_MS) {
                withContext(Dispatchers.IO) {
                    manager(context).startProxy()
                    manager(context).healthCheck { appendConsole(it) }
                }
            }
        }.getOrDefault(false)
        if (ok) "Health check passed" else "Health check failed (login required?)"
    }

    suspend fun runInPrefix(context: Context, command: String): Result<String> = runWithConsole(context, "Run command") {
        val sb = StringBuilder()
        val code = withContext(Dispatchers.IO) {
            manager(context).runInPrefix(command) { line -> appendConsole(line); sb.appendLine(line) }
        }
        if (code == 0) "OK\n${sb.toString().trim()}" else "Exit $code\n${sb.toString().trim()}"
    }

    fun apiKeyFromStore(context: Context): String {
        return runCatching { EngineKeysStore(context).getOpenAiKey() }.getOrDefault("")
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
