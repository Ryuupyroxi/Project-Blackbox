package com.blackbox.module.anyclaw.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.blackbox.core.module.ModuleBus

class OpenClawService : Service() {
    override fun onCreate() {
        super.onCreate()
        ModuleBus.publish(ModuleEvent("anyclaw", "openclaw_service_created"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ModuleBus.publish(ModuleEvent("anyclaw", "openclaw_service_started"))
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null
}
