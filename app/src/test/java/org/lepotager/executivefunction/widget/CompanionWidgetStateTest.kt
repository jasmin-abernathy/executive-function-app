package org.lepotager.executivefunction.widget

import org.junit.Assert.assertEquals
import org.junit.Test
import org.lepotager.executivefunction.model.ActiveFocus
import org.lepotager.executivefunction.model.FocusSession
import org.lepotager.executivefunction.model.FocusStatus
import org.lepotager.executivefunction.model.TaskItem
import org.lepotager.executivefunction.model.TaskStatus

class CompanionWidgetStateTest {

    @Test
    fun `quiet when there is nothing open`() {
        val state = CompanionWidgetStateFactory.create(
            activeFocus = null,
            openTasks = emptyList(),
            dayOfYear = 1,
        )

        assertEquals(CompanionWidgetStatus.QUIET, state.status)
        assertEquals(null, state.taskTitle)
    }

    @Test
    fun `ready exposes first open task without implying urgency`() {
        val task = task("Faire une chose", TaskStatus.READY)

        val state = CompanionWidgetStateFactory.create(
            activeFocus = null,
            openTasks = listOf(task),
            dayOfYear = 2,
        )

        assertEquals(CompanionWidgetStatus.READY, state.status)
        assertEquals(task.title, state.taskTitle)
    }

    @Test
    fun `running focus takes precedence over task list`() {
        val task = task("Continuer le dossier", TaskStatus.IN_PROGRESS)
        val state = CompanionWidgetStateFactory.create(
            activeFocus = ActiveFocus(task, session(task.id, FocusStatus.RUNNING)),
            openTasks = listOf(task),
            dayOfYear = 3,
        )

        assertEquals(CompanionWidgetStatus.FOCUSING, state.status)
        assertEquals(task.title, state.taskTitle)
    }

    @Test
    fun `interrupted focus becomes resumable`() {
        val task = task("Reprendre ici", TaskStatus.INTERRUPTED)
        val state = CompanionWidgetStateFactory.create(
            activeFocus = ActiveFocus(task, session(task.id, FocusStatus.INTERRUPTED)),
            openTasks = listOf(task),
            dayOfYear = 4,
        )

        assertEquals(CompanionWidgetStatus.RESUMABLE, state.status)
        assertEquals(task.title, state.taskTitle)
    }

    @Test
    fun `scene rotation is deterministic and independent from productivity`() {
        val day1 = CompanionWidgetStateFactory.create(null, emptyList(), 1).scene
        val day5 = CompanionWidgetStateFactory.create(null, emptyList(), 5).scene

        assertEquals(day1, day5)
    }

    private fun task(title: String, status: TaskStatus) = TaskItem(
        id = "task-id",
        title = title,
        firstStep = null,
        status = status,
        createdAt = 1L,
        updatedAt = 1L,
    )

    private fun session(taskId: String, status: FocusStatus) = FocusSession(
        id = "session-id",
        taskId = taskId,
        status = status,
        elapsedBeforeSegmentMs = 0L,
        segmentStartedAt = if (status == FocusStatus.RUNNING) 1L else null,
        interruptionNote = null,
        createdAt = 1L,
        updatedAt = 1L,
    )
}
