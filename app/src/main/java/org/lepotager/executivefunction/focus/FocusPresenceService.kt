package org.lepotager.executivefunction.focus

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.lepotager.executivefunction.MainActivity
import org.lepotager.executivefunction.R
import org.lepotager.executivefunction.data.AppDatabase
import org.lepotager.executivefunction.domain.SessionClock
import org.lepotager.executivefunction.model.ActiveFocus
import org.lepotager.executivefunction.model.FocusStatus

/**
 * Keeps an active or interrupted focus session visible outside the app.
 *
 * The notification uses Android's system chronometer instead of a one-second
 * background loop, so the visible timer remains cheap while another app is in
 * the foreground or the screen is locked.
 */
class FocusPresenceService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SYNC -> {
                val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE).orEmpty()
                val elapsedMs = intent.getLongExtra(EXTRA_ELAPSED_MS, 0L).coerceAtLeast(0L)
                val running = intent.getBooleanExtra(EXTRA_RUNNING, false)
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification(taskTitle, elapsedMs, running),
                )
            }

            else -> {
                // START_STICKY can recreate the service with a null intent after the
                // process was reclaimed. Re-adopt foreground state immediately, then
                // reconcile against the local database off the main thread.
                startForeground(NOTIFICATION_ID, buildRestoringNotification())
                restoreFromDatabase()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun restoreFromDatabase() {
        serviceScope.launch {
            val activeFocus = runCatching {
                AppDatabase(applicationContext).activeFocus()
            }.getOrNull()

            when (activeFocus?.session?.status) {
                FocusStatus.RUNNING,
                FocusStatus.INTERRUPTED,
                -> {
                    val elapsedMs = SessionClock.elapsedMs(
                        activeFocus.session,
                        System.currentTimeMillis(),
                    )
                    val notification = buildNotification(
                        taskTitle = activeFocus.task.title,
                        elapsedMs = elapsedMs,
                        running = activeFocus.session.status == FocusStatus.RUNNING,
                    )
                    getSystemService(NotificationManager::class.java)
                        .notify(NOTIFICATION_ID, notification)
                }

                else -> {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
    }

    private fun buildNotification(
        taskTitle: String,
        elapsedMs: Long,
        running: Boolean,
    ): Notification {
        val safeTaskTitle = taskTitle.ifBlank { getString(R.string.focus_notification_unknown_task) }
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openAppPendingIntent())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setAutoCancel(false)
            .setRequestPromotedOngoing(true)
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
                .setWhen(System.currentTimeMillis() - elapsedMs)
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

        return builder.build()
    }

    private fun buildRestoringNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openAppPendingIntent())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setAutoCancel(false)
            .setContentTitle(getString(R.string.focus_notification_restoring))
            .build()

    private fun openAppPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            REQUEST_OPEN_APP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.focus_notification_channel),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.focus_notification_channel_description)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

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

    companion object {
        private const val CHANNEL_ID = "focus_presence"
        private const val NOTIFICATION_ID = 2101
        private const val REQUEST_OPEN_APP = 2102

        private const val ACTION_SYNC = "org.lepotager.executivefunction.focus.SYNC"
        private const val EXTRA_TASK_TITLE = "task_title"
        private const val EXTRA_ELAPSED_MS = "elapsed_ms"
        private const val EXTRA_RUNNING = "running"

        fun sync(context: Context, activeFocus: ActiveFocus?) {
            val status = activeFocus?.session?.status
            if (activeFocus == null || status !in setOf(FocusStatus.RUNNING, FocusStatus.INTERRUPTED)) {
                stop(context)
                return
            }

            val elapsedMs = SessionClock.elapsedMs(
                activeFocus.session,
                System.currentTimeMillis(),
            )
            val intent = Intent(context, FocusPresenceService::class.java).apply {
                action = ACTION_SYNC
                putExtra(EXTRA_TASK_TITLE, activeFocus.task.title)
                putExtra(EXTRA_ELAPSED_MS, elapsedMs)
                putExtra(EXTRA_RUNNING, status == FocusStatus.RUNNING)
            }
            ContextCompat.startForegroundService(context.applicationContext, intent)
        }

        fun stop(context: Context) {
            context.applicationContext.stopService(
                Intent(context, FocusPresenceService::class.java),
            )
        }
    }
}
