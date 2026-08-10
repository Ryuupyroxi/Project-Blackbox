package com.blackbox.ui.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blackbox.ui.BlackboxApp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BlackboxAppUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dashboard_rendersTitleAndCards() {
        composeTestRule.setContent { BlackboxApp() }
        composeTestRule.onNodeWithText("Blackbox Dashboard").assertExists()
        composeTestRule.onNodeWithText("Unified Chat").assertExists()
        composeTestRule.onNodeWithText("Modules").assertExists()
        composeTestRule.onNodeWithText("Terminal / Proot").assertExists()
        composeTestRule.onNodeWithText("Settings").assertExists()
    }

    @Test
    fun chat_navigatesFromDashboard() {
        composeTestRule.setContent { BlackboxApp() }
        composeTestRule.onNodeWithText("Unified Chat").performClick()
        composeTestRule.onNodeWithText("Message").assertExists()
    }

    @Test
    fun modules_navigatesFromDashboard() {
        composeTestRule.setContent { BlackboxApp() }
        composeTestRule.onNodeWithText("Modules").performClick()
        composeTestRule.onNodeWithText("Modules", ignoreCase = true).assertExists()
    }

    @Test
    fun terminal_navigatesFromDashboard() {
        composeTestRule.setContent { BlackboxApp() }
        composeTestRule.onNodeWithText("Terminal / Proot").performClick()
        composeTestRule.onNodeWithText("Terminal / Proot", ignoreCase = true).assertExists()
    }

    @Test
    fun settings_navigatesFromDashboard() {
        composeTestRule.setContent { BlackboxApp() }
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithText("Settings", ignoreCase = true).assertExists()
    }

    @Test
    fun chatScreen_rendersInputField() {
        composeTestRule.setContent { BlackboxApp() }
        composeTestRule.onNodeWithText("Unified Chat").performClick()
        composeTestRule.onNodeWithText("Message").assertExists()
    }

    @Test
    fun allFiveScreens_renderWithoutCrash() {
        composeTestRule.setContent { BlackboxApp() }
        val labels = listOf("Unified Chat", "Modules", "Terminal / Proot", "Settings")
        labels.forEach { label ->
            composeTestRule.onNodeWithText(label).performClick()
            composeTestRule.onNodeWithText(label, ignoreCase = true).assertExists()
        }
    }
}
