package com.blackbox.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blackbox.core.module.BlackboxModule
import com.blackbox.core.module.ModuleBus
import com.blackbox.core.module.ModuleEvent
import com.blackbox.core.module.ModuleRegistry
import com.blackbox.module.adt.AdtModuleImpl
import com.blackbox.module.anyclaw.AnyClawModuleImpl
import com.blackbox.module.kai.KaiModuleImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModuleLifecycleIntegrationTest {

    private lateinit var context: Context
    private lateinit var registry: ModuleRegistry

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        registry = ModuleRegistry(context)
    }

    @Test
    fun registerAllModules_allPresent() {
        registry.register(KaiModuleImpl())
        registry.register(AnyClawModuleImpl())
        registry.register(AdtModuleImpl())

        assertEquals(3, registry.list().size)
        assertNotNull(registry.get("kai"))
        assertNotNull(registry.get("anyclaw"))
        assertNotNull(registry.get("adt"))
    }

    @Test
    fun moduleIds_areUnique() {
        registry.register(KaiModuleImpl())
        registry.register(AnyClawModuleImpl())
        registry.register(AdtModuleImpl())

        val ids = registry.list().map { it.id() }.toSet()
        assertEquals(3, ids.size)
        assertTrue(ids.contains("kai"))
        assertTrue(ids.contains("anyclaw"))
        assertTrue(ids.contains("adt"))
    }

    @Test
    fun unregisterModule_removesFromRegistry() {
        registry.register(KaiModuleImpl())
        assertEquals(1, registry.list().size)
        registry.unregister("kai")
        assertEquals(0, registry.list().size)
        assertEquals(null, registry.get("kai"))
    }

    @Test
    fun register_replaceSameId_overwrites() {
        registry.register(KaiModuleImpl())
        registry.register(KaiModuleImpl())
        assertEquals(1, registry.list().size)
    }
}
