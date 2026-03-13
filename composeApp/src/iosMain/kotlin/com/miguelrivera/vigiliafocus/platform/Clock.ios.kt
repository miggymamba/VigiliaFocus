package com.miguelrivera.vigiliafocus.platform

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

/**
 * iOS implementation of [currentTimeMillis].
 *
 * Converts [NSDate.timeIntervalSince1970] (seconds as Double) to
 * milliseconds as Long.
 *
 * @return Epoch milliseconds from the system wall clock.
 */
actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()