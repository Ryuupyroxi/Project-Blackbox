package com.blackbox.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ManifestSecurityAuditTest {

    @Test
    fun manifest_hasBackupDisabled() {
        val manifest = File("app/src/main/AndroidManifest.xml").readText()
        assertFalse("allowBackup should be false for security", manifest.contains("android:allowBackup=\"true\""))
    }

    @Test
    fun allServices_areNotExported() {
        val manifest = File("app/src/main/AndroidManifest.xml").readText()
        val serviceMatches = """<service[^>]*>""".toRegex().findAll(manifest)
        serviceMatches.forEach { match ->
            val serviceTag = match.value
            assertFalse(
                "Service should not be exported: $serviceTag",
                serviceTag.contains("android:exported=\"true\"")
            )
        }
    }

    @Test
    fun manifest_declaresFileProvider() {
        val manifest = File("app/src/main/AndroidManifest.xml").readText()
        assertTrue("FileProvider must be declared", manifest.contains("androidx.core.content.FileProvider"))
    }
}
