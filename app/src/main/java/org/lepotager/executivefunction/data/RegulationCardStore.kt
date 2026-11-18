package org.lepotager.executivefunction.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import org.json.JSONArray
import java.util.UUID

/** Local, user-owned steps for moments when concentration or sensory load becomes difficult. */
internal data class RegulationCard(val id: String, val title: String, val steps: List<String>)

internal object RegulationCardRules {
    const val MAX_CARDS = 12
    const val MAX_STEPS = 3
    const val MAX_TITLE_LENGTH = 80
    const val MAX_STEP_LENGTH = 160

    fun normalize(title: String, steps: List<String>): Pair<String, List<String>> {
        val cleanTitle = title.trim()
        val cleanSteps = steps.map { it.trim() }.filter { it.isNotEmpty() }
        require(cleanTitle.length in 1..MAX_TITLE_LENGTH) { "Card title must contain 1 to 80 characters" }
        require(cleanSteps.size in 1..MAX_STEPS) { "Card must contain 1 to 3 steps" }
        require(cleanSteps.all { it.length <= MAX_STEP_LENGTH }) { "Card step is too long" }
        return cleanTitle to cleanSteps
    }
}

/** Included in the regular SQLite backup, not an extra preferences silo. */
internal class RegulationCardStore(private val database: AppDatabase) {
    private val db get() = database.writableDatabase

    fun list(): List<RegulationCard> = database.readableDatabase.rawQuery(
        "SELECT id,title,steps_json FROM regulation_cards ORDER BY sort_position,id", null,
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                val array = JSONArray(cursor.getString(2))
                add(RegulationCard(
                    cursor.getString(0), cursor.getString(1),
                    (0 until array.length()).map { index -> array.getString(index) },
                ))
            }
        }
    }

    fun save(id: String?, title: String, steps: List<String>): RegulationCard {
        val (cleanTitle, cleanSteps) = RegulationCardRules.normalize(title, steps)
        val cardId = id ?: UUID.randomUUID().toString()
        val values = ContentValues().apply {
            put("title", cleanTitle)
            put("steps_json", JSONArray(cleanSteps).toString())
        }
        if (id == null) {
            require(list().size < RegulationCardRules.MAX_CARDS) { "Maximum card count reached" }
            val position = db.rawQuery(
                "SELECT COALESCE(MAX(sort_position),-1)+1 FROM regulation_cards", null,
            ).use { it.moveToFirst(); it.getInt(0) }
            values.put("id", cardId)
            values.put("sort_position", position)
            db.insertOrThrow("regulation_cards", null, values)
        } else {
            require(db.update("regulation_cards", values, "id=?", arrayOf(cardId)) == 1) {
                "Card no longer exists"
            }
        }
        return RegulationCard(cardId, cleanTitle, cleanSteps)
    }

    fun delete(id: String) {
        db.delete("regulation_cards", "id=?", arrayOf(id))
    }

    companion object {
        val tables = listOf("regulation_cards")

        fun createTables(db: SQLiteDatabase) {
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS regulation_cards (
                    id TEXT PRIMARY KEY NOT NULL,
                    title TEXT NOT NULL CHECK(length(trim(title)) BETWEEN 1 AND 80),
                    steps_json TEXT NOT NULL,
                    sort_position INTEGER NOT NULL DEFAULT 0
                )""",
            )
        }
    }
}
