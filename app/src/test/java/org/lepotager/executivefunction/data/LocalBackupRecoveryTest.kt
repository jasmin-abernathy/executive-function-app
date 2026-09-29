package org.lepotager.executivefunction.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class LocalBackupRecoveryTest {
    @Test fun exportedDataCanBeRecoveredAfterEraseAndInvalidRestoreKeepsRecoveredData() = runBlocking {
        val context: Context = RuntimeEnvironment.getApplication()
        val database = AppDatabase(context)
        context.deleteDatabase(database.databaseName)
        try {
            val repository = FocusRepository(database, Dispatchers.Unconfined, now = { 1_000_000L })
            repository.capture("Retrouver cette tâche")
            val taskId = repository.snapshot.value.tasks.single().id
            repository.start(taskId, 300_000L)
            repository.interrupt("Reprendre après la pause")
            val backup = LocalBackup(database)
            val exported = backup.export()

            backup.clear()
            assertNull(database.taskById(taskId))
            assertNull(database.activeFocus())

            backup.restore(exported)
            assertEquals("Retrouver cette tâche", database.taskById(taskId)?.title)
            assertEquals("Reprendre après la pause", database.activeFocus()?.session?.interruptionNote)
            assertEquals(300_000L, database.activeFocus()?.session?.targetDurationMs)

            val corrupted = JSONObject(exported).apply {
                getJSONArray("focus_sessions").getJSONObject(0).put("task_id", "missing-task")
            }.toString()
            assertThrows(Exception::class.java) { backup.restore(corrupted) }
            assertNotNull(database.taskById(taskId))
            assertNotNull(database.activeFocus())
            database.readableDatabase.rawQuery("PRAGMA foreign_key_check", null).use {
                assertFalse(it.moveToFirst())
            }
        } finally {
            database.close()
            context.deleteDatabase(database.databaseName)
        }
    }
}
