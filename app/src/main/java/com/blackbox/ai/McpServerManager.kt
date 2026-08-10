package com.blackbox.ai

data class McpServerConfig(
    val id: String,
    val name: String,
    val command: String,
    val args: List<String> = emptyList(),
    val env: Map<String, String> = emptyMap()
)

interface McpServerManager {
    suspend fun connect(config: McpServerConfig): Boolean
    suspend fun disconnect(id: String)
    suspend fun callTool(serverId: String, toolName: String, argsJson: String): String
    suspend fun listTools(serverId: String): List<String>
}
