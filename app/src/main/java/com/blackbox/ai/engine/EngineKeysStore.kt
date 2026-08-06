package com.blackbox.ai.engine

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores API keys and channel preferences for the unified agent engine.
 * Keys are encrypted with EncryptedSharedPreferences (AndroidX Security).
 * If the Keystore is unavailable the store falls back to plain prefs so the
 * app still works on unusual devices.
 *
 * Every feature in Blackbox can run through any channel:
 *  - local: an OpenAI-compatible server on the device (llama.cpp / Ollama)
 *  - local Termux/Ubuntu runtime (via SSH)
 *  - any API key: OpenAI, OpenRouter, Anthropic
 */
class EngineKeysStore(context: Context) {

    private val prefs: SharedPreferences = createPrefs(context)

    private fun createPrefs(context: Context): SharedPreferences {
        return runCatching {
            val masterKey = MasterKey.Builder(
                context,
                MasterKey.DEFAULT_MASTER_KEY_ALIAS
            ).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            EncryptedSharedPreferences.create(
                context,
                "blackbox_engine",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }.getOrElse {
            context.getSharedPreferences("blackbox_engine", Context.MODE_PRIVATE)
        }
    }

    // OpenAI
    fun getOpenAiKey(): String = prefs.getString(KEY_OPENAI_KEY, "") ?: ""
    fun getOpenAiModel(): String = prefs.getString(KEY_OPENAI_MODEL, "gpt-4o-mini") ?: "gpt-4o-mini"
    fun isOpenAiEnabled(): Boolean = prefs.getBoolean(KEY_OPENAI_ENABLED, false)
    fun setOpenAi(key: String, model: String, enabled: Boolean) {
        prefs.edit().putString(KEY_OPENAI_KEY, key.trim())
            .putString(KEY_OPENAI_MODEL, model.trim())
            .putBoolean(KEY_OPENAI_ENABLED, enabled).apply()
    }

    // OpenRouter
    fun getOpenRouterKey(): String = prefs.getString(KEY_OPENROUTER_KEY, "") ?: ""
    fun getOpenRouterModel(): String = prefs.getString(KEY_OPENROUTER_MODEL, "openrouter/auto") ?: "openrouter/auto"
    fun isOpenRouterEnabled(): Boolean = prefs.getBoolean(KEY_OPENROUTER_ENABLED, false)
    fun setOpenRouter(key: String, model: String, enabled: Boolean) {
        prefs.edit().putString(KEY_OPENROUTER_KEY, key.trim())
            .putString(KEY_OPENROUTER_MODEL, model.trim())
            .putBoolean(KEY_OPENROUTER_ENABLED, enabled).apply()
    }

    // Anthropic
    fun getAnthropicKey(): String = prefs.getString(KEY_ANTHROPIC_KEY, "") ?: ""
    fun getAnthropicModel(): String = prefs.getString(KEY_ANTHROPIC_MODEL, "claude-3-5-haiku-latest") ?: "claude-3-5-haiku-latest"
    fun isAnthropicEnabled(): Boolean = prefs.getBoolean(KEY_ANTHROPIC_ENABLED, false)
    fun setAnthropic(key: String, model: String, enabled: Boolean) {
        prefs.edit().putString(KEY_ANTHROPIC_KEY, key.trim())
            .putString(KEY_ANTHROPIC_MODEL, model.trim())
            .putBoolean(KEY_ANTHROPIC_ENABLED, enabled).apply()
    }

    // Local OpenAI-compatible server (llama.cpp / Ollama)
    fun getLocalBaseUrl(): String = prefs.getString(KEY_LOCAL_BASE_URL, "http://127.0.0.1:8080") ?: "http://127.0.0.1:8080"
    fun getLocalModel(): String = prefs.getString(KEY_LOCAL_MODEL, "local") ?: "local"
    fun isLocalEnabled(): Boolean = prefs.getBoolean(KEY_LOCAL_ENABLED, true)
    fun setLocal(baseUrl: String, model: String, enabled: Boolean) {
        prefs.edit().putString(KEY_LOCAL_BASE_URL, baseUrl.trim())
            .putString(KEY_LOCAL_MODEL, model.trim())
            .putBoolean(KEY_LOCAL_ENABLED, enabled).apply()
    }

    // Termux / Ubuntu SSH channel
    fun getTermuxHost(): String = prefs.getString(KEY_TERMUX_HOST, "127.0.0.1") ?: "127.0.0.1"
    fun getTermuxPort(): Int = prefs.getInt(KEY_TERMUX_PORT, 8025)
    fun getTermuxUser(): String = prefs.getString(KEY_TERMUX_USER, "root") ?: "root"
    fun getTermuxPassword(): String = prefs.getString(KEY_TERMUX_PASSWORD, "") ?: ""
    fun setTermux(host: String, port: Int, user: String, password: String) {
        prefs.edit().putString(KEY_TERMUX_HOST, host.trim())
            .putInt(KEY_TERMUX_PORT, port)
            .putString(KEY_TERMUX_USER, user.trim())
            .putString(KEY_TERMUX_PASSWORD, password).apply()
    }

    // Quick agent chat history (kept in memory is fine; store last session log)
    fun getLastSessionLog(): String = prefs.getString(KEY_SESSION_LOG, "") ?: ""
    fun setLastSessionLog(text: String) {
        prefs.edit().putString(KEY_SESSION_LOG, text).apply()
    }

    companion object {
        private const val KEY_OPENAI_KEY = "openai_key"
        private const val KEY_OPENAI_MODEL = "openai_model"
        private const val KEY_OPENAI_ENABLED = "openai_enabled"
        private const val KEY_OPENROUTER_KEY = "openrouter_key"
        private const val KEY_OPENROUTER_MODEL = "openrouter_model"
        private const val KEY_OPENROUTER_ENABLED = "openrouter_enabled"
        private const val KEY_ANTHROPIC_KEY = "anthropic_key"
        private const val KEY_ANTHROPIC_MODEL = "anthropic_model"
        private const val KEY_ANTHROPIC_ENABLED = "anthropic_enabled"
        private const val KEY_LOCAL_BASE_URL = "local_base_url"
        private const val KEY_LOCAL_MODEL = "local_model"
        private const val KEY_LOCAL_ENABLED = "local_enabled"
        private const val KEY_TERMUX_HOST = "termux_host"
        private const val KEY_TERMUX_PORT = "termux_port"
        private const val KEY_TERMUX_USER = "termux_user"
        private const val KEY_TERMUX_PASSWORD = "termux_password"
        private const val KEY_SESSION_LOG = "session_log"
    }
}
