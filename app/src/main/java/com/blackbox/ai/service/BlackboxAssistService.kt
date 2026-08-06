package com.blackbox.ai.service

import android.app.assist.AssistContent
import android.app.assist.AssistStructure
import android.content.Intent
import android.os.Bundle
import android.service.assist.AssistService
import com.blackbox.ai.MainActivity
import com.blackbox.ai.ui.navigation.Screen

/**
 * Real system assist entry (Kai-style). Once the user picks Blackbox as the
 * default assistant app (Settings → Default apps → Assist & voice input),
 * the long-press home / power gesture routes here and opens the agent chat.
 */
class BlackboxAssistService : AssistService() {

    override fun onHandleAssist(
        bundle: Bundle?,
        structure: AssistStructure?,
        content: AssistContent?
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_ASSIST
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(MainActivity.EXTRA_OPEN_ROUTE, Screen.Agent.route)
        }
        runCatching { startActivity(intent) }
        onHandleAssistFinished()
    }
}
