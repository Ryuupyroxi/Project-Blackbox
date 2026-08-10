package com.blackbox.module.anyclaw.bridge

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class DiscordBridge(
    private val context: Context,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    suspend fun sendMessage(botToken: String, channelId: String, content: String): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val body = okhttp3.RequestBody.create(null, """{"content":"$content"}""".toByteArray())
                val request = Request.Builder()
                    .url("https://discord.com/api/v10/channels/$channelId/messages")
                    .addHeader("Authorization", "Bot $botToken")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()
                httpClient.newCall(request).execute().use { response ->
                    response.isSuccessful
                }
            }.getOrDefault(false)
        }

    suspend fun readMessages(botToken: String, channelId: String, limit: Int = 50): List<Map<String, Any>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder()
                    .url("https://discord.com/api/v10/channels/$channelId/messages?limit=$limit")
                    .addHeader("Authorization", "Bot $botToken")
                    .build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext emptyList()
                    val text = response.body?.string() ?: "[]"
                    val arr = org.json.JSONArray(text)
                    buildList {
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            val map = mutableMapOf<String, Any>()
                            map["id"] = obj.optString("id")
                            map["content"] = obj.optString("content")
                            map["author"] = obj.optJSONObject("author")?.optString("username") ?: ""
                            map["timestamp"] = obj.optString("timestamp")
                            add(map)
                        }
                    }
                }
            }.getOrDefault(emptyList())
        }
}
