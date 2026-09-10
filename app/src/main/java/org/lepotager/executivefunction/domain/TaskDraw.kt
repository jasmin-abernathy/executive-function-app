package org.lepotager.executivefunction.domain

import kotlin.random.Random
import org.lepotager.executivefunction.model.TaskItem
import org.lepotager.executivefunction.model.TaskStatus

/**
 * A small, local escape hatch for choice paralysis.
 *
 * The draw only proposes a ready task. Starting it remains a separate user action.
 */
object TaskDraw {
    fun pick(
        tasks: List<TaskItem>,
        previousTaskId: String? = null,
        random: Random = Random.Default,
    ): TaskItem? {
        val eligible = tasks.filter { it.status == TaskStatus.READY }
        if (eligible.isEmpty()) return null

        val alternatives = eligible.filterNot { it.id == previousTaskId }
        val pool = alternatives.ifEmpty { eligible }
        return pool[random.nextInt(pool.size)]
    }
}
