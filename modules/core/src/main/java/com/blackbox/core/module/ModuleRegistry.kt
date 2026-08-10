package com.blackbox.core.module

import android.content.Context
import java.io.File

class ModuleRegistry(private val context: Context) {
    private val loaded = mutableMapOf<String, BlackboxModule>()

    fun register(module: BlackboxModule) {
        loaded[module.id()] = module
    }

    fun unregister(id: String) {
        loaded.remove(id)?.onUnload()
    }

    fun get(id: String): BlackboxModule? = loaded[id]
    fun list(): List<BlackboxModule> = loaded.values.toList()
}
