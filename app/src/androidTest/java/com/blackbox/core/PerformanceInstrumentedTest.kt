package com.blackbox.core

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blackbox.module.adt.runtime.AdtUnifiedRuntimeService
import com.blackbox.module.anyclaw.service.GatewayService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class PerformanceInstrumentedTest {

    @Test
    fun serviceStartLatency_under500ms() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, GatewayService::class.java)
        val start = System.nanoTime()
        context.startService(intent)
        val elapsed = (System.nanoTime() - start) / 1_000_000
        context.stopService(intent)
        assertTrue("Service start took ${elapsed}ms, expected < 500ms", elapsed < 500)
    }

    @Test
    fun moduleBus_publishSubscribeLatency_under50ms() = runTest(UnconfinedTestDispatcher()) {
        val received = mutableListOf<Long>()
        val job = launch { com.blackbox.core.module.ModuleBus.events.take(5).collect { event ->
            val latency = System.nanoTime() - event.timestamp
            received.add(latency / 1_000_000)
        } }

        repeat(5) { i ->
            com.blackbox.core.module.ModuleBus.publish(
                com.blackbox.core.module.ModuleEvent("perf_instr", "latency_$i", timestamp = System.nanoTime())
            )
        }

        job.cancel()
        assertTrue("All publish latencies should be under 50ms", received.all { it < 50 })
    }

    @Test
    fun blackboxDatabase_insertAndQuery_performance() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(context, com.blackbox.core.data.BlackboxDatabase::class.java, "perf-db")
            .allowMainThreadQueries()
            .build()
        val dao = db.channelConversationDao()

        val start = System.nanoTime()
        repeat(100) { i ->
            dao.insert(com.blackbox.core.data.ChannelConversationEntity(id = "perf_$i", channelType = "discord", channelId = "$i", title = "Perf $i", createdAt = i.toLong(), updatedAt = i.toLong()))
        }
        dao.getAll()
        val elapsed = (System.nanoTime() - start) / 1_000_000
        db.close()
        assertTrue("100 inserts + 1 query took ${elapsed}ms, expected < 1000ms", elapsed < 1000)
    }

    private fun runBlocking(block: suspend () -> Unit) = kotlinx.coroutines.runBlocking { block() }
}
