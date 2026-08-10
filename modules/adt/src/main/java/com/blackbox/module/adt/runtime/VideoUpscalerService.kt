package com.blackbox.module.adt.runtime

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class VideoUpscalerService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var nativeLoader: AdtNativeLoader

    private val running = AtomicBoolean(false)
    private val currentJobId = AtomicInteger(0)
    private var upscaleProcess: Process? = null
    private var inputFile: File? = null
    private var outputFile: File? = null

    private val stateFlow = kotlinx.coroutines.flow.MutableStateFlow("idle")

    override fun onCreate() {
        super.onCreate()
        nativeLoader = AdtNativeLoader(this)
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val input = intent?.getStringExtra("input") ?: return START_NOT_STICKY
        val output = intent?.getStringExtra("output") ?: "$filesDir/upscaled/${System.currentTimeMillis()}.mp4"
        val model = intent?.getStringExtra("model") ?: "realesrgan"
        serviceScope.launch { upscale(input, output, model) }
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        stopUpscaleProcess()
    }

    private suspend fun upscale(input: String, output: String, model: String) {
        val jobId = currentJobId.incrementAndGet()
        stateFlow.value = "running"
        running.set(true)
        inputFile = File(input)
        outputFile = File(output)
        outputFile?.parentFile?.mkdirs()

        if (!nativeLoader.isReady()) {
            nativeLoader.extractFromApk()
        }
        val libDir = nativeLoader.extractedDir
        val upscalerLib = File(libDir, "libupscaler.so")
        if (!upscalerLib.exists()) {
            stateFlow.value = "error: upscaler lib missing"
            return
        }
        try {
            val pb = ProcessBuilder(
                upscalerLib.absolutePath,
                "-i", input,
                "-o", output,
                "-m", model,
                "-s", "2"
            )
                .directory(libDir)
                .redirectErrorStream(true)
            upscaleProcess = pb.start()
            upscaleProcess?.inputStream?.bufferedReader()?.useLines { lines ->
                lines.forEach { line ->
                    stateFlow.value = line.trim()
                }
            }
            val exit = upscaleProcess?.waitFor()
            stateFlow.value = if (exit == 0) "completed" else "error: exit $exit"
        } catch (e: Exception) {
            stateFlow.value = "error: ${e.message}"
        } finally {
            running.set(false)
            upscaleProcess = null
        }
    }

    fun stop() {
        running.set(false)
        stopUpscaleProcess()
        stateFlow.value = "stopped"
    }

    private fun stopUpscaleProcess() {
        runCatching { upscaleProcess?.destroy() }
        upscaleProcess = null
    }
}
