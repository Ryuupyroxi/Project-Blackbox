package com.blackbox.module.adt.service

import com.blackbox.core.module.adt.model.AdtForegroundType
import com.blackbox.core.module.adt.model.AdtReceiverDefinition
import com.blackbox.core.module.adt.model.AdtServiceDefinition

object AdtServiceCatalog {
    val services: List<AdtServiceDefinition> = listOf(
        AdtServiceDefinition(
            id = "llama_service",
            className = "com.blackbox.module.adt.runtime.LlamaService",
            foregroundType = AdtForegroundType.DATA_SYNC,
            exported = false,
            description = "Llama inference server with LiteRT binding"
        ),
        AdtServiceDefinition(
            id = "litert_worker",
            className = "com.blackbox.module.adt.runtime.LiteRtLmWorkerService",
            foregroundType = AdtForegroundType.DATA_SYNC,
            exported = false,
            description = "LiteRT local model worker (separate process)"
        ),
        AdtServiceDefinition(
            id = "whisper_service",
            className = "com.blackbox.module.adt.runtime.WhisperService",
            foregroundType = AdtForegroundType.DATA_SYNC,
            exported = false,
            description = "Whisper transcription worker"
        ),
        AdtServiceDefinition(
            id = "model_download_service",
            className = "com.blackbox.module.adt.runtime.ModelDownloadService",
            foregroundType = AdtForegroundType.DATA_SYNC,
            exported = false,
            description = "Model download worker with progress"
        ),
        AdtServiceDefinition(
            id = "stable_diffusion_service",
            className = "com.blackbox.module.adt.runtime.StableDiffusionService",
            foregroundType = null,
            exported = false,
            description = "Stable Diffusion generation service"
        ),
        AdtServiceDefinition(
            id = "video_upscaler_service",
            className = "com.blackbox.module.adt.runtime.VideoUpscalerService",
            foregroundType = AdtForegroundType.DATA_SYNC,
            exported = false,
            description = "Video upscaler service"
        ),
        AdtServiceDefinition(
            id = "zim_share_service",
            className = "com.blackbox.module.adt.runtime.ZimShareService",
            foregroundType = AdtForegroundType.DATA_SYNC,
            exported = false,
            description = "ZIM file sharing service"
        ),
        AdtServiceDefinition(
            id = "agent_foreground_service",
            className = "com.blackbox.module.adt.runtime.AgentForegroundService",
            foregroundType = AdtForegroundType.DATA_SYNC,
            exported = false,
            description = "Unified agent runtime foreground service"
        ),
        AdtServiceDefinition(
            id = "adventure_foreground_service",
            className = "com.blackbox.module.adt.runtime.AdventureForegroundService",
            foregroundType = AdtForegroundType.DATA_SYNC,
            exported = false,
            description = "Adventure/media foreground service"
        ),
        AdtServiceDefinition(
            id = "ai_tool_server_service",
            className = "com.blackbox.module.adt.runtime.AiToolServerService",
            foregroundType = AdtForegroundType.DATA_SYNC,
            exported = false,
            description = "AI tool / MCP server service"
        )
    )

    val bootReceivers: List<AdtReceiverDefinition> = listOf(
        AdtReceiverDefinition(
            id = "boot_receiver",
            className = "com.blackbox.module.adt.runtime.AdtBootReceiver",
            exported = true,
            actions = listOf("android.intent.action.BOOT_COMPLETED")
        ),
        AdtReceiverDefinition(
            id = "zim_download_receiver",
            className = "com.blackbox.module.adt.runtime.ZimDownloadReceiver",
            exported = true,
            actions = listOf(
                "android.intent.action.VIEW",
                "android.intent.action.DOWNLOAD_COMPLETE"
            )
        )
    )
}
