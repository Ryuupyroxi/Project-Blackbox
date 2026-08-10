package com.blackbox.module.kai.runtime

class SandboxController {
    data class SandboxStatus(val installed: Boolean = false, val running: Boolean = false)
    suspend fun install() = false
    suspend fun start() = false
    suspend fun stop() = false
}
