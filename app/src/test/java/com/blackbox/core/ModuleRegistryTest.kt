package com.blackbox.core.module

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ModuleRegistryTest {

    private lateinit var registry: ModuleRegistry

    @Before
    fun setUp() {
        // ModuleRegistry only stores Context to pass to module lifecycle methods;
        // it does not call Context APIs itself. TestContext is sufficient.
        registry = ModuleRegistry(TestContext())
    }

    @Test
    fun register_addsModule() {
        val module = FakeModule("test")
        registry.register(module)
        assertEquals(module, registry.get("test"))
    }

    @Test
    fun register_replaceSameId_overwrites() {
        val first = FakeModule("same")
        val second = FakeModule("same")
        registry.register(first)
        registry.register(second)
        assertEquals(second, registry.get("same"))
    }

    @Test
    fun unregister_removesAndCallsOnUnload() {
        var unloaded = false
        val module = object : BlackboxModule {
            override fun id() = "temp"
            override fun version() = "1.0"
            override fun description() = ""
            override fun onLoad(context: Context, classLoader: ClassLoader) {}
            override fun onUnload() { unloaded = true }
        }
        registry.register(module)
        registry.unregister("temp")
        assertNull(registry.get("temp"))
        assertEquals(true, unloaded)
    }

    @Test
    fun unregister_missingId_noOp() {
        registry.unregister("does_not_exist")
        assertEquals(0, registry.list().size)
    }

    @Test
    fun list_returnsRegisteredModules() {
        registry.register(FakeModule("a"))
        registry.register(FakeModule("b"))
        val list = registry.list()
        assertEquals(2, list.size)
        assertEquals("a", list[0].id())
        assertEquals("b", list[1].id())
    }

    private class FakeModule(override val id: String) : BlackboxModule {
        override fun version() = "1.0"
        override fun description() = ""
        override fun onLoad(context: Context, classLoader: ClassLoader) {}
        override fun onUnload() {}
    }
}
