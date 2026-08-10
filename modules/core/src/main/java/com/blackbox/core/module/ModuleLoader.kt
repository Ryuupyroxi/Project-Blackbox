package com.blackbox.core.module

import android.content.Context
import com.blackbox.core.module.contract.AdtModule
import dalvik.system.DexClassLoader
import java.io.File
import java.util.zip.ZipInputStream

class ModuleLoader(private val context: Context) {
    private val moduleDir = File(context.filesDir, "modules").apply { mkdirs() }

    fun load(moduleZip: File, manifest: ModuleManifest): BlackboxModule? {
        val extracted = File(moduleDir, manifest.id).apply { mkdirs() }
        moduleZip.unzipTo(extracted)
        val dexFile = File(extracted, "module.dex")
        val classLoader = DexClassLoader(
            dexFile.absolutePath,
            extracted.absolutePath,
            null,
            context.classLoader
        )
        val moduleClass = classLoader.loadClass("com.blackbox.module.impl.BlackboxModuleImpl")
        val instance = moduleClass.getDeclaredConstructor().newInstance() as BlackboxModule
        instance.onLoad(context, classLoader)

        if (manifest.id == "adt") {
            (instance as? AdtModule)?.registerRuntime()
        }

        return instance
    }

    private fun File.unzipTo(target: File) {
        ZipInputStream(inputStream().buffered()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val outFile = File(target, entry.name)
                if (entry.isDirectory) outFile.mkdirs() else {
                    outFile.parentFile?.mkdirs()
                    outFile.writeBytes(zip.readBytes())
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    }
}
