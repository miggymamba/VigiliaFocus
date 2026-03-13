package com.miguelrivera.vigiliafocus.domain.usecase

import com.miguelrivera.vigiliafocus.domain.model.TimerState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame

class PauseTimerUseCaseTest {

    private val useCase = PauseTimerUseCase()

    @Test
    fun `pause sets running to false`() {
        val state = TimerState(isRunning = true)
        val result = useCase(state = state)
        assertFalse(actual = result.isRunning)
    }

    @Test
    fun `pause is idempotent when already paused`() {
        val state = TimerState(isRunning = false)
        val result = useCase(state = state)
        assertSame(expected = state, actual = result)
    }

    @Test
    fun `pause preserves remaining seconds`() {
        val state = TimerState(isRunning = true, remainingSeconds = 690)
        val result = useCase(state = state)
        assertEquals(expected = 690, actual = result.remainingSeconds)
    }

}