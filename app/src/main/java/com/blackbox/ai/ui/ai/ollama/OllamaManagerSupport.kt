package com.blackbox.ai.ui.ai.ollama

import com.blackbox.ai.data.db.OllamaServerEntity

internal fun resolveSelectedOllamaServer(
    servers: List<OllamaServerEntity>,
    selectedServerId: Long?
): OllamaServerEntity? {
    if (selectedServerId == null) return null
    return servers.firstOrNull { it.id == selectedServerId }
}
