package com.blackbox.module.kai.tools

import com.blackbox.core.module.kai.tools.ToolDescriptor
import com.blackbox.core.module.kai.tools.ToolRequest
import com.blackbox.core.module.kai.tools.ParamSchema
import org.junit.Assert.assertEquals
import org.junit.Test

class KaiToolExecutorContractTest {

    @Test
    fun toolDescriptor_defaultsAreStable() {
        val desc = ToolDescriptor(id = "d1", name = "D1", description = "", schema = ParamSchema("object", emptyMap()))
        assertEquals("d1", desc.id)
        assertEquals("D1", desc.name)
    }

    @Test
    fun toolRequest_defaultsAreStable() {
        val req = ToolRequest(toolId = "t1", params = emptyMap())
        assertEquals("t1", req.toolId)
        assertTrue(req.params.isEmpty())
    }

    @Test
    fun paramSchema_defaultsAreStable() {
        val schema = ParamSchema(type = "object", properties = emptyMap())
        assertEquals("object", schema.type)
        assertTrue(schema.properties.isEmpty())
    }

    @Test
    fun buildModels_defaultsAreStable() {
        val build = com.blackbox.core.module.kai.build.BuildEnvironment()
        assertTrue(build.isProot)
        assertTrue(build.isTermux)
        assertTrue(build.env.isEmpty())
    }
}
