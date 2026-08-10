package com.blackbox.module.kai.runtime

class BuildEnvironmentManager {
    data class BuildEnvironmentState(val installed: Boolean = false, val ready: Boolean = false)
    data class BuildStep(val name: String, val status: String = "pending")
    data class BuildSystemInfo(val os: String = "linux", val arch: String = "arm64")
    data class BuildTerminalSession(val lines: List<String> = emptyList())
}
