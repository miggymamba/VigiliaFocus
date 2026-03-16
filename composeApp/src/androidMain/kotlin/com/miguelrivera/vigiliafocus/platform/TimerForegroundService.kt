package com.miguelrivera.vigiliafocus.platform

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder

/**
 * Android Foreground Service responsible for keeping the application process alive
 * during an active countdown.
 *
 * Establishes a persistent notification in the system tray, fulfilling the Android
 * OS requirement for long-running background work and ensuring Doze mode does not
 * arbitrarily terminate the coroutine ticker.
 */
class TimerForegroundService : Service(){
    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildInitialNotification())
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Timer Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Displays the active countdown timer."
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun buildInitialNotification(): Notification {
        // Utilizing native Notification.Builder since minSdk >= 26
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Vigilia Focus")
            .setContentText("Timer Active....")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "vigilia_timer_channel"
        private const val NOTIFICATION_ID = 8675309

        /** Initiates the foreground service. */
        fun start(context: Context) {
            val intent = Intent(context, TimerForegroundService::class.java)
            context.startForegroundService(intent)
        }

        /** Terminates the foreground service and removes the notification. */
        fun stop(context: Context) {
            val intent = Intent(context, TimerForegroundService::class.java)
            context.stopService(intent)
        }

        /** Updates the existing notification payload with the current countdown state. */
        fun updateNotificationChannel(context: Context, remainingSeconds: Int) {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            val timeString = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"

            val notification = Notification.Builder(context, CHANNEL_ID)
                .setContentTitle("Vigilia Focus")
                .setContentText("Remaining time: $timeString")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build()

            val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
        }
    }
}