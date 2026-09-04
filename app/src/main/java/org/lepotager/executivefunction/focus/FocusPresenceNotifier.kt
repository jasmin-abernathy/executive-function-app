package org.lepotager.executivefunction.focus

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.util.Locale
import org.lepotager.executivefunction.MainActivity
import org.lepotager.executivefunction.R
import org.lepotager.executivefunction.domain.SessionClock
import org.lepotager.executivefunction.model.ActiveFocus
import org.lepotager.executivefunction.model.FocusStatus
import org.lepotager.executivefunction.navigation.ExternalLaunchAction

/**
 * Mirrors the local focus state into one lightweight Android notification.
 *
 * There is deliberately no background tick loop: SystemUI renders the running
 * chronometer from a base timestamp. The database remains authoritative.
 */
class FocusPresenceNotifier(private val context: Context) {
    private val appContext = context.applicationContext
    private val notificationManager = appContext.getSystemService(NotificationManager::class.java)
    private val preferences = FocusSurfacePreferences(appContext)

    init {
        createNotificationChannel()
    }

    fun sync(activeFocus: ActiveFocus?) {
        val status = activeFocus?.session?.status
        if (activeFocus == null || status !in setOf(FocusStatus.RUNNING, FocusStatus.INTERRUPTED)) {
            notificationManager.cancel(NOTIFICATION_ID)
            return
        }

        if (!canPostNotifications()) {
            // Focus never depends on notification permission. If permission is granted
            // later, the activity asks for a fresh sync.
            notificationManager.cancel(NOTIFICATION_ID)
            return
        }

        val elapsedMs = SessionClock.elapsedMs(
            activeFocus.session,
            System.currentTimeMillis(),
        )
        notificationManager.notify(
            NOTIFICATION_ID,
            buildNotification(
                taskTitle = activeFocus.task.title,
                elapsedMs = elapsedMs,
                running = status == FocusStatus.RUNNING,
            ),
        )
    }

    private fun buildNotification(
        taskTitle: String,
        elapsedMs: Long,
        running: Boolean,
    ): Notification {
        val safeTaskTitle = taskTitle.ifBlank { getString(R.string.focus_notification_unknown_task) }
        val builder = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(openAppPendingIntent())
            .setOngoing(running)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setAutoCancel(false)
            .setContentTitle(
                getString(
                    if (running) {
                        R.string.focus_notification_running
                    } else {
                        R.string.focus_notification_interrupted
                    },
                ),
            )

        if (running) {
            builder
                .setContentText(safeTaskTitle)
                .setWhen(System.currentTimeMillis() - elapsedMs.coerceAtLeast(0L))
                .setUsesChronometer(true)
                .setShowWhen(true)
        } else {
            builder
                .setContentText(
                    getString(
                        R.string.focus_notification_interrupted_detail,
                        safeTaskTitle,
                        formatElapsed(elapsedMs),
                    ),
                )
                .setUsesChronometer(false)
                .setShowWhen(false)
        }

        if (preferences.load().notificationActions) {
            addActions(builder, running)
        }

        return builder.build()
    }

    private fun addActions(builder: NotificationCompat.Builder, running: Boolean) {
        if (running) {
            builder.addAction(
                android.R.drawable.ic_media_pause,
                getString(R.string.focus_notification_action_interrupt),
                broadcastPendingIntent(
                    FocusNotificationActionReceiver.ACTION_INTERRUPT,
                    REQUEST_INTERRUPT,
                ),
            )
        } else {
            builder.addAction(
                android.R.drawable.ic_media_play,
                getString(R.string.focus_notification_action_resume),
                broadcastPendingIntent(
                    FocusNotificationActionReceiver.ACTION_RESUME,
                    REQUEST_RESUME,
                ),
            )
        }

        builder.addAction(
            android.R.drawable.ic_menu_add,
            getString(R.string.focus_notification_action_capture),
            launchPendingIntent(ExternalLaunchAction.CAPTURE, REQUEST_CAPTURE),
        )

        if (running) {
            builder.addAction(
                android.R.drawable.ic_menu_view,
                getString(R.string.focus_notification_action_pip),
                launchPendingIntent(ExternalLaunchAction.PICTURE_IN_PICTURE, REQUEST_PIP),
            )
        }

        builder.addAction(
            android.R.drawable.ic_menu_save,
            getString(R.string.focus_notification_action_complete),
            broadcastPendingIntent(
                FocusNotificationActionReceiver.ACTION_COMPLETE,
                REQUEST_COMPLETE,
            ),
        )
    }

    private fun openAppPendingIntent(): PendingIntent {
        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            appContext,
            REQUEST_OPEN_APP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun launchPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(ExternalLaunchAction.EXTRA, action)
        }
        return PendingIntent.getActivity(
            appContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun broadcastPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(appContext, FocusNotificationActionReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            appContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.focus_notification_channel),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.focus_notification_channel_description)
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    private fun formatElapsed(elapsedMs: Long): String {
        val totalSeconds = elapsedMs.coerceAtLeast(0L) / 1_000L
        val hours = totalSeconds / 3_600L
        val minutes = (totalSeconds % 3_600L) / 60L
        val seconds = totalSeconds % 60L
        return if (hours > 0L) {
            String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
        }
    }

    private fun getString(resId: Int, vararg formatArgs: Any): String =
        appContext.getString(resId, *formatArgs)

    companion object {
        private const val CHANNEL_ID = "focus_presence"
        private const val NOTIFICATION_ID = 2101
        private const val REQUEST_OPEN_APP = 2102
        private const val REQUEST_INTERRUPT = 2103
        private const val REQUEST_RESUME = 2104
        private const val REQUEST_CAPTURE = 2105
        private const val REQUEST_PIP = 2106
        private const val REQUEST_COMPLETE = 2107
    }
}
