package org.lepotager.executivefunction.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import java.util.UUID

internal enum class CompanionFloatingStyle { CLASSIC, COMPANION }

internal data class CompanionPreferences(
    val universeEnabled: Boolean = false,
    val homePreviewEnabled: Boolean = false,
    val floatingStyle: CompanionFloatingStyle = CompanionFloatingStyle.CLASSIC,
    val animationEnabled: Boolean = true,
    val timerLabelVisible: Boolean = true,
)

/** Relative coordinates in thousandths of the available space. */
internal data class DecorationPlacement(val x: Int, val y: Int) {
    init {
        require(x in 0..1000 && y in 0..1000)
    }
}

internal data class Decoration(val id: String, val assetKey: String, val placement: DecorationPlacement?)
internal data class DecorationDraft(val id: String, val assetKey: String, val progress: Int)
internal data class CompanionWorld(
    val decorations: List<Decoration>,
    val draft: DecorationDraft?,
)

internal class CompanionWorldStore(private val database: AppDatabase) {
    private val db get() = database.writableDatabase

    fun preferences(): CompanionPreferences = db.rawQuery(
        "SELECT universe_enabled, preview_enabled, floating_style, animation_enabled, timer_visible FROM companion_preferences",
        null,
    ).use { cursor ->
        if (!cursor.moveToFirst()) return@use CompanionPreferences()
        CompanionPreferences(
            universeEnabled = cursor.getInt(0) == 1,
            homePreviewEnabled = cursor.getInt(1) == 1,
            floatingStyle = CompanionFloatingStyle.valueOf(cursor.getString(2)),
            animationEnabled = cursor.getInt(3) == 1,
            timerLabelVisible = cursor.getInt(4) == 1,
        )
    }

    fun savePreferences(preferences: CompanionPreferences) {
        val values = ContentValues().apply {
            put("id", 1)
            put("universe_enabled", preferences.universeEnabled)
            put("preview_enabled", preferences.homePreviewEnabled)
            put("floating_style", preferences.floatingStyle.name)
            put("animation_enabled", preferences.animationEnabled)
            put("timer_visible", preferences.timerLabelVisible)
        }
        check(db.replaceOrThrow("companion_preferences", null, values) != -1L)
    }

    fun load(): CompanionWorld = db.rawQuery(
        "SELECT id, asset_key, x, y, draft_progress FROM companion_decorations ORDER BY rowid",
        null,
    ).use { cursor ->
        val decorations = mutableListOf<Decoration>()
        var draft: DecorationDraft? = null
        while (cursor.moveToNext()) {
            val id = cursor.getString(0)
            val assetKey = cursor.getString(1)
            if (cursor.isNull(4)) {
                val placement = if (cursor.isNull(2)) null else DecorationPlacement(cursor.getInt(2), cursor.getInt(3))
                decorations.add(Decoration(id, assetKey, placement))
            } else {
                draft = DecorationDraft(id, assetKey, cursor.getInt(4))
            }
        }
        CompanionWorld(decorations, draft)
    }

    fun beginDraft(assetKey: String): String = insertDecoration(assetKey, progress = 0)

    fun addDecoration(assetKey: String): String = insertDecoration(assetKey, progress = null)

    private fun insertDecoration(assetKey: String, progress: Int?): String {
        require(assetKey.isNotBlank() && assetKey.length <= 128)
        val id = UUID.randomUUID().toString()
        db.insertOrThrow("companion_decorations", null, ContentValues().apply {
            put("id", id)
            put("asset_key", assetKey)
            put("draft_progress", progress)
        })
        return id
    }

    /** Explicit progress supplied by the caller, independently of task completion and elapsed time. */
    fun saveDraftProgress(id: String, progress: Int): Boolean {
        require(progress in 0..1000)
        return updateDraft(id, ContentValues().apply { put("draft_progress", progress) })
    }

    fun finishDraft(id: String): Boolean =
        updateDraft(id, ContentValues().apply { putNull("draft_progress") })

    private fun updateDraft(id: String, values: ContentValues): Boolean =
        db.update("companion_decorations", values, "id=? AND draft_progress IS NOT NULL", arrayOf(id)) == 1

    fun placeDecoration(id: String, placement: DecorationPlacement?): Boolean =
        db.update(
            "companion_decorations",
            ContentValues().apply {
                put("x", placement?.x)
                put("y", placement?.y)
            },
            "id=? AND draft_progress IS NULL",
            arrayOf(id),
        ) == 1

    fun removeDecoration(id: String): Boolean =
        db.delete("companion_decorations", "id=?", arrayOf(id)) == 1

    fun resetDecorations() {
        db.delete("companion_decorations", null, null)
    }

    companion object {
        val tables = listOf("companion_preferences", "companion_decorations")

        fun createTables(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE companion_preferences (
                    id INTEGER PRIMARY KEY CHECK(id = 1),
                    universe_enabled INTEGER NOT NULL CHECK(universe_enabled IN (0, 1)),
                    preview_enabled INTEGER NOT NULL CHECK(preview_enabled IN (0, 1)),
                    floating_style TEXT NOT NULL CHECK(floating_style IN ('CLASSIC', 'COMPANION')),
                    animation_enabled INTEGER NOT NULL CHECK(animation_enabled IN (0, 1)),
                    timer_visible INTEGER NOT NULL CHECK(timer_visible IN (0, 1))
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE companion_decorations (
                    id TEXT PRIMARY KEY NOT NULL CHECK(length(trim(id)) > 0),
                    asset_key TEXT NOT NULL CHECK(length(trim(asset_key)) BETWEEN 1 AND 128),
                    x INTEGER CHECK(x BETWEEN 0 AND 1000),
                    y INTEGER CHECK(y BETWEEN 0 AND 1000),
                    draft_progress INTEGER CHECK(draft_progress BETWEEN 0 AND 1000),
                    CHECK((x IS NULL) = (y IS NULL)),
                    CHECK(draft_progress IS NULL OR x IS NULL)
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE UNIQUE INDEX one_companion_draft ON companion_decorations((1)) WHERE draft_progress IS NOT NULL",
            )
        }
    }
}
