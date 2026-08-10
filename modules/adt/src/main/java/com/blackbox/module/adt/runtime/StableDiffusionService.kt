package com.blackbox.module.adt.runtime

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.util.LinkedHashMap

class StableDiffusionService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var nativeLoader: AdtNativeLoader
    private val jobs = LinkedHashMap<String, JobDescriptor>()
    private val outputs = LinkedHashMap<String, File>()
    private val inputs = LinkedHashMap<String, File>()
    private val locks = LinkedHashMap<String, Any>()

    @Volatile
    private var wakeLock: PowerManager.WakeLock? = null

    private val binder = Binder()

    inner class Binder : android.os.Binder() {
        fun enqueueText2Image(prompt: String, negative: String, steps: Int, width: Int, height: Int): String {
            val id = "txt2img_${System.currentTimeMillis()}"
            val lock = Any()
            locks[id] = lock
            inputs[id] = File("$filesDir/sd_input/$id.txt").apply { parentFile?.mkdirs(); writeText("$prompt|$negative|$steps|${width}x$height") }
            serviceScope.launch { runSdJob(id, "txt2img", lock) }
            return id
        }

        fun enqueueImg2Img(inputImage: File, prompt: String, steps: Int): String {
            val id = "img2img_${System.currentTimeMillis()}"
            val lock = Any()
            locks[id] = lock
            inputs[id] = inputImage
            outputs[id] = File("$filesDir/sd_output/img2img/$id.png")
            serviceScope.launch { runSdJob(id, "img2img", lock) }
            return id
        }

        fun enqueueUpscale(inputImage: File): String {
            val id = "upscale_${System.currentTimeMillis()}"
            val lock = Any()
            locks[id] = lock
            inputs[id] = inputImage
            outputs[id] = File("$filesDir/sd_output/upscaled/$id.png")
            serviceScope.launch { runSdJob(id, "upscale", lock) }
            return id
        }

        fun getOutput(id: String): File? = outputs[id]
        fun cancel(id: String) {
            val lock = locks.remove(id)
            if (lock != null) {
                synchronized(lock) {
                    (lock as java.lang.Object).notifyAll()
                }
            }
            jobs.remove(id)
        }
    }

    data class JobDescriptor(val id: String, val type: String, val status: String)

    override fun onCreate() {
        super.onCreate()
        nativeLoader = AdtNativeLoader(this)
        val pm = getSystemService(PowerManager::class.java)
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Blackbox:StableDiffusion")
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        runCatching { wakeLock?.release() }
        jobs.values.forEach { runCatching { it } }
    }

    private suspend fun runSdJob(id: String, type: String, lock: Any) {
        jobs[id] = JobDescriptor(id, type, "running")
        acquireWakeLock()
        try {
            if (!nativeLoader.isReady()) {
                nativeLoader.extractFromApk()
            }
            val libDir = nativeLoader.extractedDir
            val sdLib = File(libDir, "libstable_diffusion.so")
            val onnxDir = File(filesDir, "onnx")
            if (!sdLib.exists() || !onnxDir.exists()) {
                jobs[id] = JobDescriptor(id, type, "error: missing model")
                return
            }
            val output = outputs[id] ?: File("$filesDir/sd_output/txt2img/$id.png")
            output.parentFile?.mkdirs()
            // In a real port, we'd invoke Stable Diffusion C API via JNA/JNI here.
            // For now, we simulate completion.
            val input = inputs[id]
            synchronized(lock) {
                runCatching {
                    if (input != null && input.exists()) {
                        output.writeBytes(input.readBytes())
                    } else {
                        output.writeText("SD placeholder $id")
                    }
                }
            }
            jobs[id] = JobDescriptor(id, type, "completed")
        } finally {
            releaseWakeLock()
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        runCatching { wakeLock?.acquire(10 * 60 * 1000L) }
    }

    private fun releaseWakeLock() {
        runCatching { if (wakeLock?.isHeld == true) wakeLock?.release() }
    }
}
