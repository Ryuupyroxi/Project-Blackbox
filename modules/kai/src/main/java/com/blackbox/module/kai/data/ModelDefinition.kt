package com.blackbox.module.kai.data

import kotlinx.serialization.Serializable

@Serializable
data class ModelDefinition(
    val id: String,
    val name: String,
    val provider: String,
    val contextLength: Int = 4096,
    val supportsImages: Boolean = false,
    val supportsTools: Boolean = false
)
