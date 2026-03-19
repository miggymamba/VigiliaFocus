package com.miguelrivera.vigiliafocus.platform

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder

/**
 * Android Foreground Service that keeps the application process alive during
 * an active or paused Pomodoro countdown.
 *
 * ### Lifecycle
 *
 * The service is started once when a countdown begins and stays running
 * until the countdown finishes naturally or [reset] is called. On [pause] the
 * tick coroutine is canceled but the service is **not** stopped — it keeps the
 * process alive in the background and displays a "Paused" notification label.
 * This avoids the repeated `startForeground` call that triggers an ANR on
 * Android 12+, and prevents the notification from flickering on every resume.
 *
 * ### Notification channels
 *
 * `IMPORTANCE_LOW` suppresses the audible alert that would otherwise fire on
 * every per-second `notify()` call.
 */
class TimerForegroundService : Service() {

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(contentText = "Timer Active…"))
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

    companion object {
        private const val CHANNEL_ID = "vigilia_timer_channel"
        private const val NOTIFICATION_ID = 8675309

        // ── Service lifecycle ──────────────────────────────────────────────

        /** Starts the foreground service. Safe to call when already running. */
        fun start(context: Context) {
            context.startForegroundService(Intent(context, TimerForegroundService::class.java))
        }

        /** Stops the foreground service and dismisses the notification. */
        fun stop(context: Context) {
            context.stopService(Intent(context, TimerForegroundService::class.java))
        }

        // ── Notification updates ───────────────────────────────────────────

        /**
         * Updates the live notification with the current countdown value.
         *
         * Called once per second by [PlatformTimer] while the countdown is running.
         * Using `notify()` on the existing ID replaces the notification in-place
         * without causing any audible alert (channel is `IMPORTANCE_LOW`).
         *
         * @param remainingSeconds Seconds remaining in the current interval.
         */
        fun updateNotification(context: Context, remainingSeconds: Int) {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            val timeString = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
            notify(context, contentText = "Remaining: $timeString")
        }

        /**
         * Switches the notification to a "Paused" label.
         *
         * Called by [PlatformTimer.pause] so the user sees a clear paused indicator
         * in the status bar without the service being stopped and restarted.
         */
        fun showPaused(context: Context) {
            notify(context, contentText = "Paused")
        }

        // ── Helpers ────────────────────────────────────────────────────────

        private fun notify(context: Context, contentText: String) {
            val notification = buildNotification(context, contentText)
            val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
        }

        /**
         * Builds a notification with the given [contentText].
         *
         * Two overloads exist because [onStartCommand] needs to call this before
         * `getSystemService` is available on the [Service] instance, so it passes
         * `this` as the context. Static companion calls pass the injected context.
         */
        private fun buildNotification(context: Context, contentText: String): Notification =
            Notification.Builder(context, CHANNEL_ID)
                .setContentTitle("Vigilia Focus")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build()

        // Called from onStartCommand where `this` (the Service) is the context.
        private fun Service.buildNotification(contentText: String): Notification =
            buildNotification(this, contentText)
    }
}