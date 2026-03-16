package com.miguelrivera.vigiliafocus.platform

import android.Manifest
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Internal dependency resolver for acquiring the Android application context.
 */
private object AlertContextProvider : KoinComponent {
    val context: Context by inject()
}

/**
 * Android implementation of [PlatformAlerter].
 * Triggers the default system notification ringtone and a standard 500ms vibration.
 */
actual class PlatformAlerter actual constructor(){
    @RequiresPermission(Manifest.permission.VIBRATE)
    actual fun playCompletionAlert() {
        val context = AlertContextProvider.context

        // 1. Play default notification sound
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, uri)
            ringtone.play()
        } catch (e: Exception) {
            // Failsafe: if the device is muted or missing the ringtone, don't crash
        }

        // 2. Trigger haptic vibration
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))

    }
}