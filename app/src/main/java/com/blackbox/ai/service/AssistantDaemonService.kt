package com.blackbox.ai.service

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.blackbox.ai.MainActivity

/**
 * Kai-style assistant daemon. A low-importance foreground service that keeps the
 * app process alive so the assistant stays responsive, with a lightweight
 * heartbeat loop. The system assist gesture (long-press home / power) opens the
 * agent chat via ACTION_ASSIST once Blackbox is set as the default assistant.
 */
class AssistantDaemonService : Service() {

    companion object {
        private const val CHANNEL_ID = "blackbox_assistant"
        private const val NOTIFICATION_ID = 9001
        private const val ACTION_STOP = "blackbox.assistant.STOP"
        private const val HEARTBEAT_INTERVAL_MS = 15 * 60 * 1000L

        /**
         * Derived from the real system service list so the UI never lies about
         * the daemon being alive (survives process restarts and kills).
         */
        fun isRunning(context: Context): Boolean {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                ?: return false
            return runCatching {
                am.getRunningServices(Int.MAX_VALUE)
                    .any { it.service.className == AssistantDaemonService::class.java.name }
            }.getOrDefault(false)
        }

        fun start(context: Context) {
            val intent = Intent(context, AssistantDaemonService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AssistantDaemonService::class.java))
        }
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var heartbeatStarted = false

    private val heartbeat = object : Runnable {
        override fun run() {
            // Keep the process alive and mark activity; no user-facing work yet.
            // Phase 2: drive scheduled tasks / feature dispatch from here.
            mainHandler.postDelayed(this, HEARTBEAT_INTERVAL_MS)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        if (!heartbeatStarted) {
            heartbeatStarted = true
            mainHandler.postDelayed(heartbeat, HEARTBEAT_INTERVAL_MS)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            mainHandler.removeCallbacks(heartbeat)
            heartbeatStarted = false
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(heartbeat)
        heartbeatStarted = false
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Blackbox Assistant",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the Blackbox assistant daemon alive"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("Blackbox Assistant")
            .setContentText("Assistant daemon running")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
