package com.blackbox.module.kai.model

data class Conversation(
    val id: String,
    val title: String = "",
    val systemPrompt: String? = null,
    val provider: Service,
    val modelId: String = "",
    val messages: List<Message> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
