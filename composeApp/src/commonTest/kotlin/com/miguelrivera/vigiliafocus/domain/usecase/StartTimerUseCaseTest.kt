package com.miguelrivera.vigiliafocus.domain.usecase

import com.miguelrivera.vigiliafocus.domain.model.TimerState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class StartTimerUseCaseTest {

    private val useCase = StartTimerUseCase()

    @Test
    fun `start sets running to true`() {
        val state = TimerState(isRunning = false)
        val result = useCase(state = state)
        assertTrue(actual = result.isRunning)
    }

    @Test
    fun `start is idempotent when already running`() {
        val state = TimerState(isRunning = true)
        val result = useCase(state = state)
        assertSame(expected = state, actual = result)
    }

    @Test
    fun `start preserves all other fields`() {
        val state = TimerState(isRunning = false, remainingSeconds = 848, completedSessions = 2)
        val result = useCase(state = state)
        assertTrue(actual = result.isRunning)
        assertEquals(expected = 848, actual = result.remainingSeconds)
        assertEquals(expected = 2, actual = result.completedSessions)
    }
}