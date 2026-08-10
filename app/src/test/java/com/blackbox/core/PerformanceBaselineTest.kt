package com.blackbox.core

import com.blackbox.core.module.ModuleBus
import com.blackbox.core.module.ModuleEvent
import com.blackbox.core.module.ModuleRegistry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PerformanceBaselineTest {

    @Test
    fun moduleRegistry_register1000Modules_completesUnder100ms() {
        val registry = ModuleRegistry(FakeContext())
        val start = System.nanoTime()
        repeat(1000) { i ->
            registry.register(object : com.blackbox.core.module.BlackboxModule {
                override fun id(): String = "perf_$i"
                override fun onLoad(context: android.content.Context) {}
                override fun onUnload(context: android.content.Context) {}
                override fun manifest(): com.blackbox.core.module.ModuleManifest? = null
            })
        }
        val elapsed = (System.nanoTime() - start) / 1_000_000
        assertTrue("Registering 1000 modules took ${elapsed}ms, expected < 100ms", elapsed < 100)
    }

    @Test
    fun moduleBus_publishSubscribeLatency_under50ms() = runTest(UnconfinedTestDispatcher()) {
        val received = mutableListOf<Long>()
        val job = launch {
            ModuleBus.events.take(10).collect { event ->
                val latency = System.nanoTime() - event.timestamp
                received.add(latency / 1_000_000)
            }
        }

        repeat(10) { i ->
            ModuleBus.publish(ModuleEvent("perf", "latency_$i", timestamp = System.nanoTime()))
        }

        job.cancel()
        assertTrue("All publish latencies should be under 50ms", received.all { it < 50 })
    }

    @Test
    fun moduleBus_1000Events_noDrops() = runTest(UnconfinedTestDispatcher()) {
        val received = mutableListOf<ModuleEvent>()
        val job = launch { ModuleBus.events.take(1000).collect { received.add(it) } }

        repeat(1000) { i ->
            ModuleBus.publish(ModuleEvent("perf", "event_$i", mapOf("index" to i.toString())))
        }

        job.cancel()
        assertEquals(1000, received.size)
    }

    @Test
    fun secretStore_encryptDecrypt_throughput() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val store = com.blackbox.core.data.SecretStore(context)
        val start = System.nanoTime()
        repeat(100) {
            store.save("perf_key_$it", "perf_value_$it")
            store.load("perf_key_$it")
        }
        val elapsed = (System.nanoTime() - start) / 1_000_000
        assertTrue("100 encrypt/decrypt ops took ${elapsed}ms, expected < 1000ms", elapsed < 1000)
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
