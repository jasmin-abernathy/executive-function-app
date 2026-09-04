package org.lepotager.executivefunction.widget

import org.lepotager.executivefunction.model.ActiveFocus
import org.lepotager.executivefunction.model.FocusStatus
import org.lepotager.executivefunction.model.TaskItem

/**
 * Visual scenes are deliberately independent from productivity.
 *
 * The future official artwork can map these values to static illustrations
 * (sewing, tinkering, resting, observing). Nothing here is animated and no
 * scene is a reward or punishment for completing tasks.
 */
enum class CompanionScene {
    RESTING,
    CRAFTING,
    SEWING,
    OBSERVING,
}

enum class CompanionWidgetStatus {
    FOCUSING,
    RESUMABLE,
    READY,
    QUIET,
}

data class CompanionWidgetState(
    val scene: CompanionScene,
    val status: CompanionWidgetStatus,
    val taskTitle: String? = null,
)

internal object CompanionWidgetStateFactory {
    fun create(
        activeFocus: ActiveFocus?,
        openTasks: List<TaskItem>,
        dayOfYear: Int,
    ): CompanionWidgetState {
        val status = when (activeFocus?.session?.status) {
            FocusStatus.RUNNING -> CompanionWidgetStatus.FOCUSING
            FocusStatus.INTERRUPTED -> CompanionWidgetStatus.RESUMABLE
            else -> if (openTasks.isNotEmpty()) CompanionWidgetStatus.READY else CompanionWidgetStatus.QUIET
        }

        return CompanionWidgetState(
            scene = CompanionScene.entries[Math.floorMod(dayOfYear - 1, CompanionScene.entries.size)],
            status = status,
            taskTitle = activeFocus?.task?.title ?: openTasks.firstOrNull()?.title,
        )
    }
}
