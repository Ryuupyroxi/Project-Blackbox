package com.blackbox.core.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.blackboxStore by preferencesDataStore(name = "blackbox_prefs")

object BlackboxKeys {
    // Provider / model
    val SELECTED_PROVIDER = stringPreferencesKey("selected_provider")
    val SELECTED_MODEL_ID = stringPreferencesKey("selected_model_id")
    val SELECTED_MODEL = stringPreferencesKey("selected_model")
    val SELECTED_MODEL_REASONING = stringPreferencesKey("selected_model_reasoning")
    val SELECTED_MODEL_IMAGES = stringPreferencesKey("selected_model_images")
    val SELECTED_MODEL_CONTEXT = stringPreferencesKey("selected_model_context")
    val SELECTED_MODEL_MAX_OUTPUT = stringPreferencesKey("selected_model_max_output")
    val OPEN_AI_COMPATIBLE_BASE_URL = stringPreferencesKey("openai_compatible_base_url")
    val OPEN_AI_COMPATIBLE_MODEL_ID = stringPreferencesKey("openai_compatible_model_id")
    val BRAVE_SEARCH_API_KEY = stringPreferencesKey("brave_search_api_key")

    // AnyClaw bridge tokens / toggles
    val DISCORD_ENABLED = booleanPreferencesKey("discord_enabled")
    val DISCORD_BOT_TOKEN = stringPreferencesKey("discord_bot_token")
    val DISCORD_GUILD_ALLOWLIST = stringPreferencesKey("discord_guild_allowlist")
    val DISCORD_REQUIRE_MENTION = booleanPreferencesKey("discord_require_mention")
    val TELEGRAM_ENABLED = booleanPreferencesKey("telegram_enabled")
    val TELEGRAM_BOT_TOKEN = stringPreferencesKey("telegram_bot_token")
    val WHATSAPP_ENABLED = booleanPreferencesKey("whatsapp_enabled")

    // OpenClaw / Codex
    val OPEN_CLAW_VERSION = stringPreferencesKey("openclaw_version")
    val AUTO_START_OPEN_CLAW_ON_BOOT = booleanPreferencesKey("auto_start_openclaw_on_boot")
    val OPEN_CLAW_UPDATE_PROMPT_SUPPRESSED_BUNDLED_VERSION = stringPreferencesKey("openclaw_update_prompt_suppressed_bundled_version")
    val CODEX_APP_VERSION = stringPreferencesKey("codexapp_version")
    val CODEX_APP_BRANCH = stringPreferencesKey("codexapp_branch")
    val AUTO_START_CODEX_ON_BOOT = booleanPreferencesKey("auto_start_codex_on_boot")
    val CUSTOM_WEB_VIEW_URL = stringPreferencesKey("custom_webview_url")
    val LAST_WEB_VIEW_PATH = stringPreferencesKey("last_webview_path")

    // App state
    val SETUP_COMPLETE = booleanPreferencesKey("setup_complete")
    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    val PREMIUM_ACTIVE = booleanPreferencesKey("premium_active")
    val HAS_RATED = booleanPreferencesKey("has_rated")
    val GATEWAY_WAS_RUNNING = booleanPreferencesKey("gateway_was_running")
    val LAST_APP_OPENED_AT = longPreferencesKey("last_app_opened_at")
    val CHECK_LOGIN_ON_START = booleanPreferencesKey("check_login_on_start")
    val AUTO_START_SSHD = booleanPreferencesKey("auto_start_sshd")
    val BATTERY_OPTIMIZATION_PROMPTED = booleanPreferencesKey("battery_optimization_prompted")
    val LOG_SECTION_UNLOCKED = booleanPreferencesKey("log_section_unlocked")
    val APP_LANGUAGE_TAG = stringPreferencesKey("app_language_tag")
    val LAST_INTERSTITIAL_AD_SHOWN_DATE = longPreferencesKey("last_interstitial_ad_shown_date")

    // Reward/premium keys kept as no-op stubs so legacy reads don't crash
    val FAKE_US_USER = booleanPreferencesKey("fake_us_user")
    val FORCE_SHOW_CALENDAR = booleanPreferencesKey("force_show_calendar")
    val FORCE_SHOW_REWARD_CALENDAR_DEBUG = booleanPreferencesKey("force_show_reward_calendar_debug")
    val REWARD_STREAK_COUNT = intPreferencesKey("reward_streak_count")
    val REWARD_LAST_CLAIM_DATE = longPreferencesKey("reward_last_claim_date")
}

