package com.blackbox.module.anyclaw.bridge

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class TelegramBridge(
    private val context: Context,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    suspend fun sendMessage(botToken: String, chatId: String, text: String): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "https://api.telegram.org/bot$botToken/sendMessage"
                val body = okhttp3.RequestBody.create(null, """{"chat_id":"$chatId","text":"$text"}""".toByteArray())
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()
                httpClient.newCall(request).execute().use { response ->
                    val resp = response.body?.string() ?: ""
                    response.isSuccessful && resp.contains("\"ok\":true")
                }
            }.getOrDefault(false)
        }

    suspend fun getUpdates(botToken: String, offset: Long = 0, limit: Int = 100): List<Map<String, Any>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "https://api.telegram.org/bot$botToken/getUpdates?offset=$offset&limit=$limit"
                val request = Request.Builder().url(url).build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext emptyList()
                    val text = response.body?.string() ?: "{}"
                    val result = org.json.JSONObject(text).optJSONArray("result") ?: return@withContext emptyList()
                    buildList {
                        for (i in 0 until result.length()) {
                            val update = result.getJSONObject(i)
                            val msg = update.optJSONObject("message")
                                ?: update.optJSONObject("channel_post")
                                ?: continue
                            val map = mutableMapOf<String, Any>()
                            map["update_id"] = update.optLong("update_id")
                            map["chat_id"] = msg.optJSONObject("chat")?.optString("id") ?: ""
                            map["text"] = msg.optString("text")
                            map["date"] = msg.optLong("date")
                            add(map)
                        }
                    }
                }
            }.getOrDefault(emptyList())
        }
}
