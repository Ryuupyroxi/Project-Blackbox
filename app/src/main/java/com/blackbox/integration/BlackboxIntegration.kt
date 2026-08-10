package com.blackbox.integration

import android.content.Context
import com.blackbox.module.anyclaw.bridge.DeviceBridge
import com.blackbox.module.anyclaw.permission.PermissionGate
import com.blackbox.core.module.kai.KaiServiceCatalog
import com.blackbox.core.module.kai.KaiProviderSelector
import com.blackbox.core.module.kai.model.Service

object BlackboxIntegration {
    fun serviceCatalog(): List<Service> = KaiServiceCatalog.entries
    fun selectProvider(serviceId: String, selectedModelId: String?) = KaiProviderSelector.select(serviceId, selectedModelId)
    fun deviceBridge(context: Context) = DeviceBridge(context)
    fun missingPermissions(context: Context, feature: String) = PermissionGate.missing(context, feature)
}
