package com.miguelrivera.vigiliafocus.platform

/**
 * Returns the current time in milliseconds since the Unix epoch.
 *
 * Platform-specific implementations delegate to the native clock:
 * - Android: [System.currentTimeMillis]
 * - iOS: `NSDate().timeIntervalSince1970 * 1000`
 *
 * Used by the domain layer to timestamp completed [Session] records
 * without coupling shared code to any platform API.
 *
 * @return Epoch milliseconds as a [Long].
 */
expect fun currentTimeMillis(): Long