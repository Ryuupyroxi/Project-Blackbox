package com.blackbox.module.kai.email

import com.blackbox.core.module.kai.data.EmailMessage

interface EmailConnection {
    suspend fun connect()
    suspend fun login()
    suspend fun search(since: Long): List<Long>
    suspend fun fetchHeaders(ids: List<Long>): List<EmailMessage>
    suspend fun fetchBody(id: Long): EmailMessage?
    suspend fun appendToMailbox(message: EmailMessage)
    suspend fun createMailbox(name: String)
    suspend fun markAsRead(id: Long)
    suspend fun selectInbox()
    suspend fun logout()
}
