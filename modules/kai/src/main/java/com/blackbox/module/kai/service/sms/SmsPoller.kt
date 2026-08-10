package com.blackbox.module.kai.service.sms

import com.blackbox.core.module.kai.data.SmsMessage

class SmsPoller {
    suspend fun poll(): List<SmsMessage> = emptyList()
}
