package com.blackbox.module.kai.inference

import kotlinx.coroutines.flow.Flow

sealed interface LocalInferenceEngine {
    suspend fun chat(messages: List<InferenceMessage>): Flow<String>
    suspend fun importModel(target: ImportTarget): ModelImportResult
    suspend fun deleteModel(modelId: String)
    suspend fun scanImportedModels(): List<LocalModel>
}
