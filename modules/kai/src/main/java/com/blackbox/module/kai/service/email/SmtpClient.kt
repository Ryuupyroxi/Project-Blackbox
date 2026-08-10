package com.blackbox.module.kai.service.email

import com.blackbox.core.module.kai.data.EmailMessage

class SmtpClient {
    suspend fun connect() {}
    suspend fun authenticate(username: String, password: String) {}
    suspend fun sendReply(message: EmailMessage) {}
    suspend fun quit() {}
}
