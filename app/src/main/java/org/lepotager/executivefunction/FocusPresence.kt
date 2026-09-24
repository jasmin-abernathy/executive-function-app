package org.lepotager.executivefunction

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.lepotager.executivefunction.data.AppDatabase
import org.lepotager.executivefunction.domain.FocusTransitions
import org.lepotager.executivefunction.domain.PauseReminderTimes
import org.lepotager.executivefunction.domain.SessionClock
import org.lepotager.executivefunction.model.ActiveFocus
import org.lepotager.executivefunction.model.FocusStatus
import org.lepotager.executivefunction.model.TaskStatus
import java.util.concurrent.Executors

internal object FocusPresence {
    const val ID=3107
    const val CHANGED="org.lepotager.executivefunction.FOCUS_CHANGED"
    private const val CHANNEL="active_focus"
    fun sync(context: Context,active: ActiveFocus?) {
        val manager=context.getSystemService(NotificationManager::class.java)
        if(active==null) {manager.cancel(ID);PauseSchedule.cancel(context);return}
        PauseSchedule.sync(context,active)
        if(Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) return
        manager.notify(ID,build(context,active))
    }
    fun build(context: Context,active: ActiveFocus): Notification {
        val manager=context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL,context.getString(R.string.focus_notification_channel),NotificationManager.IMPORTANCE_LOW))
        val running=active.session.status==FocusStatus.RUNNING
        val elapsed=SessionClock.elapsedMs(active.session,System.currentTimeMillis())
        val target=active.session.targetDurationMs
        val open=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(context,CHANNEL)
            .setSmallIcon(R.drawable.ic_timer_notification)
            .setContentTitle(context.getString(R.string.focus_notification_channel))
            .setContentText(context.getString(if(running) R.string.floating_timer_running else R.string.resume_action))
            .setContentIntent(open).setOngoing(running).setSilent(true).setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setUsesChronometer(running)
            .setWhen(System.currentTimeMillis() - elapsed + (target ?: 0L))
            .setChronometerCountDown(running && target!=null && target>elapsed)
            .addAction(0,context.getString(if(running) R.string.interrupt_action else R.string.resume_action),action(context,active,if(running) "pause" else "resume"))
            .addAction(0,context.getString(R.string.complete_action),action(context,active,"complete"))
            .build()
    }
    fun action(context: Context,active: ActiveFocus,action: String): PendingIntent = PendingIntent.getBroadcast(
        context,action.hashCode(),Intent(context,FocusActionReceiver::class.java).setAction(action).putExtra("session",active.session.id),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

internal object PauseSchedule {
    private fun prefs(context: Context)=context.getSharedPreferences("pause_schedule",Context.MODE_PRIVATE)
    fun deadline(context: Context,active: ActiveFocus,minutes: Int): Long {
        val p=prefs(context)
        val key=active.session.id
        // The first prompt is measured from the beginning of the session, even if the
        // activity is reopened much later. Resumed sessions get an explicit new deadline.
        if(!p.contains(key)) p.edit().putLong(key,PauseReminderTimes.first(minutes)).apply()
        val saved=p.getLong(key,Long.MAX_VALUE)
        // Earlier builds wrote MAX_VALUE when the user chose "Continue". Recover that
        // state so existing long-running sessions receive reminders again.
        if(saved==Long.MAX_VALUE) {
            val next=PauseReminderTimes.afterContinue(SessionClock.elapsedMs(active.session,System.currentTimeMillis()),minutes)
            p.edit().putLong(key,next).apply()
            return next
        }
        return saved
    }
    fun set(context: Context,active: ActiveFocus,value: Long) {
        prefs(context).edit().putLong(active.session.id,value).apply()
        context.getSystemService(NotificationManager::class.java).cancel(3108)
        sync(context,active)
    }
    private fun pending(context: Context)=PendingIntent.getBroadcast(context,3108,Intent(context,FocusActionReceiver::class.java).setAction("pause_prompt"),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(pending(context))
        context.getSystemService(NotificationManager::class.java).cancel(3108)
    }
    fun sync(context: Context,active: ActiveFocus) {
        cancel(context)
        val settings=context.getSharedPreferences("app_preferences",Context.MODE_PRIVATE)
        if(active.session.status!=FocusStatus.RUNNING || !settings.getBoolean("pause_suggestions_enabled",true)) return
        val due=deadline(context,active,settings.getInt("pause_after_minutes",25))
        if(due==Long.MAX_VALUE) return
        val left=due-SessionClock.elapsedMs(active.session,System.currentTimeMillis())
        // Inexact, user-requested reminder: no exact-alarm permission, no polling.
        context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+left.coerceAtLeast(1000),pending(context))
    }
    fun show(context: Context,active: ActiveFocus) {
        if(active.session.status!=FocusStatus.RUNNING) return
        val settings=context.getSharedPreferences("app_preferences",Context.MODE_PRIVATE)
        if(!settings.getBoolean("pause_suggestions_enabled",true)) return
        if(deadline(context,active,settings.getInt("pause_after_minutes",25))>SessionClock.elapsedMs(active.session,System.currentTimeMillis())) return
        if(Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) return
        val base=FocusPresence.build(context,active)
        val notification=NotificationCompat.Builder(context,"active_focus")
            .setSmallIcon(R.drawable.ic_timer_notification).setContentTitle(context.getString(R.string.pause_suggestion_title))
            .setContentText(context.getString(R.string.pause_suggestion_message,settings.getInt("pause_after_minutes",25)))
            .setContentIntent(base.contentIntent).setAutoCancel(true)
            .addAction(0,context.getString(R.string.take_a_pause),FocusPresence.action(context,active,"pause"))
            .addAction(0,context.getString(R.string.remind_pause_later),FocusPresence.action(context,active,"later"))
            .addAction(0,context.getString(R.string.continue_without_pause),FocusPresence.action(context,active,"continue"))
            .build()
        context.getSystemService(NotificationManager::class.java).notify(3108,notification)
    }
}

class FocusActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context,intent: Intent) {
        val pending=goAsync()
        executor.execute {
            val db=AppDatabase(context.applicationContext)
            try {
                val active=db.activeFocus() ?: return@execute
                if(intent.action=="pause_prompt") {PauseSchedule.show(context,active);return@execute}
                if(intent.getStringExtra("session")!=active.session.id) return@execute
                when(intent.action) {
                    "pause" -> if(active.session.status==FocusStatus.RUNNING) db.interruptFocus(FocusTransitions.interrupt(active.session,System.currentTimeMillis(),null))
                    "resume" -> if(active.session.status==FocusStatus.INTERRUPTED) {
                        db.resumeFocus(FocusTransitions.resume(active.session,System.currentTimeMillis()),active.task.firstStep)
                        val updated=db.activeFocus()!!
                        val minutes=context.getSharedPreferences("app_preferences",Context.MODE_PRIVATE).getInt("pause_after_minutes",25)
                        PauseSchedule.set(context,updated,SessionClock.elapsedMs(updated.session,System.currentTimeMillis())+minutes*60_000L)
                    }
                    "complete" -> db.closeFocus(FocusTransitions.finish(active.session,System.currentTimeMillis(),FocusStatus.COMPLETED),TaskStatus.COMPLETED)
                    "later" -> PauseSchedule.set(context,active,PauseReminderTimes.afterSnooze(SessionClock.elapsedMs(active.session,System.currentTimeMillis())))
                    "continue" -> {
                        val minutes=context.getSharedPreferences("app_preferences",Context.MODE_PRIVATE).getInt("pause_after_minutes",25).coerceIn(5,120)
                        PauseSchedule.set(context,active,PauseReminderTimes.afterContinue(SessionClock.elapsedMs(active.session,System.currentTimeMillis()),minutes))
                    }
                }
                FocusPresence.sync(context,db.activeFocus())
            } catch (_: Exception) {
                // A stale/invalid action must not crash the app or expose task text.
            } finally {
                db.close()
                if(intent.action!="pause_prompt") context.sendBroadcast(Intent(FocusPresence.CHANGED).setPackage(context.packageName))
                pending.finish()
            }
        }
    }
    companion object {private val executor=Executors.newSingleThreadExecutor()}
}
