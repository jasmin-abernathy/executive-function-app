package org.lepotager.executivefunction.domain

import kotlin.random.Random
import org.lepotager.executivefunction.model.TaskItem
import org.lepotager.executivefunction.model.TaskStatus

/**
 * A small, local escape hatch for choice paralysis.
 *
 * pick proposes a ready task; permute prepares an explicitly requested order. Starting it remains a separate user action.
 */
object TaskDraw {
    data class Order(val tasks: List<TaskItem>, val proposed: TaskItem?)

    fun permute(
        tasks: List<TaskItem>,
        eligibleIds: Set<String>? = null,
        previousTaskId: String? = null,
        random: Random = Random.Default,
    ): Order {
        require(tasks.map { it.id }.distinct().size == tasks.size)
        val ready = tasks.filter {
            it.status == TaskStatus.READY && (eligibleIds == null || it.id in eligibleIds)
        }.shuffled(random).toMutableList()
        if (ready.size > 1 && ready.first().id == previousTaskId) {
            val alternate = random.nextInt(1, ready.size)
            val first = ready[0]
            ready[0] = ready[alternate]
            ready[alternate] = first
        }
        val included = ready.map { it.id }.toSet()
        return Order(ready + tasks.filterNot { it.id in included }, ready.firstOrNull())
    }

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
