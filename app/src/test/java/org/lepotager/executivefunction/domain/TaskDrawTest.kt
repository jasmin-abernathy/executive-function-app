package org.lepotager.executivefunction.domain

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.lepotager.executivefunction.model.TaskItem
import org.lepotager.executivefunction.model.TaskStatus

class TaskDrawTest {
    @Test
    fun onlyReadyTasksAreEligible() {
        val tasks = listOf(
            task("ready", TaskStatus.READY),
            task("active", TaskStatus.IN_PROGRESS),
            task("interrupted", TaskStatus.INTERRUPTED),
            task("done", TaskStatus.COMPLETED),
        )

        assertEquals("ready", TaskDraw.pick(tasks, random = Random(1))?.id)
    }

    @Test
    fun redrawAvoidsThePreviousTaskWhenAnotherIsAvailable() {
        val tasks = listOf(task("first"), task("second"))

        assertEquals(
            "second",
            TaskDraw.pick(tasks, previousTaskId = "first", random = Random(1))?.id,
        )
    }

    @Test
    fun oneTaskCanStillBeProposedAgain() {
        assertEquals(
            "only",
            TaskDraw.pick(listOf(task("only")), previousTaskId = "only", random = Random(1))?.id,
        )
    }

    @Test
    fun emptyEligibleSetReturnsNoSuggestion() {
        assertNull(TaskDraw.pick(listOf(task("done", TaskStatus.COMPLETED))))
    }

    private fun task(id: String, status: TaskStatus = TaskStatus.READY) = TaskItem(
        id = id,
        title = id,
        firstStep = null,
        status = status,
        createdAt = 0,
        updatedAt = 0,
    )
}
