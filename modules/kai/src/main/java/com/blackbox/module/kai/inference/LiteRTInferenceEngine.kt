package com.blackbox.module.kai.inference

import kotlinx.coroutines.flow.Flow

class LiteRTInferenceEngine {
    data class EngineState(val loaded: Boolean = false, val modelId: String = "")
    suspend fun chat(messages: List<com.blackbox.module.kai.data.BlackboxMessage>): Flow<String> = kotlinx.coroutines.flow.flowOf("")
    suspend fun initialize(modelId: String) = false
    suspend fun release() {}
}
