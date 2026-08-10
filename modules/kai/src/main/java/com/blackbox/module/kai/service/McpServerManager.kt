package com.blackbox.module.kai.service

import com.blackbox.core.module.kai.mcp.McpServerConfig

class McpServerManager {
    suspend fun connectAndDiscoverTools(config: McpServerConfig) = emptyList<com.blackbox.module.kai.mcp.McpToolDefinition>()
}
