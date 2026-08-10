package com.blackbox.runtime

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.blackbox.bridge.BridgeServer
import java.io.File

class BlackboxRuntimeService : Service() {
    private lateinit var bridgeServer: BridgeServer

    override fun onCreate() {
        super.onCreate()
        bridgeServer = BridgeServer(File(filesDir, "bridge"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, BlackboxApp.CHANNEL_ID)
            .setContentTitle("Blackbox")
            .setContentText("Runtime running")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .build()
        startForeground(1, notification)
        bridgeServer.start()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        bridgeServer.stop()
        super.onDestroy()
    }
}
