package com.blackbox.module.kai.service.sms

import com.blackbox.core.module.kai.data.SmsMessage

class SmsReader {
    suspend fun readInbox(since: Long): List<SmsMessage> = emptyList()
    suspend fun search(query: String): List<SmsMessage> = emptyList()
    suspend fun readById(id: String): SmsMessage? = null
}
