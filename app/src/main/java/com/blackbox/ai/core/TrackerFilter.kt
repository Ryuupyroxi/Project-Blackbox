package com.blackbox.ai.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TrackerFilter {
    
    private val blockedDomains = mutableSetOf<String>()
    private val allowedDomains = mutableSetOf<String>()
    
    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()
    
    private val _domainCount = MutableStateFlow(0)
    val domainCount: StateFlow<Int> = _domainCount.asStateFlow()
    
    fun shouldBlock(url: String): Boolean {
        if (!_isEnabled.value) return false
        
        val host = extractHost(url) ?: return false
        
        if (allowedDomains.contains(host)) return false
        if (blockedDomains.contains(host)) return true
        
        return false
    }
    
    fun loadDomains(domains: Set<String>) {
        blockedDomains.addAll(domains)
        _domainCount.value = blockedDomains.size
    }
    
    fun addAllowedDomain(domain: String) {
        allowedDomains.add(domain)
    }
    
    fun removeAllowedDomain(domain: String) {
        allowedDomains.remove(domain)
    }
    
    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
    }
    
    fun getDomainCount(): Int = blockedDomains.size
    
    private fun extractHost(url: String): String? {
        return try {
            java.net.URL(url).host
        } catch (e: Exception) {
            null
        }
    }
}
