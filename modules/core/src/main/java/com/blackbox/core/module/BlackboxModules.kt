package com.blackbox.core.module

import android.content.Context
import com.blackbox.core.module.adt.AdtModuleImpl
import com.blackbox.core.module.adt.bridge.AdtManifestMapper
import com.blackbox.core.module.adt.model.AdtReceiverDefinition
import com.blackbox.core.module.adt.model.AdtServiceDefinition

class AdtModuleImpl : BlackboxModule {
    override fun id(): String = "adt"
    override fun version(): String = "0.948"
    override fun description(): String = "AI-Doomsday-Toolbox runtime port"

    private var services: List<AdtServiceDefinition> = emptyList()
    private var receivers: List<AdtReceiverDefinition> = emptyList()

    override fun onLoad(context: Context, classLoader: ClassLoader) {
        services = AdtManifestMapper.services()
        receivers = AdtManifestMapper.bootReceivers()
    }

    fun registerRuntime(serviceDefs: List<AdtServiceDefinition>, receiverDefs: List<AdtReceiverDefinition>) {
        services = serviceDefs
        receivers = receiverDefs
    }
}
