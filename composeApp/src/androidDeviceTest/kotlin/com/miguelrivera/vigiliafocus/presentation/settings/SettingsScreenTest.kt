package com.miguelrivera.vigiliafocus.presentation.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.miguelrivera.vigiliafocus.domain.model.TimerSettings
import kotlin.test.Test

/**
 * UI tests for [SettingsScreen] utilizing the Compose Multiplatform testing library.
 *
 * Verifies that the settings interface correctly initializes with provided 
 * values and presents the expected configuration labels.
 */
@OptIn(ExperimentalTestApi::class)
class SettingsScreenTest {

    /**
     * Verifies that all configuration sliders are visible and correctly 
     * initialized with values from the provided [TimerSettings].
     */
    @Test
    fun settings_screen_displays_correct_initial_values() = runComposeUiTest {
        val initialSettings = TimerSettings(
            focusDuration = 25,
            shortBreakDuration = 5,
            longBreakDuration = 15,
            sessionsBeforeLongBreak = 4
        )

        setContent {
            SettingsLayout(
                settings = initialSettings,
                onSaveAndBack = {}
            )
        }

        // Verify that the bold value text next to each slider matches the initial settings
        onNodeWithText("25").assertIsDisplayed()
        onNodeWithText("5").assertIsDisplayed()
        onNodeWithText("15").assertIsDisplayed()
        onNodeWithText("4").assertIsDisplayed()
    }
}