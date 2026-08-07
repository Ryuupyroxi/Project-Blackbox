package com.blackbox.ai.engine

import android.content.Context
import com.blackbox.ai.service.OllamaService
import com.blackbox.ai.service.AgentTool
import com.blackbox.ai.service.OllamaService.ChatMessage
import com.blackbox.ai.service.OllamaService.ChatResponse
import com.blackbox.ai.service.OllamaService.ChatUsage
import com.blackbox.ai.service.OllamaService.ToolCall
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
 * Adapter that routes AgentService LLM calls through the unified ChatChannel.
 * This allows the agent to use any configured channel: Local, OpenAI, OpenRouter, Anthropic.
 *
 * Usage in AgentService:
 *   val adapter = AgentEngineAdapter(context, engineKeysStore)
 *   val response = adapter.chatWithToolsStreaming(...)
 *
 * The adapter mirrors the OllamaService/LlamaServerChatService streaming interface
 * so existing AgentService code can call it with minimal changes.
 */
class AgentEngineAdapter(
    private val context: Context,
    private val keys: EngineKeysStore,
) {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun httpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Selects the best available channel based on configuration and priority.
     * Order: Local → OpenRouter → OpenAI → Anthropic
     * Falls back to first available if local server is down.
     */
    private suspend fun selectChannel(): ChatChannel? = withContext(Dispatchers.IO) {
        val channels = enabledChannels(keys)
        for (channel in channels) {
            if (channel.isConfigured()) {
                // Quick health check for local server
                if (channel is ChatChannel.LocalOpenAi) {
                    if (checkLocalServer(channel.baseUrl)) return@withContext channel
                } else {
                    return@withContext channel
                }
            }
        }
        null
    }

    private suspend fun checkLocalServer(baseUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(baseUrl.trimEnd('/') + "/v1/models")
                .head()
                .build()
            httpClient().newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Streaming chat with tools — compatible with OllamaService.chatWithToolsStreaming signature.
     * Returns a Result containing the final ChatResponse.
     */
    suspend fun chatWithToolsStreaming(
        messages: List<ChatMessage>,
        tools: List<AgentTool>,
        modelLabel: String,
        thinkingEnabled: Boolean,
        numCtx: Int,
        onChunk: (String?, String?) -> Unit,
    ): Result<ChatResponse> = withContext(Dispatchers.IO) {
        val channel = selectChannel() ?: return@withContext Result.failure(Exception("No configured channel available"))

        // Convert messages + tools to OpenAI-compatible format
        val body = when (channel) {
            is ChatChannel.Anthropic -> buildAnthropicBody(channel.model, messages, tools, thinkingEnabled)
            else -> buildOpenAiBody(channel, messages, tools, thinkingEnabled, numCtx)
        }

        val url = when (channel) {
            is ChatChannel.Anthropic -> "https://api.anthropic.com/v1/messages"
            is ChatChannel.OpenAi -> "https://api.openai.com/v1/chat/completions"
            is ChatChannel.OpenRouter -> "https://openrouter.ai/api/v1/chat/completions"
            is ChatChannel.LocalOpenAi -> channel.baseUrl.trimEnd('/') + "/v1/chat/completions"
        }

        val requestBuilder = Request.Builder().url(url).post(body.toString().toRequestBody(jsonMediaType))
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
                return@withContext Result.failure(Exception("HTTP ${response.code}: $text"))
            }

            val result = when (channel) {
                is ChatChannel.Anthropic -> parseAnthropicResponse(channel, text, onChunk)
                else -> parseOpenAiStreamingResponse(channel, text, onChunk)
            }
            Result.success(result)
        }
    }

    /**
     * Non-streaming chat — for simpler calls.
     */
    suspend fun chat(
        messages: List<Pair<String, String>>,
    ): Result<String> = withContext(Dispatchers.IO) {
        val channel = selectChannel() ?: return@withContext Result.failure(Exception("No configured channel available"))
        return@withContext ChatChannelClient.chat(channel, messages)
    }

    // ── Anthropic request/response ────────────────────────────────────────────

    private fun buildAnthropicBody(
        model: String,
        messages: List<ChatMessage>,
        tools: List<AgentTool>,
        thinkingEnabled: Boolean,
    ): JSONObject {
        val body = JSONObject()
        body.put("model", model)
        body.put("max_tokens", 8192)
        if (thinkingEnabled) {
            body.put("thinking", JSONObject().put("type", "enabled").put("budget_tokens", 2048))
        }

        val msgArray = JSONArray()
        var systemPrompt = ""
        for (msg in messages) {
            if (msg.role == "system") {
                systemPrompt = msg.content
            } else {
                val m = JSONObject()
                m.put("role", msg.role)
                m.put("content", msg.content)
                msgArray.put(m)
            }
        }
        if (systemPrompt.isNotBlank()) body.put("system", systemPrompt)
        body.put("messages", msgArray)

        if (tools.isNotEmpty()) {
            val toolsArray = JSONArray()
            for (tool in tools) {
                val t = JSONObject()
                t.put("name", tool.name)
                t.put("description", tool.description)
                t.put("input_schema", tool.parameters)
                toolsArray.put(t)
            }
            body.put("tools", toolsArray)
            body.put("tool_choice", JSONObject().put("type", "auto"))
        }
        return body
    }

    private fun parseAnthropicResponse(
        channel: ChatChannel,
        jsonText: String,
        onChunk: (String?, String?) -> Unit,
    ): ChatResponse {
        val json = JSONObject(jsonText)
        val content = json.getJSONArray("content")
        var fullText = ""
        var fullThinking = ""
        var toolCall: OllamaService.ToolCall? = null

        for (i in 0 until content.length()) {
            val block = content.getJSONObject(i)
            val type = block.getString("type")
            when (type) {
                "text" -> {
                    val text = block.getString("text")
                    fullText += text
                    onChunk(text, null)
                }
                "thinking" -> {
                    val thinking = block.getString("thinking")
                    fullThinking += thinking
                    onChunk(null, thinking)
                }
                "tool_use" -> {
                    val argsJson = block.getJSONObject("input")
                    val args = mutableMapOf<String, String>()
                    val keys = argsJson.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        args[k] = argsJson.optString(k)
                    }
                    toolCall = ToolCall(
                        id = block.getString("id"),
                        name = block.getString("name"),
                        arguments = args
                    )
                }
            }
        }

        val usage = json.optJSONObject("usage")
        return OllamaService.ChatResponse(
            message = OllamaService.ChatMessage(
                role = "assistant",
                content = fullText,
                thinking = fullThinking.takeIf { it.isNotBlank() },
                toolCalls = if (toolCall != null) listOf(toolCall) else null
            ),
            done = true,
            usage = ChatUsage(
                promptTokens = usage?.optInt("input_tokens"),
                completionTokens = usage?.optInt("output_tokens"),
                totalTokens = (usage?.optInt("input_tokens") ?: 0) + (usage?.optInt("output_tokens") ?: 0),
                backend = channel.label
            )
        )
    }

    // ── OpenAI-compatible request/response ────────────────────────────────────

    private fun buildOpenAiBody(
        channel: ChatChannel,
        messages: List<ChatMessage>,
        tools: List<AgentTool>,
        thinkingEnabled: Boolean,
        numCtx: Int,
    ): JSONObject {
        val body = JSONObject()
        body.put("model", when (channel) {
            is ChatChannel.LocalOpenAi -> channel.model
            is ChatChannel.OpenAi -> channel.model
            is ChatChannel.OpenRouter -> channel.model
            else -> "local"
        })
        body.put("max_tokens", 8192)
        body.put("stream", false)

        if (thinkingEnabled && channel is ChatChannel.OpenAi) {
            // OpenAI reasoning effort (if supported by model)
            body.put("reasoning_effort", "medium")
        }

        val msgArray = JSONArray()
        for (msg in messages) {
            val m = JSONObject()
            m.put("role", msg.role)
            m.put("content", msg.content)
            msg.toolCallId?.let { m.put("tool_call_id", it) }
            msg.toolCalls?.takeIf { it.isNotEmpty() }?.let { calls ->
                m.put("tool_calls", JSONArray().apply {
                    for (tc in calls) {
                        put(JSONObject().apply {
                            put("id", tc.id ?: "call_${System.nanoTime()}")
                            put("type", "function")
                            put("function", JSONObject().apply {
                                put("name", tc.name)
                                put("arguments", JSONObject(tc.arguments))
                            })
                        })
                    }
                })
            }
            msgArray.put(m)
        }
        body.put("messages", msgArray)

        if (tools.isNotEmpty()) {
            val toolsArray = JSONArray()
            for (tool in tools) {
                val t = JSONObject()
                t.put("type", "function")
                val func = JSONObject()
                func.put("name", tool.name)
                func.put("description", tool.description)
                func.put("parameters", tool.parameters)
                t.put("function", func)
                toolsArray.put(t)
            }
            body.put("tools", toolsArray)
            body.put("tool_choice", "auto")
        }
        return body
    }

    private fun parseOpenAiStreamingResponse(
        channel: ChatChannel,
        jsonText: String,
        onChunk: (String?, String?) -> Unit,
    ): ChatResponse {
        val json = JSONObject(jsonText)
        val choice = json.getJSONArray("choices").getJSONObject(0)
        val message = choice.getJSONObject("message")
        val content = message.optString("content", "")
        val toolCallsJson = message.optJSONArray("tool_calls")
        val reasoning = message.optString("reasoning", "")
        val usageJson = json.optJSONObject("usage")

        if (content.isNotBlank()) onChunk(content, null)
        if (reasoning.isNotBlank()) onChunk(null, reasoning)

        var toolCall: OllamaService.ToolCall? = null
        if (toolCallsJson != null && toolCallsJson.length() > 0) {
            val tc = toolCallsJson.getJSONObject(0)
            val func = tc.getJSONObject("function")
            val argsJson = func.getJSONObject("arguments")
            val args = mutableMapOf<String, String>()
            val keys = argsJson.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                args[k] = argsJson.optString(k)
            }
            toolCall = ToolCall(
                id = tc.getString("id"),
                name = func.getString("name"),
                arguments = args
            )
        }

        return OllamaService.ChatResponse(
            message = OllamaService.ChatMessage(
                role = "assistant",
                content = content,
                thinking = reasoning.takeIf { it.isNotBlank() },
                toolCalls = if (toolCall != null) listOf(toolCall) else null
            ),
            done = true,
            usage = ChatUsage(
                promptTokens = usageJson?.optInt("prompt_tokens"),
                completionTokens = usageJson?.optInt("completion_tokens"),
                totalTokens = usageJson?.optInt("total_tokens"),
                backend = channel.label
            )
        )
    }
}
