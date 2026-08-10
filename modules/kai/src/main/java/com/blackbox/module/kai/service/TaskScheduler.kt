package com.blackbox.module.kai.service

import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class TaskScheduler {
    suspend fun schedule(task: com.blackbox.module.kai.data.ScheduledTask) {}
}

class TaskSchedulerWorker(context: android.content.Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): androidx.work.Result = androidx.work.Result.success()
}
