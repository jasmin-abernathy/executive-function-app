package org.lepotager.executivefunction.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import org.lepotager.executivefunction.model.TaskStatus
import java.util.UUID

internal data class WorkspaceFolder(val id: String, val name: String)
internal data class WorkspaceContent(
    val folders: List<WorkspaceFolder> = emptyList(),
    val taskFolders: Map<String, String> = emptyMap(),
    val noteFolders: Map<String, String> = emptyMap(),
    val notes: List<QuickNote> = emptyList(),
)

/** Additive tables keep existing tasks, notes and old backups intact. */
internal class WorkspaceStore(private val database: AppDatabase) {
    private val db get() = database.writableDatabase
    private val journal get() = LearningJournal(database)

    fun load() = WorkspaceContent(
        folders = db.rawQuery("SELECT id,name FROM folders ORDER BY name COLLATE NOCASE", null).use { c ->
            buildList { while (c.moveToNext()) add(WorkspaceFolder(c.getString(0), c.getString(1))) }
        },
        taskFolders = memberships("folder_tasks", "task_id"),
        noteFolders = memberships("folder_notes", "note_id"),
        notes = journal.notes(),
    )

    private fun memberships(table: String, column: String): Map<String, String> =
        db.rawQuery("SELECT $column,folder_id FROM $table", null).use { c ->
            buildMap { while (c.moveToNext()) put(c.getString(0), c.getString(1)) }
        }

    fun createFolder(name: String): String {
        require(name.isNotBlank())
        val id = UUID.randomUUID().toString()
        db.insertOrThrow("folders", null, ContentValues().apply { put("id", id); put("name", name.trim()) })
        return id
    }

    fun renameFolder(id: String, name: String) {
        require(name.isNotBlank())
        require(db.update("folders", ContentValues().apply { put("name", name.trim()) }, "id=?", arrayOf(id)) == 1)
    }

    /** Removing a folder only removes membership, never its contents. */
    fun deleteFolder(id: String) { db.delete("folders", "id=?", arrayOf(id)) }
    fun assignTask(id: String, folder: String?) = assign("folder_tasks", "task_id", id, folder)
    fun assignNote(id: String, folder: String?) = assign("folder_notes", "note_id", id, folder)
    private fun assign(table: String, column: String, id: String, folder: String?) {
        if (folder == null) db.delete(table, "$column=?", arrayOf(id))
        else db.insertWithOnConflict(table, null, ContentValues().apply {
            put(column, id); put("folder_id", folder)
        }, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun saveNote(id: String?, body: String, folder: String?) = transaction {
        require(body.isNotBlank())
        val key = id ?: UUID.randomUUID().toString()
        val values = ContentValues().apply { put("body", body.trim()) }
        if (id == null) {
            values.put("id", key); values.put("created_at", System.currentTimeMillis())
            db.insertOrThrow("quick_notes", null, values)
        } else require(db.update("quick_notes", values, "id=?", arrayOf(id)) == 1)
        assignNote(key, folder)
    }

    fun ensureSteps(taskId: String) = transaction {
        val task = requireNotNull(database.taskById(taskId))
        if (journal.steps(taskId).isEmpty() && !task.firstStep.isNullOrBlank()) journal.addStep(taskId, task.firstStep)
    }

    fun linkedTask(stepId: String): String? = db.rawQuery(
        "SELECT task_id FROM step_tasks WHERE step_id=?", arrayOf(stepId),
    ).use { if (it.moveToFirst()) it.getString(0) else null }

    /** Idempotent: restarting an unfinished step reuses its task and history. */
    fun decomposeStep(parentId: String, stepId: String): String = transaction {
        val step = journal.steps(parentId).first { it.id == stepId }
        require(!step.completed)
        linkedTask(stepId)?.let { return@transaction it }
        val parent = requireNotNull(database.taskById(parentId))
        require(database.activeFocus()?.task?.id != parentId)
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        database.insertTask(parent.copy(id = id, title = "${parent.title} · ${step.title}", firstStep = null,
            status = TaskStatus.READY, createdAt = now, updatedAt = now, sortPosition = database.nextTaskPosition()))
        db.insertOrThrow("step_tasks", null, ContentValues().apply { put("step_id", stepId); put("task_id", id) })
        memberships("folder_tasks", "task_id")[parentId]?.let { assignTask(id, it) }
        id
    }

    fun decomposeAll(parentId: String) = transaction {
        journal.steps(parentId).filterNot { it.completed }.map { decomposeStep(parentId, it.id) }
    }

    fun completeLinkedStep(taskId: String) {
        val stepId = db.rawQuery("SELECT step_id FROM step_tasks WHERE task_id=?", arrayOf(taskId)).use {
            if (it.moveToFirst()) it.getString(0) else null
        }
        if (stepId != null) journal.finishStep(stepId, true)
    }

    private fun <T> transaction(block: () -> T): T {
        db.beginTransaction()
        try { return block().also { db.setTransactionSuccessful() } } finally { db.endTransaction() }
    }

    companion object {
        val tables = listOf("folders", "folder_tasks", "folder_notes", "step_tasks")
        fun createTables(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE folders (id TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL CHECK(length(trim(name)) > 0))")
            db.execSQL("CREATE TABLE folder_tasks (task_id TEXT PRIMARY KEY REFERENCES tasks(id) ON DELETE CASCADE, folder_id TEXT NOT NULL REFERENCES folders(id) ON DELETE CASCADE)")
            db.execSQL("CREATE TABLE folder_notes (note_id TEXT PRIMARY KEY REFERENCES quick_notes(id) ON DELETE CASCADE, folder_id TEXT NOT NULL REFERENCES folders(id) ON DELETE CASCADE)")
            db.execSQL("CREATE TABLE step_tasks (step_id TEXT PRIMARY KEY REFERENCES task_steps(id) ON DELETE CASCADE, task_id TEXT NOT NULL UNIQUE REFERENCES tasks(id) ON DELETE CASCADE)")
        }
    }
}
