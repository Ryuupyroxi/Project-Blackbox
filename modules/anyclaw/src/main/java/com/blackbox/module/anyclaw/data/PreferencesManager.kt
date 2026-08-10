package com.blackbox.module.anyclaw.data

import android.content.Context
import com.blackbox.core.data.BlackboxPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesManager(context: Context) {
    private val prefs = BlackboxPreferences(context)

    val apiProvider: Flow<String?> = prefs.selectedProvider
    val selectedModel: Flow<String?> = prefs.selectedModel
    val selectedModelId: Flow<String?> = prefs.selectedModelId
    val selectedModelReasoning: Flow<String?> = prefs.selectedModelReasoning
    val selectedModelImages: Flow<String?> = prefs.selectedModelImages
    val selectedModelContext: Flow<String?> = prefs.selectedModelContext
    val selectedModelMaxOutput: Flow<String?> = prefs.selectedModelMaxOutput
    val openAiCompatibleBaseUrl: Flow<String?> = prefs.openAiCompatibleBaseUrl
    val openAiCompatibleModelId: Flow<String?> = prefs.openAiCompatibleModelId
    val discordEnabled: Flow<Boolean> = prefs.discordEnabled
    val telegramEnabled: Flow<Boolean> = prefs.telegramEnabled
    val whatsappEnabled: Flow<Boolean> = prefs.whatsappEnabled
    val openClawVersion: Flow<String?> = prefs.openClawVersion
    val codexappVersion: Flow<String?> = prefs.codexAppVersion
    val codexappBranch: Flow<String?> = prefs.codexAppBranch
    val autoStartOpenClawOnBoot: Flow<Boolean> = prefs.autoStartOpenClawOnBoot
    val autoStartCodexOnBoot: Flow<Boolean> = prefs.autoStartCodexOnBoot
    val autoStartSshd: Flow<Boolean> = prefs.autoStartSshd
    val setupComplete: Flow<Boolean> = prefs.setupComplete
    val onboardingComplete: Flow<Boolean> = prefs.onboardingComplete
    val customWebViewUrl: Flow<String?> = prefs.customWebViewUrl
    val lastWebViewPath: Flow<String?> = prefs.lastWebViewPath
    val appLanguageTag: Flow<String?> = prefs.appLanguageTag
    val premiumActive: Flow<Boolean> = prefs.premiumActive
    val hasRated: Flow<Boolean> = prefs.hasRated
    val gatewayWasRunning: Flow<Boolean> = prefs.gatewayWasRunning
    val lastAppOpenedAt: Flow<Long> = prefs.lastAppOpenedAt
    val checkLoginOnStart: Flow<Boolean> = prefs.checkLoginOnStart
    val batteryOptimizationPrompted: Flow<Boolean> = prefs.batteryOptimizationPrompted
    val logSectionUnlocked: Flow<Boolean> = prefs.logSectionUnlocked
    val fakeUsUser: Flow<Boolean> = prefs.fakeUsUser
    val forceShowCalendar: Flow<Boolean> = prefs.forceShowCalendar
    val forceShowRewardCalendarDebug: Flow<Boolean> = prefs.forceShowRewardCalendarDebug
    val rewardStreakCount: Flow<Int> = prefs.rewardStreakCount
    val rewardLastClaimDate: Flow<Long> = prefs.rewardLastClaimDate

    suspend fun setApiProvider(provider: String) {
        prefs.setSelectedProvider(provider)
    }

    suspend fun setSelectedModel(model: String) {
        prefs.setSelectedModel(model)
    }

    suspend fun setApiKey(provider: String, key: String) {
        when (provider.lowercase()) {
            "discord" -> prefs.setDiscordBotToken(key)
            "telegram" -> prefs.setTelegramBotToken(key)
            "openai", "anthropic", "openai_compatible", "openrouter" -> prefs.setSelectedProvider(provider)
            else -> prefs.setSelectedProvider(provider)
        }
    }

    suspend fun setOpenAiCompatibleBaseUrl(url: String) {
        prefs.setOpenAiCompatibleBaseUrl(url)
    }

    suspend fun setOpenAiCompatibleModelId(modelId: String) {
        prefs.setOpenAiCompatibleModelId(modelId)
    }

    suspend fun setAutoStartOpenClawOnBoot(enabled: Boolean) {
        prefs.setAutoStartOpenClawOnBoot(enabled)
    }

    suspend fun setAutoStartCodexOnBoot(enabled: Boolean) {
        prefs.setAutoStartCodexOnBoot(enabled)
    }

    suspend fun setAutoStartSshd(enabled: Boolean) {
        prefs.setAutoStartSshd(enabled)
    }

    suspend fun setSetupComplete(complete: Boolean) {
        prefs.setSetupComplete(complete)
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        prefs.setOnboardingComplete(complete)
    }

    suspend fun setCustomWebViewUrl(url: String) {
        prefs.setCustomWebViewUrl(url)
    }

    suspend fun setLastWebViewPath(path: String) {
        prefs.setLastWebViewPath(path)
    }

    suspend fun setCodexappVersion(version: String) {
        prefs.setCodexAppVersion(version)
    }

    suspend fun setCodexappBranch(branch: String) {
        prefs.setCodexAppBranch(branch)
    }

    suspend fun setOpenClawVersion(version: String) {
        prefs.setOpenClawVersion(version)
    }

    suspend fun setAppLanguageTag(tag: String) {
        prefs.setAppLanguageTag(tag)
    }

    suspend fun setPremiumActive(active: Boolean) {
        prefs.setPremiumActive(active)
    }

    suspend fun setHasRated(rated: Boolean) {
        prefs.setHasRated(rated)
    }

    suspend fun setRewardStreak(count: Int) {
        prefs.setRewardStreak(count)
    }

    suspend fun setRewardLastClaimDate(timestamp: Long) {
        prefs.setRewardLastClaimDate(timestamp)
    }

    fun hasApiKeyForProvider(provider: String): Flow<Boolean> {
        return when (provider.lowercase()) {
            "discord" -> prefs.discordBotToken.map { !it.isNullOrBlank() }
            "telegram" -> prefs.telegramBotToken.map { !it.isNullOrBlank() }
            else -> prefs.selectedProvider.map { it == provider }
        }
    }
}
