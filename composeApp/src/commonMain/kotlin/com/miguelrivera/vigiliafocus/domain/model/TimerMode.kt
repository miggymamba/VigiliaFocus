package com.miguelrivera.vigiliafocus.domain.model

/**
 * Represents the active phase of a Pomodoro cycle.
 *
 * The cycle progresses as follows: [FOCUS] repeats until
 * [TimerSettings.sessionsBeforeLongBreak] sessions are completed, at which
 * point a [LONG_BREAK] is scheduled. A [SHORT_BREAK] follows every [FOCUS]
 * session that does not trigger a long break.
 */
enum class TimerMode {
    /** An active work interval. */
    FOCUS,

    /** A brief rest interval between focus sessions. */
    SHORT_BREAK,

    /** An extended rest interval after a full set of focus sessions. */
    LONG_BREAK
}