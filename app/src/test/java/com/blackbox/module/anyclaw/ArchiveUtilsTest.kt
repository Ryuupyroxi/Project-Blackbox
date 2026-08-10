package com.blackbox.module.anyclaw.proot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ArchiveUtilsTest {

    @Test
    fun extractZip_roundTrip_preservesFiles() {
        val tmp = File.createTempFile("archive", ".zip")
        ZipOutputStream(tmp.outputStream()).use { zos ->
            zos.putNextEntry(ZipEntry("hello.txt"))
            zos.write("world".toByteArray())
            zos.closeEntry()
            zos.putNextEntry(ZipEntry("sub/nested.txt"))
            zos.write("nested".toByteArray())
            zos.closeEntry()
        }

        val target = File.createTempFile("extract", "").apply { delete(); mkdir(); }
        val utils = ArchiveUtils(FakeContext())
        utils.extractZip(tmp, target)

        val hello = File(target, "hello.txt")
        val nested = File(target, "sub/nested.txt")
        assertTrue(hello.exists())
        assertEquals("world", hello.readText())
        assertTrue(nested.exists())
        assertEquals("nested", nested.readText())
    }

    @Test
    fun extractZip_emptyZip_createsTargetDir() {
        val tmp = File.createTempFile("empty", ".zip")
        ZipOutputStream(tmp.outputStream()).use { zos ->
            // no entries
        }
        val target = File.createTempFile("extract", "").apply { delete(); mkdir(); }
        val utils = ArchiveUtils(FakeContext())
        utils.extractZip(tmp, target)
        assertTrue(target.isDirectory)
        assertEquals(0, target.listFiles()?.size ?: -1)
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
