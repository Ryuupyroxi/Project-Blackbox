package com.blackbox.module.kai.data

import kotlinx.serialization.Serializable

@Serializable
data class BlackboxMessage(
    val id: String,
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
