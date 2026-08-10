package com.blackbox.module.anyclaw.runtime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BlackboxBootController : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Boot completed: restart gateway/Codex/OpenClaw if enabled in prefs.
        }
    }
}
