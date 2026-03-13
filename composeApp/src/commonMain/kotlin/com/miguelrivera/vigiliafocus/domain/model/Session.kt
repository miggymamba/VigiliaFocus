package com.miguelrivera.vigiliafocus.domain.model

/**
 * An immutable record of a single completed timer interval.
 *
 * Created by the domain layer when a countdown reaches zero. Intended for
 * future history/statistics features; not persisted in Phase 2.
 *
 * @property id Unique identifier for this session, sourced from the platform
 *   clock via [currentTimeMillis] at the moment of completion.
 * @property mode The [TimerMode] that was active when this session completed.
 * @property completedAt Epoch milliseconds when the session finished.
 * @property durationMinutes The configured duration for this session's mode,
 *   in minutes, taken from [TimerSettings] at the time of completion.
 */
data class Session(
    val id: Long,
    val mode: TimerMode,
    val completedAt: Long,
    val durationMinutes: Int
)
