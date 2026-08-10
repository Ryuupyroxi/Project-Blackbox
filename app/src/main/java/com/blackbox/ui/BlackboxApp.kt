package com.blackbox.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.blackbox.ui.screen.chat.ChatScreen
import com.blackbox.ui.screen.dashboard.DashboardScreen
import com.blackbox.ui.screen.modules.ModulesScreen
import com.blackbox.ui.screen.settings.SettingsScreen
import com.blackbox.ui.screen.terminal.TerminalScreen

@Composable
fun BlackboxApp() {
    var screen by remember { mutableStateOf("dashboard") }
    Scaffold { innerPadding ->
        when (screen) {
            "dashboard" -> DashboardScreen(
                onNavigateToChat = { screen = "chat" },
                onNavigateToSettings = { screen = "settings" },
                onNavigateToModules = { screen = "modules" },
                onNavigateToTerminal = { screen = "terminal" }
            )
            "chat" -> ChatScreen(onBack = { screen = "dashboard" })
            "settings" -> SettingsScreen(onBack = { screen = "dashboard" })
            "modules" -> ModulesScreen(onBack = { screen = "dashboard" })
            "terminal" -> TerminalScreen(onBack = { screen = "dashboard" })
        }
    }
}
