package com.blackbox.module.adt.runtime

import android.content.Context
import android.os.Process
import java.io.File

class AdtNativeLoader(private val context: Context) {
    companion object {
        const val NATIVE_DIR = "adt_native"
        const val READY_MARKER = ".adt_native_ready"
    }

    val extractedDir: File = File(context.filesDir, NATIVE_DIR).apply { mkdirs() }
    private val readyMarker = File(extractedDir, READY_MARKER)

    fun isReady(): Boolean = readyMarker.exists() && extractedDir.listFiles()?.isNotEmpty() == true

    fun markReady() {
        readyMarker.writeText(System.currentTimeMillis().toString())
    }

    fun listExpectedLibraries(): List<String> = listOf(
        "libllama.so",
        "libwhisper.so",
        "libkiwix.so",
        "libstable_diffusion.so",
        "liblitert.so",
        "libtensorflowlite.so",
        "libopencv.so"
    )

    fun missingLibraries(): List<String> = listExpectedLibraries().filter { File(extractedDir, it).exists().not() }

    fun extractFromApk() {
        val apk = context.packageResourcePath ?: return
        val abi = when {
            Process.SUPPORTS_64_BIT -> "arm64-v8a"
            else -> "armeabi-v7a"
        }
        runCatching {
            java.util.zip.ZipFile(apk).use { zip ->
                listExpectedLibraries().forEach { lib ->
                    val entry = zip.getEntry("lib/$abi/$lib") ?: zip.getEntry("lib/armeabi/$lib") ?: return@forEach
                    File(extractedDir, lib).outputStream().use { out ->
                        zip.getInputStream(entry).use { input -> input.copyTo(out) }
                    }
                    File(extractedDir, lib).setExecutable(true)
                }
            }
            markReady()
        }
    }
}
