package com.blackbox.module.anyclaw.proot

import com.blackbox.core.module.ModuleBus
import com.blackbox.core.module.ModuleEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProcessManagerTest {

    private lateinit var manager: ProcessManager
    private lateinit var prefs: com.blackbox.module.anyclaw.data.PreferencesManager

    @Before
    fun setUp() {
        prefs = com.blackbox.module.anyclaw.data.PreferencesManager(FakeContext())
        manager = ProcessManager(FakeContext(), prefs)
    }

    @Test
    fun startOpenCode_publishesEvent() = runTest(UnconfinedTestDispatcher()) {
        val received = mutableListOf<ModuleEvent>()
        val job = launch { ModuleBus.events.take(1).collect { received.add(it) } }
        manager.startOpenCode()
        job.cancel()
        assertEquals("anyclaw", received[0].moduleId)
        assertEquals("openclaw_start_requested", received[0].type)
    }

    @Test
    fun stopOpenCode_publishesEvent() = runTest(UnconfinedTestDispatcher()) {
        val received = mutableListOf<ModuleEvent>()
        val job = launch { ModuleBus.events.take(1).collect { received.add(it) } }
        manager.stopOpenCode()
        job.cancel()
        assertEquals("openclaw_stop_requested", received[0].type)
    }

    @Test
    fun startCodexWebLocal_publishesEvent() = runTest(UnconfinedTestDispatcher()) {
        val received = mutableListOf<ModuleEvent>()
        val job = launch { ModuleBus.events.take(1).collect { received.add(it) } }
        manager.startCodexWebLocal()
        job.cancel()
        assertEquals("codex_start_requested", received[0].type)
    }

    @Test
    fun stopSshd_publishesEvent() = runTest(UnconfinedTestDispatcher()) {
        val received = mutableListOf<ModuleEvent>()
        val job = launch { ModuleBus.events.take(1).collect { received.add(it) } }
        manager.stopSshd()
        job.cancel()
        assertEquals("sshd_stop_requested", received[0].type)
    }

    @Test
    fun startHermesWebUi_publishesEvent() = runTest(UnconfinedTestDispatcher()) {
        val received = mutableListOf<ModuleEvent>()
        val job = launch { ModuleBus.events.take(1).collect { received.add(it) } }
        manager.startHermesWebUi()
        job.cancel()
        assertEquals("hermes_start_requested", received[0].type)
    }

    @Test
    fun startSshdInBackground_publishesEvent() = runTest(UnconfinedTestDispatcher()) {
        val received = mutableListOf<ModuleEvent>()
        val job = launch { ModuleBus.events.take(1).collect { received.add(it) } }
        manager.startSshdInBackground()
        job.cancel()
        assertEquals("sshd_start_requested", received[0].type)
    }

    private class FakeContext : android.content.Context {
        override fun getApplicationContext(): android.content.Context = this
        override fun getPackageName(): String = "com.blackbox.test"
        override fun getClassLoader(): ClassLoader = javaClass.classLoader!!
        override fun getSystemService(name: String): Any = throw UnsupportedOperationException()
        override fun <T> getSystemService(serviceClass: Class<T>): T = throw UnsupportedOperationException()
        override fun getMainLooper(): android.os.Looper = throw UnsupportedOperationException()
    }
}
