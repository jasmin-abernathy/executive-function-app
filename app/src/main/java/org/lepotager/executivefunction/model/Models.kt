package org.lepotager.executivefunction.model

enum class TaskStatus {
    READY,
    IN_PROGRESS,
    INTERRUPTED,
    COMPLETED,
}

enum class FocusStatus {
    RUNNING,
    INTERRUPTED,
    POSTPONED,
    COMPLETED,
}

data class TaskItem(
    val id: String,
    val title: String,
    val firstStep: String?,
    val status: TaskStatus,
    val createdAt: Long,
    val updatedAt: Long,
)

data class FocusSession(
    val id: String,
    val taskId: String,
    val status: FocusStatus,
    val elapsedBeforeSegmentMs: Long,
    val segmentStartedAt: Long?,
    val interruptionNote: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

data class ActiveFocus(
    val task: TaskItem,
    val session: FocusSession,
)

data class AppSnapshot(
    val tasks: List<TaskItem> = emptyList(),
    val activeFocus: ActiveFocus? = null,
    val loading: Boolean = true,
)
