package com.blackbox.module.anyclaw.bridge

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class WhatsAppBridge(
    private val context: Context,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    suspend fun sendMessage(phoneNumber: String, message: String): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "https://api.whatsapp.com/v1/messages"
                val payload = """{"to":"$phoneNumber","message":"$message"}"""
                val body = okhttp3.RequestBody.create(null, payload.toByteArray())
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()
                httpClient.newCall(request).execute().use { response ->
                    response.isSuccessful
                }
            }.getOrDefault(false)
        }

    suspend fun getStatus(phoneNumber: String): Map<String, Any> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "https://api.whatsapp.com/v1/status?phone=$phoneNumber"
                val request = Request.Builder().url(url).build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext mapOf("error" to "http_${response.code}")
                    val text = response.body?.string() ?: "{}"
                    val obj = org.json.JSONObject(text)
                    val map = mutableMapOf<String, Any>()
                    obj.keys().forEach { map[it] = obj.get(it) }
                    map
                }
            }.getOrDefault(mapOf("error" to "exception"))
        }
}
