package com.blackbox.module.adt

import android.content.Context
import com.blackbox.core.module.BlackboxModule
import com.blackbox.core.module.contract.AdtModule
import com.blackbox.core.module.contract.AdtReceiverDefinition
import com.blackbox.core.module.contract.AdtServiceDefinition
import com.blackbox.module.adt.bridge.AdtManifestMapper
import com.blackbox.module.adt.runtime.AdtModuleLoader
import com.blackbox.module.adt.runtime.AdtNativeLoader
import dalvik.system.DexClassLoader

class AdtModuleImpl : BlackboxModule, AdtModule {
    private lateinit var classLoader: DexClassLoader
    private lateinit var context: Context
    private var nativeLoader: AdtNativeLoader? = null
    private var moduleLoader: AdtModuleLoader? = null
    private var services: List<AdtServiceDefinition> = emptyList()
    private var receivers: List<AdtReceiverDefinition> = emptyList()

    override fun id() = "adt"
    override fun version() = "0.948"
    override fun description() = "AI-Doomsday-Toolbox runtime port"

    override fun onLoad(context: Context, classLoader: DexClassLoader) {
        this.context = context
        this.classLoader = classLoader
        this.nativeLoader = AdtNativeLoader(context)
        if (!nativeLoader!!.isReady()) {
            nativeLoader!!.extractFromApk()
        }
        this.moduleLoader = AdtModuleLoader(context).also { it.startAllServices() }
    }

    override fun onUnload() {
        moduleLoader?.stopAllServices()
        nativeLoader = null
        moduleLoader = null
    }

    override fun registerRuntime() {
        services = AdtManifestMapper.services()
        receivers = AdtManifestMapper.bootReceivers()
    }

    fun serviceCatalog(): List<AdtServiceDefinition> = moduleLoader?.loadServices() ?: emptyList()
}
