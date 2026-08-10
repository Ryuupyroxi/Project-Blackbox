package com.blackbox.module.adt.runtime

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LiteRtLmWorkerService : Service() {
    companion object {
        const val MSG_RUN_INFERENCE = 1
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var nativeLoader: AdtNativeLoader

    private val messenger = Messenger(Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            MSG_RUN_INFERENCE -> {
                val replyTo = msg.replyTo ?: return@Handler true
                val data = msg.data
                val requestId = data.getString("request_id") ?: ""
                val requestJson = data.getString("request_json") ?: ""
                if (requestId.isNotBlank() && requestJson.isNotBlank()) {
                    serviceScope.launch {
                        handleInference(requestId, requestJson, replyTo)
                    }
                } else {
                    sendError(replyTo, requestId, "missing request_id or request_json")
                }
                true
            }
            else -> false
        }
    })

    override fun onCreate() {
        super.onCreate()
        nativeLoader = AdtNativeLoader(this)
    }

    override fun onBind(intent: Intent): IBinder = messenger.binder

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private suspend fun handleInference(requestId: String, requestJson: String, replyTo: Messenger) {
        try {
            if (!nativeLoader.isReady()) {
                nativeLoader.extractFromApk()
            }
            val libDir = nativeLoader.extractedDir
            val liteRtLib = File(libDir, "liblitert.so")
            if (!liteRtLib.exists()) {
                sendError(replyTo, requestId, "liblitert.so missing")
                return
            }
            // In a real port, we'd invoke LiteRT C API via JNA/JNI here.
            // For now we simulate inference completion with the parsed input.
            val result = runCatching {
                "LiteRT processed request=$requestId with payload=$requestJson"
            }.getOrElse { "LiteRT inference error: ${it.message}" }
            sendResult(replyTo, requestId, result)
        } catch (e: Exception) {
            sendError(replyTo, requestId, e.message ?: "unknown error")
        }
    }

    private fun sendResult(replyTo: Messenger, requestId: String, result: String) {
        val data = Message.obtain(null, MSG_RUN_INFERENCE).apply {
            data = android.os.Bundle().apply {
                putString("request_id", requestId)
                putString("status", "ok")
                putString("result", result)
            }
            replyTo.send(this)
        }
    }

    private fun sendError(replyTo: Messenger, requestId: String, error: String) {
        val data = Message.obtain(null, MSG_RUN_INFERENCE).apply {
            data = android.os.Bundle().apply {
                putString("request_id", requestId)
                putString("status", "error")
                putString("error", error)
            }
            replyTo.send(this)
        }
    }
}
