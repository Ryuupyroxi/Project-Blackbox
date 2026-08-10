package com.blackbox.core.module

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ModuleBusTest {

    @Test
    fun publish_deliversToSubscriber() = runTest(UnconfinedTestDispatcher()) {
        val received = mutableListOf<ModuleEvent>()
        val job = launch { ModuleBus.events.take(1).collect { received.add(it) } }

        ModuleBus.publish(ModuleEvent("test", "type1", mapOf("k" to "v")))

        job.cancel()
        assertEquals(1, received.size)
        assertEquals("test", received[0].moduleId)
        assertEquals("type1", received[0].type)
        assertEquals("v", received[0].payload["k"])
    }

    @Test
    fun publish_multipleSubscribers_allReceive() = runTest(UnconfinedTestDispatcher()) {
        val received1 = mutableListOf<ModuleEvent>()
        val received2 = mutableListOf<ModuleEvent>()
        val job1 = launch { ModuleBus.events.take(1).collect { received1.add(it) } }
        val job2 = launch { ModuleBus.events.take(1).collect { received2.add(it) } }

        ModuleBus.publish(ModuleEvent("multi", "broadcast"))

        job1.cancel(); job2.cancel()
        assertEquals(1, received1.size)
        assertEquals(1, received2.size)
        assertEquals("broadcast", received1[0].type)
        assertEquals("broadcast", received2[0].type)
    }

    @Test
    fun publish_emptyPayload_defaultsToEmptyMap() = runTest(UnconfinedTestDispatcher()) {
        val received = mutableListOf<ModuleEvent>()
        val job = launch { ModuleBus.events.take(1).collect { received.add(it) } }

        ModuleBus.publish(ModuleEvent("e", "t"))

        job.cancel()
        assertEquals(emptyMap<String, String>(), received[0].payload)
    }

    @Test
    fun bus_sharedFlow_replayZero() = runTest(UnconfinedTestDispatcher()) {
        val received = mutableListOf<ModuleEvent>()
        ModuleBus.publish(ModuleEvent("late", "sub"))
        val job = launch { ModuleBus.events.take(1).collect { received.add(it) } }
        // SharedFlow with replay=0 should not emit to late collector without new publish
        job.cancel()
        // After a second publish, late collector should receive
        ModuleBus.publish(ModuleEvent("late2", "sub2"))
        val received2 = mutableListOf<ModuleEvent>()
        val job2 = launch { ModuleBus.events.take(1).collect { received2.add(it) } }
        job2.cancel()
        assertEquals("sub2", received2[0].type)
    }
}
