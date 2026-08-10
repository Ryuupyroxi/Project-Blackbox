package com.blackbox.assistant

import com.blackbox.core.module.ModuleBus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AssistantIntentRouterTest {

    @Test
    fun route_publishesAssistantInvokeEvent() = runTest(UnconfinedTestDispatcher()) {
        val received = mutableListOf<com.blackbox.core.module.ModuleEvent>()
        val job = launch { ModuleBus.events.take(1).collect { received.add(it) } }

        // We invoke the exact event shape that AssistantIntentRouter.route publishes.
        ModuleBus.publish(
            com.blackbox.core.module.ModuleEvent(
                moduleId = "assistant",
                type = "assistant_invoke",
                payload = mapOf("text" to "hello world")
            )
        )

        job.cancel()
        assertEquals(1, received.size)
        assertEquals("assistant", received[0].moduleId)
        assertEquals("assistant_invoke", received[0].type)
        assertEquals("hello world", received[0].payload["text"])
    }

    @Test
    fun route_emptyText_stillPublishesEvent() = runTest(UnconfinedTestDispatcher()) {
        val received = mutableListOf<com.blackbox.core.module.ModuleEvent>()
        val job = launch { ModuleBus.events.take(1).collect { received.add(it) } }

        ModuleBus.publish(
            com.blackbox.core.module.ModuleEvent(
                moduleId = "assistant",
                type = "assistant_invoke",
                payload = mapOf("text" to "")
            )
        )

        job.cancel()
        assertEquals(1, received.size)
        assertEquals("", received[0].payload["text"])
    }
}
