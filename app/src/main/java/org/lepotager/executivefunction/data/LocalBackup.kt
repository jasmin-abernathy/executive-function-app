package org.lepotager.executivefunction.data

import android.content.ContentValues
import org.json.JSONArray
import org.json.JSONObject

/** Explicit plaintext export. Import is atomic and checks schema and foreign keys. */
internal class LocalBackup(private val database: AppDatabase) {
    companion object {
        // Keep one symmetric ceiling: this app must never create a backup that its own
        // importer rejects purely because of size. 32 MiB leaves headroom above the
        // historical 10,000,000-character cap while still bounding JSONObject memory use.
        const val MAX_BACKUP_BYTES = 32 * 1024 * 1024
    }

    private fun requireBackupSize(text: String) {
        require(text.toByteArray(Charsets.UTF_8).size <= MAX_BACKUP_BYTES) {
            "Backup exceeds the 32 MiB safety limit"
        }
    }
    private val tables = listOf("tasks", "focus_sessions", "learning_exclusions", "learning_overrides", "task_steps", "task_planning", "check_ins", "quick_notes", "session_context", "learning_resets", "recurrences", "task_reminders", "session_steps")

    fun export(): String {
        val db=database.readableDatabase
        val result=JSONObject().put("format","executive-function-local").put("version",4).put("exported_at",System.currentTimeMillis())
        db.beginTransaction()
        try {
            tables.forEach { table ->
                val rows=JSONArray()
                db.rawQuery("SELECT * FROM $table",null).use { c ->
                    while(c.moveToNext()) {
                        val row=JSONObject()
                        c.columnNames.forEachIndexed { i,name ->
                            row.put(name,when(c.getType(i)) {
                                android.database.Cursor.FIELD_TYPE_NULL -> JSONObject.NULL
                                android.database.Cursor.FIELD_TYPE_INTEGER -> c.getLong(i)
                                else -> c.getString(i)
                            })
                        }
                        rows.put(row)
                    }
                }
                result.put(table,rows)
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
        return result.toString(2).also(::requireBackupSize)
    }

    fun clear() {
        val db=database.writableDatabase
        db.beginTransaction()
        try {tables.reversed().forEach {db.delete(it,null,null)};db.setTransactionSuccessful()} finally {db.endTransaction()}
    }

    fun restore(text: String) {
        requireBackupSize(text)
        val root=JSONObject(text)
        require(root.getString("format")=="executive-function-local" && root.getInt("version")==4)
        val exportedAt=root.getLong("exported_at")
        require(exportedAt>0 && exportedAt<=System.currentTimeMillis()+300_000L)
        val db=database.writableDatabase
        // Fully validate columns before touching existing rows.
        val rows=tables.associateWith { table ->
            val columnTypes=db.rawQuery("PRAGMA table_info($table)",null).use { c ->buildMap {while(c.moveToNext()) put(c.getString(1),c.getString(2))} }
            val columns=columnTypes.keys
            val array=root.getJSONArray(table)
            require(array.length()<=100_000)
            (0 until array.length()).map { index ->
                val obj=array.getJSONObject(index)
                require(obj.keys().asSequence().toSet()==columns)
                ContentValues().apply {
                    columns.forEach { key ->
                        val value=obj.get(key)
                        require(value==JSONObject.NULL || if(columnTypes.getValue(key).contains("INTEGER")) value is Int || value is Long else value is String)
                        when(value) {
                            JSONObject.NULL -> putNull(key)
                            is String -> put(key,value)
                            is Int -> put(key,value)
                            is Long -> put(key,value)
                            else -> error("Unsupported backup value")
                        }
                    }
                }
            }
        }
        db.beginTransaction()
        try {
            tables.reversed().forEach { db.delete(it,null,null) }
            tables.forEach { table -> rows.getValue(table).forEach { db.insertOrThrow(table,null,it) } }
            db.rawQuery("PRAGMA foreign_key_check",null).use { require(!it.moveToFirst()) }
            db.rawQuery("SELECT 1 FROM tasks WHERE status NOT IN ('READY','IN_PROGRESS','INTERRUPTED','COMPLETED') UNION ALL SELECT 1 FROM focus_sessions WHERE status NOT IN ('RUNNING','INTERRUPTED','POSTPONED','COMPLETED')",null).use { require(!it.moveToFirst()) }
            db.rawQuery("SELECT 1 FROM check_ins WHERE mood NOT BETWEEN 0 AND 3 OR motivation NOT BETWEEN 0 AND 3 OR energy NOT BETWEEN 0 AND 3 UNION ALL SELECT 1 FROM task_planning WHERE importance NOT BETWEEN 0 AND 3 OR energy NOT BETWEEN 0 AND 3 UNION ALL SELECT 1 FROM focus_sessions WHERE (is_active=1 AND status NOT IN ('RUNNING','INTERRUPTED')) OR (status='RUNNING' AND segment_started_at IS NULL)",null).use { require(!it.moveToFirst()) }
            db.rawQuery("SELECT 1 FROM tasks t WHERE (t.status IN ('IN_PROGRESS','INTERRUPTED')) != EXISTS(SELECT 1 FROM focus_sessions s WHERE s.task_id=t.id AND s.is_active=1) UNION ALL SELECT 1 FROM task_steps WHERE completed NOT IN (0,1)",null).use {require(!it.moveToFirst())}
            // A restored running session stops at export time, never counts days spent in a backup.
            db.execSQL("UPDATE focus_sessions SET elapsed_before_segment_ms=elapsed_before_segment_ms+MAX(?-segment_started_at,0),segment_started_at=NULL,status='INTERRUPTED',updated_at=? WHERE is_active=1 AND status='RUNNING'",arrayOf(exportedAt,exportedAt))
            db.execSQL("UPDATE tasks SET status='INTERRUPTED' WHERE id IN (SELECT task_id FROM focus_sessions WHERE is_active=1)")
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }
}
