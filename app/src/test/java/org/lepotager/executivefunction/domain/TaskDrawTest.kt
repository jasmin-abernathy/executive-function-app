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

    @Test
    fun permutationPreservesEveryTaskAndExcludedRelativeOrder() {
        val tasks = listOf(task("a"), task("excluded"), task("b"), task("active", TaskStatus.IN_PROGRESS))
        repeat(40) { seed ->
            val result = TaskDraw.permute(tasks, setOf("a", "b", "active"), random = Random(seed))
            assertEquals(tasks.map { it.id }.toSet(), result.tasks.map { it.id }.toSet())
            assertEquals(tasks.size, result.tasks.size)
            org.junit.Assert.assertTrue(result.proposed?.id in setOf("a", "b"))
            assertEquals(result.proposed, result.tasks.first())
            assertEquals(listOf("excluded", "active"), result.tasks.drop(2).map { it.id })
        }
    }

    @Test
    fun permutationIsDeterministicAndRerollAvoidsPreviousFirst() {
        val tasks = listOf(task("a"), task("b"), task("c"))
        repeat(40) { seed ->
            val first = TaskDraw.permute(tasks, random = Random(seed))
            assertEquals(first, TaskDraw.permute(tasks, random = Random(seed)))
            val next = TaskDraw.permute(first.tasks, previousTaskId = first.proposed?.id, random = Random(seed))
            org.junit.Assert.assertNotEquals(first.proposed?.id, next.proposed?.id)
        }
    }

    @Test
    fun zeroAndOneEligibleTask() {
        assertNull(TaskDraw.permute(emptyList()).proposed)
        val tasks = listOf(task("done", TaskStatus.COMPLETED), task("ready"))
        assertEquals(tasks, TaskDraw.permute(tasks, emptySet()).tasks)
        assertNull(TaskDraw.permute(tasks, emptySet()).proposed)
        assertEquals("ready", TaskDraw.permute(tasks, previousTaskId = "ready").proposed?.id)
    }

    @Test
    fun calmModeAndDisabledSystemAnimationsSkipMotion() {
        assertEquals(0, DieMotion.durationMillis(true, true))
        assertEquals(0, DieMotion.durationMillis(false, false))
        assertEquals(500, DieMotion.durationMillis(false, true))
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
