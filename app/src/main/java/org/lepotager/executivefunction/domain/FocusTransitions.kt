package org.lepotager.executivefunction.domain

import org.lepotager.executivefunction.model.FocusSession
import org.lepotager.executivefunction.model.FocusStatus

object FocusTransitions {
    fun interrupt(session: FocusSession, now: Long, note: String?): FocusSession {
        require(session.status == FocusStatus.RUNNING)
        return session.copy(
            status = FocusStatus.INTERRUPTED,
            elapsedBeforeSegmentMs = SessionClock.elapsedMs(session, now),
            segmentStartedAt = null,
            interruptionNote = note.normalizedOrNull(),
            updatedAt = now,
        )
    }

    fun resume(session: FocusSession, now: Long): FocusSession {
        require(session.status == FocusStatus.INTERRUPTED)
        return session.copy(
            status = FocusStatus.RUNNING,
            segmentStartedAt = now,
            updatedAt = now,
        )
    }

    fun continuePostponed(session: FocusSession, now: Long): FocusSession {
        require(session.status == FocusStatus.POSTPONED)
        return session.copy(
            status = FocusStatus.RUNNING,
            segmentStartedAt = now,
            updatedAt = now,
        )
    }

    fun finish(session: FocusSession, now: Long, status: FocusStatus): FocusSession {
        require(status == FocusStatus.COMPLETED || status == FocusStatus.POSTPONED)
        require(session.status == FocusStatus.RUNNING || session.status == FocusStatus.INTERRUPTED)
        return session.copy(
            status = status,
            elapsedBeforeSegmentMs = SessionClock.elapsedMs(session, now),
            segmentStartedAt = null,
            updatedAt = now,
        )
    }

    private fun String?.normalizedOrNull(): String? = this?.trim()?.takeUnless { it.isEmpty() }
}
