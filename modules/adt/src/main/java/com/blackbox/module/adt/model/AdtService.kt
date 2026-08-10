package com.blackbox.module.adt.model

enum class AdtForegroundType(val value: String) {
    DATA_SYNC("dataSync"),
    MEDIA_PLAYBACK("mediaPlayback"),
    MICROPHONE("microphone"),
    SPECIAL_USE("specialUse")
}

data class AdtServiceDefinition(
    val id: String,
    val className: String,
    val foregroundType: AdtForegroundType?,
    val exported: Boolean,
    val description: String
)

data class AdtReceiverDefinition(
    val id: String,
    val className: String,
    val exported: Boolean,
    val actions: List<String>
)

data class AdtPermissionDefinition(
    val name: String,
    val required: Boolean,
    val rationale: String
)
