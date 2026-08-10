package com.blackbox.module.anyclaw.ui.screen.settings

import androidx.lifecycle.ViewModel
import com.blackbox.module.anyclaw.auth.AuthorizationFlow
import com.blackbox.module.anyclaw.auth.OpenRouterAuth
import com.blackbox.module.anyclaw.data.PreferencesManager
import com.blackbox.core.module.ModuleBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val prefs: PreferencesManager,
    private val auth: OpenRouterAuth
) : ViewModel() {
    private val _authFlow = MutableStateFlow<AuthorizationFlow>(AuthorizationFlow())
    val authFlow: StateFlow<AuthorizationFlow> = _authFlow

    fun fetchCodexappVersions() {
        // Versions fetch stub.
    }

    fun loginOpenAiCodexOAuth() {
        _authFlow.value = AuthorizationFlow(authUrl = "https://openrouter.ai/oauth")
        auth.launchAuthorizationFlow(_authFlow.value.authUrl)
    }

    fun logoutOpenAiCodex() {
        _authFlow.value = AuthorizationFlow()
    }

    fun refreshCodexAuthStatus() {
        // Auth status refresh stub.
    }

    fun runCodexDirectPkceLogin() {
        // PKCE login stub.
    }

    fun runOpenClawDoctorFix() {
        ModuleBus.publish(ModuleEvent("anyclaw", "run_openclaw_doctor_fix"))
    }

    fun runOpenClawUpdate() {
        ModuleBus.publish(ModuleEvent("anyclaw", "run_openclaw_update"))
    }

    fun runRecoveryInstall() {
        ModuleBus.publish(ModuleEvent("anyclaw", "run_recovery_install"))
    }

    fun saveOpenAiCompatibleConfig(baseUrl: String, modelId: String) {
        // Save config stub.
    }

    fun setApiProvider(provider: String) {
        // Set provider stub.
    }

    fun setAutoStartCodexOnBoot(enabled: Boolean) {}
    fun setAutoStartOpenClawOnBoot(enabled: Boolean) {}
    fun setCodexappBranch(branch: String) {}
    fun setCodexappVersion(version: String) {}
    fun setSelectedModel(model: String) {}
    fun setGptSubscription() {}
    fun shouldShowRestartPromptForProvider(): Boolean = false
}
