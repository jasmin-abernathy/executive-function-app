package org.lepotager.executivefunction

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.lepotager.executivefunction.model.ActiveFocus
import org.lepotager.executivefunction.model.FocusSession
import org.lepotager.executivefunction.model.FocusStatus
import org.lepotager.executivefunction.model.TaskItem
import org.lepotager.executivefunction.model.TaskStatus
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BreakScheduleTest {
    private fun active(now: Long): ActiveFocus {
        val task = TaskItem(
            id = "task",
            title = "Lire",
            firstStep = null,
            status = TaskStatus.IN_PROGRESS,
            createdAt = now,
            updatedAt = now,
        )
        val session = FocusSession(
            id = "session",
            taskId = task.id,
            status = FocusStatus.RUNNING,
            elapsedBeforeSegmentMs = 0L,
            segmentStartedAt = now,
            interruptionNote = null,
            createdAt = now,
            updatedAt = now,
        )
        return ActiveFocus(task, session)
    }

    @Test
    fun selectedBreakDurationIsStoredAndCancelable() {
        val context: Context = RuntimeEnvironment.getApplication()
        BreakSchedule.cancel(context)
        val before = System.currentTimeMillis()

        BreakSchedule.start(context, active(before), 12)

        val prefs = context.getSharedPreferences("break_schedule", Context.MODE_PRIVATE)
        assertEquals("session", prefs.getString("session", null))
        assertEquals(12, prefs.getInt("minutes", -1))
        val until = prefs.getLong("until", 0L)
        assertTrue(until >= before + 12 * 60_000L)
        assertTrue(until <= System.currentTimeMillis() + 12 * 60_000L + 2_000L)

        BreakSchedule.cancel(context)
        assertFalse(prefs.contains("session"))
        assertFalse(prefs.contains("minutes"))
        assertFalse(prefs.contains("until"))
    }

    @Test
    fun breakDurationIsClampedToSupportedRange() {
        val context: Context = RuntimeEnvironment.getApplication()
        val now = System.currentTimeMillis()

        BreakSchedule.start(context, active(now), 0)
        assertEquals(1, context.getSharedPreferences("break_schedule", Context.MODE_PRIVATE).getInt("minutes", -1))

        BreakSchedule.start(context, active(now), 99)
        assertEquals(60, context.getSharedPreferences("break_schedule", Context.MODE_PRIVATE).getInt("minutes", -1))

        BreakSchedule.cancel(context)
    }
}