class BlackboxPreferences(private val context: Context) {
    val selectedProvider: Flow<String?> = context.blackboxStore.data.map { it[BlackboxKeys.SELECTED_PROVIDER] }
    val selectedModelId: Flow<String?> = context.blackboxStore.data.map { it[BlackboxKeys.SELECTED_MODEL_ID] }
    val selectedModel: Flow<String?> = context.blackboxStore.data.map { it[BlackboxKeys.SELECTED_MODEL] }
    val selectedModelReasoning: Flow<String?> = context.blackboxStore.data.map { it[BlackboxKeys.SELECTED_MODEL_REASONING] }
    val selectedModelImages: Flow<String?> = context.blackboxStore.data.map { it[BlackboxKeys.SELECTED_MODEL_IMAGES] }
    val selectedModelContext: Flow<String?> = context.blackboxStore.data.map { it[BlackboxKeys.SELECTED_MODEL_CONTEXT] }
    val selectedModelMaxOutput: Flow<String?> = context.blackboxStore.data.map { it[BlackboxKeys.SELECTED_MODEL_MAX_OUTPUT] }
    val discordEnabled: Flow<Boolean> = context.blackboxStore.data.map { it[BlackboxKeys.DISCORD_ENABLED] ?: false }
    val telegramEnabled: Flow<Boolean> = context.blackboxStore.data.map { it[BlackboxKeys.TELEGRAM_ENABLED] ?: false }
    val whatsappEnabled: Flow<Boolean> = context.blackboxStore.data.map { it[BlackboxKeys.WHATSAPP_ENABLED] ?: false }
    val setupComplete: Flow<Boolean> = context.blackboxStore.data.map { it[BlackboxKeys.SETUP_COMPLETE] ?: false }
    val onboardingComplete: Flow<Boolean> = context.blackboxStore.data.map { it[BlackboxKeys.ONBOARDING_COMPLETE] ?: false }
    val autoStartOpenClawOnBoot: Flow<Boolean> = context.blackboxStore.data.map { it[BlackboxKeys.AUTO_START_OPEN_CLAW_ON_BOOT] ?: false }
    val autoStartCodexOnBoot: Flow<Boolean> = context.blackboxStore.data.map { it[BlackboxKeys.AUTO_START_CODEX_ON_BOOT] ?: false }
    val autoStartSshd: Flow<Boolean> = context.blackboxStore.data.map { it[BlackboxKeys.AUTO_START_SSHD] ?: false }
    val premiumActive: Flow<Boolean> = context.blackboxStore.data.map { it[BlackboxKeys.PREMIUM_ACTIVE] ?: false }
    val hasRated: Flow<Boolean> = context.blackboxStore.data.map { it[BlackboxKeys.HAS_RATED] ?: false }
    val gatewayWasRunning: Flow<Boolean> = context.blackboxStore.data.map { it[BlackboxKeys.GATEWAY_WAS_RUNNING] ?: false }
    val lastAppOpenedAt: Flow<Long> = context.blackboxStore.data.map { it[BlackboxKeys.LAST_APP_OPENED_AT] ?: 0L }
    val checkLoginOnStart: Flow<Boolean> = context.blackboxStore.data.map { it[BlackboxKeys.CHECK_LOGIN_ON_START] ?: false }
    val batteryOptimizationPrompted: Flow<Boolean> = context.blackboxStore.data.map { it[BlackboxKeys.BATTERY_OPTIMIZATION_PROMPTED] ?: false }
    val logSectionUnlocked: Flow<Boolean> = context.blackboxStore.data.map { it[BlackboxKeys.LOG_SECTION_UNLOCKED] ?: false }
    val appLanguageTag: Flow<String?> = context.blackboxStore.data.map { it[BlackboxKeys.APP_LANGUAGE_TAG] }
    val lastInterstitialAdShownDate: Flow<Long> = context.blackboxStore.data.map { it[BlackboxKeys.LAST_INTERSTITIAL_AD_SHOWN_DATE] ?: 0L }
    val fakeUsUser: Flow<Boolean> = context.blackboxStore.data.map { it[BlackboxKeys.FAKE_US_USER] ?: false }
    val forceShowCalendar: Flow<Boolean> = context.blackboxStore.data.map { it[BlackboxKeys.FORCE_SHOW_CALENDAR] ?: false }
    val forceShowRewardCalendarDebug: Flow<Boolean> = context.blackboxStore.data.map { it[BlackboxKeys.FORCE_SHOW_REWARD_CALENDAR_DEBUG] ?: false }
    val rewardStreakCount: Flow<Int> = context.blackboxStore.data.map { it[BlackboxKeys.REWARD_STREAK_COUNT] ?: 0 }
    val rewardLastClaimDate: Flow<Long> = context.blackboxStore.data.map { it[BlackboxKeys.REWARD_LAST_CLAIM_DATE] ?: 0L }

