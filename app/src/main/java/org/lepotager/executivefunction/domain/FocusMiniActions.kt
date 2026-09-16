package org.lepotager.executivefunction.domain

import org.lepotager.executivefunction.model.FocusStatus

/** Commands exposed by the reduced focus surface (PiP). */
internal enum class FocusMiniCommand {
    PAUSE,
    RESUME,
    ADD_NOTE,
}

/**
 * Pure mapping kept outside MainActivity so PiP actions stay testable.
 * ADD_NOTE is an activity action; pause/resume remain focus-state actions.
 */
internal object FocusMiniActions {
    fun commands(status: FocusStatus): List<FocusMiniCommand> = when (status) {
        FocusStatus.RUNNING -> listOf(FocusMiniCommand.PAUSE, FocusMiniCommand.ADD_NOTE)
        FocusStatus.INTERRUPTED -> listOf(FocusMiniCommand.RESUME, FocusMiniCommand.ADD_NOTE)
        FocusStatus.POSTPONED, FocusStatus.COMPLETED -> emptyList()
    }

    fun receiverAction(command: FocusMiniCommand): String? = when (command) {
        FocusMiniCommand.PAUSE -> "pause"
        FocusMiniCommand.RESUME -> "resume"
        FocusMiniCommand.ADD_NOTE -> null
    }
}
