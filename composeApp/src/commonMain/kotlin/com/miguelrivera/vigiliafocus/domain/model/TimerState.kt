package com.miguelrivera.vigiliafocus.domain.model

/**
 * Complete snapshot of the timer at any point in time.
 *
 * This is the single source of truth passed between the domain layer and
 * the presentation layer. All fields are immutable; use cases return a
 * new [TimerState] rather than mutating an existing one.
 *
 * @property mode The current phase of the Pomodoro cycle.
 * @property remainingSeconds Seconds left in the current interval.
 *   Initializes to [TimerSettings.focusDuration] converted to seconds.
 * @property isRunning Whether the countdown is actively ticking.
 * @property completedSessions Number of [TimerMode.FOCUS] sessions completed
 *   in the current cycle. Resets to 0 after a [TimerMode.LONG_BREAK].
 * @property settings The user configuration active for this state.
 */
data class TimerState(
    val mode: TimerMode = TimerMode.FOCUS,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val completedSessions: Int = 0,
    val settings: TimerSettings = TimerSettings()
) {
    /**
     * The baseline duration in seconds for the current [mode] based on [settings].
     * * Provides a central point for calculating the total duration of a session,
     * ensuring consistency between the countdown logic and UI progress indicators.
     */
    val totalSecondsForMode: Int
        get() = when (mode) {
            TimerMode.FOCUS -> settings.focusDuration * 60
            TimerMode.SHORT_BREAK -> settings.shortBreakDuration * 60
            TimerMode.LONG_BREAK -> settings.longBreakDuration * 60
        }

    /**
     * Determines if the timer is currently at the starting point of its [mode].
     */
    val isAtStart: Boolean
        get() = !isRunning && remainingSeconds == totalSecondsForMode
}