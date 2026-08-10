package com.blackbox.bridge

import com.blackbox.module.anyclaw.bridge.DeviceBridge
import org.json.JSONArray
import org.json.JSONObject

class BridgeCommandRouter(private val deviceBridge: DeviceBridge) {
    fun dispatch(cmd: String, args: JSONArray): JSONObject {
        return deviceBridge.handleCommand(cmd, args)
    }
}
