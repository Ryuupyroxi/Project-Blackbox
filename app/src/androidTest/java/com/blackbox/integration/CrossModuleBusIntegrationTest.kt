package com.blackbox.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blackbox.core.module.ModuleBus
import com.blackbox.core.module.ModuleEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class CrossModuleBusIntegrationTest {

    @Test
    fun publishFromModule_receivedByBus() = runTest(UnconfinedTestDispatcher()) {
        val received = mutableListOf<ModuleEvent>()
        val job = launch { ModuleBus.events.take(1).collect { received.add(it) } }

        ModuleBus.publish(ModuleEvent("integration-test", "cross_module_event", mapOf("key" to "value")))

        job.cancel()
        assertEquals(1, received.size)
        assertEquals("integration-test", received[0].moduleId)
        assertEquals("cross_module_event", received[0].type)
        assertEquals("value", received[0].payload["key"])
    }

    @Test
    fun multiplePublishes_allReceived() = runTest(UnconfinedTestDispatcher()) {
        val received = mutableListOf<ModuleEvent>()
        val job = launch { ModuleBus.events.take(3).collect { received.add(it) } }

        ModuleBus.publish(ModuleEvent("m1", "event1"))
        ModuleBus.publish(ModuleEvent("m2", "event2", mapOf("a" to "1")))
        ModuleBus.publish(ModuleEvent("m3", "event3", mapOf("b" to "2")))

        job.cancel()
        assertEquals(3, received.size)
        assertEquals("event1", received[0].type)
        assertEquals("event2", received[1].type)
        assertEquals("event3", received[2].type)
    }

    @Test
    fun publish_emptyPayload_defaultsCorrectly() = runTest(UnconfinedTestDispatcher()) {
        val received = mutableListOf<ModuleEvent>()
        val job = launch { ModuleBus.events.take(1).collect { received.add(it) } }

        ModuleBus.publish(ModuleEvent("m", "t"))

        job.cancel()
        assertEquals(emptyMap<String, String>(), received[0].payload)
    }

    @Test
    fun moduleBus_isSharedAcrossInstances() = runTest(UnconfinedTestDispatcher()) {
        // ModuleBus is an object; publishing from one collector context should
        // still be observable in another collector context.
        val received1 = mutableListOf<ModuleEvent>()
        val job1 = launch { ModuleBus.events.take(1).collect { received1.add(it) } }

        ModuleBus.publish(ModuleEvent("shared", "bus_test"))

        job1.cancel()
        assertEquals(1, received1.size)
        assertEquals("shared", received1[0].moduleId)
    }
}
