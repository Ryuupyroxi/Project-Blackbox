package com.blackbox.ai.agent.runtime

/**
 * A coding agent that can be installed and run inside the local Termux/Ubuntu
 * runtime over the SSH channel. The commands below are taken directly from the
 * termux-agents-hub script so behavior matches the proven Termux workflow.
 */
data class RuntimeAgent(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val port: Int,
    val webUrl: String?,
    val installCheckCommand: String,
    val installCommands: List<String>,
    val runCommand: String,
    val stopPattern: String,
    val runCommands: List<String> = emptyList(),
    val stopPatterns: List<String> = emptyList(),
    val ports: List<Int> = emptyList()
) {
    /** Effective run commands: list if provided, else single runCommand wrapped in a list. */
    val effectiveRunCommands: List<String>
        get() = runCommands.ifEmpty { listOf(runCommand) }

    /** Effective stop patterns: list if provided, else single stopPattern. */
    val effectiveStopPatterns: List<String>
        get() = stopPatterns.ifEmpty { listOf(stopPattern) }

    /** Effective ports for health checks: list if provided, else single port. */
    val effectivePorts: List<Int>
        get() = ports.ifEmpty { listOf(port) }
}

object AgentCatalog {

    private const val DEPS = "if command -v pkg >/dev/null 2>&1; then pkg install -y nodejs python git curl 2>&1; else DEBIAN_FRONTEND=noninteractive apt-get update -y 2>&1 && DEBIAN_FRONTEND=noninteractive apt-get install -y nodejs npm python3 python3-pip curl git 2>&1; fi"

    val hermes = RuntimeAgent(
        id = "hermes",
        name = "Hermes",
        emoji = "🪄",
        description = "Nous Research agent: dashboard web UI + OpenAI-compatible chat API",
        port = 9119,
        webUrl = "http://127.0.0.1:9119",
        installCheckCommand = "command -v hermes >/dev/null 2>&1 && echo INSTALLED || echo MISSING",
        installCommands = listOf(
            DEPS,
            "pip install --no-input hermes-agent 2>&1 || pip3 install --no-input hermes-agent 2>&1"
        ),
        runCommand = "hermes dashboard --host 0.0.0.0 --port 9119 --no-open",
        stopPattern = "hermes.*dashboard",
        runCommands = listOf(
            "hermes dashboard --host 0.0.0.0 --port 9119 --no-open",
            "hermes chat --host 0.0.0.0 --port 9120"
        ),
        stopPatterns = listOf("hermes.*dashboard", "hermes.*serve"),
        ports = listOf(9119, 9120)
    )

    val codex = RuntimeAgent(
        id = "codex",
        name = "Codex CLI",
        emoji = "🤖",
        description = "OpenAI Codex CLI coding agent",
        port = 8082,
        webUrl = null,
        installCheckCommand = "command -v codex >/dev/null 2>&1 && echo INSTALLED || echo MISSING",
        installCommands = listOf(
            DEPS,
            "npm install -g --no-audit --no-fund @openai/codex 2>&1"
        ),
        runCommand = "codex",
        stopPattern = "node.*codex"
    )

    val openclaw = RuntimeAgent(
        id = "openclaw",
        name = "OpenClaw Gateway",
        emoji = "🐙",
        description = "Multi-agent gateway with web control UI",
        port = 18789,
        webUrl = "http://127.0.0.1:18789",
        installCheckCommand = "command -v openclaw >/dev/null 2>&1 && echo INSTALLED || echo MISSING",
        installCommands = listOf(
            DEPS,
            "npm install -g --no-audit --no-fund openclaw 2>&1"
        ),
        runCommand = "openclaw gateway --host 0.0.0.0 --port 18789",
        stopPattern = "openclaw"
    )

    val openCode = RuntimeAgent(
        id = "opencode",
        name = "OpenCode",
        emoji = "⚡",
        description = "Open-source AI coding agent (Go binary, TUI + LSP)",
        port = 8083,
        webUrl = null,
        installCheckCommand = "command -v opencode >/dev/null 2>&1 && echo INSTALLED || echo MISSING",
        installCommands = listOf(
            DEPS,
            "curl -fsSL https://raw.githubusercontent.com/opencode-ai/opencode/main/install.sh | bash 2>&1 || " +
                "(export GOPATH=/tmp/gopath && export PATH=\"\\$GOPATH/bin:\\$PATH\" && " +
                "command -v go >/dev/null 2>&1 || DEBIAN_FRONTEND=noninteractive apt-get install -y golang-go 2>&1 && " +
                "go install github.com/opencode-ai/opencode@latest 2>&1 && " +
                "cp \\$GOPATH/bin/opencode /usr/local/bin/opencode 2>&1)"
        ),
        runCommand = "opencode --non-interactive --port 8083",
        stopPattern = "opencode"
    )

    val all = listOf(hermes, codex, openclaw, openCode)
}
