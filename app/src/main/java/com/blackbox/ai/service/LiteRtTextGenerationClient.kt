package com.blackbox.ai.service

import android.content.Context
import com.blackbox.ai.R
import com.blackbox.ai.data.model.LITERT_BACKEND_AUTO
import com.blackbox.ai.data.model.LITERT_BACKEND_CPU
import com.blackbox.ai.data.model.LITERT_BACKEND_GPU
import com.blackbox.ai.data.model.LiteRtModelEntity
import com.blackbox.ai.data.model.LlamaChatEntity
import com.blackbox.ai.data.model.defaultLiteRtChatContextTokens
import com.blackbox.ai.data.model.isLikelyLiteRtGpuPackage
import com.blackbox.ai.data.model.normalizeLiteRtBackend
import com.blackbox.ai.util.DebugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LiteRtTextGenerationResult(
    val output: String,
    val rawOutput: String,
    val stats: LiteRtLmChatStats
)

class LiteRtTextGenerationClient(private val context: Context) {
    suspend fun generate(
        model: LiteRtModelEntity,
        title: String,
        systemPrompt: String,
        messages: List<LiteRtConversationMessage>,
        userPrompt: String,
        contextSize: Int,
        maxTokens: Int?,
        temperature: Float,
        thinkingEnabled: Boolean,
        backendMode: String,
        mtpEnabled: Boolean,
        userImagePath: String? = null,
        userAudioPath: String? = null,
        onChunk: suspend (String) -> Unit = {},
        onThinkingChunk: suspend (String) -> Unit = {}
    ): LiteRtTextGenerationResult = withContext(Dispatchers.IO) {
        val resolvedContext = contextSize
            .takeIf { it > 0 }
            ?: model.defaultLiteRtChatContextTokens()
            ?: 4_000
        val output = StringBuilder()
        val thinking = StringBuilder()
        val request = LiteRtLmChatRequest(
            model = model,
            chat = LlamaChatEntity(
                title = title,
                contextSize = resolvedContext,
                systemPrompt = systemPrompt
            ),
            history = emptyList(),
            backendMode = normalizeLiteRtBackend(backendMode),
            params = mapOf(
                "temperature" to temperature.toDouble(),
                "top_k" to 40,
                "top_p" to 0.95,
                "enable_thinking" to thinkingEnabled,
                LITERT_PARAM_MTP_ENABLED to mtpEnabled,
                LITERT_PARAM_MAX_OUTPUT_TOKENS to (maxTokens ?: resolvedContext)
            ),
            conversationOverride = LiteRtConversationOverride(
                systemInstruction = systemPrompt,
                initialMessages = messages,
                userMessage = userPrompt,
                userImagePath = userImagePath,
                userAudioPath = userAudioPath
            )
        )
        val stats = streamSafely(
            model = model,
            request = request,
            onStatus = {},
            onChunk = { chunk ->
                output.append(chunk)
                onChunk(chunk)
            },
            onThinkingChunk = { chunk ->
                thinking.append(chunk)
                onThinkingChunk(chunk)
            }
        )
        val raw = output.toString()
        val cleaned = PDFSummaryLogic.cleanLlamaOutput(raw)
        LiteRtTextGenerationResult(
            output = cleaned,
            rawOutput = raw,
            stats = stats.copy(
                completionTokens = stats.completionTokens.takeIf { it > 0 }
                    ?: estimateLiteRtCompletionTokens(cleaned)
            )
        )
    }

    private suspend fun streamSafely(
        model: LiteRtModelEntity,
        request: LiteRtLmChatRequest,
        onStatus: suspend (String) -> Unit,
        onChunk: suspend (String) -> Unit,
        onThinkingChunk: suspend (String) -> Unit
    ): LiteRtLmChatStats {
        val backendMode = normalizeLiteRtBackend(request.backendMode)

        suspend fun runGpuWorker(): LiteRtLmChatStats =
            LiteRtLmWorkerClient(context).streamGpuChat(
                request = request.copy(backendMode = LITERT_BACKEND_GPU),
                onStatus = onStatus,
                onChunk = onChunk,
                onThinkingChunk = onThinkingChunk
            )

        if (backendMode == LITERT_BACKEND_GPU) {
            return try {
                runGpuWorker()
            } catch (error: Throwable) {
                val detail = error.message?.takeIf { it.isNotBlank() } ?: error.javaClass.name
                LiteRtLmAcceleratorHealth.recordGpuCrash(context, model, detail)
                throw IllegalStateException(
                    context.getString(R.string.litert_error_explicit_backend_failed, "GPU", detail),
                    error
                )
            }
        }

        if (backendMode == LITERT_BACKEND_AUTO && model.supportsGpu && model.isLikelyLiteRtGpuPackage()) {
            if (!LiteRtLmAcceleratorHealth.isGpuQuarantined(context, model)) {
                try {
                    return runGpuWorker()
                } catch (error: Throwable) {
                    val detail = error.message?.takeIf { it.isNotBlank() } ?: error.javaClass.name
                    LiteRtLmAcceleratorHealth.recordGpuCrash(context, model, detail)
                    DebugLog.log("LiteRtTextGenerationClient: GPU worker failed, falling back to CPU: $detail")
                }
            }
        }

        return LiteRtLmChatService(context).streamChat(
            request = request.copy(backendMode = LITERT_BACKEND_CPU),
            onStatus = onStatus,
            onChunk = onChunk,
            onThinkingChunk = onThinkingChunk
        )
    }
}
