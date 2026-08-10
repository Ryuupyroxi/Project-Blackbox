package com.blackbox.core.module

import android.content.Context
import java.io.File

class ModuleManager(private val context: Context) {
    private val moduleDir = File(context.filesDir, "modules").apply { mkdirs() }

    fun install(moduleZip: File, manifest: ModuleManifest): Boolean {
        if (!ModuleVerifier.verify(manifest, moduleZip.readBytes())) return false
        val target = File(moduleDir, "${manifest.id}-${manifest.version}.zip")
        moduleZip.copyTo(target, overwrite = true)
        return true
    }

    fun listInstalled(): List<ModuleManifest> {
        // TODO: parse module.json from installed zips for version/id discovery
        return emptyList()
    }
}
