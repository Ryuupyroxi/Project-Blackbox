package com.blackbox.core.data

class BlackboxRepository(
    private val database: BlackboxDatabase,
    private val preferences: BlackboxPreferences,
    private val secretStore: SecretStore
) {
    // TODO: add Flow/StateFlow wrappers around DAOs + preferences in later pass.
}
