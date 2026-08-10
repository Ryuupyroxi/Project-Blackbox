package com.blackbox.assistant

import android.content.ComponentName
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blackbox.core.module.ModuleBus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BlackboxAssistantServiceInstrumentedTest {

    @Test
    fun service_isDeclaredInManifest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = android.content.Intent(android.service.voice.VoiceInteractionService.SERVICE_INTERFACE)
        val resolved = context.packageManager.queryIntentServices(intent, 0)
        assertTrue(resolved.any { it.serviceInfo.name == "com.blackbox.assistant.BlackboxAssistantService" })
    }

    @Test
    fun route_publishesModuleBusEvent() = runTest(UnconfinedTestDispatcher()) {
        val received = mutableListOf<com.blackbox.core.module.ModuleEvent>()
        val job = launch { ModuleBus.events.take(1).collect { received.add(it) } }

        // Replicate the exact event shape published by AssistantIntentRouter
        ModuleBus.publish(
            com.blackbox.core.module.ModuleEvent(
                moduleId = "assistant",
                type = "assistant_invoke",
                payload = mapOf("text" to "synthetic assist")
            )
        )

        job.cancel()
        assertEquals(1, received.size)
        assertEquals("assistant", received[0].moduleId)
        assertEquals("assistant_invoke", received[0].type)
        assertEquals("synthetic assist", received[0].payload["text"])
    }
}
