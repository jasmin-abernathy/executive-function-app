package org.lepotager.executivefunction.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import java.util.UUID

/** A paused routine can be resumed after any absence, with no streak or penalty. */
internal data class RoutineStep(val id: String, val title: String, val position: Int, val clockMinute: Int?)
internal data class RoutineRunStep(val position: Int, val title: String, val clockMinute: Int?, val state: String)
internal data class RoutineRun(val id: String, val status: String, val steps: List<RoutineRunStep>)
internal data class RoutineTemplate(val id: String, val title: String, val steps: List<RoutineStep>, val openRun: RoutineRun?)

/** Optional displayed time: never schedules an alarm. */
internal object RoutineClock {
    fun parse(text: String): Int? {
        val value = text.trim()
        if (value.isEmpty()) return null
        require(Regex("[0-9]{2}:[0-9]{2}").matches(value))
        val hours = value.substring(0, 2).toInt()
        val minutes = value.substring(3).toInt()
        require(hours in 0..23 && minutes in 0..59)
        return hours * 60 + minutes
    }
    fun format(minutes: Int?): String {
        if (minutes == null) return ""
        require(minutes in 0..1439)
        return "%02d:%02d".format(java.util.Locale.ROOT, minutes / 60, minutes % 60)
    }
}

/**
 * Templates and executions are local and separate from tasks and focus sessions.
 * Execution steps snapshot wording and time so editing a template cannot rewrite
 * an already-started sequence.
 */
internal class RoutineStore(private val database: AppDatabase) {
    private val db get() = database.writableDatabase

    fun load(): List<RoutineTemplate> {
        val names = db.rawQuery("SELECT id,title FROM routine_templates ORDER BY created_at,id", null).use { c ->
            buildList {
                while (c.moveToNext()) add(c.getString(0) to c.getString(1))
            }
        }
        return names.map { (id, title) ->
            val current = db.rawQuery(
                "SELECT id,status FROM routine_runs WHERE routine_id=? AND status IN ('ACTIVE','PAUSED') LIMIT 1", arrayOf(id)
            ).use { c -> if (c.moveToFirst()) c.getString(0) to c.getString(1) else null }
            val run = current?.let { (runId, state) ->
                val steps = db.rawQuery(
                    "SELECT position,title,clock_minute,state FROM routine_run_steps WHERE run_id=? ORDER BY position",
                    arrayOf(runId)
                ).use { c -> buildList {
                    while (c.moveToNext()) add(RoutineRunStep(
                        c.getInt(0), c.getString(1), if (c.isNull(2)) null else c.getInt(2), c.getString(3)
                    ))
                } }
                RoutineRun(runId, state, steps)
            }
            RoutineTemplate(id, title, readSteps(id), run)
        }
    }

    private fun readSteps(id: String): List<RoutineStep> = db.rawQuery(
        "SELECT id,title,position,clock_minute FROM routine_steps WHERE routine_id=? ORDER BY position,id", arrayOf(id)
    ).use { c -> buildList {
        while (c.moveToNext()) add(RoutineStep(
            c.getString(0), c.getString(1), c.getInt(2), if (c.isNull(3)) null else c.getInt(3)
        ))
    } }

    private fun title(text: String): String = text.trim().also { require(it.isNotEmpty() && it.length <= 120) }
    private fun clock(minute: Int?) { require(minute == null || minute in 0..1439) }
    private fun now() = System.currentTimeMillis()
    private fun touch(id: String) {
        require(db.update("routine_templates", ContentValues().apply {
            put("updated_at", now())
        }, "id=?", arrayOf(id)) == 1)
    }

    private fun <T> transaction(action: () -> T): T {
        db.beginTransaction()
        try {
            val result = action()
            db.setTransactionSuccessful()
            return result
        } finally { db.endTransaction() }
    }

    fun create(name: String): String {
        val id = UUID.randomUUID().toString()
        val timestamp = now()
        db.insertOrThrow("routine_templates", null, ContentValues().apply {
            put("id", id); put("title", title(name)); put("created_at", timestamp); put("updated_at", timestamp)
        })
        return id
    }

    fun rename(id: String, name: String) {
        require(db.update("routine_templates", ContentValues().apply {
            put("title", title(name)); put("updated_at", now())
        }, "id=?", arrayOf(id)) == 1)
    }

    fun addStep(routineId: String, text: String, minute: Int?): String = transaction {
        clock(minute)
        require(readSteps(routineId).size < 30)
        val id = UUID.randomUUID().toString()
        val position = db.rawQuery(
            "SELECT COALESCE(MAX(position),-1)+1 FROM routine_steps WHERE routine_id=?", arrayOf(routineId)
        ).use { c -> c.moveToFirst(); c.getInt(0) }
        db.insertOrThrow("routine_steps", null, ContentValues().apply {
            put("id", id); put("routine_id", routineId); put("title", title(text)); put("position", position)
            if (minute == null) putNull("clock_minute") else put("clock_minute", minute)
        })
        touch(routineId)
        id
    }

    fun editStep(routineId: String, stepId: String, text: String, minute: Int?) = transaction {
        clock(minute)
        require(db.update("routine_steps", ContentValues().apply {
            put("title", title(text))
            if (minute == null) putNull("clock_minute") else put("clock_minute", minute)
        }, "id=? AND routine_id=?", arrayOf(stepId, routineId)) == 1)
        touch(routineId)
    }

    fun deleteStep(routineId: String, stepId: String) = transaction {
        require(db.delete("routine_steps", "id=? AND routine_id=?", arrayOf(stepId, routineId)) == 1)
        touch(routineId)
    }

