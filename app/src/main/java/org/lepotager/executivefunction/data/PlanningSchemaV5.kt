package org.lepotager.executivefunction.data

/**
 * Additive schema contract prepared for SQLite 4 -> 5.
 * AppDatabase still owns the actual migration transaction and version bump.
 */
internal object PlanningSchemaV5 {
    const val VERSION = 5

    const val THEME_KEY = "theme_key"
    const val TASK_TYPE = "task_type"
    const val PLANNED_START_AT = "planned_start_at"
    const val DEADLINE_AT = "deadline_at"
    const val COMMITMENT = "commitment"
    const val SESSION_THEME_KEY = "task_theme_key"

    val taskPlanningMigrationSql = listOf(
        "ALTER TABLE task_planning ADD COLUMN $THEME_KEY TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE task_planning ADD COLUMN $TASK_TYPE TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE task_planning ADD COLUMN $PLANNED_START_AT INTEGER",
        "ALTER TABLE task_planning ADD COLUMN $DEADLINE_AT INTEGER",
        "ALTER TABLE task_planning ADD COLUMN $COMMITMENT TEXT NOT NULL DEFAULT ''",
    )

    val sessionContextMigrationSql = listOf(
        "ALTER TABLE session_context ADD COLUMN $SESSION_THEME_KEY TEXT NOT NULL DEFAULT ''",
    )

    /** Neutral values used when restoring a valid v4 backup into a v5 schema. */
    val v4TaskPlanningDefaults: Map<String, Any?> = mapOf(
        THEME_KEY to "",
        TASK_TYPE to "",
        PLANNED_START_AT to null,
        DEADLINE_AT to null,
        COMMITMENT to "",
    )

    val v4SessionContextDefaults: Map<String, Any?> = mapOf(
        SESSION_THEME_KEY to "",
    )
}
