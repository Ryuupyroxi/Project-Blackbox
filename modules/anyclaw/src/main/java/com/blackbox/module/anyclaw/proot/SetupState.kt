package com.blackbox.module.anyclaw.proot

sealed interface SetupState {
    data object Idle : SetupState
    data object Running : SetupState
    data class Progress(val step: SetupStep, val progress: Float) : SetupState
    data class Complete(val success: Boolean) : SetupState
    data class Error(val message: String) : SetupState
}
