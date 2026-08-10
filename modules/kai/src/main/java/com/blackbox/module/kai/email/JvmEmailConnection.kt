package com.blackbox.module.kai.email

import com.blackbox.core.module.kai.data.EmailMessage

class JvmEmailConnection {
    suspend fun close() {}
    suspend fun readLine(): String? = null
    suspend fun upgradeToTls(): Boolean = false
    suspend fun writeLine(line: String) {}
}
