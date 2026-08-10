package com.blackbox.module.kai.chat

import com.blackbox.core.module.kai.data.BlackboxMessage
import com.blackbox.core.module.kai.data.Model
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KaiAiProviderTest {

    @Test
    fun stream_returnsEmptyFlow() = runTest(UnconfinedTestDispatcher()) {
        val provider = KaiAiProvider()
        val model = Model(id = "m1", name = "Test")
        val messages = listOf(BlackboxMessage(role = "user", text = "hi"))
        val flow: Flow<String> = provider.stream(model, messages)
        // Empty flow should emit nothing
        val emitted = mutableListOf<String>()
        runTest(UnconfinedTestDispatcher()) {
            flow.collect { emitted.add(it) }
        }
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun stream_acceptsAnyModelAndMessages() = runTest(UnconfinedTestDispatcher()) {
        val provider = KaiAiProvider()
        val model = Model(id = "x", name = "X")
        val messages = listOf(
            BlackboxMessage(role = "user", text = "hello"),
            BlackboxMessage(role = "assistant", text = "hi"),
            BlackboxMessage(role = "user", text = "there")
        )
        val flow: Flow<String> = provider.stream(model, messages)
        val emitted = mutableListOf<String>()
        runTest(UnconfinedTestDispatcher()) {
            flow.collect { emitted.add(it) }
        }
        assertTrue(emitted.isEmpty())
    }
}
