package com.blackbox.module.anyclaw.data

enum class GatewayStatus { STOPPED, STARTING, RUNNING, STOPPING, ERROR }

data class BundleUpdateFailureRecord(
    val bundledVersion: String,
    val timestamp: Long,
    val failureType: String
)
