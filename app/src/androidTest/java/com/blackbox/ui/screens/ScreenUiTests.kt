package com.blackbox.ui.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blackbox.ui.screen.chat.ChatScreen
import com.blackbox.ui.screen.dashboard.DashboardScreen
import com.blackbox.ui.screen.modules.ModulesScreen
import com.blackbox.ui.screen.settings.SettingsScreen
import com.blackbox.ui.screen.terminal.TerminalScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenUiTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dashboardScreen_rendersAllCards() {
        composeTestRule.setContent { DashboardScreen() }
        composeTestRule.onNodeWithText("Blackbox Dashboard").assertExists()
        composeTestRule.onNodeWithText("Unified Chat").assertExists()
        composeTestRule.onNodeWithText("Manage AnyClaw, Kai, ADT runtime services").assertExists()
        composeTestRule.onNodeWithText("Terminal / Proot").assertExists()
        composeTestRule.onNodeWithText("Proot shell, SSH, sandbox sessions").assertExists()
    }

    @Test
    fun chatScreen_rendersTitleAndInput() {
        composeTestRule.setContent { ChatScreen() }
        composeTestRule.onNodeWithText("Unified Chat").assertExists()
        composeTestRule.onNodeWithText("Message").assertExists()
    }

    @Test
    fun settingsScreen_rendersTitleAndDescription() {
        composeTestRule.setContent { SettingsScreen() }
        composeTestRule.onNodeWithText("Settings").assertExists()
        composeTestRule.onNodeWithText("Providers, models, bridges, permissions, assistant routing.").assertExists()
    }

    @Test
    fun modulesScreen_rendersTitleAndDescription() {
        composeTestRule.setContent { ModulesScreen() }
        composeTestRule.onNodeWithText("Modules").assertExists()
        composeTestRule.onNodeWithText("AnyClaw, Kai, ADT runtime services and module lifecycle.").assertExists()
    }

    @Test
    fun terminalScreen_rendersTitleAndDescription() {
        composeTestRule.setContent { TerminalScreen() }
        composeTestRule.onNodeWithText("Terminal / Proot").assertExists()
        composeTestRule.onNodeWithText("Proot shell, SSH sessions, sandbox status.").assertExists()
    }
}
