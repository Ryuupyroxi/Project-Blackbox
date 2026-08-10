package com.blackbox.assistant

import android.content.Context
import android.content.Intent
import com.blackbox.core.module.ModuleBus
import com.blackbox.core.data.BlackboxPreferences
import com.blackbox.core.data.SecretStore

object AssistantIntentRouter {
    fun register(context: Context) {
        // Hotword/assistant session initialization hooks go here.
    }

    fun route(context: Context, extractedText: String) {
        val prefs = BlackboxPreferences(context)
        val secretStore = SecretStore(context)
        ModuleBus.publish(
            ModuleEvent(
                moduleId = "assistant",
                type = "assistant_invoke",
                payload = mapOf("text" to extractedText)
            )
        )
    }
}
