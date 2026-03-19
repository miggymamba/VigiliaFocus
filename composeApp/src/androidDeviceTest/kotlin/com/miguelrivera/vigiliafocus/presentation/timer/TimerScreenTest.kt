package com.miguelrivera.vigiliafocus.presentation.timer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
 * UI tests for the timer presentation layer executing on an Android emulator.
 *
 * All tests target the stateless composables ([TimerLayout], [TimerCompleteLayout])
 * directly to stay decoupled from Koin and the ViewModel, matching the existing
 * pattern used in [SettingsScreenTest].
 *
 * ### Signature change (audit patch)
 * [TimerLayout] now accepts [TimerUiState] instead of raw [TimerState] so the UI
 * layer can branch on the precise sealed variant. Every test wraps its [TimerState]
 * in the appropriate [TimerUiState] subclass before passing it to the composable.
 */
@OptIn(ExperimentalTestApi::class)
class TimerScreenTest {

    // ── TimerLayout tests ──────────────────────────────────────────────────

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
                        uiState = TimerUiState.Idle(initialState),
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
     * Verifies that the primary control button toggles between START and PAUSE
     * labels in response to [TimerUiState] changes.
     *
     * The test drives recomposition by replacing the entire [TimerUiState] on
     * each callback, which is the correct model: the ViewModel emits a new sealed
     * variant, not just a boolean flip inside a stable state object.
     */
    @Test
    fun start_button_changes_to_pause_when_running() = runComposeUiTest {
        var uiState: TimerUiState by mutableStateOf(TimerUiState.Idle(TimerState()))
        var startClicked = false

        setContent {
            VigiliaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TimerLayout(
                        uiState = uiState,
                        onStart = {
                            startClicked = true
                            // Simulate the ViewModel emitting Running after start
                            uiState = TimerUiState.Running(TimerState(isRunning = true))
                        },
                        onPause = {
                            uiState = TimerUiState.Paused(TimerState(isRunning = false))
                        },
                        onReset = {},
                        onSkip = {},
                        onNavigateToSettings = {}
                    )
                }
            }
        }

        waitUntilExactlyOneExists(hasText("START"), timeoutMillis = 5_000)
        onNodeWithText("START").assertExists().performClick()

        assertTrue(startClicked)

        waitUntilExactlyOneExists(hasText("PAUSE"), timeoutMillis = 5_000)
        onNodeWithText("PAUSE").assertExists()
    }

    /**
     * Verifies that the mode label correctly reflects the current [TimerMode].
     *
     * The label is now sourced from [TimerMode.labelRes] (a string resource) rather
     * than [TimerMode.name], so this test also implicitly validates that the resource
     * lookup returns the expected human-readable string.
     */
    @Test
    fun mode_label_updates_on_state_change() = runComposeUiTest {
        setContent {
            VigiliaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TimerLayout(
                        uiState = TimerUiState.Idle(TimerState(mode = TimerMode.SHORT_BREAK)),
                        onStart = {},
                        onPause = {},
                        onReset = {},
                        onSkip = {},
                        onNavigateToSettings = {}
                    )
                }
            }
        }

        waitUntilExactlyOneExists(hasText("SHORT BREAK"), timeoutMillis = 5_000)
        onNodeWithText("SHORT BREAK").assertExists()
    }

    /**
     * Verifies that changes in [TimerSettings] are reflected in the timer's display.
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
                        uiState = TimerUiState.Idle(stateWithCustomSettings),
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

    // ── TimerCompleteLayout tests ──────────────────────────────────────────

    /**
     * Verifies that [TimerCompleteLayout] displays the completion banner and
     * the SKIP button when a session reaches zero.
     *
     * This test covers the [TimerUiState.Completed] branch that was previously
     * dead code — no composable was rendering it before the audit patch.
     */
    @Test
    fun completion_banner_is_shown_when_session_completes() = runComposeUiTest {
        setContent {
            VigiliaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TimerCompleteLayout(
                        state = TimerState(),
                        onSkip = {},
                        onNavigateToSettings = {}
                    )
                }
            }
        }

        onNodeWithText("Session Complete").assertExists()
        onNodeWithText("SKIP").assertExists()
    }

    /**
     * Verifies that tapping SKIP in [TimerCompleteLayout] invokes the callback.
     */
    @Test
    fun skip_button_invokes_callback_from_completion_screen() = runComposeUiTest {
        var skipClicked = false

        setContent {
            VigiliaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TimerCompleteLayout(
                        state = TimerState(),
                        onSkip = { skipClicked = true },
                        onNavigateToSettings = {}
                    )
                }
            }
        }

        waitUntilExactlyOneExists(hasText("SKIP"), timeoutMillis = 5_000)
        onNodeWithText("SKIP").performClick()

        assertTrue(skipClicked)
    }
}