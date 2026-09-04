package org.lepotager.executivefunction.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.lepotager.executivefunction.model.ActiveFocus
import org.lepotager.executivefunction.model.FocusSession
import org.lepotager.executivefunction.model.FocusStatus
import org.lepotager.executivefunction.model.TaskItem
import org.lepotager.executivefunction.model.TaskStatus

class FocusNotificationStateTest {
    @Test
    fun runningSessionUsesPersistedAndCurrentSegmentElapsedTime() {
        val state = FocusNotificationStateFactory.create(
            activeFocus = activeFocus(
                status = FocusStatus.RUNNING,
                elapsedBeforeSegmentMs = 5_000,
                segmentStartedAt = 8_000,
            ),
            now = 10_000,
        )

        assertEquals(FocusNotificationKind.RUNNING, state.kind)
        assertEquals("Write report", state.taskTitle)
        assertEquals(7_000, state.elapsedMs)
    }

    @Test
    fun interruptedSessionKeepsFrozenElapsedTime() {
        val state = FocusNotificationStateFactory.create(
            activeFocus = activeFocus(
                status = FocusStatus.INTERRUPTED,
                elapsedBeforeSegmentMs = 7_000,
                segmentStartedAt = null,
            ),
            now = 20_000,
        )

        assertEquals(FocusNotificationKind.RESUMABLE, state.kind)
        assertEquals(7_000, state.elapsedMs)
    }

    @Test
    fun absentFocusHidesNotification() {
        val state = FocusNotificationStateFactory.create(activeFocus = null, now = 10_000)

        assertEquals(FocusNotificationKind.HIDDEN, state.kind)
        assertNull(state.taskTitle)
        assertEquals(0, state.elapsedMs)
    }

    private fun activeFocus(
        status: FocusStatus,
        elapsedBeforeSegmentMs: Long,
        segmentStartedAt: Long?,
    ): ActiveFocus = ActiveFocus(
        task = TaskItem(
            id = "task-1",
            title = "Write report",
            firstStep = null,
            status = if (status == FocusStatus.RUNNING) TaskStatus.IN_PROGRESS else TaskStatus.INTERRUPTED,
            createdAt = 1_000,
            updatedAt = 8_000,
        ),
        session = FocusSession(
            id = "session-1",
            taskId = "task-1",
            status = status,
            elapsedBeforeSegmentMs = elapsedBeforeSegmentMs,
            segmentStartedAt = segmentStartedAt,
            interruptionNote = null,
            createdAt = 1_000,
            updatedAt = 8_000,
        ),
    )
}
