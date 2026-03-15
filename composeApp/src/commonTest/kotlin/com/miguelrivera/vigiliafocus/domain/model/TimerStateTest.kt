package com.miguelrivera.vigiliafocus.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [TimerState].
 *
 * Verifies that the computed properties correctly derive session metadata
 * from the underlying settings and current remaining time.
 */
class TimerStateTest {

    /**
     * Verifies that totalSecondsForMode correctly maps mode durations from settings
     * into their equivalent second values across all timer modes.
     */
    @Test
    fun `totalSecondsForMode calculates correctly from settings`() {
        val settings = TimerSettings(
            focusDuration = 25,
            shortBreakDuration = 5,
            longBreakDuration = 15
        )

        val focusState = TimerState(mode = TimerMode.FOCUS, settings = settings)
        assertEquals(expected = 25 * 60, actual = focusState.totalSecondsForMode)

        val shortState = TimerState(mode = TimerMode.SHORT_BREAK, settings = settings)
        assertEquals(expected = 5 * 60, actual = shortState.totalSecondsForMode)

        val longState = TimerState(mode = TimerMode.LONG_BREAK, settings = settings)
        assertEquals(expected = 15 * 60, actual = longState.totalSecondsForMode)
    }

    /**
     * Verifies that isAtStart correctly identifies the specific state where
     * the timer is idle and has not yet begun counting down.
     */
    @Test
    fun `isAtStart identifies initial idle state`() {
        val settings = TimerSettings(focusDuration = 25)
        val fullDuration = 25 * 60

        val idleAtStart = TimerState(
            remainingSeconds = fullDuration,
            isRunning = false,
            settings = settings
        )

        assertTrue(actual = idleAtStart.isAtStart)

        // Assert false if the timer is actively running
        val runningAtStart = idleAtStart.copy(isRunning = true)
        assertFalse(runningAtStart.isAtStart)

        // Assert false if the timer has partially elapsed
        val idlePartiallyElapsed = idleAtStart.copy(remainingSeconds = fullDuration - 1)
        assertFalse(idlePartiallyElapsed.isAtStart)
    }
}