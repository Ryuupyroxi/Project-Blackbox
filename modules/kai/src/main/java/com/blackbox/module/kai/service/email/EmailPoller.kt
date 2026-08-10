package com.blackbox.module.kai.service.email

import com.blackbox.core.module.kai.data.EmailAccount
import com.blackbox.core.module.kai.data.EmailMessage
import com.blackbox.core.module.kai.email.EmailConnection
import kotlinx.coroutines.flow.Flow

class EmailPoller(private val connection: EmailConnection) {
    suspend fun poll(account: EmailAccount): List<EmailMessage> = emptyList()
    fun pollFlow(account: EmailAccount): Flow<List<EmailMessage>> = kotlinx.coroutines.flow.flowOf(emptyList())
}
