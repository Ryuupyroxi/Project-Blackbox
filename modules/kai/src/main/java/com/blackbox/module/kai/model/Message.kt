package com.blackbox.module.kai.model

data class Message(
    val id: String,
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
