package com.blackbox.module.anyclaw

import com.blackbox.core.module.BlackboxModule
import android.content.Context
import dalvik.system.DexClassLoader

class AnyClawModuleImpl : BlackboxModule {
    private lateinit var classLoader: DexClassLoader
    private lateinit var context: Context

    override fun id() = "anyclaw"
    override fun version() = "2.1.565"
    override fun description() = "AnyClaw bridge, proot, and runtime port"

    override fun onLoad(context: Context, classLoader: DexClassLoader) {
        this.context = context
        this.classLoader = classLoader
    }

    override fun onUnload() {
        // stop bridge, close proot sessions, shutdown gateway if running
    }
}
