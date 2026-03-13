package com.miguelrivera.vigiliafocus.domain.usecase

import com.miguelrivera.vigiliafocus.domain.model.TimerMode
import com.miguelrivera.vigiliafocus.domain.model.TimerState

/**
 * Advances the Pomodoro cycle to the next [TimerMode] and resets the
 * countdown to the full duration for that mode.
 *
 * Transition rules:
 * - [TimerMode.FOCUS] → increments [TimerState.completedSessions].
 *   If the new count reaches [TimerState.settings.sessionsBeforeLongBreak],
 *   the next mode is [TimerMode.LONG_BREAK] and [TimerState.completedSessions]
 *   resets to 0. Otherwise, the next mode is [TimerMode.SHORT_BREAK].
 * - [TimerMode.SHORT_BREAK] → always transitions to [TimerMode.FOCUS].
 * - [TimerMode.LONG_BREAK] → always transitions to [TimerMode.FOCUS].
 *
 * The returned state is always stopped ([TimerState.isRunning] = `false`),
 * requiring an explicit [StartTimerUseCase] call to begin the next interval.
 *
 * @param state The current [TimerState], typically called when the countdown
 *   reaches zero or the user manually skips.
 * @return A new [TimerState] reflecting the next mode, updated session count,
 *   and full remaining seconds for the incoming mode.
 */
class SkipToNextModeUseCase {
    operator fun invoke(state: TimerState): TimerState {
        return when (state.mode) {
            TimerMode.FOCUS -> {
                val newCompletedSessions = state.completedSessions + 1
                val isLongBreakDue = newCompletedSessions >= state.settings.sessionsBeforeLongBreak

                if (isLongBreakDue) {
                    state.copy(
                        mode = TimerMode.LONG_BREAK,
                        remainingSeconds = state.settings.longBreakDuration * 60,
                        isRunning = false,
                        completedSessions = 0
                    )
                } else {
                    state.copy(
                        mode = TimerMode.SHORT_BREAK,
                        remainingSeconds = state.settings.shortBreakDuration * 60,
                        isRunning = false,
                        completedSessions = newCompletedSessions
                    )
                }
            }

            TimerMode.SHORT_BREAK -> state.copy(
                mode = TimerMode.FOCUS,
                remainingSeconds = state.settings.focusDuration * 60,
                isRunning = false
            )

            TimerMode.LONG_BREAK -> state.copy(
                mode = TimerMode.FOCUS,
                remainingSeconds = state.settings.focusDuration * 60,
                isRunning = false
            )
        }
    }
}