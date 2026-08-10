package com.blackbox.module.adt.runtime

import android.content.Context
import android.content.Intent
import com.blackbox.core.module.contract.AdtReceiverDefinition
import com.blackbox.core.module.contract.AdtServiceDefinition
import com.blackbox.module.adt.bridge.AdtManifestMapper

class AdtModuleLoader(private val context: Context) {
    fun loadServices(): List<AdtServiceDefinition> = AdtManifestMapper.services()
    fun loadBootReceivers(): List<com.blackbox.core.module.contract.AdtReceiverDefinition> = AdtManifestMapper.bootReceivers()

    fun startService(service: AdtServiceDefinition) {
        val intent = Intent().setClassName(context.packageName, service.className)
        val fg = service.foregroundType
        if (fg != null) {
            intent.putExtra("foreground_type", fg.name)
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