    fun moveStep(routineId: String, stepId: String, delta: Int) = transaction {
        require(delta == -1 || delta == 1)
        val steps = readSteps(routineId)
        val index = steps.indexOfFirst { it.id == stepId }
        require(index >= 0)
        val other = index + delta
        if (other in steps.indices) {
            // Positions are not UNIQUE; both updates remain atomic.
            db.update("routine_steps", ContentValues().apply { put("position", steps[other].position) }, "id=?", arrayOf(stepId))
            db.update("routine_steps", ContentValues().apply { put("position", steps[index].position) }, "id=?", arrayOf(steps[other].id))
            touch(routineId)
        }
    }

    /** Resume first; a running/paused sequence must never be silently replaced. */
    fun startOrResume(routineId: String): String = transaction {
        val existing = db.rawQuery(
            "SELECT id FROM routine_runs WHERE routine_id=? AND status IN ('ACTIVE','PAUSED') LIMIT 1", arrayOf(routineId)
        ).use { c -> if (c.moveToFirst()) c.getString(0) else null }
        val timestamp = now()
        if (existing != null) {
            require(db.update("routine_runs", ContentValues().apply {
                put("status", "ACTIVE"); put("updated_at", timestamp)
            }, "id=?", arrayOf(existing)) == 1)
            return@transaction existing
        }
        val definitions = readSteps(routineId)
        require(definitions.isNotEmpty())
        val id = UUID.randomUUID().toString()
        db.insertOrThrow("routine_runs", null, ContentValues().apply {
            put("id", id); put("routine_id", routineId); put("status", "ACTIVE")
            put("started_at", timestamp); put("updated_at", timestamp); putNull("completed_at")
        })
        definitions.forEachIndexed { index, step ->
            db.insertOrThrow("routine_run_steps", null, ContentValues().apply {
                put("run_id", id); put("position", index); put("title", step.title)
                if (step.clockMinute == null) putNull("clock_minute") else put("clock_minute", step.clockMinute)
                put("state", "PENDING"); putNull("decided_at")
            })
        }
        id
    }

    fun pause(runId: String) {
        require(db.update("routine_runs", ContentValues().apply {
            put("status", "PAUSED"); put("updated_at", now())
        }, "id=? AND status='ACTIVE'", arrayOf(runId)) == 1)
    }

    /** Only the next pending step may be marked, so double taps cannot skip steps. */
    fun decide(runId: String, position: Int, skip: Boolean) = transaction {
        require(db.rawQuery("SELECT 1 FROM routine_runs WHERE id=? AND status='ACTIVE'", arrayOf(runId)).use { it.moveToFirst() })
        val next = db.rawQuery(
            "SELECT position FROM routine_run_steps WHERE run_id=? AND state='PENDING' ORDER BY position LIMIT 1", arrayOf(runId)
        ).use { c -> if (c.moveToFirst()) c.getInt(0) else null }
        require(next == position)
        val timestamp = now()
        require(db.update("routine_run_steps", ContentValues().apply {
            put("state", if (skip) "SKIPPED" else "DONE"); put("decided_at", timestamp)
        }, "run_id=? AND position=? AND state='PENDING'", arrayOf(runId, position.toString())) == 1)
        val remaining = db.rawQuery(
            "SELECT 1 FROM routine_run_steps WHERE run_id=? AND state='PENDING' LIMIT 1", arrayOf(runId)
        ).use { it.moveToFirst() }
        require(db.update("routine_runs", ContentValues().apply {
            put("status", if (remaining) "ACTIVE" else "COMPLETED")
            put("updated_at", timestamp)
            if (!remaining) put("completed_at", timestamp)
        }, "id=?", arrayOf(runId)) == 1)
    }

    companion object {
        val tables = listOf("routine_templates", "routine_steps", "routine_runs", "routine_run_steps")
        fun createTables(db: SQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS routine_templates (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL CHECK(length(trim(title)) BETWEEN 1 AND 120),
                created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL
            )""")
            db.execSQL("""CREATE TABLE IF NOT EXISTS routine_steps (
                id TEXT PRIMARY KEY NOT NULL,
                routine_id TEXT NOT NULL REFERENCES routine_templates(id) ON DELETE CASCADE,
                title TEXT NOT NULL CHECK(length(trim(title)) BETWEEN 1 AND 120),
                position INTEGER NOT NULL CHECK(position >= 0),
                clock_minute INTEGER CHECK(clock_minute IS NULL OR clock_minute BETWEEN 0 AND 1439)
            )""")
            db.execSQL("CREATE INDEX IF NOT EXISTS routine_steps_order ON routine_steps(routine_id,position)")
            db.execSQL("""CREATE TABLE IF NOT EXISTS routine_runs (
                id TEXT PRIMARY KEY NOT NULL,
                routine_id TEXT NOT NULL REFERENCES routine_templates(id) ON DELETE RESTRICT,
                status TEXT NOT NULL CHECK(status IN ('ACTIVE','PAUSED','COMPLETED')),
                started_at INTEGER NOT NULL, updated_at INTEGER NOT NULL, completed_at INTEGER
            )""")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS routine_one_open_run ON routine_runs(routine_id) WHERE status IN ('ACTIVE','PAUSED')")
            db.execSQL("""CREATE TABLE IF NOT EXISTS routine_run_steps (
                run_id TEXT NOT NULL REFERENCES routine_runs(id) ON DELETE CASCADE,
                position INTEGER NOT NULL CHECK(position >= 0),
                title TEXT NOT NULL CHECK(length(trim(title)) BETWEEN 1 AND 120),
                clock_minute INTEGER CHECK(clock_minute IS NULL OR clock_minute BETWEEN 0 AND 1439),
                state TEXT NOT NULL DEFAULT 'PENDING' CHECK(state IN ('PENDING','DONE','SKIPPED')),
                decided_at INTEGER, PRIMARY KEY(run_id,position)
            )""")
        }
    }
}
