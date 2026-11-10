package org.lepotager.executivefunction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.lepotager.executivefunction.data.AppDatabase
import org.lepotager.executivefunction.model.TaskItem
import org.lepotager.executivefunction.model.TaskStatus
import java.util.UUID
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TaskReminderIntentTest {
    @Test fun parentTasksNeverOfferUnsafeImmediateCompletion() {
        val context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("executive-function.db")
        val db = AppDatabase(context)
        try {
            val id = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            db.insertTask(TaskItem(id,"Préparer",null,TaskStatus.READY,now,now))
            assertFalse(TaskReminder.needsCompletionReview(db,id))
            db.writableDatabase.execSQL(
                "INSERT INTO task_steps (id,task_id,title,position,completed) VALUES (?,?,?,?,?)",
                arrayOf(UUID.randomUUID().toString(),id,"Premier geste",0,0)
            )
            assertTrue(TaskReminder.needsCompletionReview(db,id))
            // Even the last checked small step still requires explicit parent confirmation.
            db.writableDatabase.execSQL("UPDATE task_steps SET completed=1 WHERE task_id=?",arrayOf(id))
            assertTrue(TaskReminder.needsCompletionReview(db,id))
        } finally { db.close() }
    }

    @Test fun snoozePresetsReplaceTheSameLocalReminder() {
        val context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("executive-function.db")
        val db = AppDatabase(context)
        try {
            val id = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            db.insertTask(TaskItem(id,"À faire",null,TaskStatus.READY,now,now))
            TaskReminder.set(context,db,id,10)
            val first = TaskReminder.pendingAt(db,id)
            assertNotNull(first)
            TaskReminder.set(context,db,id,60)
            val second = TaskReminder.pendingAt(db,id)
            assertTrue(requireNotNull(second) > requireNotNull(first))
            val count = db.readableDatabase.rawQuery("SELECT COUNT(*) FROM task_reminders WHERE task_id=?",arrayOf(id)).use {
                it.moveToFirst(); it.getInt(0)
            }
            assertEquals(1,count)
            TaskReminder.cancel(context,db,id)
            assertNull(TaskReminder.pendingAt(db,id))
        } finally { db.close() }
    }

    @Test fun tappingTheReminderShowsDetailsWhileStartIsSeparate() {
        val context = RuntimeEnvironment.getApplication()
        val details = TaskReminder.taskIntent(context,"task-abc",false)
        val start = TaskReminder.taskIntent(context,"task-abc",true)
        assertEquals("task-abc",details.getStringExtra("open_task_details"))
        assertNull(details.getStringExtra("requested_task"))
        assertEquals("task-abc",start.getStringExtra("requested_task"))
        assertNull(start.getStringExtra("open_task_details"))
        assertNotEquals(details.data,start.data)
    }
}