    val discordBotToken: Flow<String?> = context.blackboxStore.data.map { it[BlackboxKeys.DISCORD_BOT_TOKEN] }
    val telegramBotToken: Flow<String?> = context.blackboxStore.data.map { it[BlackboxKeys.TELEGRAM_BOT_TOKEN] }
    val openClawVersion: Flow<String?> = context.blackboxStore.data.map { it[BlackboxKeys.OPEN_CLAW_VERSION] }
    val codexAppVersion: Flow<String?> = context.blackboxStore.data.map { it[BlackboxKeys.CODEX_APP_VERSION] }
    val codexAppBranch: Flow<String?> = context.blackboxStore.data.map { it[BlackboxKeys.CODEX_APP_BRANCH] }
    val customWebViewUrl: Flow<String?> = context.blackboxStore.data.map { it[BlackboxKeys.CUSTOM_WEB_VIEW_URL] }
    val lastWebViewPath: Flow<String?> = context.blackboxStore.data.map { it[BlackboxKeys.LAST_WEB_VIEW_PATH] }
    val openAiCompatibleBaseUrl: Flow<String?> = context.blackboxStore.data.map { it[BlackboxKeys.OPEN_AI_COMPATIBLE_BASE_URL] }
    val openAiCompatibleModelId: Flow<String?> = context.blackboxStore.data.map { it[BlackboxKeys.OPEN_AI_COMPATIBLE_MODEL_ID] }
    val braveSearchApiKey: Flow<String?> = context.blackboxStore.data.map { it[BlackboxKeys.BRAVE_SEARCH_API_KEY] }

    suspend fun setSelectedProvider(provider: String) {
        context.blackboxStore.edit { it[BlackboxKeys.SELECTED_PROVIDER] = provider }
    }

    suspend fun setSelectedModelId(modelId: String) {
        context.blackboxStore.edit { it[BlackboxKeys.SELECTED_MODEL_ID] = modelId }
    }

    suspend fun setSelectedModel(model: String) {
        context.blackboxStore.edit { it[BlackboxKeys.SELECTED_MODEL] = model }
    }

    suspend fun setSelectedModelReasoning(reasoning: String) {
        context.blackboxStore.edit { it[BlackboxKeys.SELECTED_MODEL_REASONING] = reasoning }
    }

    suspend fun setSelectedModelImages(images: String) {
        context.blackboxStore.edit { it[BlackboxKeys.SELECTED_MODEL_IMAGES] = images }
    }

    suspend fun setSelectedModelContext(context: String) {
        context.blackboxStore.edit { it[BlackboxKeys.SELECTED_MODEL_CONTEXT] = context }
    }

    suspend fun setSelectedModelMaxOutput(maxOutput: String) {
        context.blackboxStore.edit { it[BlackboxKeys.SELECTED_MODEL_MAX_OUTPUT] = maxOutput }
    }

    suspend fun setOpenAiCompatibleBaseUrl(url: String) {
        context.blackboxStore.edit { it[BlackboxKeys.OPEN_AI_COMPATIBLE_BASE_URL] = url }
    }

    suspend fun setOpenAiCompatibleModelId(modelId: String) {
        context.blackboxStore.edit { it[BlackboxKeys.OPEN_AI_COMPATIBLE_MODEL_ID] = modelId }
    }

    suspend fun setBraveSearchApiKey(key: String) {
        context.blackboxStore.edit { it[BlackboxKeys.BRAVE_SEARCH_API_KEY] = key }
    }

    suspend fun setDiscordEnabled(enabled: Boolean) {
        context.blackboxStore.edit { it[BlackboxKeys.DISCORD_ENABLED] = enabled }
    }

    suspend fun setDiscordBotToken(token: String) {
        context.blackboxStore.edit { it[BlackboxKeys.DISCORD_BOT_TOKEN] = token }
    }

    suspend fun setDiscordGuildAllowlist(value: String) {
        context.blackboxStore.edit { it[BlackboxKeys.DISCORD_GUILD_ALLOWLIST] = value }
    }

    suspend fun setDiscordRequireMention(required: Boolean) {
        context.blackboxStore.edit { it[BlackboxKeys.DISCORD_REQUIRE_MENTION] = required }
    }

