package com.blackbox.module.kai.service.sms

import com.blackbox.core.module.kai.data.SmsMessage

class SmsSender {
    suspend fun send(to: String, body: String): SmsMessage? = null
}
