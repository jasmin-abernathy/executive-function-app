package org.lepotager.executivefunction.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.lepotager.executivefunction.model.ActiveFocus
import org.lepotager.executivefunction.model.FocusSession
import org.lepotager.executivefunction.model.FocusStatus
import org.lepotager.executivefunction.model.TaskItem
import org.lepotager.executivefunction.model.TaskColor
import org.lepotager.executivefunction.model.TaskStatus
import org.lepotager.executivefunction.domain.TimeLearning
import java.util.Locale

internal class AppDatabase(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE tasks (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL CHECK(length(trim(title)) > 0),
                learning_key TEXT NOT NULL,
                first_step TEXT,
                color_key TEXT NOT NULL DEFAULT 'NEUTRAL',
                sort_position INTEGER NOT NULL DEFAULT 0,
                status TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE focus_sessions (
                id TEXT PRIMARY KEY NOT NULL,
                task_id TEXT NOT NULL,
                status TEXT NOT NULL,
                is_active INTEGER NOT NULL DEFAULT 0 CHECK(is_active IN (0, 1)),
                elapsed_before_segment_ms INTEGER NOT NULL DEFAULT 0 CHECK(elapsed_before_segment_ms >= 0),
                segment_started_at INTEGER,
                interruption_note TEXT,
                target_duration_ms INTEGER CHECK(target_duration_ms IS NULL OR target_duration_ms > 0),
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY(task_id) REFERENCES tasks(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX tasks_status_updated ON tasks(status, updated_at DESC)")
        db.execSQL("CREATE INDEX sessions_task ON focus_sessions(task_id, updated_at DESC)")
        db.execSQL("CREATE INDEX tasks_learning_key ON tasks(learning_key)")
        db.execSQL("CREATE INDEX tasks_sort_position ON tasks(sort_position)")
        db.execSQL(
            "CREATE UNIQUE INDEX one_active_focus ON focus_sessions(is_active) WHERE is_active = 1",
        )
        createLearningTables(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        var version = oldVersion
        if (version == 1) {
            db.execSQL("ALTER TABLE tasks ADD COLUMN learning_key TEXT")
            db.execSQL("UPDATE tasks SET learning_key = lower(trim(title))")
            db.execSQL("CREATE INDEX tasks_learning_key ON tasks(learning_key)")
            db.execSQL("ALTER TABLE focus_sessions ADD COLUMN target_duration_ms INTEGER")
            version = 2
        }
        if (version == 2) {
            db.execSQL("ALTER TABLE tasks ADD COLUMN color_key TEXT NOT NULL DEFAULT 'NEUTRAL'")
            db.execSQL("ALTER TABLE tasks ADD COLUMN sort_position INTEGER NOT NULL DEFAULT 0")
            db.execSQL("UPDATE tasks SET sort_position = rowid")
            db.execSQL("CREATE INDEX tasks_sort_position ON tasks(sort_position)")
            version = 3
        }
        if (version == 3) {
            createLearningTables(db)
            version = 4
        }
        check(version == newVersion) { "Missing migration from $oldVersion to $newVersion" }
    }

    private fun createLearningTables(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE learning_exclusions (session_id TEXT PRIMARY KEY REFERENCES focus_sessions(id) ON DELETE CASCADE)")
        db.execSQL("CREATE TABLE learning_overrides (learning_key TEXT PRIMARY KEY, duration_ms INTEGER NOT NULL CHECK(duration_ms > 0))")
        db.execSQL("CREATE TABLE task_steps (id TEXT PRIMARY KEY, task_id TEXT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE, title TEXT NOT NULL, position INTEGER NOT NULL, completed INTEGER NOT NULL DEFAULT 0)")
        db.execSQL("CREATE TABLE task_planning (task_id TEXT PRIMARY KEY REFERENCES tasks(id) ON DELETE CASCADE, importance INTEGER NOT NULL DEFAULT 0, energy INTEGER NOT NULL DEFAULT 0, context TEXT NOT NULL DEFAULT '', today TEXT NOT NULL DEFAULT '')")
        db.execSQL("CREATE TABLE check_ins (id TEXT PRIMARY KEY, recorded_at INTEGER NOT NULL, mood INTEGER NOT NULL, motivation INTEGER NOT NULL, energy INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE quick_notes (id TEXT PRIMARY KEY, body TEXT NOT NULL, created_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE session_context (session_id TEXT PRIMARY KEY REFERENCES focus_sessions(id) ON DELETE CASCADE, check_in_id TEXT REFERENCES check_ins(id) ON DELETE SET NULL)")
        db.execSQL("CREATE TABLE learning_resets (learning_key TEXT PRIMARY KEY, cutoff INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE recurrences (source_task_id TEXT PRIMARY KEY REFERENCES tasks(id) ON DELETE CASCADE, interval_days INTEGER NOT NULL CHECK(interval_days > 0), next_date TEXT NOT NULL)")
        db.execSQL("CREATE TABLE task_reminders (task_id TEXT PRIMARY KEY REFERENCES tasks(id) ON DELETE CASCADE, due_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE session_steps (session_id TEXT NOT NULL REFERENCES focus_sessions(id) ON DELETE CASCADE, position INTEGER NOT NULL, title TEXT NOT NULL, PRIMARY KEY(session_id,position))")
    }

    fun eligibleDrawIds(tasks: List<TaskItem>): Set<String>? {
        val preferences=context.getSharedPreferences("wellbeing",Context.MODE_PRIVATE)
        if(!preferences.getBoolean("adapt",false)) return null
        val journal=LearningJournal(this)
        val recent=journal.checkIns().firstOrNull()?.takeIf { System.currentTimeMillis()-it.date in 0..14_400_000L }
        val energy=if(preferences.getBoolean("low_energy",false)) 1 else recent?.energy
        val minutes=preferences.getInt("available_minutes",0).takeIf { it>0 }
        val selectedContext=preferences.getString("context","").orEmpty()
        return tasks.filter { task ->
            val plan=journal.planning(task.id)
            org.lepotager.executivefunction.domain.TaskEligibility.accepts(plan.energy,energy,suggestedDurationMs(task.id),minutes,plan.context,selectedContext)
        }.map { it.id }.toSet()
    }

    fun insertTask(task: TaskItem) {
        writableDatabase.insertOrThrow("tasks", null, task.toValues())
    }

    fun taskById(id: String): TaskItem? = readableDatabase.query(
        "tasks",
        TASK_COLUMNS,
        "id = ?",
        arrayOf(id),
        null,
        null,
        null,
        "1",
    ).use { cursor -> if (cursor.moveToFirst()) cursor.toTask() else null }

    fun postponedTaskIds(): Set<String> = readableDatabase.rawQuery(
        """
        SELECT DISTINCT s.task_id
        FROM focus_sessions s
        INNER JOIN tasks t ON t.id = s.task_id
        WHERE s.status = ? AND t.status != ?
        """.trimIndent(),
        arrayOf(FocusStatus.POSTPONED.name, TaskStatus.COMPLETED.name),
    ).use { cursor ->
        buildSet { while (cursor.moveToNext()) add(cursor.getString(0)) }
    }

    fun latestPostponedFocus(taskId: String): ActiveFocus? {
        val session = readableDatabase.query(
            "focus_sessions",
            SESSION_COLUMNS,
            "task_id = ? AND status = ? AND is_active = 0",
            arrayOf(taskId, FocusStatus.POSTPONED.name),
            null,
            null,
            "updated_at DESC, rowid DESC",
            "1",
        ).use { cursor -> if (cursor.moveToFirst()) cursor.toSession() else null } ?: return null
        return taskById(taskId)?.let { ActiveFocus(it, session) }
    }

    // Kept for compatibility with older start flows. A dedicated continue action now
    // reactivates the postponed session itself instead of creating a new session.
    fun postponedElapsedMs(taskId: String): Long = readableDatabase.rawQuery(
        "SELECT elapsed_before_segment_ms FROM focus_sessions WHERE task_id=? AND status='POSTPONED' ORDER BY updated_at DESC, rowid DESC LIMIT 1",
        arrayOf(taskId),
    ).use { if (it.moveToFirst()) it.getLong(0) else 0L }

    fun nextTaskPosition(): Long = readableDatabase.rawQuery(
        "SELECT COALESCE(MAX(sort_position), -1) + 1 FROM tasks",
        null,
    ).use { cursor ->
        cursor.moveToFirst()
        cursor.getLong(0)
    }

    fun openTasks(): List<TaskItem> = readableDatabase.query(
        "tasks",
        TASK_COLUMNS,
        "status != ?",
        arrayOf(TaskStatus.COMPLETED.name),
        null,
        null,
        "sort_position ASC, created_at ASC",
    ).use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.toTask()) } }

    fun moveOpenTask(taskId: String, offset: Int) = transaction { db ->
        val ids = db.query(
            "tasks",
            arrayOf("id"),
            "status != ?",
            arrayOf(TaskStatus.COMPLETED.name),
            null,
            null,
            "sort_position ASC, created_at ASC",
        ).use { cursor ->
            buildList { while (cursor.moveToNext()) add(cursor.getString(0)) }
        }.toMutableList()
        val from = ids.indexOf(taskId)
        if (from == -1) return@transaction
        val to = (from + offset).coerceIn(ids.indices)
        if (from == to) return@transaction
        ids.add(to, ids.removeAt(from))
        ids.forEachIndexed { index, id ->
            val values = ContentValues().apply { put("sort_position", index.toLong()) }
            require(db.update("tasks", values, "id = ?", arrayOf(id)) == 1)
        }
    }

    fun updateTaskColor(taskId: String, color: TaskColor) {
        val values = ContentValues().apply { put("color_key", color.name) }
        require(writableDatabase.update("tasks", values, "id = ?", arrayOf(taskId)) == 1)
    }

    fun applyTaskOrder(ids: List<String>) = transaction { db ->
        val open=openTasks().map {it.id}
        require(ids.size==open.size && ids.toSet()==open.toSet())
        ids.forEachIndexed {position,id -> db.update("tasks",ContentValues().apply{put("sort_position",position)},"id=?",arrayOf(id))}
    }

    fun activeFocus(): ActiveFocus? {
        val session = readableDatabase.query(
            "focus_sessions",
            SESSION_COLUMNS,
            "is_active = 1",
            null,
            null,
            null,
            "updated_at DESC",
            "1",
        ).use { cursor -> if (cursor.moveToFirst()) cursor.toSession() else null } ?: return null
        return taskById(session.taskId)?.let { ActiveFocus(it, session) }
    }

    /**
     * Repeated tasks are currently matched by a conservative normalized title.
     * A future explicit task-template UI can replace the key without changing
     * the learning calculation or exposing data outside the device.
     */
    fun suggestedDurationMs(taskId: String): Long? {
        readableDatabase.rawQuery(
            "SELECT duration_ms FROM learning_overrides WHERE learning_key = (SELECT learning_key FROM tasks WHERE id = ?)",
            arrayOf(taskId),
        ).use { if (it.moveToFirst()) return it.getLong(0) }
        val sql = """
            SELECT COUNT(*) AS completed_count,
                   MAX(s.elapsed_before_segment_ms) AS longest_duration
            FROM focus_sessions s
            INNER JOIN tasks history_task ON history_task.id = s.task_id
            WHERE s.status = ?
              AND history_task.learning_key = (
                  SELECT learning_key FROM tasks WHERE id = ? LIMIT 1
              )
              AND s.elapsed_before_segment_ms > 0
              AND NOT EXISTS (SELECT 1 FROM learning_exclusions e WHERE e.session_id = s.id)
              AND s.created_at > COALESCE((SELECT cutoff FROM learning_resets WHERE learning_key=history_task.learning_key), -1)
        """.trimIndent()
        return readableDatabase.rawQuery(
            sql,
            arrayOf(FocusStatus.COMPLETED.name, taskId),
        ).use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val completedCount = cursor.getInt(cursor.getColumnIndexOrThrow("completed_count"))
            val longestColumn = cursor.getColumnIndexOrThrow("longest_duration")
            val longest = if (cursor.isNull(longestColumn)) null else cursor.getLong(longestColumn)
            TimeLearning.suggestedDurationMs(completedCount, longest)
        }
    }

    fun startFocus(taskId: String, session: FocusSession) = transaction { db ->
        deactivateExistingFocus(db, session.updatedAt)
        updateTaskStatus(db, taskId, TaskStatus.IN_PROGRESS, session.updatedAt)
        db.insertOrThrow("focus_sessions", null, session.toValues(isActive = true))
        val recent=LearningJournal(this).checkIns().firstOrNull()?.takeIf { session.createdAt-it.date in 0..14_400_000L }
        if(recent!=null) db.insertOrThrow("session_context",null,ContentValues().apply {put("session_id",session.id);put("check_in_id",recent.id)})
    }

    fun interruptFocus(session: FocusSession) = transaction { db ->
        updateSession(db, session, isActive = true)
        updateTaskStatus(db, session.taskId, TaskStatus.INTERRUPTED, session.updatedAt)
    }

    fun resumeFocus(session: FocusSession, firstStep: String?) = transaction { db ->
        val taskValues = ContentValues().apply {
            put("status", TaskStatus.IN_PROGRESS.name)
            putNullableString("first_step", firstStep)
            put("updated_at", session.updatedAt)
        }
        require(db.update("tasks", taskValues, "id = ?", arrayOf(session.taskId)) == 1)
        updateSession(db, session, isActive = true)
    }

    fun resumePostponedFocus(session: FocusSession, firstStep: String?) = transaction { db ->
        deactivateExistingFocus(db, session.updatedAt)
        val taskValues = ContentValues().apply {
            put("status", TaskStatus.IN_PROGRESS.name)
            putNullableString("first_step", firstStep)
            put("updated_at", session.updatedAt)
        }
        require(db.update("tasks", taskValues, "id = ?", arrayOf(session.taskId)) == 1)
        updateSession(db, session, isActive = true)
    }

    fun closeFocus(session: FocusSession, taskStatus: TaskStatus) = transaction { db ->
        updateSession(db, session, isActive = false)
        updateTaskStatus(db, session.taskId, taskStatus, session.updatedAt)
        if (session.status == FocusStatus.COMPLETED) {
            db.execSQL("INSERT OR IGNORE INTO session_steps(session_id,position,title) SELECT ?,position,title FROM task_steps WHERE task_id=?", arrayOf(session.id,session.taskId))
        }
    }

    private fun deactivateExistingFocus(db: SQLiteDatabase, now: Long) {
        val current = db.query(
            "focus_sessions",
            SESSION_COLUMNS,
            "is_active = 1",
            null,
            null,
            null,
            null,
            "1",
        ).use { cursor -> if (cursor.moveToFirst()) cursor.toSession() else null } ?: return
        val frozen = current.copy(
            status = FocusStatus.POSTPONED,
            elapsedBeforeSegmentMs = current.elapsedBeforeSegmentMs +
                (current.segmentStartedAt?.let { (now - it).coerceAtLeast(0) } ?: 0),
            segmentStartedAt = null,
            updatedAt = now,
        )
        updateSession(db, frozen, isActive = false)
        updateTaskStatus(db, current.taskId, TaskStatus.READY, now)
    }

    private fun updateTaskStatus(
        db: SQLiteDatabase,
        taskId: String,
        status: TaskStatus,
        now: Long,
    ) {
        val values = ContentValues().apply {
            put("status", status.name)
            put("updated_at", now)
        }
        require(db.update("tasks", values, "id = ?", arrayOf(taskId)) == 1)
    }

    private fun updateSession(db: SQLiteDatabase, session: FocusSession, isActive: Boolean) {
        require(
            db.update(
                "focus_sessions",
                session.toValues(isActive),
                "id = ?",
                arrayOf(session.id),
            ) == 1,
        )
    }

    private inline fun <T> transaction(block: (SQLiteDatabase) -> T): T {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val value = block(db)
            db.setTransactionSuccessful()
            value
        } finally {
            db.endTransaction()
        }
    }

    private fun TaskItem.toValues() = ContentValues().apply {
        put("id", id)
        put("title", title)
        put("learning_key", learningKey(title))
        putNullableString("first_step", firstStep)
        put("color_key", color.name)
        put("sort_position", sortPosition)
        put("status", status.name)
        put("created_at", createdAt)
        put("updated_at", updatedAt)
    }

    private fun FocusSession.toValues(isActive: Boolean) = ContentValues().apply {
        put("id", id)
        put("task_id", taskId)
        put("status", status.name)
        put("is_active", if (isActive) 1 else 0)
        put("elapsed_before_segment_ms", elapsedBeforeSegmentMs)
        if (segmentStartedAt == null) putNull("segment_started_at") else put("segment_started_at", segmentStartedAt)
        putNullableString("interruption_note", interruptionNote)
        if (targetDurationMs == null) putNull("target_duration_ms") else put("target_duration_ms", targetDurationMs)
        put("created_at", createdAt)
        put("updated_at", updatedAt)
    }

    private fun ContentValues.putNullableString(key: String, value: String?) {
        if (value.isNullOrBlank()) putNull(key) else put(key, value.trim())
    }

    private fun Cursor.toTask() = TaskItem(
        id = getString(getColumnIndexOrThrow("id")),
        title = getString(getColumnIndexOrThrow("title")),
        firstStep = getNullableString("first_step"),
        status = TaskStatus.valueOf(getString(getColumnIndexOrThrow("status"))),
        createdAt = getLong(getColumnIndexOrThrow("created_at")),
        updatedAt = getLong(getColumnIndexOrThrow("updated_at")),
        color = runCatching {
            TaskColor.valueOf(getString(getColumnIndexOrThrow("color_key")))
        }.getOrDefault(TaskColor.NEUTRAL),
        sortPosition = getLong(getColumnIndexOrThrow("sort_position")),
    )

    private fun Cursor.toSession() = FocusSession(
        id = getString(getColumnIndexOrThrow("id")),
        taskId = getString(getColumnIndexOrThrow("task_id")),
        status = FocusStatus.valueOf(getString(getColumnIndexOrThrow("status"))),
        elapsedBeforeSegmentMs = getLong(getColumnIndexOrThrow("elapsed_before_segment_ms")),
        segmentStartedAt = getNullableLong("segment_started_at"),
        interruptionNote = getNullableString("interruption_note"),
        createdAt = getLong(getColumnIndexOrThrow("created_at")),
        updatedAt = getLong(getColumnIndexOrThrow("updated_at")),
        targetDurationMs = getNullableLong("target_duration_ms"),
    )

    private fun Cursor.getNullableString(column: String): String? {
        val index = getColumnIndexOrThrow(column)
        return if (isNull(index)) null else getString(index)
    }

    private fun Cursor.getNullableLong(column: String): Long? {
        val index = getColumnIndexOrThrow(column)
        return if (isNull(index)) null else getLong(index)
    }

    private companion object {
        const val DATABASE_NAME = "executive-function.db"
        const val DATABASE_VERSION = 4
        val TASK_COLUMNS = arrayOf(
            "id",
            "title",
            "first_step",
            "color_key",
            "sort_position",
            "status",
            "created_at",
            "updated_at",
        )
        val SESSION_COLUMNS = arrayOf(
            "id",
            "task_id",
            "status",
            "elapsed_before_segment_ms",
            "segment_started_at",
            "interruption_note",
            "target_duration_ms",
            "created_at",
            "updated_at",
        )

        fun learningKey(title: String): String = title
            .trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("\\s+"), " ")
    }
}
