package org.lepotager.executivefunction.data

import android.content.Context
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
class WorkspaceStoreTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()

    @Test fun foldersAndNotesSurviveReopenAndRemovingFolderPreservesContents() = runBlocking {
        context.deleteDatabase("executive-function.db")
        var db = AppDatabase(context)
        try {
            var store = WorkspaceStore(db)
            val folder = store.createFolder("Projet")
            FocusRepository(db, Dispatchers.Unconfined).capture("Préparer", folderId = folder)
            val task = db.openTasks().single().id
            store.saveNote(null, "Une idée", folder)
            val note = store.load().notes.single().id
            store.saveNote(note, "Idée précisée", folder)
            db.close(); db = AppDatabase(context); store = WorkspaceStore(db)
            assertEquals(folder, store.load().taskFolders[task])
            assertEquals(folder, store.load().noteFolders[note])
            assertEquals("Idée précisée", store.load().notes.single().text)
            store.deleteFolder(folder)
            assertTrue(store.load().folders.isEmpty())
            assertTrue(store.load().taskFolders.isEmpty())
            assertTrue(store.load().noteFolders.isEmpty())
            assertEquals(task, db.openTasks().single().id)
            assertEquals(note, store.load().notes.single().id)
        } finally { db.close() }
    }

    @Test fun decomposingIsIdempotentAndCompletingTimerCompletesOnlyItsStep() = runBlocking {
        context.deleteDatabase("executive-function.db")
        val db = AppDatabase(context)
        try {
            val repository = FocusRepository(db, Dispatchers.Unconfined)
            val journal = LearningJournal(db)
            val store = WorkspaceStore(db)
            val folder = store.createFolder("Travail")
            repository.capture("Dossier", "Lire", folderId = folder)
            val parent = db.openTasks().single().id
            journal.addStep(parent, "Rédiger")
            val first = journal.steps(parent).first().id
            val child = store.decomposeStep(parent, first)
            assertEquals(child, store.decomposeStep(parent, first))
            val children = store.decomposeAll(parent)
            assertEquals(children, store.decomposeAll(parent))
            assertEquals(3, db.openTasks().size)
            assertEquals(folder, store.load().taskFolders[child])
            repository.start(child, 120_000L)
            assertEquals(child, db.activeFocus()!!.task.id)
            repository.interrupt("Reprendre")
            repository.resume()
            assertEquals(120_000L, db.activeFocus()!!.session.targetDurationMs)
            repository.complete()
            assertTrue(journal.steps(parent).first().completed)
            assertFalse(journal.steps(parent).last().completed)
            assertEquals("Rédiger", db.taskById(parent)!!.firstStep)
            assertTrue(db.openTasks().any { it.id == parent })
            assertEquals(listOf(children.last()), store.decomposeAll(parent))
        } finally { db.close() }
    }

    @Test fun v4UpgradePreservesDataAndCanReadLegacyFirstStep() = runBlocking {
        context.deleteDatabase("executive-function.db")
        var db = AppDatabase(context)
        try {
            FocusRepository(db, Dispatchers.Unconfined).capture("Ancienne tâche", "Premier geste")
            val id = db.openTasks().single().id
            LearningJournal(db).note("Note ancienne")
            // Recreate the actual v4 shape: only the four additive tables differ.
            WorkspaceStore.tables.reversed().forEach { db.writableDatabase.execSQL("DROP TABLE $it") }
            db.writableDatabase.delete("task_steps", null, null)
            db.writableDatabase.version = 4
            db.close(); db = AppDatabase(context)
            val store = WorkspaceStore(db)
            assertEquals(5, db.writableDatabase.version)
            store.ensureSteps(id); store.ensureSteps(id)
            assertEquals("Premier geste", LearningJournal(db).steps(id).single().title)
            assertEquals("Note ancienne", store.load().notes.single().text)
        } finally { db.close() }
    }

    @Test fun backupsRoundTripRelationshipsAndAcceptOldVersionAtomically() = runBlocking {
        context.deleteDatabase("executive-function.db")
        val db = AppDatabase(context)
        try {
            val store = WorkspaceStore(db)
            val repo = FocusRepository(db, Dispatchers.Unconfined)
            val folder = store.createFolder("Personnel")
            repo.capture("Courses", "Liste", folderId = folder)
            val parent = db.openTasks().single().id
            val step = LearningJournal(db).steps(parent).single().id
            val child = store.decomposeStep(parent, step)
            store.saveNote(null, "Penser au pain", folder)
            val backup = LocalBackup(db).export()
            LocalBackup(db).clear()
            LocalBackup(db).restore(backup)
            assertEquals(child, store.linkedTask(step))
            assertEquals(folder, store.load().noteFolders.values.single())
            val invalid = JSONObject(backup)
            invalid.getJSONArray("folder_tasks").getJSONObject(0).put("folder_id", "missing")
            assertThrows(Exception::class.java) { LocalBackup(db).restore(invalid.toString()) }
            assertEquals(child, store.linkedTask(step))
            val legacy = JSONObject(backup).put("version", 4)
            WorkspaceStore.tables.forEach { legacy.remove(it) }
            LocalBackup(db).restore(legacy.toString())
            assertEquals(2, db.openTasks().size)
            assertEquals(1, store.load().notes.size)
            assertTrue(store.load().folders.isEmpty())
            assertNull(store.linkedTask(step))
        } finally { db.close() }
    }
}
