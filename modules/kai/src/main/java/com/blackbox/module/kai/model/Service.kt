package com.blackbox.module.kai.model

data class Service(
    val id: String,
    val name: String,
    val baseUrl: String? = null,
    val apiKeyEnv: String? = null,
    val isLocal: Boolean = false
)
