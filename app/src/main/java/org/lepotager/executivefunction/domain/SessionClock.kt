package org.lepotager.executivefunction.domain

import org.lepotager.executivefunction.model.FocusSession
import org.lepotager.executivefunction.model.FocusStatus

object SessionClock {
    fun elapsedMs(session: FocusSession, now: Long): Long {
        val liveSegment = if (session.status == FocusStatus.RUNNING) {
            session.segmentStartedAt?.let { startedAt -> (now - startedAt).coerceAtLeast(0) } ?: 0
        } else {
            0
        }
        return (session.elapsedBeforeSegmentMs + liveSegment).coerceAtLeast(0)
    }
}
