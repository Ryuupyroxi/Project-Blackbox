package com.blackbox.core.module

data class ModuleManifest(
    val id: String,
    val name: String,
    val version: String,
    val sha256: String,
    val dexFiles: List<String>,
    val nativeLibs: List<String>,
    val assets: List<String>
)
