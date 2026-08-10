package com.blackbox.module.adt

import android.content.Context
import com.blackbox.core.module.BlackboxModule
import com.blackbox.core.module.adt.model.AdtServiceDefinition
import com.blackbox.module.adt.runtime.AdtModuleLoader
import com.blackbox.module.adt.runtime.AdtNativeLoader
import dalvik.system.DexClassLoader

class AdtModuleImpl : BlackboxModule {
    private lateinit var classLoader: DexClassLoader
    private lateinit var context: Context
    private var nativeLoader: AdtNativeLoader? = null
    private var moduleLoader: AdtModuleLoader? = null

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

    fun serviceCatalog(): List<AdtServiceDefinition> = moduleLoader?.loadServices() ?: emptyList()
}
