package com.blackbox.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "blackbox_settings")

object SettingsKeys {
    val SELECTED_PROVIDER = stringPreferencesKey("selected_provider")
    val SELECTED_MODEL = stringPreferencesKey("selected_model")
    val API_KEY = stringPreferencesKey("api_key")
}

class SettingsRepository(private val context: Context) {
    val selectedProvider: Flow<String?> = context.dataStore.data
        .map { it[SettingsKeys.SELECTED_PROVIDER] }

    val selectedModel: Flow<String?> = context.dataStore.data
        .map { it[SettingsKeys.SELECTED_MODEL] }

    suspend fun setProvider(provider: String) {
        context.dataStore.edit { it[SettingsKeys.SELECTED_PROVIDER] = provider }
    }

    suspend fun setModel(model: String) {
        context.dataStore.edit { it[SettingsKeys.SELECTED_MODEL] = model }
    }
}
