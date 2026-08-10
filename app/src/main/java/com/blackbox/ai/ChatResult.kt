package com.blackbox.ai

sealed interface ChatResult {
    data class Message(val content: String, val role: String) : ChatResult
    data class ToolCall(val id: String, val name: String, val args: String) : ChatResult
    data class Error(val message: String) : ChatResult
}
