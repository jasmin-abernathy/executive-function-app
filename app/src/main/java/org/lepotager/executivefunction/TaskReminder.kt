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
import java.util.concurrent.Executors

internal object TaskReminder {
    private fun alarm(context: Context,id: String)=PendingIntent.getBroadcast(context,0,
        Intent(context,TaskReminderReceiver::class.java).setAction("notify").setData(Uri.Builder().scheme("task-reminder").authority("local").appendPath(id).build()),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    fun set(context: Context,db: AppDatabase,id: String,minutes: Int) {
        require(minutes in 1..10080)
        requireNotNull(db.taskById(id))
        val due=System.currentTimeMillis()+minutes*60_000L
        db.writableDatabase.insertWithOnConflict("task_reminders",null,ContentValues().apply{put("task_id",id);put("due_at",due)},android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
        context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,due,alarm(context,id))
    }
    fun cancel(context: Context,db: AppDatabase,id: String) {
        db.writableDatabase.delete("task_reminders","task_id=?",arrayOf(id))
        context.getSystemService(AlarmManager::class.java).cancel(alarm(context,id))
        context.getSystemService(NotificationManager::class.java).cancel(id,3200)
    }
    fun restore(context: Context,db: AppDatabase) {
        db.readableDatabase.rawQuery("SELECT task_id,due_at FROM task_reminders",null).use {c ->while(c.moveToNext()) context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,c.getLong(1).coerceAtLeast(System.currentTimeMillis()+1000),alarm(context,c.getString(0)))}
    }
    fun notify(context: Context,db: AppDatabase,id: String) {
        val task=db.taskById(id) ?: return
        if(task.status==TaskStatus.COMPLETED) {cancel(context,db,id);return}
        val exists=db.readableDatabase.rawQuery("SELECT 1 FROM task_reminders WHERE task_id=?",arrayOf(id)).use {it.moveToFirst()}
        if(!exists) return
        db.writableDatabase.delete("task_reminders","task_id=?",arrayOf(id))
        if(Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) return
        val manager=context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("task_reminders",context.getString(R.string.task_reminder),NotificationManager.IMPORTANCE_DEFAULT))
        val open=PendingIntent.getActivity(context,id.hashCode(),Intent(context,MainActivity::class.java).putExtra("requested_task",id),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        fun action(name: String)=PendingIntent.getBroadcast(context,0,Intent(context,TaskReminderReceiver::class.java).setAction(name).setData(Uri.Builder().scheme("task-reminder").authority("local").appendPath(id).build()),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        manager.notify(id,3200,NotificationCompat.Builder(context,"task_reminders")
            .setSmallIcon(R.drawable.ic_timer_notification).setContentTitle(context.getString(R.string.task_reminder)).setContentText(task.title)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setContentIntent(open).setAutoCancel(true)
            .addAction(0,context.getString(R.string.start_action),open)
            .addAction(0,context.getString(R.string.complete_action),action("complete"))
            .addAction(0,context.getString(R.string.not_now),action("later")).build())
    }
}

class TaskReminderReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context,intent: Intent) {
        val result=goAsync()
        executor.execute {
            val db=AppDatabase(context.applicationContext)
            try {
                if(intent.action==Intent.ACTION_BOOT_COMPLETED) {
                    TaskReminder.restore(context,db);FocusPresence.sync(context,db.activeFocus());return@execute
                }
                val id=intent.data?.lastPathSegment ?: return@execute
                when(intent.action) {
                    "notify" -> TaskReminder.notify(context,db,id)
                    "later" -> {TaskReminder.set(context,db,id,10);context.getSystemService(NotificationManager::class.java).cancel(id,3200)}
                    "complete" -> {
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
