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
import android.os.IBinder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.blackbox.ai.MainActivity
import com.blackbox.ai.agent.workspace.WorkspaceAgentSession
import com.blackbox.ai.agent.workspace.WorkspaceStore
import com.blackbox.ai.engine.AgentEngineAdapter
import com.blackbox.ai.engine.EngineKeysStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import android.content.Context


@OptIn(ExperimentalTime::class)
class AssistantDaemonService : Service(), LifecycleEventObserver {

    companion object {
        private const val CHANNEL_ID = "blackbox_assistant"
        private const val NOTIFICATION_ID = 9001
        private const val ACTION_STOP = "blackbox.assistant.STOP"
        private const val POLL_INTERVAL_MS = 60_000L
        private const val MAX_BACKOFF_MS = 3_600_000L // 1 hour
        private const val HEARTBEAT_PREVIEW_CHARS = 240
        private const val PREFS_NAME = "blackbox_assistant_prefs"
        private const val KEY_LAST_HEARTBEAT = "last_heartbeat_at"

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

    // Long-lived scheduler scope — decoupled from callers so scheduled tasks
    // and heartbeats keep firing as long as the OS keeps the process alive.
    private val schedulerScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + kotlinx.coroutines.CoroutineName("AssistantScheduler")
    )

    private var schedulerJob: Job? = null

    // Injected dependencies (set via BlackboxApplication or lazy init)
    private var workspaceSession: WorkspaceAgentSession? = null
    private var engineAdapter: AgentEngineAdapter? = null
    private var featureAccess: com.blackbox.ai.agent.workspace.FeatureAccessStore? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = buildNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (_: Exception) {
            stopSelf()
            return
        }

        // Initialize dependencies lazily
        val app = applicationContext as? com.blackbox.ai.BlackboxApplication
        if (app != null) {
            workspaceSession = app.getWorkspaceSession()
            engineAdapter = app.getEngineAdapter()
            featureAccess = app.getFeatureAccessStore()
        }

        // Start the scheduler loop
        startScheduler()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopScheduler()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopScheduler()
        schedulerScope.coroutineContext[Job]?.cancel()
        super.onDestroy()
    }

    private fun startScheduler() {
        if (schedulerJob?.isActive == true) return
        schedulerJob = schedulerScope.launch {
            while (isActive) {
                delay(POLL_INTERVAL_MS.milliseconds)
                runSchedulerTick()
            }
        }
    }

    private fun stopScheduler() {
        schedulerJob?.cancel()
        schedulerJob = null
    }

    /**
     * Main scheduler tick — runs periodic work (heartbeat, scheduled tasks, etc.)
     * Mirrors Kai's TaskScheduler loop.
     */
    private suspend fun runSchedulerTick() {
        // Real daemon work: keep ADT's scheduled tasks fresh and record a heartbeat.
        // ADT owns the actual task store (LlamaScheduledTaskScheduler); the daemon
        // re-asserts alarms so a killed process doesn't leave tasks stuck.
        runCatching {
            LlamaScheduledTaskScheduler.rescheduleAll(this)
        }.onFailure { e ->
            android.util.Log.w("AssistantDaemon", "rescheduleAll failed: ${e.message}")
        }

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_HEARTBEAT, System.currentTimeMillis()).apply()
    }

    // LifecycleEventObserver — track app foreground/background for heartbeat escalation
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        // Could track appInForeground here for heartbeat notification logic
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
