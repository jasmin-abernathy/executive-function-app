package org.lepotager.executivefunction.notification

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
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import org.lepotager.executivefunction.MainActivity
import org.lepotager.executivefunction.R
import org.lepotager.executivefunction.model.ActiveFocus

/**
 * Mirrors the persisted focus state into one quiet system notification.
 *
 * The clock itself remains timestamp-based in the domain model. Android SystemUI
 * renders the chronometer, so this class does not poll or wake once per second.
 */
class FocusNotificationManager(
    private val context: Context,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createChannel()
    }

    fun sync(activeFocus: ActiveFocus?) {
        val state = FocusNotificationStateFactory.create(activeFocus, now())
        if (state.kind == FocusNotificationKind.HIDDEN || !canPostNotifications()) {
            cancel()
            return
        }

        notificationManager.notify(NOTIFICATION_ID, buildNotification(state))
    }

    fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun buildNotification(state: FocusNotificationState): Notification {
        val title = state.taskTitle.orEmpty()
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_focus_status)
            .setContentTitle(title)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openAppIntent())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setAutoCancel(false)
            .setShowWhen(true)

        when (state.kind) {
            FocusNotificationKind.RUNNING -> {
                builder
                    .setContentText(context.getString(R.string.focus_notification_running))
                    .setWhen(now() - state.elapsedMs)
                    .setUsesChronometer(true)
            }

            FocusNotificationKind.RESUMABLE -> {
                builder
                    .setContentText(
                        context.getString(
                            R.string.focus_notification_resumable,
                            formatElapsed(state.elapsedMs),
                        ),
                    )
                    .setUsesChronometer(false)
                    .setShowWhen(false)
            }

            FocusNotificationKind.HIDDEN -> Unit
        }

        return builder.build()
    }

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_APP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun canPostNotifications(): Boolean {
        if (!notificationManager.areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.focus_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.focus_notification_channel_description)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    private fun formatElapsed(elapsedMs: Long): String {
        val totalSeconds = (elapsedMs / 1_000).coerceAtLeast(0)
        val hours = totalSeconds / 3_600
        val minutes = (totalSeconds % 3_600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    private companion object {
        const val CHANNEL_ID = "focus_session"
        const val NOTIFICATION_ID = 1201
        const val REQUEST_CODE_OPEN_APP = 1201
    }
}
