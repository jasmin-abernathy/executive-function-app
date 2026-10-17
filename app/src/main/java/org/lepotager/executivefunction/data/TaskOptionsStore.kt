package org.lepotager.executivefunction.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import java.time.LocalDate
import org.lepotager.executivefunction.domain.TaskDeadline

internal data class TaskOptions(val deadlineDay: Long? = null, val warningDays: Int = 7, val timerOverride: Boolean = false, val timerTarget: Long? = null)

internal class TaskOptionsStore(private val database: AppDatabase) {
    private val db get() = database.writableDatabase
    fun load(id: String): TaskOptions = db.rawQuery("SELECT deadline_day,warning_days,timer_override,timer_target FROM task_options WHERE task_id=?", arrayOf(id)).use { c ->
        if (!c.moveToFirst()) TaskOptions() else TaskOptions(if (c.isNull(0)) null else c.getLong(0), c.getInt(1), c.getInt(2) == 1, if (c.isNull(3)) null else c.getLong(3))
    }
    private fun update(id: String, values: ContentValues) {
        db.insertWithOnConflict("task_options", null, ContentValues().apply { put("task_id", id) }, SQLiteDatabase.CONFLICT_IGNORE)
        require(db.update("task_options", values, "task_id=?", arrayOf(id)) == 1)
    }
    fun deadline(id: String, date: LocalDate?, today: LocalDate = LocalDate.now()) = update(id, ContentValues().apply {
        if (date == null) putNull("deadline_day") else put("deadline_day", date.toEpochDay())
        put("warning_days", if (date == null) 7 else TaskDeadline.warningDays(date, today))
    })
    fun timer(id: String, target: Long?) {
        require(target == null || target > 0)
        update(id, ContentValues().apply { put("timer_override", 1); if (target == null) putNull("timer_target") else put("timer_target", target) })
    }
    companion object {
        val tables = listOf("task_options")
        fun createTables(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS task_options (task_id TEXT PRIMARY KEY REFERENCES tasks(id) ON DELETE CASCADE, deadline_day INTEGER CHECK(deadline_day IS NULL OR deadline_day BETWEEN -25567 AND 47846), warning_days INTEGER NOT NULL DEFAULT 7 CHECK(warning_days IN (7,14)), timer_override INTEGER NOT NULL DEFAULT 0 CHECK(timer_override IN (0,1)), timer_target INTEGER CHECK(timer_target IS NULL OR timer_target>0))")
        }
    }
}
