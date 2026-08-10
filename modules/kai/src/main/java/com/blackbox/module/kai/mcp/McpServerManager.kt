package com.blackbox.module.kai.mcp

class McpServerManager {
    suspend fun connectAndDiscoverTools(config: McpServerConfig): List<McpToolDefinition> = emptyList()
    suspend fun connectEnabledServers() {}
}
