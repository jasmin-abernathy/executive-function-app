package org.lepotager.executivefunction.data

import android.content.ContentValues
import java.util.UUID

internal data class JournalTask(val id: String, val title: String, val key: String, val completed: Boolean)
internal data class Observation(val id: String, val duration: Long, val date: Long, val included: Boolean, val status: String, val beforeReset: Boolean = false)
internal data class JournalStep(val id: String, val title: String, val completed: Boolean)
internal data class CheckIn(val id: String, val date: Long, val mood: Int, val motivation: Int, val energy: Int)
internal data class Planning(val importance: Int = 0, val energy: Int = 0, val context: String = "", val today: Boolean = false)
internal data class QuickNote(val id: String, val text: String)
internal data class StepRecommendation(val sourceSessionId: String, val observationCount: Int, val steps: List<String>)

/** User corrections change selection metadata, never raw elapsed time. */
internal class LearningJournal(private val database: AppDatabase) {
    private val db get() = database.writableDatabase

    fun tasks(): List<JournalTask> = db.rawQuery("SELECT id,title,learning_key,status FROM tasks ORDER BY sort_position,created_at", null).use { c ->
        buildList { while (c.moveToNext()) add(JournalTask(c.getString(0), c.getString(1), c.getString(2), c.getString(3) == "COMPLETED")) }
    }

    fun observations(key: String): List<Observation> = db.rawQuery(
        """SELECT s.id,s.elapsed_before_segment_ms,s.updated_at,e.session_id,s.status,
        s.created_at <= COALESCE((SELECT cutoff FROM learning_resets WHERE learning_key=t.learning_key),-1)
        FROM focus_sessions s JOIN tasks t ON t.id=s.task_id
        LEFT JOIN learning_exclusions e ON e.session_id=s.id
        WHERE t.learning_key=? ORDER BY s.updated_at DESC""", arrayOf(key),
    ).use { c -> buildList { while(c.moveToNext()) add(Observation(c.getString(0),c.getLong(1),c.getLong(2),c.isNull(3),c.getString(4),c.getInt(5)==1)) } }

