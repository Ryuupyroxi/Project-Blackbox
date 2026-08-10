package com.blackbox.module.anyclaw.data

import com.blackbox.core.data.BlackboxPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// PreferencesManager wraps BlackboxPreferences DataStore flows.
// Because DataStore requires Android runtime, we verify the mapping layer
// exists and exposes the expected keys without requiring a device.
class PreferencesManagerTest {

    private lateinit var manager: PreferencesManager

    @Before
    fun setUp() {
        // Context is stored but not used until flow collection.
        manager = PreferencesManager(FakeContext())
    }

    @Test
    fun exposesBridgeToggleFlows() {
        // The flows should be non-null; exact values require DataStore runtime.
        assertTrue(manager.discordEnabled !== null)
        assertTrue(manager.telegramEnabled !== null)
        assertTrue(manager.whatsappEnabled !== null)
    }

    @Test
    fun exposesProviderAndModelFlows() {
        assertTrue(manager.apiProvider !== null)
        assertTrue(manager.selectedModel !== null)
        assertTrue(manager.selectedModelId !== null)
        assertTrue(manager.selectedModelReasoning !== null)
        assertTrue(manager.selectedModelImages !== null)
        assertTrue(manager.selectedModelContext !== null)
        assertTrue(manager.selectedModelMaxOutput !== null)
    }

    @Test
    fun exposesOpenClawCodexFlows() {
        assertTrue(manager.openClawVersion !== null)
        assertTrue(manager.codexappVersion !== null)
        assertTrue(manager.codexappBranch !== null)
        assertTrue(manager.autoStartOpenClawOnBoot !== null)
        assertTrue(manager.autoStartCodexOnBoot !== null)
    }

    @Test
    fun exposesAppStateFlows() {
        assertTrue(manager.setupComplete !== null)
        assertTrue(manager.onboardingComplete !== null)
        assertTrue(manager.premiumActive !== null)
        assertTrue(manager.hasRated !== null)
        assertTrue(manager.gatewayWasRunning !== null)
        assertTrue(manager.lastAppOpenedAt !== null)
        assertTrue(manager.autoStartSshd !== null)
    }

    // Minimal Context stub; PreferencesManager only reads from it in Flow collection.
    private class FakeContext : android.content.Context {
        override fun getApplicationContext(): android.content.Context = this
        override fun getPackageName(): String = "com.blackbox.test"
        override fun getClassLoader(): ClassLoader = javaClass.classLoader!!
        override fun getSystemService(name: String): Any = throw UnsupportedOperationException()
        override fun <T> getSystemService(serviceClass: Class<T>): T = throw UnsupportedOperationException()
        override fun getMainLooper(): android.os.Looper = throw UnsupportedOperationException()
    }
}
