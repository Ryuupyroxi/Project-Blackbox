package com.blackbox.ai.core

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object WebViewSessionStore {
    
    data class WebViewConfig(
        val id: String,
        val url: String,
        val title: String = "",
        val isActive: Boolean = true,
        val lastAccessed: Long = System.currentTimeMillis()
    )
    
    private val _tabs = MutableStateFlow<List<WebViewConfig>>(emptyList())
    val tabs: StateFlow<List<WebViewConfig>> = _tabs.asStateFlow()
    
    private val webViews = mutableMapOf<String, WebView>()
    private val configs = mutableMapOf<String, WebViewConfig>()
    
    fun getOrCreate(tabId: String, url: String, title: String = ""): WebViewConfig {
        val config = configs.getOrPut(tabId) {
            WebViewConfig(id = tabId, url = url, title = title)
        }
        _tabs.value = configs.values.toList()
        return config
    }
    
    fun saveState(tabId: String) {
        configs[tabId]?.let { config ->
            configs[tabId] = config.copy(lastAccessed = System.currentTimeMillis())
        }
    }
    
    fun restoreState(tabId: String): WebViewConfig? = configs[tabId]
    
    fun listTabs(): List<WebViewConfig> = configs.values.toList()
    
    fun deleteTab(tabId: String) {
        configs.remove(tabId)
        webViews.remove(tabId)
        _tabs.value = configs.values.toList()
    }
    
    fun updateTab(tabId: String, url: String? = null, title: String? = null) {
        configs[tabId]?.let { config ->
            configs[tabId] = config.copy(
                url = url ?: config.url,
                title = title ?: config.title,
                lastAccessed = System.currentTimeMillis()
            )
            _tabs.value = configs.values.toList()
        }
    }
}
