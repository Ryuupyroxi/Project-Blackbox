package com.blackbox.module.anyclaw.receiver

import android.app.PendingIntent
import android.content.Context
import android.content.Intent

class ReminderScheduler(private val context: Context) {
    fun schedule(reminderId: String, triggerAt: Long) {
        // Schedule reminder via AlarmManager/WorkManager.
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Fire reminder notification or activity.
    }
}
