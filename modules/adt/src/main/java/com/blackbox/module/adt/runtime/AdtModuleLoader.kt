package com.blackbox.module.adt.runtime

import android.content.Context
import android.content.Intent
import com.blackbox.core.module.adt.bridge.AdtManifestMapper
import com.blackbox.core.module.adt.model.AdtServiceDefinition

class AdtModuleLoader(private val context: Context) {
    fun loadServices(): List<AdtServiceDefinition> = AdtManifestMapper.services()
    fun loadBootReceivers(): List<com.blackbox.core.module.adt.model.AdtReceiverDefinition> = AdtManifestMapper.bootReceivers()

    fun startService(service: AdtServiceDefinition) {
        val intent = Intent().setClassName(context.packageName, service.className)
        if (service.foregroundType != null) {
            intent.putExtra("foreground_type", service.foregroundType.name)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopService(service: AdtServiceDefinition) {
        val intent = Intent().setClassName(context.packageName, service.className)
        context.stopService(intent)
    }

    fun startAllServices() {
        loadServices().forEach { startService(it) }
    }

    fun stopAllServices() {
        loadServices().forEach { stopService(it) }
    }
}
