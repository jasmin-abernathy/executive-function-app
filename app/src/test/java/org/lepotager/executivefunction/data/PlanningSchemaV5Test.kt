package org.lepotager.executivefunction.data

import org.junit.Assert.*
import org.junit.Test

class PlanningSchemaV5Test {
    @Test
    fun migrationAddsOnlyNewPlanningColumns() {
        val sql = PlanningSchemaV5.taskPlanningMigrationSql.joinToString("\n")
        listOf(
            PlanningSchemaV5.THEME_KEY,
            PlanningSchemaV5.TASK_TYPE,
            PlanningSchemaV5.PLANNED_START_AT,
            PlanningSchemaV5.DEADLINE_AT,
            PlanningSchemaV5.COMMITMENT,
        ).forEach { column -> assertTrue(sql.contains("ADD COLUMN $column")) }
        assertEquals(5, PlanningSchemaV5.taskPlanningMigrationSql.size)
    }

    @Test
    fun sessionThemeSnapshotHasNeutralV4Default() {
        assertEquals(
            listOf(PlanningSchemaV5.SESSION_THEME_KEY),
            PlanningSchemaV5.v4SessionContextDefaults.keys.toList(),
        )
        assertEquals("", PlanningSchemaV5.v4SessionContextDefaults[PlanningSchemaV5.SESSION_THEME_KEY])
    }

    @Test
    fun oldBackupsGetNeutralValuesForEveryNewPlanningColumn() {
        assertEquals(
            setOf(
                PlanningSchemaV5.THEME_KEY,
                PlanningSchemaV5.TASK_TYPE,
                PlanningSchemaV5.PLANNED_START_AT,
                PlanningSchemaV5.DEADLINE_AT,
                PlanningSchemaV5.COMMITMENT,
            ),
            PlanningSchemaV5.v4TaskPlanningDefaults.keys,
        )
        assertNull(PlanningSchemaV5.v4TaskPlanningDefaults[PlanningSchemaV5.PLANNED_START_AT])
        assertNull(PlanningSchemaV5.v4TaskPlanningDefaults[PlanningSchemaV5.DEADLINE_AT])
    }
}
