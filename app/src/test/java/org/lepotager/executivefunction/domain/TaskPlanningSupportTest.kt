package org.lepotager.executivefunction.domain

import org.junit.Assert.*
import org.junit.Test

class TaskPlanningSupportTest {
    @Test
    fun builtInAndCustomThemesStayExplicit() {
        assertTrue(TaskThemes.isValid(TaskThemes.WORK))
        assertTrue(TaskThemes.isValid(TaskThemes.PERSONAL))
        assertEquals("side project", TaskThemes.customLabel(TaskThemes.custom("  side   project  ")))
        assertTrue(TaskThemes.isValid(""))
        assertFalse(TaskThemes.isValid("mystery"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyCustomThemeIsRejected() {
        TaskThemes.custom("   ")
    }

    @Test
    fun manualDurationWinsWithoutChangingLearnedValue() {
        assertEquals(900_000L, TaskPlanningSupport.effectiveDurationMs(900_000L, 1_200_000L))
        assertEquals(1_200_000L, TaskPlanningSupport.effectiveDurationMs(null, 1_200_000L))
        assertNull(TaskPlanningSupport.effectiveDurationMs(null, null))
    }

    @Test
    fun comfortableStartRequiresRealDeadlineAndKnownDuration() {
        assertEquals(3_600_000L, TaskPlanningSupport.lastComfortableStartAt(5_400_000L, 1_800_000L))
        assertNull(TaskPlanningSupport.lastComfortableStartAt(null, 1_800_000L))
        assertNull(TaskPlanningSupport.lastComfortableStartAt(5_400_000L, null))
        assertEquals(0L, TaskPlanningSupport.lastComfortableStartAt(60_000L, 120_000L))
    }

    @Test
    fun fiveMinuteSuggestionExistsOnlyWhenRequested() {
        assertEquals(400_000L, TaskPlanningSupport.fiveMinuteSuggestionAt(100_000L, true))
        assertNull(TaskPlanningSupport.fiveMinuteSuggestionAt(100_000L, false))
    }

    @Test
    fun contextAndTimeRulesRemainPermissiveWhenUnknown() {
        assertTrue(TaskPlanningSupport.contextMatches("bureau", "BUREAU"))
        assertTrue(TaskPlanningSupport.contextMatches("", "maison"))
        assertTrue(TaskPlanningSupport.contextMatches("bureau", ""))
        assertFalse(TaskPlanningSupport.contextMatches("bureau", "maison"))

        assertTrue(TaskPlanningSupport.fitsAvailableTime(null, 15))
        assertTrue(TaskPlanningSupport.fitsAvailableTime(30 * 60_000L, 0))
        assertTrue(TaskPlanningSupport.fitsAvailableTime(10 * 60_000L, 15))
        assertFalse(TaskPlanningSupport.fitsAvailableTime(20 * 60_000L, 15))
    }

    @Test
    fun plannedStartAndDeadlineStayDifferentConcepts() {
        val now = 1_000L
        assertFalse(TaskPlanningSupport.plannedStartReached(2_000L, now))
        assertTrue(TaskPlanningSupport.plannedStartReached(500L, now))
        assertFalse(TaskPlanningSupport.deadlineOverdue(2_000L, now))
        assertTrue(TaskPlanningSupport.deadlineOverdue(500L, now))
    }
}
