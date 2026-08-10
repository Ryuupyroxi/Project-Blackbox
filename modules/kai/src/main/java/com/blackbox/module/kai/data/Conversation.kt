package com.blackbox.module.kai.data

import kotlinx.serialization.Serializable

@Serializable
data class Conversation(
    val id: String,
    val title: String = "",
    val serviceId: String = "",
    val modelId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    @Serializable
    data class Message(
        val id: String,
        val role: Role,
        val content: String,
        val timestamp: Long = System.currentTimeMillis()
    )
}
