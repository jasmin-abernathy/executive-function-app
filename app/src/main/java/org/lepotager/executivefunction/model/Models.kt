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

enum class TaskColor {
    NEUTRAL,
    SAGE,
    BLUE,
    TERRACOTTA,
    LAVENDER,
    SAND,
}

data class TaskItem(
    val id: String,
    val title: String,
    val firstStep: String?,
    val status: TaskStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val color: TaskColor = TaskColor.NEUTRAL,
    val sortPosition: Long = 0,
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
    /** Persisted timer choice: null for stopwatch, positive duration for countdown. */
    val targetDurationMs: Long? = null,
)

data class ActiveFocus(
    val task: TaskItem,
    val session: FocusSession,
)

data class AppSnapshot(
    val tasks: List<TaskItem> = emptyList(),
    val activeFocus: ActiveFocus? = null,
    val loading: Boolean = true,
    val eligibleDrawIds: Set<String>? = null,
    val suggestedTaskId: String? = null,
)
