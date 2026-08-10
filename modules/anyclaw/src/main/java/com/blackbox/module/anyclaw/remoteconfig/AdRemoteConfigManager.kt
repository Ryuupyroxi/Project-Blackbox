package com.blackbox.module.anyclaw.remoteconfig

data class AdCountryConfig(val country: String = "US")
data class UpdateAvailableConfig(val version: String = "", val forced: Boolean = false)

class AdRemoteConfigManager {
    suspend fun initAndFetch() = null
}
