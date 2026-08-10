package com.blackbox.module.anyclaw.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionGate {
    data class Rule(val permissions: List<String>, val rationale: String, val fallback: String)

    val featureRules = mapOf(
        "messaging_sms" to Rule(
            listOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS),
            "Required for SMS read/send tools",
            "SMS tools disabled"
        ),
        "messaging_email" to Rule(
            listOf(Manifest.permission.INTERNET),
            "Required for email sync",
            "Email sync disabled"
        ),
        "calendar" to Rule(
            listOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR),
            "Required for calendar tools",
            "Calendar tools disabled"
        ),
        "camera" to Rule(
            listOf(Manifest.permission.CAMERA),
            "Required for QR/camera capture",
            "Camera tools disabled"
        ),
        "audio" to Rule(
            listOf(Manifest.permission.RECORD_AUDIO),
            "Required for voice input/audio tools",
            "Audio tools disabled"
        ),
        "files" to Rule(
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            "Required for file tools",
            "File tools restricted to app storage"
        ),
        "location" to Rule(
            listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            "Required for location tools",
            "Location tools disabled"
        )
    )

    fun missing(context: Context, feature: String): List<String> {
        val rule = featureRules[feature] ?: return emptyList()
        return rule.permissions.filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
    }

    fun rationale(feature: String): String = featureRules[feature]?.rationale.orEmpty()
    fun fallback(feature: String): String = featureRules[feature]?.fallback.orEmpty()
}
