package org.lepotager.executivefunction.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import org.lepotager.executivefunction.model.FocusSession
import org.lepotager.executivefunction.model.FocusStatus

class SessionClockTest {
    @Test
    fun runningSessionAddsCurrentSegment() {
        val session = session(
            status = FocusStatus.RUNNING,
            elapsedBeforeSegmentMs = 12_000,
            segmentStartedAt = 100_000,
        )

        assertEquals(17_000, SessionClock.elapsedMs(session, now = 105_000))
    }

    @Test
    fun interruptedSessionDoesNotKeepCounting() {
        val session = session(
            status = FocusStatus.INTERRUPTED,
            elapsedBeforeSegmentMs = 17_000,
            segmentStartedAt = null,
        )

        assertEquals(17_000, SessionClock.elapsedMs(session, now = 500_000))
    }

    @Test
    fun clockChangesCannotProduceNegativeElapsedTime() {
        val session = session(
            status = FocusStatus.RUNNING,
            elapsedBeforeSegmentMs = 0,
            segmentStartedAt = 200_000,
        )

        assertEquals(0, SessionClock.elapsedMs(session, now = 100_000))
    }

    private fun session(
        status: FocusStatus,
        elapsedBeforeSegmentMs: Long,
        segmentStartedAt: Long?,
    ) = FocusSession(
        id = "session",
        taskId = "task",
        status = status,
        elapsedBeforeSegmentMs = elapsedBeforeSegmentMs,
        segmentStartedAt = segmentStartedAt,
        interruptionNote = null,
        createdAt = 0,
        updatedAt = 0,
    )
}
