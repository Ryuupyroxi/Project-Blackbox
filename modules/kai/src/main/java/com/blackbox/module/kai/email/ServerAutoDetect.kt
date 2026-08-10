package com.blackbox.module.kai.email

data class ServerAutoDetect(
    val imapHost: String = "",
    val imapPort: Int = 993,
    val smtpHost: String = "",
    val smtpPort: Int = 465
) {
    data class ServerConfig(val host: String, val port: Int)
}
