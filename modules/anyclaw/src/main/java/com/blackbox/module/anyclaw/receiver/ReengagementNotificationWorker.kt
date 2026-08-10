package com.blackbox.module.anyclaw.receiver

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReengagementNotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        // Re-engagement notification stub. Replace with actual logic.
        return Result.success()
    }
}
