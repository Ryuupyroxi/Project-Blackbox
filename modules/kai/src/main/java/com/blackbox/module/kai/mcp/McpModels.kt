package com.blackbox.module.kai.mcp

import kotlinx.serialization.Serializable

@Serializable
data class McpServerConfig(
    val id: String,
    val name: String,
    val url: String,
    val enabled: Boolean = true
)

@Serializable
data class McpToolDefinition(
    val name: String,
    val description: String = "",
    val inputSchema: Map<String, String> = emptyMap()
)

@Serializable
data class McpTool(
    val definition: McpToolDefinition,
    val execute: suspend (Map<String, String>) -> McpCallToolResult
)

@Serializable
data class McpCallToolResult(
    val content: List<McpContent> = emptyList(),
    val isError: Boolean = false
)

@Serializable
data class McpContent(
    val type: String,
    val text: String? = null
)

@Serializable
data class McpToolsResult(
    val tools: List<McpToolDefinition> = emptyList()
)

@Serializable
data class McpException(val message: String) : Exception(message)

@Serializable
data class JsonRpcRequest(
    val jsonrpc: String = "2.0",
    val id: String? = null,
    val method: String,
    val params: Map<String, String> = emptyMap()
)

@Serializable
data class JsonRpcResponse(
    val jsonrpc: String = "2.0",
    val id: String? = null,
    val result: Map<String, String>? = null,
    val error: JsonRpcError? = null
)

@Serializable
data class JsonRpcError(
    val code: Int,
    val message: String,
    val data: Map<String, String>? = null
)

@Serializable
data class McpToolMetadata(
    val tags: List<String> = emptyList(),
    val requiresPermission: Boolean = false
)
