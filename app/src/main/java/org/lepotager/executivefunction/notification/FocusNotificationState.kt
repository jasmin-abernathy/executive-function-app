package org.lepotager.executivefunction.notification

import org.lepotager.executivefunction.domain.SessionClock
import org.lepotager.executivefunction.model.ActiveFocus
import org.lepotager.executivefunction.model.FocusStatus

enum class FocusNotificationKind {
    HIDDEN,
    RUNNING,
    RESUMABLE,
}

data class FocusNotificationState(
    val kind: FocusNotificationKind,
    val taskTitle: String? = null,
    val elapsedMs: Long = 0,
)

internal object FocusNotificationStateFactory {
    fun create(activeFocus: ActiveFocus?, now: Long): FocusNotificationState {
        if (activeFocus == null) return FocusNotificationState(FocusNotificationKind.HIDDEN)

        val kind = when (activeFocus.session.status) {
            FocusStatus.RUNNING -> FocusNotificationKind.RUNNING
            FocusStatus.INTERRUPTED -> FocusNotificationKind.RESUMABLE
            FocusStatus.POSTPONED,
            FocusStatus.COMPLETED,
            -> FocusNotificationKind.HIDDEN
        }

        if (kind == FocusNotificationKind.HIDDEN) {
            return FocusNotificationState(kind)
        }

        return FocusNotificationState(
            kind = kind,
            taskTitle = activeFocus.task.title,
            elapsedMs = SessionClock.elapsedMs(activeFocus.session, now).coerceAtLeast(0),
        )
    }
}
