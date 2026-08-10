package com.blackbox.runtime

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProotSupervisorTest {

    private lateinit var supervisor: ProotSupervisor

    @Before
    fun setUp() {
        supervisor = ProotSupervisor(FakeContext())
    }

    @Test
    fun initialState_isStopped() = runTest(UnconfinedTestDispatcher()) {
        assertEquals(ProotState.Stopped, supervisor.state.first())
    }

    @Test
    fun start_transitionsToRunning() = runTest(UnconfinedTestDispatcher()) {
        supervisor.start()
        val state = supervisor.state.first()
        assertTrue(state is ProotState.Running)
        assertEquals("stub-proot", (state as ProotState.Running).session)
    }

    @Test
    fun stop_transitionsToStopped() = runTest(UnconfinedTestDispatcher()) {
        supervisor.start()
        supervisor.stop()
        assertEquals(ProotState.Stopped, supervisor.state.first())
    }

    @Test
    fun healthCheck_whenStopped_returnsFalse() = runTest(UnconfinedTestDispatcher()) {
        assertFalse(supervisor.healthCheck())
    }

    @Test
    fun healthCheck_whenRunning_returnsTrue() = runTest(UnconfinedTestDispatcher()) {
        supervisor.start()
        assertTrue(supervisor.healthCheck())
    }

    @Test
    fun execute_whenRunning_returnsOk() = runTest(UnconfinedTestDispatcher()) {
        supervisor.start()
        val result = supervisor.execute("echo hello")
        assertTrue(result is ShellResult.Ok)
        assertEquals("stub-echo hello", (result as ShellResult.Ok).output)
    }

    @Test
    fun execute_whenStopped_returnsErr() = runTest(UnconfinedTestDispatcher()) {
        val result = supervisor.execute("echo hello")
        assertTrue(result is ShellResult.Err)
        assertEquals("proot not running", (result as ShellResult.Err).error)
    }

    @Test
    fun start_thenExecute_thenStop_cycle() = runTest(UnconfinedTestDispatcher()) {
        supervisor.start()
        assertTrue(supervisor.healthCheck())
        val result = supervisor.execute("ls /")
        assertTrue(result is ShellResult.Ok)
        supervisor.stop()
        assertFalse(supervisor.healthCheck())
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
