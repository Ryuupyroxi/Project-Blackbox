package com.blackbox.ai

interface ToolExecutor {
    suspend fun executeTool(toolName: String, argsJson: String, context: String): String
    fun getToolDisplayName(toolName: String): String
}