    fun include(sessionId: String, include: Boolean) {
        if (include) db.delete("learning_exclusions", "session_id=?", arrayOf(sessionId))
        else db.insertWithOnConflict("learning_exclusions", null, ContentValues().apply { put("session_id", sessionId) }, android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun override(key: String, minutes: Long?) {
        require(minutes == null || minutes in 1..10080)
        if (minutes == null) db.delete("learning_overrides", "learning_key=?", arrayOf(key))
        else db.insertWithOnConflict("learning_overrides", null, ContentValues().apply { put("learning_key",key); put("duration_ms",minutes * 60_000) }, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun hasOverride(key: String): Boolean = db.rawQuery("SELECT 1 FROM learning_overrides WHERE learning_key=?",arrayOf(key)).use { it.moveToFirst() }
    fun hasReset(key: String): Boolean = db.rawQuery("SELECT 1 FROM learning_resets WHERE learning_key=?",arrayOf(key)).use {it.moveToFirst()}
    fun resetLearning(key: String, reset: Boolean) {
        if(!reset) db.delete("learning_resets","learning_key=?",arrayOf(key))
        else db.insertWithOnConflict("learning_resets",null,ContentValues().apply {put("learning_key",key);put("cutoff",System.currentTimeMillis())},android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun group(taskId: String, key: String) {
        require(key.isNotBlank())
        db.update("tasks",ContentValues().apply { put("learning_key",key) },"id=?",arrayOf(taskId))
    }

    fun steps(taskId: String): List<JournalStep> = db.rawQuery("SELECT id,title,completed FROM task_steps WHERE task_id=? ORDER BY position",arrayOf(taskId)).use { c ->
        buildList { while(c.moveToNext()) add(JournalStep(c.getString(0),c.getString(1),c.getInt(2)==1)) }
    }

    fun addStep(taskId: String, title: String) {
        require(title.isNotBlank())
        val position = db.rawQuery("SELECT COALESCE(MAX(position),-1)+1 FROM task_steps WHERE task_id=?",arrayOf(taskId)).use { it.moveToFirst(); it.getInt(0) }
        db.insertOrThrow("task_steps", null,ContentValues().apply { put("id",UUID.randomUUID().toString());put("task_id",taskId);put("title",title.trim());put("position",position) })
        updateFirstStep(taskId)
    }

    fun finishStep(id: String, completed: Boolean) {
        db.update("task_steps",ContentValues().apply {put("completed",if(completed) 1 else 0)},"id=?",arrayOf(id))
        val taskId=db.rawQuery("SELECT task_id FROM task_steps WHERE id=?",arrayOf(id)).use { if(it.moveToFirst()) it.getString(0) else return }
        updateFirstStep(taskId)
    }

    private fun updateFirstStep(taskId: String) {
        val next=steps(taskId).firstOrNull { !it.completed }?.title
        db.update("tasks",ContentValues().apply {put("first_step",next)},"id=?",arrayOf(taskId))
    }

    fun moveStep(taskId: String, id: String, offset: Int) {
        val all=steps(taskId).toMutableList()
        val from=all.indexOfFirst {it.id==id}
        if(from<0) return
        val to=(from+offset).coerceIn(all.indices)
        all.add(to,all.removeAt(from))
        db.beginTransaction()
        try {
            all.forEachIndexed {index,step -> db.update("task_steps",ContentValues().apply {put("position",index)},"id=?",arrayOf(step.id))}
            updateFirstStep(taskId)
            db.setTransactionSuccessful()
        } finally {db.endTransaction()}
    }

    fun planning(id: String): Planning = db.rawQuery("SELECT importance,energy,context,today FROM task_planning WHERE task_id=?",arrayOf(id)).use {
        if(it.moveToFirst()) Planning(it.getInt(0),it.getInt(1),it.getString(2),it.getString(3)==java.time.LocalDate.now().toString()) else Planning()
    }

    fun plan(id: String, value: Planning) {
        require(value.importance in 0..3 && value.energy in 0..3)
        db.insertWithOnConflict("task_planning", null,ContentValues().apply { put("task_id",id);put("importance",value.importance);put("energy",value.energy);put("context",value.context);put("today",if(value.today) java.time.LocalDate.now().toString() else "") },android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    /** New occurrence, explicit family identity, independent steps and history. */
    fun repeat(id: String): String {
        val task = requireNotNull(database.taskById(id))
        val key = tasks().first { it.id==id }.key
        val newId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        db.beginTransaction()
        try {
            database.insertTask(task.copy(id=newId,status=org.lepotager.executivefunction.model.TaskStatus.READY,createdAt=now,updatedAt=now,sortPosition=database.nextTaskPosition()))
            group(newId,key)
            steps(id).forEach { addStep(newId,it.title) }
            plan(newId,planning(id).copy(today=false))
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
        return newId
    }

    fun recurrence(id: String): Int? = db.rawQuery("SELECT interval_days FROM recurrences WHERE source_task_id=?",arrayOf(id)).use { if(it.moveToFirst()) it.getInt(0) else null }
    fun setRecurrence(id: String, days: Int?) {
        require(days==null || days in 1..365)
        if(days==null) db.delete("recurrences","source_task_id=?",arrayOf(id))
        else db.insertWithOnConflict("recurrences",null,ContentValues().apply {put("source_task_id",id);put("interval_days",days);put("next_date",java.time.LocalDate.now().plusDays(days.toLong()).toString())},android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }
    /** At most one pending occurrence per family; absence never creates a backlog. */
    fun materializeRecurrences() {
        val today=java.time.LocalDate.now()
        db.beginTransaction()
        try {
            val due=db.rawQuery("SELECT source_task_id,interval_days FROM recurrences WHERE next_date<=?",arrayOf(today.toString())).use {c ->buildList {while(c.moveToNext()) add(c.getString(0) to c.getInt(1))}}
            due.forEach {(id,days) ->
                val key=tasks().first {it.id==id}.key
                val exists=db.rawQuery("SELECT 1 FROM tasks WHERE learning_key=? AND status!='COMPLETED' LIMIT 1",arrayOf(key)).use {it.moveToFirst()}
                if(!exists) {val created=repeat(id);plan(created,planning(created).copy(today=true))}
                db.update("recurrences",ContentValues().apply {put("next_date",today.plusDays(days.toLong()).toString())},"source_task_id=?",arrayOf(id))
            }
            db.setTransactionSuccessful()
        } finally {db.endTransaction()}
    }

    fun notes(): List<QuickNote> = db.rawQuery("SELECT id,body FROM quick_notes ORDER BY created_at DESC",null).use {c -> buildList {while(c.moveToNext()) add(QuickNote(c.getString(0),c.getString(1)))}}
    fun note(text: String) {
        require(text.isNotBlank())
        db.insertOrThrow("quick_notes",null,ContentValues().apply {put("id",UUID.randomUUID().toString());put("body",text.trim());put("created_at",System.currentTimeMillis())})
    }
    fun deleteNote(id: String) {db.delete("quick_notes","id=?",arrayOf(id))}
    fun deleteTask(id: String) {
        require(database.activeFocus()?.task?.id!=id)
        db.delete("tasks","id=?",arrayOf(id))
    }
    fun editCheckIn(id: String,mood: Int,motivation: Int,energy: Int) {
        require(mood in 0..3 && motivation in 0..3 && energy in 0..3)
        db.update("check_ins",ContentValues().apply {put("mood",mood);put("motivation",motivation);put("energy",energy)},"id=?",arrayOf(id))
    }

    fun checkIns(): List<CheckIn> = db.rawQuery("SELECT id,recorded_at,mood,motivation,energy FROM check_ins ORDER BY recorded_at DESC",null).use { c ->
        buildList { while(c.moveToNext()) add(CheckIn(c.getString(0),c.getLong(1),c.getInt(2),c.getInt(3),c.getInt(4))) }
    }

    fun checkIn(mood: Int, motivation: Int, energy: Int) {
        require(mood in 0..3 && motivation in 0..3 && energy in 0..3)
        db.insertOrThrow("check_ins",null,ContentValues().apply {put("id",UUID.randomUUID().toString());put("recorded_at",System.currentTimeMillis());put("mood",mood);put("motivation",motivation);put("energy",energy)})
    }

    fun deleteCheckIn(id: String) { db.delete("check_ins","id=?",arrayOf(id)) }
    fun recommendedSteps(taskId: String): StepRecommendation? {
        val current=checkIns().firstOrNull()?.takeIf {System.currentTimeMillis()-it.date in 0..14_400_000L} ?: return null
        val key=tasks().firstOrNull {it.id==taskId}?.key ?: return null
        val sources=db.rawQuery(
            """SELECT s.id FROM focus_sessions s JOIN tasks t ON t.id=s.task_id
            JOIN session_context sc ON sc.session_id=s.id JOIN check_ins c ON c.id=sc.check_in_id
            WHERE t.learning_key=? AND s.status='COMPLETED' AND s.elapsed_before_segment_ms>0
            AND (c.energy<=1)=? AND (c.motivation<=1)=? AND (c.mood<=1)=?
            AND NOT EXISTS(SELECT 1 FROM learning_exclusions e WHERE e.session_id=s.id)
            AND s.created_at>COALESCE((SELECT cutoff FROM learning_resets WHERE learning_key=t.learning_key),-1)
            ORDER BY s.updated_at DESC""",arrayOf(key,if(current.energy<=1) "1" else "0",if(current.motivation<=1) "1" else "0",if(current.mood<=1) "1" else "0"),
        ).use {c ->buildList {while(c.moveToNext()) add(c.getString(0))}}
        if(sources.size<3) return null
        val source=sources.distinct().maxByOrNull {sessionSteps(it).size} ?: return null
        val plan=sessionSteps(source)
        if(plan.isEmpty()) return null
        return StepRecommendation(source,sources.size,plan)
    }
    fun applyRecommendedSteps(taskId: String, sourceId: String) {
        val task=requireNotNull(database.taskById(taskId))
        require(task.status==org.lepotager.executivefunction.model.TaskStatus.READY)
        require(steps(taskId).isEmpty())
        val proposed=sessionSteps(sourceId)
        require(proposed.isNotEmpty())
        db.beginTransaction()
        try {proposed.forEach {addStep(taskId,it)};db.setTransactionSuccessful()} finally {db.endTransaction()}
    }
    private fun sessionSteps(id: String): List<String> = db.rawQuery("SELECT title FROM session_steps WHERE session_id=? ORDER BY position",arrayOf(id)).use {c -> buildList {while(c.moveToNext()) add(c.getString(0))}}
    fun rename(id: String, title: String) {
        require(title.isNotBlank())
        db.update("tasks",ContentValues().apply {put("title",title.trim());put("updated_at",System.currentTimeMillis())},"id=?",arrayOf(id))
    }
}