    suspend fun setTelegramEnabled(enabled: Boolean) {
        context.blackboxStore.edit { it[BlackboxKeys.TELEGRAM_ENABLED] = enabled }
    }

    suspend fun setTelegramBotToken(token: String) {
        context.blackboxStore.edit { it[BlackboxKeys.TELEGRAM_BOT_TOKEN] = token }
    }

    suspend fun setWhatsAppEnabled(enabled: Boolean) {
        context.blackboxStore.edit { it[BlackboxKeys.WHATSAPP_ENABLED] = enabled }
    }

    suspend fun setOpenClawVersion(version: String) {
        context.blackboxStore.edit { it[BlackboxKeys.OPEN_CLAW_VERSION] = version }
    }

    suspend fun setAutoStartOpenClawOnBoot(enabled: Boolean) {
        context.blackboxStore.edit { it[BlackboxKeys.AUTO_START_OPEN_CLAW_ON_BOOT] = enabled }
    }

    suspend fun setOpenClawUpdatePromptSuppressedBundledVersion(version: String) {
        context.blackboxStore.edit { it[BlackboxKeys.OPEN_CLAW_UPDATE_PROMPT_SUPPRESSED_BUNDLED_VERSION] = version }
    }

    suspend fun setCodexAppVersion(version: String) {
        context.blackboxStore.edit { it[BlackboxKeys.CODEX_APP_VERSION] = version }
    }

    suspend fun setCodexAppBranch(branch: String) {
        context.blackboxStore.edit { it[BlackboxKeys.CODEX_APP_BRANCH] = branch }
    }

    suspend fun setAutoStartCodexOnBoot(enabled: Boolean) {
        context.blackboxStore.edit { it[BlackboxKeys.AUTO_START_CODEX_ON_BOOT] = enabled }
    }

    suspend fun setCustomWebViewUrl(url: String) {
        context.blackboxStore.edit { it[BlackboxKeys.CUSTOM_WEB_VIEW_URL] = url }
    }

    suspend fun setLastWebViewPath(path: String) {
        context.blackboxStore.edit { it[BlackboxKeys.LAST_WEB_VIEW_PATH] = path }
    }

    suspend fun setSetupComplete(complete: Boolean) {
        context.blackboxStore.edit { it[BlackboxKeys.SETUP_COMPLETE] = complete }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.blackboxStore.edit { it[BlackboxKeys.ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setGatewayWasRunning(running: Boolean) {
        context.blackboxStore.edit { it[BlackboxKeys.GATEWAY_WAS_RUNNING] = running }
    }

    suspend fun setLastAppOpenedAt(timestamp: Long) {
        context.blackboxStore.edit { it[BlackboxKeys.LAST_APP_OPENED_AT] = timestamp }
    }

    suspend fun setCheckLoginOnStart(enabled: Boolean) {
        context.blackboxStore.edit { it[BlackboxKeys.CHECK_LOGIN_ON_START] = enabled }
    }

    suspend fun setAutoStartSshd(enabled: Boolean) {
        context.blackboxStore.edit { it[BlackboxKeys.AUTO_START_SSHD] = enabled }
    }

    suspend fun setBatteryOptimizationPrompted(prompted: Boolean) {
        context.blackboxStore.edit { it[BlackboxKeys.BATTERY_OPTIMIZATION_PROMPTED] = prompted }
    }

    suspend fun setLogSectionUnlocked(unlocked: Boolean) {
        context.blackboxStore.edit { it[BlackboxKeys.LOG_SECTION_UNLOCKED] = unlocked }
    }

    suspend fun setAppLanguageTag(tag: String) {
        context.blackboxStore.edit { it[BlackboxKeys.APP_LANGUAGE_TAG] = tag }
    }

    suspend fun setPremiumActive(active: Boolean) {
        context.blackboxStore.edit { it[BlackboxKeys.PREMIUM_ACTIVE] = active }
    }

    suspend fun setHasRated(rated: Boolean) {
        context.blackboxStore.edit { it[BlackboxKeys.HAS_RATED] = rated }
    }

    suspend fun setRewardStreak(count: Int) {
        context.blackboxStore.edit { it[BlackboxKeys.REWARD_STREAK_COUNT] = count }
    }

    suspend fun setRewardLastClaimDate(timestamp: Long) {
        context.blackboxStore.edit { it[BlackboxKeys.REWARD_LAST_CLAIM_DATE] = timestamp }
    }
}
