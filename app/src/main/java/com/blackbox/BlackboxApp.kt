package com.blackbox

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.blackbox.module.adt.AdtModuleImpl

class BlackboxApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PermissionCoordinator.refresh(this)
        createNotificationChannel()
        registerBlackboxModules()
    }

    private fun registerBlackboxModules() {
        ModuleRegistry(this).apply {
            register(com.blackbox.module.kai.KaiModuleImpl())
            register(com.blackbox.module.anyclaw.AnyClawModuleImpl())
            register(AdtModuleImpl())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Blackbox Runtime",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Blackbox core runtime service"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "blackbox-runtime"
    }
}
