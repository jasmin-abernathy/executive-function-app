package org.lepotager.executivefunction.domain

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class TaskDeadlineTest {
    @Test fun nearbyDatesUseSevenDaysOtherwiseFourteen() {
        val today = LocalDate.of(2026, 10, 11)
        assertEquals(7, TaskDeadline.warningDays(today.plusDays(14), today))
        assertEquals(14, TaskDeadline.warningDays(today.plusDays(15), today))
        assertEquals(7, TaskDeadline.warningDays(today.minusDays(1), today))
    }
    @Test fun borderStartsAtWindowAndStrengthensWithoutGrowingForever() {
        assertEquals(0f, TaskDeadline.intensity(8, 7), 0f)
        assertTrue(TaskDeadline.intensity(7, 7) > 0f)
        assertTrue(TaskDeadline.intensity(3, 7) > TaskDeadline.intensity(7, 7))
        assertEquals(1f, TaskDeadline.intensity(0, 7), 0f)
        assertEquals(1f, TaskDeadline.intensity(-10, 14), 0f)
        assertEquals(0f, TaskDeadline.intensity(15, 14), 0f)
        assertTrue(TaskDeadline.intensity(14, 14) > 0f)
    }
}
