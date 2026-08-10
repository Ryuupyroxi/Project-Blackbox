package com.blackbox.core

import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class PermissionAuditTest {

    @Test
    fun noReadLogsPermission() {
        val manifest = File("app/src/main/AndroidManifest.xml").readText()
        assertFalse("READ_LOGS permission must not be declared", manifest.contains("READ_LOGS"))
    }

    @Test
    fun noSystemAlertWindowPermission() {
        val manifest = File("app/src/main/AndroidManifest.xml").readText()
        assertFalse("SYSTEM_ALERT_WINDOW permission must not be declared", manifest.contains("SYSTEM_ALERT_WINDOW"))
    }

    @Test
    fun noAccessibilityServiceAbuse() {
        val manifest = File("app/src/main/AndroidManifest.xml").readText()
        assertFalse("AccessibilityService must not be declared for security reasons", manifest.contains("AccessibilityService"))
    }

    @Test
    fun voiceInteractionPermission_isGated() {
        val manifest = File("app/src/main/AndroidManifest.xml").readText()
        // BIND_VOICE_INTERACTION should only be used with the assistant service
        val count = manifest.split("BIND_VOICE_INTERACTION").size - 1
        assertEquals("BIND_VOICE_INTERACTION should appear only once", 1, count)
    }
}
