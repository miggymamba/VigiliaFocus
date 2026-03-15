package com.miguelrivera.vigiliafocus.presentation.timer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.miguelrivera.vigiliafocus.domain.model.TimerMode
import com.miguelrivera.vigiliafocus.domain.model.TimerSettings
import com.miguelrivera.vigiliafocus.domain.model.TimerState
import com.miguelrivera.vigiliafocus.presentation.theme.VigiliaTheme
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * UI tests for [TimerScreen] executing on an Android emulator.
 *
 * Verifies the stateless UI's reaction to domain state changes and ensures 
 * interaction callbacks are correctly triggered.
 */
@OptIn(ExperimentalTestApi::class)
class TimerScreenTest {

    /**
     * Verifies that the timer dial displays the correct initial time string 
     * formatted from the provided [TimerState].
     */
    @Test
    fun timer_displays_initial_time_correctly() = runComposeUiTest {
        val initialState = TimerState(remainingSeconds = 1500) // 25 minutes

        setContent {
            VigiliaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TimerLayout(
                        state = initialState,
                        onStart = {},
                        onPause = {},
                        onReset = {},
                        onSkip = {},
                        onNavigateToSettings = {}
                    )
                }
            }
        }

        onNodeWithText("25:00").assertExists()
    }

    /**
     * Verifies that the control button toggles between START and PAUSE labels 
     * based on the [TimerState.isRunning] property.
     */
    @Test
    fun start_button_changes_to_pause_when_running() = runComposeUiTest {
        val isRunningState = mutableStateOf(false)
        var startClicked = false

        setContent {
            VigiliaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TimerLayout(
                        state = TimerState(isRunning = isRunningState.value),
                        onStart = {
                            startClicked = true
                            isRunningState.value = true
                        },
                        onPause = { isRunningState.value = false },
                        onReset = {},
                        onSkip = {},
                        onNavigateToSettings = {}
                    )
                }
            }
        }

        waitUntilExactlyOneExists(hasText("START"), timeoutMillis = 5000)
        onNodeWithText("START").assertExists().performClick()

        assertTrue(startClicked)

        waitUntilExactlyOneExists(hasText("PAUSE"), timeoutMillis = 5000)
        onNodeWithText("PAUSE").assertExists()
    }

    /**
     * Verifies that the mode label correctly reflects the current [TimerMode].
     */
    @Test
    fun mode_label_updates_on_state_change() = runComposeUiTest {
        setContent {
            VigiliaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TimerLayout(
                        state = TimerState(mode = TimerMode.SHORT_BREAK),
                        onStart = {},
                        onPause = {},
                        onReset = {},
                        onSkip = {},
                        onNavigateToSettings = {}
                    )
                }
            }
        }

        waitUntilExactlyOneExists(hasText("SHORT BREAK"), timeoutMillis = 5000)
        onNodeWithText("SHORT BREAK").assertExists()
    }

    /**
     * Verifies that changes in [TimerSettings] are reflected in the timer's initial display.
     */
    @Test
    fun settings_changes_reflected_on_timer_screen() = runComposeUiTest {
        val customSettings = TimerSettings(focusDuration = 45)
        val stateWithCustomSettings = TimerState(
            remainingSeconds = 45 * 60,
            settings = customSettings
        )

        setContent {
            VigiliaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TimerLayout(
                        state = stateWithCustomSettings,
                        onStart = {},
                        onPause = {},
                        onReset = {},
                        onSkip = {},
                        onNavigateToSettings = {}
                    )
                }
            }
        }

        onNodeWithText("45:00").assertExists()
    }
}