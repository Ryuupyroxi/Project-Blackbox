package com.blackbox.assistant

import android.app.assist.AssistStructure
import android.service.voice.VoiceInteractionService
import android.content.Context
import com.blackbox.core.module.ModuleBus
import com.blackbox.core.data.BlackboxPreferences
import com.blackbox.core.data.SecretStore

class BlackboxAssistantService : VoiceInteractionService() {
    override fun onAssistStructure(structure: AssistStructure?) {
        super.onAssistStructure(structure)
        val text = AssistTextExtractor.extract(structure)
        if (!text.isNullOrBlank()) {
            AssistantIntentRouter.route(this, text)
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Assistant session initialized; routing handled in onAssistStructure.
    }
}
