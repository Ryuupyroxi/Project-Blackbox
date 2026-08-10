package com.blackbox.module.adt.runtime

import com.blackbox.core.module.adt.bridge.AdtManifestMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdtModuleLoaderTest {

    @Test
    fun loadServices_returnsCatalog() {
        val loader = AdtModuleLoader(FakeContext())
        val services = loader.loadServices()
        assertEquals(AdtManifestMapper.services(), services)
    }

    @Test
    fun loadBootReceivers_returnsCatalog() {
        val loader = AdtModuleLoader(FakeContext())
        val receivers = loader.loadBootReceivers()
        assertEquals(AdtManifestMapper.bootReceivers(), receivers)
    }

    @Test
    fun stopService_doesNotThrow() {
        val loader = AdtModuleLoader(FakeContext())
        val svc = loader.loadServices().first()
        loader.stopService(svc)
    }

    @Test
    fun startAllServices_thenStopAllServices_doesNotThrow() {
        val loader = AdtModuleLoader(FakeContext())
        loader.startAllServices()
        loader.stopAllServices()
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
