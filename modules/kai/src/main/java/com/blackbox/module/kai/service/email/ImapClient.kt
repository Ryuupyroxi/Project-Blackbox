package com.blackbox.module.kai.service.email

import com.blackbox.core.module.kai.email.EmailConnection
import com.blackbox.core.module.kai.data.EmailMessage

class ImapClient(private val connection: EmailConnection) {
    suspend fun connect() {}
    suspend fun login() {}
    suspend fun search(since: Long): List<Long> = emptyList()
    suspend fun fetchHeaders(ids: List<Long>): List<EmailMessage> = emptyList()
    suspend fun fetchBody(id: Long): EmailMessage? = null
    suspend fun appendToMailbox(message: EmailMessage) {}
    suspend fun createMailbox(name: String) {}
    suspend fun markAsRead(id: Long) {}
    suspend fun selectInbox() {}
    suspend fun logout() {}
}
