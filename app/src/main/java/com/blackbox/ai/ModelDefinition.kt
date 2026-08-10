package com.blackbox.ai

data class ModelDefinition(
    val id: String,
    val name: String,
    val provider: String,
    val contextTokens: Int? = null,
    val supportsTools: Boolean = false,
    val supportsVision: Boolean = false
)
