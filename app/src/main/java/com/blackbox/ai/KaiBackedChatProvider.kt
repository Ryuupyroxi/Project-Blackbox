package com.blackbox.ai

import com.blackbox.core.module.kai.KaiServiceCatalog
import com.blackbox.core.module.kai.KaiChatProvider
import com.blackbox.core.module.kai.KaiProviderSelector
import com.blackbox.core.module.kai.model.Service
import com.blackbox.core.module.kai.model.ServiceEntry

class KaiBackedChatProvider(
    private val initialServiceId: String = "openai"
) : ChatProvider {

    private var currentEntry: ServiceEntry = KaiProviderSelector.select(initialServiceId, null)
    private val provider = KaiChatProvider(currentEntry.service)

    override suspend fun chat(messages: List<ChatResult.Message>): ChatResult {
        return runCatching {
            val raw = provider.send(messages.map { com.blackbox.module.kai.model.BlackboxMessage(it.role, it.content) })
            raw.fold(
                onSuccess = { ChatResult.Message(it, "assistant") },
                onFailure = { ChatResult.Error(it.message ?: "Unknown chat error") }
            )
        }.getOrElse { ChatResult.Error(it.message ?: "Unknown chat error") }
    }

    override suspend fun stop() {
        provider.send(emptyList())
    }

    fun currentService(): Service = currentEntry.service
    fun catalog(): List<Service> = KaiServiceCatalog.entries
    fun selectService(serviceId: String, selectedModelId: String?) {
        currentEntry = KaiProviderSelector.select(serviceId, selectedModelId)
    }
}
