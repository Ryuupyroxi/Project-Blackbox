package com.blackbox.core

import java.io.File
import java.lang.reflect.Method
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReflectionAuditTest {

    @Test
    fun appLayer_hasNoReflectionCalls() {
        val forbidden = listOf("Class.forName", "Method.invoke", "getDeclaredField", "getDeclaredMethod", "isAssignableFrom")
        val matches = mutableListOf<String>()

        val base = File("app/src/main")
        base.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            file.forEachLine { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("//") || trimmed.startsWith("*")) return@forEachLine
                forbidden.forEach { pattern ->
                    if (trimmed.contains(pattern)) {
                        matches += "${file.relativeTo(base)}: $pattern"
                    }
                }
            }
        }

        assertTrue(
            "Reflection calls found in app layer: ${matches.joinToString("\n")}",
            matches.isEmpty()
        )
    }
}
