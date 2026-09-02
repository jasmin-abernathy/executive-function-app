package org.lepotager.executivefunction.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.lepotager.executivefunction.model.FocusSession
import org.lepotager.executivefunction.model.FocusStatus

class FocusTransitionsTest {
    @Test
    fun interruptFreezesTimeAndNormalizesContext() {
        val result = FocusTransitions.interrupt(runningSession(), now = 15_000, note = "  section 2  ")

        assertEquals(FocusStatus.INTERRUPTED, result.status)
        assertEquals(7_000, result.elapsedBeforeSegmentMs)
        assertNull(result.segmentStartedAt)
        assertEquals("section 2", result.interruptionNote)
    }

    @Test
    fun resumeKeepsElapsedTimeAndStartsNewSegment() {
        val interrupted = FocusTransitions.interrupt(runningSession(), now = 15_000, note = null)
        val result = FocusTransitions.resume(interrupted, now = 20_000)

        assertEquals(FocusStatus.RUNNING, result.status)
        assertEquals(7_000, result.elapsedBeforeSegmentMs)
        assertEquals(20_000, result.segmentStartedAt)
    }

    @Test
    fun completingAfterResumeCountsBothSegmentsExactlyOnce() {
        val interrupted = FocusTransitions.interrupt(runningSession(), now = 15_000, note = null)
        val resumed = FocusTransitions.resume(interrupted, now = 20_000)
        val result = FocusTransitions.finish(resumed, now = 23_000, status = FocusStatus.COMPLETED)

        assertEquals(FocusStatus.COMPLETED, result.status)
        assertEquals(10_000, result.elapsedBeforeSegmentMs)
        assertNull(result.segmentStartedAt)
    }

    @Test(expected = IllegalArgumentException::class)
    fun interruptedSessionCannotBeInterruptedAgain() {
        val interrupted = FocusTransitions.interrupt(runningSession(), now = 15_000, note = null)
        FocusTransitions.interrupt(interrupted, now = 16_000, note = null)
    }

    private fun runningSession() = FocusSession(
        id = "session",
        taskId = "task",
        status = FocusStatus.RUNNING,
        elapsedBeforeSegmentMs = 2_000,
        segmentStartedAt = 10_000,
        interruptionNote = null,
        createdAt = 10_000,
        updatedAt = 10_000,
    )
}
