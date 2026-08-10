package com.blackbox.core.module.contract

import com.blackbox.core.module.BlackboxModule

/**
 * Shared ADT runtime contracts.
 *
 * These types live in `:modules:core` (not `:modules:adt`) so that the core
 * module-loader can reference them at compile time without creating a
 * circular `core -> adt -> core` dependency. `:modules:adt` implements them.
 * This keeps the runtime DexClassLoader module-loading architecture intact:
 * core only ever knows the contract interfaces.
 */
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

interface AdtModule : BlackboxModule {
    /** Populate this module's service/receiver catalogs from its manifest. */
    fun registerRuntime()
}
