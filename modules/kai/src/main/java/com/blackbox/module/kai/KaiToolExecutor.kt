package com.blackbox.module.kai

import com.blackbox.core.module.kai.data.Service
import com.blackbox.core.module.kai.data.BlackboxMessage
import kotlinx.coroutines.flow.Flow

class KaiToolExecutor {
    fun execute(service: Service, messages: List<BlackboxMessage>): Flow<String> = kotlinx.coroutines.flow.flowOf("")
}
