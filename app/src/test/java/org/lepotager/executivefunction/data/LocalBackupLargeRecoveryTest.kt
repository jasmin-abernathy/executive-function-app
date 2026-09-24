package org.lepotager.executivefunction.data

import android.content.ContentValues
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class LocalBackupLargeRecoveryTest {
    @Test fun realLocalNotesAboveOldLimitCanBeExportedErasedAndRestored() {
        val context: Context = RuntimeEnvironment.getApplication()
        val database = AppDatabase(context)
        context.deleteDatabase(database.databaseName)
        try {
            // Several ordinary-size rows avoid CursorWindow limits while the
            // complete exported file exceeds the old import ceiling.
            val note = "n".repeat(400_000)
            repeat(30) { index ->
                database.writableDatabase.insertOrThrow("quick_notes", null, ContentValues().apply {
                    put("id", "large-note-$index")
                    put("body", note)
                    put("created_at", 1_000_000L + index)
                })
            }
            val backup = LocalBackup(database)
            val exported = backup.export()
            assertTrue(exported.toByteArray(Charsets.UTF_8).size > 10_000_000)
            assertTrue(exported.toByteArray(Charsets.UTF_8).size <= LocalBackup.MAX_BACKUP_BYTES)

            backup.clear()
            backup.restore(exported)
            database.readableDatabase.rawQuery("SELECT COUNT(*), MIN(LENGTH(body)), MAX(LENGTH(body)) FROM quick_notes", null).use {
                assertTrue(it.moveToFirst())
                assertEquals(30, it.getInt(0))
                assertEquals(note.length, it.getInt(1))
                assertEquals(note.length, it.getInt(2))
            }
        } finally {
            database.close()
            context.deleteDatabase(database.databaseName)
        }
    }
}
