package com.blackbox.ai.service.online

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AiServiceDefinition(
    val id: String,
    val name: String,
    val url: String,
    val category: String = "general",
    val isEnabled: Boolean = true
)

object AiServiceManager {
    
    private val _availableServices = MutableStateFlow(defaultServices)
    val availableServices: StateFlow<List<AiServiceDefinition>> = _availableServices.asStateFlow()
    
    private val _enabledServices = MutableStateFlow<List<String>>(emptyList())
    val enabledServices: StateFlow<List<String>> = _enabledServices.asStateFlow()
    
    private val defaultServices = listOf(
        AiServiceDefinition("chatgpt", "ChatGPT", "https://chat.openai.com", "chat"),
        AiServiceDefinition("claude", "Claude", "https://claude.ai", "chat"),
        AiServiceDefinition("gemini", "Gemini", "https://gemini.google.com", "chat"),
        AiServiceDefinition("copilot", "GitHub Copilot", "https://copilot.github.com", "code"),
        AiServiceDefinition("perplexity", "Perplexity", "https://perplexity.ai", "search"),
        AiServiceDefinition("huggingface", "HuggingFace", "https://huggingface.co", "models"),
        AiServiceDefinition("ollama", "Ollama (Local)", "http://localhost:11434", "local"),
        AiServiceDefinition("openrouter", "OpenRouter", "https://openrouter.ai", "api"),
        AiServiceDefinition("deepseek", "DeepSeek", "https://chat.deepseek.com", "chat"),
        AiServiceDefinition("mistral", "Mistral AI", "https://chat.mistral.ai", "chat")
    )
    
    fun addService(service: AiServiceDefinition) {
        val current = _availableServices.value.toMutableList()
        if (current.none { it.id == service.id }) {
            current.add(service)
            _availableServices.value = current
        }
    }
    
    fun removeService(id: String) {
        _availableServices.value = _availableServices.value.filter { it.id != id }
    }
    
    fun enableService(id: String) {
        val current = _enabledServices.value.toMutableList()
        if (!current.contains(id)) {
            current.add(id)
            _enabledServices.value = current
        }
    }
    
    fun disableService(id: String) {
        _enabledServices.value = _enabledServices.value.filter { it != id }
    }
    
    fun reorderServices(fromIndex: Int, toIndex: Int) {
        val current = _availableServices.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _availableServices.value = current
        }
    }
}
