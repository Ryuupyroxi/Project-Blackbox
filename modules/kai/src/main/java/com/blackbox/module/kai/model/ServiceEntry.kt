package com.blackbox.module.kai.model

data class ServiceEntry(
    val instanceId: String,
    val serviceId: String,
    val modelId: String? = null,
    val icon: Any? = null
)
