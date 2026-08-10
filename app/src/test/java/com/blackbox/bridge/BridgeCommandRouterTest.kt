package com.blackbox.bridge

import com.blackbox.core.module.anyclaw.bridge.DeviceBridge
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class BridgeCommandRouterTest {

    @Test
    fun dispatch_returnsErrorForUnknownCommand() {
        val bridge = DeviceBridge(FakeContext())
        val router = BridgeCommandRouter(bridge)
        val result = router.dispatch("unknown_cmd", JSONArray())
        assertFalse(result.optBoolean("ok", true))
        assertEquals("unknown command: unknown_cmd", result.optString("error", ""))
    }

    @Test
    fun dispatch_emptyCommand_returnsError() {
        val bridge = DeviceBridge(FakeContext())
        val router = BridgeCommandRouter(bridge)
        val result = router.dispatch("", JSONArray())
        assertFalse(result.optBoolean("ok", true))
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
