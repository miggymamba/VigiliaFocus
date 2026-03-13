package com.miguelrivera.vigiliafocus.domain.usecase

import com.miguelrivera.vigiliafocus.domain.model.TimerMode
import com.miguelrivera.vigiliafocus.domain.model.TimerSettings
import com.miguelrivera.vigiliafocus.domain.model.TimerState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ResetTimerUseCaseTest {

    private val useCase = ResetTimerUseCase()
    private val settings = TimerSettings(
        focusDuration = 25,
        shortBreakDuration = 5,
        longBreakDuration = 15
    )

    @Test
    fun `reset stores full focus duration`() {
        val state = TimerState(mode = TimerMode.FOCUS, remainingSeconds = 100, settings = settings)
        val result = useCase(state = state)
        assertEquals(expected = 25 * 60, actual = result.remainingSeconds)
        assertFalse(actual = result.isRunning)
    }

    @Test
    fun `reset restores full short break duration`() {
        val state =
            TimerState(mode = TimerMode.SHORT_BREAK, remainingSeconds = 30, settings = settings)
        val result = useCase(state = state)
        assertEquals(expected = 5 * 60, actual = result.remainingSeconds)
        assertFalse(actual = result.isRunning)
    }

    @Test
    fun `reset stores full long break duration`() {
        val state =
            TimerState(mode = TimerMode.LONG_BREAK, remainingSeconds = 30, settings = settings)
        val result = useCase(state = state)
        assertEquals(expected = 15 * 60, actual = result.remainingSeconds)
        assertFalse(actual = result.isRunning)
    }

    @Test
    fun `reset preserves mode and completedSessions`() {
        val state = TimerState(
            mode = TimerMode.SHORT_BREAK,
            completedSessions = 3,
            settings = settings
        )
        val result = useCase(state = state)
        assertEquals(TimerMode.SHORT_BREAK, result.mode)
        assertEquals(3, result.completedSessions)
    }
}