package com.blackbox.core.module

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ModuleEventTest {

    @Test
    fun equalsAndHashCode() = runTest(UnconfinedTestDispatcher()) {
        val a = ModuleEvent("m", "type", mapOf("k" to "v"))
        val b = ModuleEvent("m", "type", mapOf("k" to "v"))
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun copy_createsNewInstance() = runTest(UnconfinedTestDispatcher()) {
        val original = ModuleEvent("m", "type", mapOf("k" to "v"))
        val copy = original.copy(type = "newType")
        assertEquals("newType", copy.type)
        assertEquals("m", copy.moduleId)
    }

    @Test
    fun payloadIsIsolatedBetweenInstances() = runTest(UnconfinedTestDispatcher()) {
        val received = mutableListOf<ModuleEvent>()
        val job = launch { ModuleBus.events.take(1).collect { received.add(it) } }
        ModuleBus.publish(ModuleEvent("m", "t", mapOf("a" to "1")))
        job.cancel()
        val received2 = mutableListOf<ModuleEvent>()
        val job2 = launch { ModuleBus.events.take(1).collect { received2.add(it) } }
        ModuleBus.publish(ModuleEvent("m", "t", mapOf("b" to "2")))
        job2.cancel()
        assertEquals("1", received[0].payload["a"])
        assertEquals("2", received2[0].payload["b"])
    }
}
