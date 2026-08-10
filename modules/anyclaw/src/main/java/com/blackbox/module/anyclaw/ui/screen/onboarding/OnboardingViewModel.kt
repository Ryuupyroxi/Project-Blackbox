package com.blackbox.module.anyclaw.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import com.blackbox.core.module.ModuleBus

class OnboardingViewModel : ViewModel() {
    fun exchangeCodexToken(code: String) {
        ModuleBus.publish(ModuleEvent("anyclaw", "exchange_codex_token", mapOf("code" to code)))
    }

    fun handleCodexAuthCode(code: String) {
        ModuleBus.publish(ModuleEvent("anyclaw", "handle_codex_auth_code", mapOf("code" to code)))
    }

    fun startCodexCallbackServer() {
        ModuleBus.publish(ModuleEvent("anyclaw", "start_callback_server"))
    }
}
