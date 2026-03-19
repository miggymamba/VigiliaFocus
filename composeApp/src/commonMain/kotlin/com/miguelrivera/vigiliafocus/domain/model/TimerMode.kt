package com.miguelrivera.vigiliafocus.domain.model

import org.jetbrains.compose.resources.StringResource
import vigiliafocus.composeapp.generated.resources.Res
import vigiliafocus.composeapp.generated.resources.mode_focus
import vigiliafocus.composeapp.generated.resources.mode_long_break
import vigiliafocus.composeapp.generated.resources.mode_short_break

/**
 * Represents the active phase of a Pomodoro cycle.
 *
 * The cycle progresses as follows: [FOCUS] repeats until
 * [TimerSettings.sessionsBeforeLongBreak] sessions are completed, at which
 * point a [LONG_BREAK] is scheduled. A [SHORT_BREAK] follows every [FOCUS]
 * session that does not trigger a long break.
 *
 * @property labelRes The string resource for the human-readable mode label displayed
 *   in the timer UI. Using a resource reference rather than [name] ensures the label
 *   survives future enum renames and participates in localization without any
 *   presentation-layer changes.
 */
enum class TimerMode(val labelRes: StringResource) {
    /** An active work interval. */
    FOCUS(Res.string.mode_focus),

    /** A brief rest interval between focus sessions. */
    SHORT_BREAK(Res.string.mode_short_break),

    /** An extended rest interval after a full set of focus sessions. */
    LONG_BREAK(Res.string.mode_long_break)
}