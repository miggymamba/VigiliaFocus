package com.miguelrivera.vigiliafocus.platform

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Internal dependency resolver for acquiring the Android application context
 * safely without altering the cross-platform `expect` constructor signature.
 */
private object ContextProvider : KoinComponent {
    val context: Context by inject()
}

/**
 * Android implementation of [PlatformTimer] using Kotlin coroutines.
 *
 * A [SupervisorJob] anchors the timer's [CoroutineScope] so that cancellation
 * of the active tick job does not cancel the scope itself, allowing [start] to
 * be called again after [pause] or [reset].
 *
 * The countdown ticks every 1 000 ms on [Dispatchers.Default]. Each tick invokes
 * [onTick] with the updated remaining seconds; [onFinish] is invoked once when
 * the countdown reaches zero.
 *
 * ### Foreground-service lifecycle
 *
 * he [TimerForegroundService] is started when a countdown begins and
 * stopped only when the countdown finishes or is explicitly reset. On [pause] the
 * tick coroutine is canceled but the service **stays alive** — it keeps the
 * process alive in the background so Android does not evict it while the user has
 * the timer paused. The notification is updated to a "Paused" label rather than
 * being dismissed and re-shown on every resume, which avoids the repeated
 * `startForeground` call that can trigger an ANR on Android 12+.
 *
 * Lifecycle contract:
 * - [start] launches a new tick job; calling [start] while already running
 *   cancels the previous job before launching a new one.
 * - [pause] cancels the tick job and updates the notification to "Paused".
 *   The service remains running.
 * - [reset] cancels the tick job, clears [remainingSeconds], and stops the service.
 */
actual class PlatformTimer actual constructor() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var tickJob: Job? = null
    private var remainingSeconds: Int = 0

    /**
     * Starts a countdown from [durationSeconds], invoking [onTick] once per second
     * with the updated remaining value and [onFinish] when it reaches zero.
     *
     * If a countdown is already running it is canceled before the new one begins,
     * making this call safe for resume-after-pause scenarios when the caller passes
     * the preserved remaining seconds.
     *
     * @param durationSeconds Total seconds to count down from. Must be > 0.
     * @param onTick Invoked each second with the current remaining seconds.
     * @param onFinish Invoked once when remaining seconds reaches zero.
     */
    actual fun start(durationSeconds: Int, onTick: (remaining: Int) -> Unit, onFinish: () -> Unit) {
        tickJob?.cancel()
        remainingSeconds = durationSeconds

        val context = ContextProvider.context

        // Start the service if it is not already running. On resume after pause the
        // service is already alive, so this is a cheap no-op from the OS perspective.
        TimerForegroundService.start(context)

        tickJob = scope.launch {
            while (remainingSeconds > 0) {
                delay(1_000L)
                remainingSeconds--
                TimerForegroundService.updateNotification(context, remainingSeconds)
                onTick(remainingSeconds)
            }
            // Countdown complete — stop the service and notify the caller.
            TimerForegroundService.stop(context)
            onFinish()
        }
    }

    /**
     * Suspends the countdown while keeping the foreground service alive.
     *
     * The tick coroutine is canceled and [remainingSeconds] is preserved so
     * that a subsequent [start] call resumes from the correct point. The service
     * notification is updated to a "Paused" label instead of being dismissed —
     * this avoids the repeated `startForeground` round-trip on every resume.
     */
    actual fun pause() {
        tickJob?.cancel()
        tickJob = null
        // Keep the service alive; just switch the notification to a paused label.
        TimerForegroundService.showPaused(ContextProvider.context)
    }

    /**
     * Cancels the countdown, clears internal state, and stops the foreground service.
     *
     * After reset, [remainingSeconds] is 0 and a fresh [start] call is required to
     * begin a new countdown.
     */
    actual fun reset() {
        tickJob?.cancel()
        tickJob = null
        remainingSeconds = 0
        TimerForegroundService.stop(ContextProvider.context)
    }
}