package com.blackbox.module.anyclaw.bridge

import android.content.Context

class DeviceBridge(private val context: Context) {
    inner class AudioHelper
    inner class CameraHelper
    inner class ClipboardHelper
    inner class LocationHelper
    inner class SensorHelper

    data class SentinelResult(val ok: Boolean, val message: String = "")

    fun handleCommand(cmd: String, args: org.json.JSONArray): org.json.JSONObject {
        return org.json.JSONObject().put("ok", false).put("error", "unknown command: $cmd")
    }
}
