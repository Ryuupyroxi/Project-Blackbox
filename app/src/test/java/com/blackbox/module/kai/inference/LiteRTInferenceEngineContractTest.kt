package com.blackbox.module.kai.inference

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LiteRTInferenceEngineContractTest {

    @Test
    fun newEngine_defaultState_isNotLoaded() = runTest(UnconfinedTestDispatcher()) {
        val engine = LiteRTInferenceEngine()
        assertFalse(engine.initialize("model-id"))
        // chat on unloaded engine should still return empty flow
        val flow = engine.chat(emptyList())
        val emitted = mutableListOf<String>()
        runTest(UnconfinedTestDispatcher()) {
            flow.collect { emitted.add(it) }
        }
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun engineState_defaultsAreStable() {
        val state = LiteRTInferenceEngine.EngineState()
        assertFalse(state.loaded)
        assertEquals("", state.modelId)
    }

    @Test
    fun initialize_returnsFalse_onFirstCall() = runTest(UnconfinedTestDispatcher()) {
        val engine = LiteRTInferenceEngine()
        assertEquals(false, engine.initialize("model-1"))
    }

    @Test
    fun release_doesNotThrow() = runTest(UnconfinedTestDispatcher()) {
        val engine = LiteRTInferenceEngine()
        engine.initialize("m1")
        engine.release() // should not throw
    }
}
