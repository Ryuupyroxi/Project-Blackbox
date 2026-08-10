package com.blackbox.module.adt.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class AdtNativeLoaderTest {

    private lateinit var loader: AdtNativeLoader
    private lateinit var nativeDir: File

    @Before
    fun setUp() {
        nativeDir = File.createTempFile("native", "").apply { delete(); mkdir(); }
        loader = AdtNativeLoader(FakeContext(nativeDir))
    }

    @Test
    fun listExpectedLibraries_returnsExactList() {
        val libs = loader.listExpectedLibraries()
        assertEquals(7, libs.size)
        assertTrue(libs.contains("libllama.so"))
        assertTrue(libs.contains("libwhisper.so"))
        assertTrue(libs.contains("libkiwix.so"))
        assertTrue(libs.contains("libstable_diffusion.so"))
        assertTrue(libs.contains("liblitert.so"))
        assertTrue(libs.contains("libtensorflowlite.so"))
        assertTrue(libs.contains("libopencv.so"))
    }

    @Test
    fun missingLibraries_emptyDir_returnsAll() {
        assertEquals(loader.listExpectedLibraries().toSet(), loader.missingLibraries().toSet())
    }

    @Test
    fun missingLibraries_partialPresence_returnsMissing() {
        loader.listExpectedLibraries().take(2).forEach { lib ->
            File(nativeDir, lib).writeText("fake")
        }
        val missing = loader.missingLibraries()
        assertEquals(5, missing.size)
        assertTrue(missing.none { loader.listExpectedLibraries().take(2).contains(it) })
    }

    @Test
    fun isReady_falseWhenNoMarker() {
        assertTrue(!loader.isReady())
    }

    @Test
    fun markReady_setsMarker() {
        loader.markReady()
        assertTrue(loader.isReady())
    }

    private class FakeContext(private val nativeDir: File) : android.content.Context {
        override fun getApplicationContext(): android.content.Context = this
        override fun getPackageName(): String = "com.blackbox.test"
        override fun getFilesDir(): java.io.File = nativeDir
        override fun getClassLoader(): ClassLoader = javaClass.classLoader!!
        override fun getSystemService(name: String): Any = throw UnsupportedOperationException()
        override fun <T> getSystemService(serviceClass: Class<T>): T = throw UnsupportedOperationException()
        override fun getMainLooper(): android.os.Looper = throw UnsupportedOperationException()
    }
}
