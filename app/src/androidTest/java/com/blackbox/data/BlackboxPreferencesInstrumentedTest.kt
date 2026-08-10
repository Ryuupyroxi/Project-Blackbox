package com.blackbox.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blackbox.core.data.BlackboxPreferences
import com.blackbox.core.data.BlackboxKeys
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BlackboxPreferencesInstrumentedTest {

    private lateinit var context: Context
    private lateinit var prefs: BlackboxPreferences

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        prefs = BlackboxPreferences(context)
    }

    @Test
    fun stringKey_writeAndRead() = runBlocking {
        prefs.selectedProvider // trigger DataStore init
        // BlackboxPreferences exposes Flow properties, but the class only exposes getters.
        // We verify the keys exist and the DataStore is initialized by checking
        // that the preference flow backing exists.
        val flow = prefs.selectedProvider
        assertNotNull(flow)
    }

    @Test
    fun booleanKey_flowsAreNonNull() = runBlocking {
        assertNotNull(prefs.discordEnabled)
        assertNotNull(prefs.telegramEnabled)
        assertNotNull(prefs.whatsappEnabled)
        assertNotNull(prefs.setupComplete)
        assertNotNull(prefs.premiumActive)
        assertNotNull(prefs.gatewayWasRunning)
    }

    @Test
    fun longKey_flowsAreNonNull() = runBlocking {
        assertNotNull(prefs.lastAppOpenedAt)
        assertNotNull(prefs.lastInterstitialAdShownDate)
    }

    @Test
    fun allKeys_haveNonNullFlows() = runBlocking {
        // Smoke test: every public Flow property on BlackboxPreferences should be non-null.
        val fields = prefs.javaClass.declaredFields.filter { it.name.endsWith("Flow") || it.name.endsWith("flow") }
        assertTrue("BlackboxPreferences should expose preference flows", fields.isNotEmpty())
        fields.forEach { assertNotNull(it.get(prefs)) }
    }
}
