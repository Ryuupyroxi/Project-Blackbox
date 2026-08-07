package com.blackbox.ai.service

import android.app.assist.AssistContent
import android.app.assist.AssistStructure
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.voice.VoiceInteractionService
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import android.util.Log
import com.blackbox.ai.MainActivity
import com.blackbox.ai.ui.navigation.Screen

/**
 * Real system assist entry (Kai-style). Once the user picks Blackbox as the
 * default assistant app (Settings → Apps → Default apps → Digital assistant),
 * the long-press home / power gesture routes here and opens the agent chat.
 *
 * The legacy `android.service.assist.AssistService` class does not exist in the
 * Android SDK; the actual assist-gesture mechanism is the VoiceInteraction
 * family (VoiceInteractionService + VoiceInteractionSessionService +
 * VoiceInteractionSession), so this is implemented against those APIs.
 */
class BlackboxAssistService : VoiceInteractionService() {

    override fun onReady() {
        super.onReady()
        Log.d("BlackboxAssist", "Registered as default digital assistant")
    }
}

/** Bridges the system to the session that handles each assist invocation. */
class BlackboxAssistSessionService : VoiceInteractionSessionService() {

    override fun onNewSession(sessionArgs: Bundle?): VoiceInteractionSession {
        return BlackboxAssistSession(this)
    }
}

/** Opens the agent chat on long-press home / assist gesture. */
class BlackboxAssistSession(context: Context) : VoiceInteractionSession(context) {

    override fun onHandleAssist(
        bundle: Bundle?,
        structure: AssistStructure?,
        content: AssistContent?
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_ASSIST
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(MainActivity.EXTRA_OPEN_ROUTE, Screen.Agent.route)
        }
        runCatching { context.startActivity(intent) }
        finish()
    }
}
