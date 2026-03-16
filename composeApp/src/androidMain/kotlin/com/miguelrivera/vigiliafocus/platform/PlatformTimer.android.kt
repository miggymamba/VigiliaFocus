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
private object ContextProvider: KoinComponent {
    val context: Context by inject()
}

/**
 * Android implementation of [PlatformTimer] using Kotlin coroutines.
 *
 * A [SupervisorJob] anchors the timer's [CoroutineScope] so that
 * cancellation of the active tick job does not cancel the scope itself,
 * allowing [start] to be called again after [pause] or [reset].
 *
 * The countdown ticks every 1 000 ms on [Dispatchers.Default]. Callers
 * receive each remaining-seconds value via [onTick] and a final [onFinish]
 * callback when the countdown reaches zero.
 *
 * Lifecycle contract:
 * - [start] launches a new tick job; calling [start] while already running
 *   cancels the previous job before launching a new one.
 * - [pause] cancels the tick job and preserves [remainingSeconds].
 * - [reset] cancels the tick job and clears [remainingSeconds].
 */
actual class PlatformTimer actual constructor() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var tickJob: Job? = null
    private var remainingSeconds: Int = 0

    /**
     * Starts a countdown from [durationSeconds], invoking [onTick] once per
     * second with the updated remaining value and [onFinish] when it reaches
     * zero.
     *
     * If a countdown is already running it is canceled before the new one
     * begins, making this call idempotent for resume-after-pause scenarios
     * when the caller passes the preserved remaining seconds.
     *
     * @param durationSeconds Total seconds to count down from. Must be > 0.
     * @param onTick Invoked each second with the current remaining seconds.
     * @param onFinish Invoked once when remaining seconds reaches zero.
     */
    actual fun start(durationSeconds: Int, onTick: (remaining: Int) -> Unit, onFinish: () -> Unit) {
        tickJob?.cancel()
        remainingSeconds = durationSeconds

        val context = ContextProvider.context
        TimerForegroundService.start(context)

        tickJob = scope.launch {
            while (remainingSeconds > 0) {
                delay(1_000L)
                remainingSeconds--
                TimerForegroundService.updateNotificationChannel(context, remainingSeconds)
                onTick(remainingSeconds)
            }
            TimerForegroundService.stop(context)
            onFinish()
        }
    }

    /**
     * Suspends the countdown, preserving [remainingSeconds] for a subsequent
     * [start] call. Has no effect if the timer is not running.
     */
    actual fun pause() {
        tickJob?.cancel()
        tickJob = null
        TimerForegroundService.stop(ContextProvider.context)
    }

    /**
     * Cancels the countdown and clears internal state. After reset, [remainingSeconds]
     * is 0 and a fresh [start] call is required to begin a new countdown.
     */
    actual fun reset() {
        tickJob?.cancel()
        tickJob = null
        remainingSeconds = 0
        TimerForegroundService.stop(ContextProvider.context)
    }
}