package com.blackbox.ai.core

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppSettings {
    
    private const val PREFS_NAME = "blackbox_settings"
    private lateinit var prefs: SharedPreferences
    
    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()
    
    private val _baseUrl = MutableStateFlow("https://api.deepseek.com")
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()
    
    private val _model = MutableStateFlow("deepseek-chat")
    val model: StateFlow<String> = _model.asStateFlow()
    
    private val _theme = MutableStateFlow("dark")
    val theme: StateFlow<String> = _theme.asStateFlow()
    
    private val _trackerEnabled = MutableStateFlow(true)
    val trackerEnabled: StateFlow<Boolean> = _trackerEnabled.asStateFlow()
    
    private val _wakeLock = MutableStateFlow(false)
    val wakeLock: StateFlow<Boolean> = _wakeLock.asStateFlow()
    
    private val _notifications = MutableStateFlow(true)
    val notifications: StateFlow<Boolean> = _notifications.asStateFlow()
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadAll()
    }
    
    private fun loadAll() {
        _apiKey.value = prefs.getString("api_key", "") ?: ""
        _baseUrl.value = prefs.getString("base_url", "https://api.deepseek.com") ?: "https://api.deepseek.com"
        _model.value = prefs.getString("model", "deepseek-chat") ?: "deepseek-chat"
        _theme.value = prefs.getString("theme", "dark") ?: "dark"
        _trackerEnabled.value = prefs.getBoolean("tracker_enabled", true)
        _wakeLock.value = prefs.getBoolean("wake_lock", false)
        _notifications.value = prefs.getBoolean("notifications", true)
    }
    
    fun saveApiKey(key: String) {
        prefs.edit().putString("api_key", key).apply()
        _apiKey.value = key
    }
    
    fun saveBaseUrl(url: String) {
        prefs.edit().putString("base_url", url).apply()
        _baseUrl.value = url
    }
    
    fun saveModel(model: String) {
        prefs.edit().putString("model", model).apply()
        _model.value = model
    }
    
    fun saveTheme(theme: String) {
        prefs.edit().putString("theme", theme).apply()
        _theme.value = theme
    }
    
    fun saveTrackerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("tracker_enabled", enabled).apply()
        _trackerEnabled.value = enabled
    }
    
    fun saveWakeLock(enabled: Boolean) {
        prefs.edit().putBoolean("wake_lock", enabled).apply()
        _wakeLock.value = enabled
    }
    
    fun saveNotifications(enabled: Boolean) {
        prefs.edit().putBoolean("notifications", enabled).apply()
        _notifications.value = enabled
    }
}
