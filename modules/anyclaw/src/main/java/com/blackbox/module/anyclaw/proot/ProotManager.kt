package com.blackbox.module.anyclaw.proot

import android.content.Context
import com.blackbox.module.anyclaw.data.PreferencesManager
import kotlinx.coroutines.flow.Flow

class ProotManager(private val context: Context, private val prefs: PreferencesManager) {
    sealed class CommandResult {
        data class Success(val output: String) : CommandResult()
        data class Failure(val error: String) : CommandResult()
    }

    suspend fun execute(command: List<String>): CommandResult = CommandResult.Failure("not_implemented")

    fun detectChromiumExecutablePath(): Flow<String> = kotlinx.coroutines.flow.flowOf("")
}
