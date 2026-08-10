package com.blackbox.module.kai.data

import kotlinx.coroutines.flow.flowOf

class RemoteDataRepository {
    suspend fun ask(prompt: String, service: Service): String = ""
}
