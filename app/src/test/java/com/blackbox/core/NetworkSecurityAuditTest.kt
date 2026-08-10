package com.blackbox.core

import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class NetworkSecurityAuditTest {

    @Test
    fun noCleartextTrafficFlag() {
        val manifest = File("app/src/main/AndroidManifest.xml").readText()
        assertFalse(
            "Manifest should not enable cleartext traffic",
            manifest.contains("android:usesCleartextTraffic=\"true\"")
        )
    }

    @Test
    fun noHardcodedIpAddresses() {
        val forbidden = File("app/src/main").walkTopDown()
            .filter { it.extension == "kt" || it.extension == "xml" }

        val ipPattern = """\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b""".toRegex()
        val matches = mutableListOf<String>()

        forbidden.forEach { file ->
            file.forEachLine { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("//") || trimmed.startsWith("*")) return@forEachLine
                ipPattern.find(trimmed)?.let {
                    matches += "${file.name}: ${it.value}"
                }
            }
        }

        assertTrue("Hardcoded IP addresses found: ${matches.joinToString("\n")}", matches.isEmpty())
    }

    @Test
    fun noPlaintextApiKeys() {
        val apiKeyPattern = """(api[_-]?key|apikey|token|secret)\s*[:=]\s*["'][A-Za-z0-9+/]{20,}["']""".toRegex(RegexOption.IGNORE_CASE)
        val forbidden = File("app/src/main").walkTopDown()
            .filter { it.extension == "kt" || it.extension == "xml" }

        val matches = mutableListOf<String>()
        forbidden.forEach { file ->
            file.forEachLine { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("//") || trimmed.startsWith("*")) return@forEachLine
                apiKeyPattern.find(trimmed)?.let {
                    matches += "${file.name}: ${it.value}"
                }
            }
        }

        assertTrue("Plaintext API keys found: ${matches.joinToString("\n")}", matches.isEmpty())
    }
}
