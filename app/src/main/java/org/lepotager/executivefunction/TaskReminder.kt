package org.lepotager.executivefunction

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.lepotager.executivefunction.data.AppDatabase
import org.lepotager.executivefunction.model.TaskStatus
import org.lepotager.executivefunction.domain.ReminderTiming
import java.util.concurrent.Executors

internal object TaskReminder {
    /**
     * Never consume a queued reminder when Android prevents its delivery.
     * A later app opening can re-arm the locally stored reminder.
     */
    private fun notificationsAllowed(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return false
        val manager = context.getSystemService(NotificationManager::class.java)
        if (!manager.areNotificationsEnabled()) return false
        val channel = manager.getNotificationChannel("task_reminders")
        return channel == null || channel.importance != NotificationManager.IMPORTANCE_NONE
    }

    private fun alarm(context: Context,id: String)=PendingIntent.getBroadcast(context,0,
        Intent(context,TaskReminderReceiver::class.java).setAction("notify").setData(Uri.Builder().scheme("task-reminder").authority("local").appendPath(id).build()),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    fun set(context: Context,db: AppDatabase,id: String,minutes: Int) {
        require(minutes in 1..10080)
        requireNotNull(db.taskById(id))
        val due=System.currentTimeMillis()+minutes*60_000L
        db.writableDatabase.insertWithOnConflict("task_reminders",null,ContentValues().apply{put("task_id",id);put("due_at",due)},android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
        context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,due,alarm(context,id))
    }
    /** A parent with any small steps requires explicit completion in the UI. */
    internal fun needsCompletionReview(db: AppDatabase, id: String): Boolean =
        db.readableDatabase.rawQuery("SELECT 1 FROM task_steps WHERE task_id=? LIMIT 1", arrayOf(id)).use { it.moveToFirst() }

    internal fun pendingAt(db: AppDatabase, id: String): Long? =
        db.readableDatabase.rawQuery("SELECT due_at FROM task_reminders WHERE task_id=?", arrayOf(id)).use { c ->
            if (c.moveToFirst()) c.getLong(0) else null
        }

    fun cancel(context: Context,db: AppDatabase,id: String) {
        db.writableDatabase.delete("task_reminders","task_id=?",arrayOf(id))
        context.getSystemService(AlarmManager::class.java).cancel(alarm(context,id))
        context.getSystemService(NotificationManager::class.java).cancel(id,3200)
    }
    fun restore(context: Context,db: AppDatabase) {
        if (!notificationsAllowed(context)) return
        val now = System.currentTimeMillis()
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        db.readableDatabase.rawQuery("SELECT task_id,due_at FROM task_reminders",null).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val due = ReminderTiming.nextDeliveryAt(cursor.getLong(1), now)
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, due, alarm(context, id))
            }
        }
    }
    /** Tapping the reminder shows context; only its Start action requests a timer. */
    internal fun taskIntent(context: Context, id: String, start: Boolean): Intent =
        Intent(context, MainActivity::class.java)
            .setData(Uri.Builder().scheme("task-context").authority("local")
                .appendPath(if (start) "start" else "details").appendPath(id).build())
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(if (start) "requested_task" else "open_task_details", id)

    private fun taskPendingIntent(context: Context, id: String, start: Boolean): PendingIntent =
        PendingIntent.getActivity(context, 0, taskIntent(context, id, start),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    fun notify(context: Context,db: AppDatabase,id: String) {
        val task=db.taskById(id) ?: return
        if(task.status==TaskStatus.COMPLETED) {cancel(context,db,id);return}
        val exists=db.readableDatabase.rawQuery("SELECT 1 FROM task_reminders WHERE task_id=?",arrayOf(id)).use {it.moveToFirst()}
        if(!exists) return
        if (!notificationsAllowed(context)) return
        val manager=context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("task_reminders",context.getString(R.string.task_reminder),NotificationManager.IMPORTANCE_DEFAULT))
        val open=taskPendingIntent(context,id,false)
        val start=taskPendingIntent(context,id,true)
        fun action(name: String)=PendingIntent.getBroadcast(context,0,Intent(context,TaskReminderReceiver::class.java).setAction(name).setData(Uri.Builder().scheme("task-reminder").authority("local").appendPath(id).build()),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(context,"task_reminders")
            .setSmallIcon(R.drawable.ic_timer_notification).setContentTitle(context.getString(R.string.task_reminder)).setContentText(task.title)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setContentIntent(open).setAutoCancel(true)
            .addAction(0,context.getString(R.string.start_action),start)
        // Never offer an instant "Done" for a parent with small steps.
        if (!needsCompletionReview(db,id)) {
            notification.addAction(0,context.getString(R.string.complete_action),action("complete"))
        }
        notification.addAction(0,context.getString(R.string.not_now),action("later"))
        manager.notify(id,3200,notification.build())
        // Remove only after notifying succeeds; blocked notifications must not lose the reminder.
        db.writableDatabase.delete("task_reminders","task_id=?",arrayOf(id))
    }
}

class TaskReminderReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context,intent: Intent) {
        val result=goAsync()
        executor.execute {
            val db=AppDatabase(context.applicationContext)
            try {
                if(intent.action==Intent.ACTION_BOOT_COMPLETED || intent.action=="restore") {
                    TaskReminder.restore(context,db)
                    if (intent.action==Intent.ACTION_BOOT_COMPLETED) FocusPresence.sync(context,db.activeFocus())
                    return@execute
                }
                val id=intent.data?.lastPathSegment ?: return@execute
                when(intent.action) {
                    "notify" -> TaskReminder.notify(context,db,id)
                    "later" -> {
                        val task = db.taskById(id)
                        if (task == null || task.status == TaskStatus.COMPLETED) {
                            TaskReminder.cancel(context,db,id)
                        } else {
                            TaskReminder.set(context,db,id,10)
                        }
                        context.getSystemService(NotificationManager::class.java).cancel(id,3200)
                    }
                    "complete" -> {
                        if (TaskReminder.needsCompletionReview(db,id)) {
                            // Old notifications may still expose the previous action.
                            // Open details instead of silently closing the parent.
                            context.getSystemService(NotificationManager::class.java).cancel(id,3200)
                            context.startActivity(
                                TaskReminder.taskIntent(context,id,false).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                            return@execute
                        }
                        val active=db.activeFocus()
                        if(active?.task?.id==id) {
                            db.closeFocus(org.lepotager.executivefunction.domain.FocusTransitions.finish(active.session,System.currentTimeMillis(),org.lepotager.executivefunction.model.FocusStatus.COMPLETED),TaskStatus.COMPLETED)
                            FocusPresence.sync(context,db.activeFocus())
                        } else {
                            db.writableDatabase.update("tasks",ContentValues().apply {put("status",TaskStatus.COMPLETED.name);put("updated_at",System.currentTimeMillis())},"id=?",arrayOf(id))
                        }
                        TaskReminder.cancel(context,db,id)
                    }
                }
            } catch (_: Exception) { } finally {
                db.close()
                if(intent.action=="complete") context.sendBroadcast(Intent(FocusPresence.CHANGED).setPackage(context.packageName))
                result.finish()
            }
        }
    }
    companion object {private val executor=Executors.newSingleThreadExecutor()}
}
