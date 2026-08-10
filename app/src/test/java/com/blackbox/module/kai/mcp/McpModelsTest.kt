package com.blackbox.module.kai.mcp

import com.blackbox.core.module.kai.mcp.McpServerConfig
import com.blackbox.core.module.kai.mcp.McpTransport
import com.blackbox.core.module.kai.mcp.SseMcpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class McpModelsTest {

    @Test
    fun sseMcpClient_defaultsAreStable() {
        val client = SseMcpClient()
        assertTrue(client.url.isEmpty())
    }

    @Test
    fun mcpServerConfig_defaultTransport() {
        val cfg = McpServerConfig(name = "t1", command = "python", transport = McpTransport.STDIO)
        assertEquals("t1", cfg.name)
        assertEquals("python", cfg.command)
        assertEquals(McpTransport.STDIO, cfg.transport)
    }

    @Test
    fun mcpServerConfig_copyPreservesFields() {
        val original = McpServerConfig(name = "s1", url = "http://localhost:3001/sse", transport = McpTransport.SSE, command = null, args = emptyList())
        val copy = original.copy(url = "http://localhost:3002/sse")
        assertEquals("s1", copy.name)
        assertEquals("http://localhost:3002/sse", copy.url)
        assertEquals(McpTransport.SSE, copy.transport)
    }
}
