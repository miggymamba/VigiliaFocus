package com.miguelrivera.vigiliafocus.domain.usecase

import com.miguelrivera.vigiliafocus.domain.model.TimerState

/**
 * Transitions a [TimerState] to the running state.
 *
 * Does not interact with [com.miguelrivera.vigiliafocus.platform.PlatformTimer]
 * directly — that is the responsibility of the ViewModel. This use case
 * is solely concerned with producing the correct domain state.
 *
 * If the timer is already running, the same state is returned unchanged,
 * making the operation idempotent.
 *
 * @param state The current [TimerState].
 * @return A new [TimerState] with [TimerState.isRunning] set to `true`,
 *   or the same instance if already running.
 */
class StartTimerUseCase {
    operator fun invoke(state: TimerState): TimerState {
        if (state.isRunning) return state
        return state.copy(isRunning = true)
    }
}