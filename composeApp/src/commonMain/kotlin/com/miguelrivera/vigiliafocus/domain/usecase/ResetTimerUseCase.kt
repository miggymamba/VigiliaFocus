package com.miguelrivera.vigiliafocus.domain.usecase

import com.miguelrivera.vigiliafocus.domain.model.TimerMode
import com.miguelrivera.vigiliafocus.domain.model.TimerState

/**
 * Resets the current interval back to its full configured duration.
 *
 * The [TimerState.mode] and [TimerState.completedSessions] are preserved —
 * reset only affects the current interval, it does not advance the cycle.
 * The timer is also stopped as part of the reset.
 *
 * @param state The current [TimerState].
 * @return A new [TimerState] with [TimerState.isRunning] set to `false` and
 *   [TimerState.remainingSeconds] restored to the full duration for the
 *   current [TimerState.mode] as defined in [TimerState.settings].
 */
class ResetTimerUseCase {
    operator fun invoke(state: TimerState): TimerState {
        val fullDuration = when (state.mode) {
            TimerMode.FOCUS -> state.settings.focusDuration
            TimerMode.SHORT_BREAK -> state.settings.shortBreakDuration
            TimerMode.LONG_BREAK -> state.settings.longBreakDuration
        } * 60

        return state.copy(
            isRunning = false,
            remainingSeconds = fullDuration
        )
    }
}