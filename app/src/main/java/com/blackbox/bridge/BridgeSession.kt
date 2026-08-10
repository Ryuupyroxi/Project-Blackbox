package com.blackbox.bridge

import java.io.File

class BridgeSession(private val requestFile: File) {
    fun close() {
        requestFile.delete()
    }
}
