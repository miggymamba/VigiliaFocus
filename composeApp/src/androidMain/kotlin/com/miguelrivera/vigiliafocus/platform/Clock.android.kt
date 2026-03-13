package com.miguelrivera.vigiliafocus.platform

/**
 * Android implementation of [currentTimeMillis].
 *
 * Delegates directly to [System.currentTimeMillis].
 *
 * @return Epoch milliseconds from the system wall clock.
 */
actual fun currentTimeMillis(): Long = System.currentTimeMillis()