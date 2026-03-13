package com.miguelrivera.vigiliafocus.domain.usecase

import com.miguelrivera.vigiliafocus.domain.model.TimerMode
import com.miguelrivera.vigiliafocus.domain.model.TimerSettings
import com.miguelrivera.vigiliafocus.domain.model.TimerState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SkipToNextModeUseCaseTest {

    private val useCase = SkipToNextModeUseCase()
    private val settings = TimerSettings(
        focusDuration = 25,
        shortBreakDuration = 5,
        longBreakDuration = 15,
        sessionsBeforeLongBreak = 4
    )

    @Test
    fun `focus transitions to short break after session threshold`() {
        val state = TimerState(mode = TimerMode.FOCUS, completedSessions = 0, settings = settings)
        val result = useCase(state = state)
        assertEquals(expected = TimerMode.SHORT_BREAK, actual = result.mode)
        assertEquals(expected = 1, actual = result.completedSessions)
        assertEquals(expected = 5 * 60, actual = result.remainingSeconds)
        assertFalse(actual = result.isRunning)
    }

    @Test
    fun `focus transitions to long break after session threshold`() {
        // 3 completed + this one = 4 = sessionsBeforeLongBreak
        val state = TimerState(mode = TimerMode.FOCUS, completedSessions = 3, settings = settings)
        val result = useCase(state = state)
        assertEquals(expected = TimerMode.LONG_BREAK, actual = result.mode)
        assertEquals(expected = 0, actual = result.completedSessions)
        assertEquals(expected = 15 * 60, actual = result.remainingSeconds)
        assertFalse(actual = result.isRunning)
    }

    @Test
    fun `focus transitions to long break when sessions exceed threshold`() {
        // edge case: completedSessions somehow already at threshold
        val state = TimerState(mode = TimerMode.FOCUS, completedSessions = 4, settings = settings)
        val result = useCase(state = state)
        assertEquals(expected = TimerMode.LONG_BREAK, actual = result.mode)
        assertEquals(expected = 0, actual = result.completedSessions)
    }

    @Test
    fun `short break transitions to focus`() {
        val state = TimerState(mode = TimerMode.SHORT_BREAK, completedSessions = 2, settings = settings)
        val result = useCase(state = state)
        assertEquals(expected = TimerMode.FOCUS, actual = result.mode)
        assertEquals(expected = 25 * 60, actual = result.remainingSeconds)
        assertEquals(expected = 2, actual = result.completedSessions)
        assertFalse(actual = result.isRunning)
    }

    @Test
    fun `long break transitions to focus and preserves zero sessions`() {
        val state =
            TimerState(mode = TimerMode.LONG_BREAK, completedSessions = 0, settings = settings)
        val result = useCase(state = state)
        assertEquals(expected = TimerMode.FOCUS, actual = result.mode)
        assertEquals(expected = 25 * 60, actual = result.remainingSeconds)
        assertEquals(expected = 0, actual = result.completedSessions)
        assertFalse(actual = result.isRunning)
    }

    @Test
    fun `full cycle - 4 focus sessions trigger long break`() {
        var state = TimerState(settings = settings)
        repeat(3) {
            state = useCase(state = state) // SHORT_BREAK
            state = useCase(state = state) // back to FOCUS
        }
        // 3 completed, one more focus skip should trigger long break
        state = useCase(state = state)
        assertEquals(expected = TimerMode.LONG_BREAK, actual = state.mode)
        assertEquals(expected = 0, actual = state.completedSessions)
    }

    @Test
    fun `result is always stopped`() {
        val focusState = TimerState(mode = TimerMode.FOCUS, isRunning = true, settings = settings)
        assertFalse(actual = useCase(state = focusState).isRunning)

        val breakState = TimerState(mode = TimerMode.SHORT_BREAK, isRunning = true, settings = settings)
        assertFalse(actual = useCase(state = breakState).isRunning)
    }
}