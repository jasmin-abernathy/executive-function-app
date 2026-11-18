package org.lepotager.executivefunction.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CompanionWorldStoreTest {
    private inline fun <T> withDatabase(context: Context, block: (AppDatabase) -> T): T {
        val db = AppDatabase(context)
        try {
            return block(db)
        } finally {
            db.close()
        }
    }

    @Test
    fun worldSurvivesReopenAndBackupAndCanBeCleared() {
        val context: Context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("executive-function.db")
        val preferences = CompanionPreferences(true, true, CompanionFloatingStyle.COMPANION, false, false)
        val placement = DecorationPlacement(250, 900)
        val draftId = withDatabase(context) { db ->
            val world = CompanionWorldStore(db)
            assertEquals(CompanionPreferences(), world.preferences())
            assertEquals(CompanionWorld(emptyList(), null), world.load())
            world.savePreferences(preferences)
            val id = world.addDecoration("decoration")
            assertTrue(world.placeDecoration(id, placement))
            val draft = world.beginDraft("creation")
            assertTrue(world.saveDraftProgress(draft, 240))
            assertFalse(world.placeDecoration(draft, placement))
            assertThrows(SQLiteConstraintException::class.java) { world.beginDraft("other") }
            draft
        }

        withDatabase(context) { db ->
            val world = CompanionWorldStore(db)
            val backup = LocalBackup(db)
            val before = world.load()
            assertEquals(DecorationDraft(draftId, "creation", 240), before.draft)
            assertEquals(placement, before.decorations.single().placement)

            val exported = backup.export()
            backup.clear()
            assertEquals(CompanionWorld(emptyList(), null), world.load())
            assertEquals(CompanionPreferences(), world.preferences())
            backup.restore(exported)
            assertEquals(before, world.load())
            assertEquals(preferences, world.preferences())

            val corrupt = JSONObject(exported)
            val rows = corrupt.getJSONArray("companion_decorations")
            rows.getJSONObject(0).put("x", 1001)
            assertThrows(SQLiteConstraintException::class.java) { backup.restore(corrupt.toString()) }
            assertEquals(before, world.load())
            assertEquals(preferences, world.preferences())

            assertTrue(world.finishDraft(draftId))
            assertFalse(world.finishDraft(draftId))
            assertNull(world.load().draft)
            assertTrue(world.placeDecoration(draftId, placement))
            assertTrue(world.removeDecoration(draftId))
            world.resetDecorations()
            assertEquals(CompanionWorld(emptyList(), null), world.load())
            assertEquals(preferences, world.preferences())
        }
    }

    @Test
    fun versionSixDatabaseAndBackupPreserveTasks() = runBlocking {
        val context: Context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("executive-function.db")
        val (taskId, legacyBackup) = withDatabase(context) { db ->
            val id = FocusRepository(db, Dispatchers.Unconfined).capture("Existing task")
            val legacy = JSONObject(LocalBackup(db).export()).put("version", 6)
            CompanionWorldStore.tables.forEach {
                legacy.remove(it)
                db.writableDatabase.execSQL("DROP TABLE $it")
            }
            db.writableDatabase.version = 6
            id to legacy.toString()
        }

        withDatabase(context) { db ->
            val world = CompanionWorldStore(db)
            assertEquals("Existing task", db.taskById(taskId)?.title)
            assertEquals(CompanionWorld(emptyList(), null), world.load())
            world.beginDraft("creation")
            LocalBackup(db).restore(legacyBackup)
            assertEquals("Existing task", db.taskById(taskId)?.title)
            assertEquals(CompanionWorld(emptyList(), null), world.load())
        }
    }
}
