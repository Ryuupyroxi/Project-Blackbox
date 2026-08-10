package com.blackbox.module.anyclaw.data

data class SessionLogEntry(
    val id: String,
    val timestamp: Long,
    val source: String,
    val message: String
)
