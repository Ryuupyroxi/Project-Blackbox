package com.blackbox.runtime

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PermissionCoordinator {
    private val _status = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val status: StateFlow<Map<String, Boolean>> = _status.asStateFlow()

    fun refresh(context: Application) {
        val map = buildList {
            add(Pair("android.permission.POST_NOTIFICATIONS", isGranted(context, Manifest.permission.POST_NOTIFICATIONS)))
            add(Pair("android.permission.RECORD_AUDIO", isGranted(context, Manifest.permission.RECORD_AUDIO)))
            add(Pair("android.permission.CAMERA", isGranted(context, Manifest.permission.CAMERA)))
            add(Pair("android.permission.READ_SMS", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) isGranted(context, Manifest.permission.READ_SMS) else true))
            add(Pair("android.permission.SEND_SMS", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) isGranted(context, Manifest.permission.SEND_SMS) else true))
            add(Pair("android.permission.READ_CONTACTS", isGranted(context, Manifest.permission.READ_CONTACTS)))
            add(Pair("android.permission.READ_EXTERNAL_STORAGE", isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)))
        }.toMap()
        _status.value = map
    }

    private fun isGranted(context: Application, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
