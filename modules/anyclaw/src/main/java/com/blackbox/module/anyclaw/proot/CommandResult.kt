package com.blackbox.module.anyclaw.proot

sealed interface CommandResult {
    data class Ok(val output: String) : CommandResult
    data class Err(val error: String) : CommandResult
}
