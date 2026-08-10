package com.blackbox.module.adt.runtime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.blackbox.core.module.adt.service.AdtServiceCatalog

class AdtBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        AdtServiceCatalog.services.forEach { def ->
            if (def.foregroundType != null) {
                val i = Intent().setClassName(context.packageName, def.className)
                context.startForegroundService(i)
            }
        }
    }
}
