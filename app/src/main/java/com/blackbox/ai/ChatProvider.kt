package com.blackbox.ai

interface ChatProvider {
    suspend fun chat(messages: List<ChatResult.Message>): ChatResult
    suspend fun stop()
}
