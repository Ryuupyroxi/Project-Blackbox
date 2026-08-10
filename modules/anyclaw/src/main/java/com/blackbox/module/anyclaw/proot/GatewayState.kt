package com.blackbox.module.anyclaw.proot

sealed interface GatewayState {
    data object Stopped : GatewayState
    data object Starting : GatewayState
    data object Running : GatewayState
    data class Error(val message: String) : GatewayState
}
