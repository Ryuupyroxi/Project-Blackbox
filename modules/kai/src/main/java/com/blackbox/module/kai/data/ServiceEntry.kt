package com.blackbox.module.kai.data

import kotlinx.serialization.Serializable

@Serializable
data class ServiceEntry(
    val service: Service,
    val instance: ServiceInstance? = null,
    val selectedModelId: String = ""
)

@Serializable
data class ServiceInstance(
    val id: String,
    val label: String,
    val baseUrl: String? = null,
    val apiKey: String? = null,
    val modelId: String? = null
)
