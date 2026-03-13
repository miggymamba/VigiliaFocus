package com.miguelrivera.vigiliafocus.domain.model

/**
 * User-configurable durations and cycle rules for the Pomodoro timer.
 *
 * All duration fields are expressed in **minutes**. Default values follow
 * the classic Pomodoro Technique as defined by Francesco Cirillo.
 *
 * @property focusDuration Minutes per focus session. Must be > 0.
 * @property shortBreakDuration Minutes per short break. Must be > 0.
 * @property longBreakDuration Minutes per long break. Must be >= [shortBreakDuration].
 * @property sessionsBeforeLongBreak Number of focus sessions completed before
 *   a long break is scheduled. Must be >= 1.
 */
data class TimerSettings(
    val focusDuration: Int = 25,
    val shortBreakDuration: Int = 5,
    val longBreakDuration: Int = 15,
    val sessionsBeforeLongBreak: Int = 4
)