package org.lepotager.executivefunction.data

import android.content.Context
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TaskOptionsStoreTest {
    @Test fun datesAndTimerChoicesSurviveBackupIndependentlyAndLegacyImportsStillWork() = runBlocking {
        val context: Context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("executive-function.db")
        val db = AppDatabase(context)
        try {
            val id = FocusRepository(db, Dispatchers.Unconfined).capture("Préparer", customTimer = true, timerTarget = null)
            val store = TaskOptionsStore(db)
            val today = LocalDate.of(2026, 10, 11)
            store.deadline(id, today.plusDays(30), today)
            assertEquals(14, db.taskById(id)!!.deadlineWarningDays)
            assertTrue(db.taskById(id)!!.timerOverride)
            assertNull(db.taskById(id)!!.timerTarget)
            val backup = LocalBackup(db).export()
            LocalBackup(db).clear()
            LocalBackup(db).restore(backup)
            assertEquals(today.plusDays(30).toEpochDay(), db.taskById(id)!!.deadlineDay)
            store.timer(id, 600_000L)
            assertEquals(today.plusDays(30).toEpochDay(), db.taskById(id)!!.deadlineDay)
            store.deadline(id, null)
            assertNull(db.taskById(id)!!.deadlineDay)
            assertEquals(600_000L, db.taskById(id)!!.timerTarget)
            val legacy = JSONObject(backup).put("version", 5)
            legacy.remove("task_options")
            LocalBackup(db).restore(legacy.toString())
            assertNull(db.taskById(id)!!.deadlineDay)
            assertFalse(db.taskById(id)!!.timerOverride)
        } finally { db.close() }
    }
    @Test fun versionFiveUpgradePreservesExistingTasks() = runBlocking {
        val context: Context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("executive-function.db")
        var db = AppDatabase(context)
        try {
            val id = FocusRepository(db, Dispatchers.Unconfined).capture("Déjà là")
            db.writableDatabase.execSQL("DROP TABLE task_options")
            db.writableDatabase.version = 5
            db.close()
            db = AppDatabase(context)
            assertEquals("Déjà là", db.taskById(id)!!.title)
            assertNull(db.taskById(id)!!.deadlineDay)
            assertEquals(6, db.writableDatabase.version)
        } finally { db.close() }
    }
}
