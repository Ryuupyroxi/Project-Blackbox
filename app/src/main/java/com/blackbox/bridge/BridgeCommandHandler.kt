package com.blackbox.bridge

import org.json.JSONObject

object BridgeCommandHandler {
    fun handle(cmd: String, raw: String): String {
        return try {
            when (cmd) {
                "start-activity", "start" -> ok(cmd)
                "broadcast" -> ok(cmd)
                "shellExec" -> ok(cmd)
                "mcpCall" -> ok(cmd)
                "smsSend", "emailSend", "calendarRead", "notificationRead", "fileRead", "fileWrite", "fileList", "bridgeSend" -> ok(cmd)
                else -> error("unknown command: $cmd")
            }
        } catch (e: Exception) {
            error(e.message ?: "handler failed")
        }
    }

    private fun ok(cmd: String): String =
        JSONObject()
            .put("id", "0")
            .put("result", "ok")
            .put("cmd", cmd)
            .toString()

    private fun error(message: String): String =
        JSONObject()
            .put("id", "0")
            .put("result", "error")
            .put("error", message)
            .toString()
}
