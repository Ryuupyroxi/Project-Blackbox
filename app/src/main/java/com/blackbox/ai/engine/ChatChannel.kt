package com.blackbox.ai.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Unified execution channels. Every Blackbox option can be run through any of
 * these:
 *  - [LocalOpenAi]: on-device llama.cpp / Ollama OpenAI-compatible server
 *  - [OpenAi]: any OpenAI API key
 *  - [OpenRouter]: any OpenRouter API key (one key, many models)
 *  - [Anthropic]: any Anthropic API key
 *
 * The Termux runtime is a separate execution channel (see AgentRuntimeManager);
 * it provides shell/agent execution rather than chat completion.
 */
sealed class ChatChannel {

    abstract val label: String

    data class LocalOpenAi(val baseUrl: String, val model: String) : ChatChannel() {
        override val label: String = "Local server"
    }

    data class OpenAi(val apiKey: String, val model: String) : ChatChannel() {
        override val label: String = "OpenAI"
    }

    data class OpenRouter(val apiKey: String, val model: String) : ChatChannel() {
        override val label: String = "OpenRouter"
    }

    data class Anthropic(val apiKey: String, val model: String) : ChatChannel() {
        override val label: String = "Anthropic"
    }

    /** True when this channel has what it needs to run. */
    fun isConfigured(): Boolean = when (this) {
        is LocalOpenAi -> baseUrl.isNotBlank()
        is OpenAi -> apiKey.isNotBlank()
        is OpenRouter -> apiKey.isNotBlank()
        is Anthropic -> apiKey.isNotBlank()
    }

    fun detail(): String = when (this) {
        is LocalOpenAi -> "$baseUrl · $model"
        is OpenAi -> model
        is OpenRouter -> model
        is Anthropic -> model
    }
}

/**
 * Builds the list of enabled channels from the key store, in priority order:
 * local first, then any API key channels the user enabled.
 */
fun enabledChannels(keys: EngineKeysStore): List<ChatChannel> {
    val result = mutableListOf<ChatChannel>()
    if (keys.isLocalEnabled()) {
        result += ChatChannel.LocalOpenAi(keys.getLocalBaseUrl(), keys.getLocalModel())
    }
    if (keys.isOpenRouterEnabled() && keys.getOpenRouterKey().isNotBlank()) {
        result += ChatChannel.OpenRouter(keys.getOpenRouterKey(), keys.getOpenRouterModel())
    }
    if (keys.isOpenAiEnabled() && keys.getOpenAiKey().isNotBlank()) {
        result += ChatChannel.OpenAi(keys.getOpenAiKey(), keys.getOpenAiModel())
    }
    if (keys.isAnthropicEnabled() && keys.getAnthropicKey().isNotBlank()) {
        result += ChatChannel.Anthropic(keys.getAnthropicKey(), keys.getAnthropicModel())
    }
    return result
}

/**
 * Minimal OpenAI-compatible / Anthropic chat client used by the Agent Hub.
 * Messages are (role, content) pairs. Returns the assistant text reply.
 */
object ChatChannelClient {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun httpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun chat(
        channel: ChatChannel,
        messages: List<Pair<String, String>>,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val body = when (channel) {
                is ChatChannel.Anthropic -> anthropicBody(channel.model, messages)
                else -> openAiBody(channel, messages)
            }
            val url = when (channel) {
                is ChatChannel.Anthropic -> "https://api.anthropic.com/v1/messages"
                is ChatChannel.OpenAi -> "https://api.openai.com/v1/chat/completions"
                is ChatChannel.OpenRouter -> "https://openrouter.ai/api/v1/chat/completions"
                is ChatChannel.LocalOpenAi ->
                    channel.baseUrl.trimEnd('/') + "/v1/chat/completions"
            }
            val requestBuilder = Request.Builder().url(url).post(body.toRequestBody(jsonMediaType))
            when (channel) {
                is ChatChannel.Anthropic -> {
                    requestBuilder.header("x-api-key", channel.apiKey)
                    requestBuilder.header("anthropic-version", "2023-06-01")
                }
                is ChatChannel.OpenAi -> requestBuilder.header("Authorization", "Bearer ${channel.apiKey}")
                is ChatChannel.OpenRouter -> requestBuilder.header("Authorization", "Bearer ${channel.apiKey}")
                is ChatChannel.LocalOpenAi -> Unit
            }
            httpClient().newCall(requestBuilder.build()).execute().use { response ->
                val text = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("HTTP ${response.code}: ${text.take(500)}")
                    )
                }
                val parsed = when (channel) {
                    is ChatChannel.Anthropic -> JSONObject(text)
                        .optJSONArray("content")?.optJSONObject(0)?.optString("text") ?: ""
                    else -> JSONObject(text)
                        .optJSONArray("choices")?.optJSONObject(0)
                        ?.optJSONObject("message")?.optString("content") ?: ""
                }
                if (parsed.isBlank()) {
                    Result.failure(Exception("Empty reply from ${channel.label}"))
                } else {
                    Result.success(parsed.trim())
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Sends a one-message ping and reports whether the channel answered. */
    suspend fun test(channel: ChatChannel): Result<String> = chat(
        channel,
        listOf("user" to "Reply with exactly: OK")
    )

    private fun openAiBody(channel: ChatChannel, messages: List<Pair<String, String>>): String {
        val arr = JSONArray()
        for ((role, content) in messages) {
            arr.put(JSONObject().put("role", role).put("content", content))
        }
        val model = when (channel) {
            is ChatChannel.LocalOpenAi -> channel.model
            is ChatChannel.OpenAi -> channel.model
            is ChatChannel.OpenRouter -> channel.model
            else -> ""
        }
        return JSONObject()
            .put("model", model)
            .put("messages", arr)
            .put("max_tokens", 2048)
            .toString()
    }

    private fun anthropicBody(model: String, messages: List<Pair<String, String>>): String {
        val arr = JSONArray()
        for ((role, content) in messages) {
            // Anthropic only accepts user/assistant turns in "messages". A future
            // system prompt is folded into the first user turn rather than dropped.
            val mappedRole = if (role == "assistant") "assistant" else "user"
            val mappedContent = if (role == "system") "System instruction: $content" else content
            arr.put(JSONObject().put("role", mappedRole).put("content", mappedContent))
        }
        return JSONObject()
            .put("model", model)
            .put("max_tokens", 2048)
            .put("messages", arr)
            .toString()
    }
}
