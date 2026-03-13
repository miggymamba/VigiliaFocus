package com.miguelrivera.vigiliafocus.domain.usecase

import com.miguelrivera.vigiliafocus.domain.model.TimerState

/**
 * Transitions a [TimerState] to the paused state.
 *
 * Preserves [TimerState.remainingSeconds] so the countdown can be resumed
 * from the same point via [StartTimerUseCase].
 *
 * If the timer is already paused, the same state is returned unchanged,
 * making the operation idempotent.
 *
 * @param state The current [TimerState].
 * @return A new [TimerState] with [TimerState.isRunning] set to `false`,
 *   or the same instance if already paused.
 */
class PauseTimerUseCase {
    operator fun invoke(state: TimerState): TimerState {
        if (!state.isRunning) return state
        return state.copy(isRunning = false)
    }
}