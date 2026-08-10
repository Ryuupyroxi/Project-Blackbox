package com.blackbox.module.kai.network.dtos.anthropic

import kotlinx.serialization.Serializable

@Serializable
data class AnthropicChatRequest(
    val model: String,
    val messages: List<AnthropicChatMessage>,
    val maxTokens: Int = 1024,
    val system: String? = null,
    val tools: List<Map<String, String>>? = null
)

@Serializable
data class AnthropicChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class AnthropicChatResponse(
    val id: String,
    val model: String,
    val content: List<AnthropicChatDelta>,
    val stopReason: String? = null
)

@Serializable
data class AnthropicChatDelta(
    val type: String,
    val text: String? = null
)

@Serializable
data class AnthropicChatUsage(
    val inputTokens: Int,
    val outputTokens: Int
)
