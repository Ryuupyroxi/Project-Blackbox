package com.blackbox.module.adt.runtime

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.LinkedHashMap

class AiToolServerService : Service() {
    companion object {
        const val TOOL_ONNX = "onnx"
        const val TOOL_IMG2IMG = "img2img"
        const val TOOL_UPSCALE = "upscale"
        const val TOOL_TXT2IMG = "txt2img"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val binder = Binder()

    private val _tools = MutableStateFlow<Map<String, ToolDescriptor>>(emptyMap())
    val tools: StateFlow<Map<String, ToolDescriptor>> = _tools

    private val _requests = MutableStateFlow<Map<String, ToolRequest>>(emptyMap())
    val requests: StateFlow<Map<String, ToolRequest>> = _requests

    private val toolRegistry = LinkedHashMap<String, ToolDescriptor>()

    inner class Binder : android.os.Binder() {
        fun listTools(): List<ToolDescriptor> = toolRegistry.values.toList()
        fun registerTool(descriptor: ToolDescriptor) {
            toolRegistry[descriptor.id] = descriptor
            _tools.value = toolRegistry.toMap()
        }
        fun invokeTool(toolId: String, params: Map<String, String>): ToolRequest {
            val req = ToolRequest(
                id = "${toolId}_${System.currentTimeMillis()}",
                toolId = toolId,
                params = params,
                status = "queued"
            )
            serviceScope.launch { execute(req) }
            return req
        }
    }

    data class ToolDescriptor(
        val id: String,
        val name: String,
        val description: String,
        val parameters: Map<String, String>
    )

    data class ToolRequest(
        val id: String,
        val toolId: String,
        val params: Map<String, String>,
        val status: String,
        val result: File? = null,
        val error: String? = null
    )

    override fun onCreate() {
        super.onCreate()
        seedDefaultTools()
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun seedDefaultTools() {
        registerTool(
            ToolDescriptor(
                id = TOOL_TXT2IMG,
                name = "Text to Image",
                description = "Generate image from text prompt",
                parameters = mapOf("prompt" to "string", "negative" to "string", "steps" to "int")
            )
        )
        registerTool(
            ToolDescriptor(
                id = TOOL_IMG2IMG,
                name = "Image to Image",
                description = "Transform image with prompt",
                parameters = mapOf("input" to "file", "prompt" to "string", "steps" to "int")
            )
        )
        registerTool(
            ToolDescriptor(
                id = TOOL_UPSCALE,
                name = "Upscale",
                description = "Upscale image/video",
                parameters = mapOf("input" to "file", "scale" to "int")
            )
        )
        registerTool(
            ToolDescriptor(
                id = TOOL_ONNX,
                name = "ONNX Inference",
                description = "Run ONNX model",
                parameters = mapOf("model" to "string", "input" to "file")
            )
        )
    }

    private suspend fun execute(req: ToolRequest) {
        val updated = req.copy(status = "running")
        _requests.value = _requests.value + (req.id to updated)
        try {
            val outputFile = resolveOutput(req.toolId, req.params)
            // Simulate execution
            kotlinx.coroutines.delay(500)
            outputFile.parentFile?.mkdirs()
            outputFile.writeText("tool_output_${req.id}")
            val done = updated.copy(status = "completed", result = outputFile)
            _requests.value = _requests.value + (req.id to done)
        } catch (e: Exception) {
            val failed = updated.copy(status = "error", error = e.message)
            _requests.value = _requests.value + (req.id to failed)
        }
    }

    private fun resolveOutput(toolId: String, params: Map<String, String>): File {
        val base = when (toolId) {
            TOOL_ONNX -> File(filesDir, "onnx_image_output/${params["model"] ?: "default"}")
            TOOL_IMG2IMG -> File(filesDir, "sd_output/img2img")
            TOOL_UPSCALE -> File(filesDir, "sd_output/upscaled")
            else -> File(filesDir, "sd_output/txt2img")
        }
        val serverName = "server_${System.currentTimeMillis()}.png"
        return File(base, serverName)
    }
}
