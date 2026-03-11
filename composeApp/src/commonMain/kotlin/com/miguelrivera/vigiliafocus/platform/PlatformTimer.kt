package com.miguelrivera.vigiliafocus.platform

/**
 * Platform-specific timer mechanism.
 *
 * Each target platform provides a concrete implementation that drives
 * the countdown tick at a one-second interval. The expect declaration
 * enforces a consistent contract without coupling the domain layer to
 * any platform API.
 *
 * Implementations reside in `androidMain` and `iosMain`.
 */

expect class PlatformTimer() {

    /** Starts the countdown, invoking [onTick] each second and [onFinish] when it reaches zero. */
    fun start(durationSeconds: Int, onTick: (remaining: Int) -> Unit, onFinish: () -> Unit)

    /** Suspends the countdown, preserving the remaining duration. */
    fun pause()

    /** Cancels the countdown and resets internal state. */
    fun reset()
}