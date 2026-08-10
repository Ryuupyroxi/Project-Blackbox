package com.blackbox.module.kai.chat

import com.blackbox.core.module.kai.data.BlackboxMessage
import com.blackbox.core.module.kai.data.Model
import com.blackbox.core.module.kai.data.Service
import kotlinx.coroutines.flow.Flow

class KaiAiProvider {
    fun stream(model: Model, messages: List<BlackboxMessage>): Flow<String> = kotlinx.coroutines.flow.flowOf("")
}
