package com.blackbox.module.kai.data

import kotlinx.coroutines.flow.flowOf

class FakeBlackboxRepository : RemoteDataRepository() {
    override suspend fun ask(prompt: String, service: Service): String = "fake"
}
