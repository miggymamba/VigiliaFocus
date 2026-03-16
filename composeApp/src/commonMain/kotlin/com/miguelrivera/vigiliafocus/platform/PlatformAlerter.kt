package com.miguelrivera.vigiliafocus.platform

/**
 * Cross-platform hardware feedback mechanism.
 *
 * Decouples audio and haptic alert logic from the presentation layer.
 * Android implements via RingtoneManager/Vibrator, iOS via AudioServices.
 */
expect class PlatformAlerter() {
    /** Plays the system default notification sound and triggers a short haptic vibration. */
    fun playCompletionAlert()
}