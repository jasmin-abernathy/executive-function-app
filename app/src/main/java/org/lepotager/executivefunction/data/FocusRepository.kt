package org.lepotager.executivefunction.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.lepotager.executivefunction.domain.FocusTransitions
import org.lepotager.executivefunction.model.AppSnapshot
import org.lepotager.executivefunction.model.FocusSession
import org.lepotager.executivefunction.model.FocusStatus
import org.lepotager.executivefunction.model.TaskItem
import org.lepotager.executivefunction.model.TaskColor
import org.lepotager.executivefunction.model.TaskStatus
import java.util.UUID

class FocusRepository internal constructor(
    private val database: AppDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val now: () -> Long = System::currentTimeMillis,
    private val newId: () -> String = { UUID.randomUUID().toString() },
) {
    private val mutex = Mutex()
    private val mutableSnapshot = MutableStateFlow(AppSnapshot())
    val snapshot: StateFlow<AppSnapshot> = mutableSnapshot.asStateFlow()

    suspend fun load() = mutate { LearningJournal(database).materializeRecurrences(); refresh() }

    suspend fun capture(
        title: String,
        firstStep: String? = null,
        color: TaskColor = TaskColor.NEUTRAL,
    ) = mutate {
        val cleanTitle = title.trim()
        require(cleanTitle.isNotEmpty())
        val timestamp = now()
        database.insertTask(
            TaskItem(
                id = newId(),
                title = cleanTitle,
                firstStep = firstStep.normalizedOrNull(),
                status = TaskStatus.READY,
                createdAt = timestamp,
                updatedAt = timestamp,
                color = color,
                sortPosition = database.nextTaskPosition(),
            ),
        )
        refresh()
    }

    suspend fun start(taskId: String) = mutate {
        requireNotNull(database.taskById(taskId))
        val timestamp = now()
        val learnedTargetDurationMs = database.suggestedDurationMs(taskId)
        val previous = database.activeFocus()
        if (previous?.task?.id == taskId && previous.session.status == FocusStatus.RUNNING) return@mutate
        if (previous?.task?.id == taskId && previous.session.status == FocusStatus.INTERRUPTED) {
            database.resumeFocus(FocusTransitions.resume(previous.session, timestamp), previous.task.firstStep)
            refresh()
            return@mutate
        }
        val carried = database.postponedElapsedMs(taskId)
        database.startFocus(
            taskId,
            FocusSession(
                id = newId(),
                taskId = taskId,
                status = FocusStatus.RUNNING,
                elapsedBeforeSegmentMs = carried,
                segmentStartedAt = timestamp,
                interruptionNote = null,
                createdAt = timestamp,
                updatedAt = timestamp,
                targetDurationMs = learnedTargetDurationMs,
            ),
        )
        refresh()
    }

    suspend fun applyTaskOrder(ids: List<String>) = mutate {
        database.applyTaskOrder(ids)
        refresh()
    }

    suspend fun moveTask(taskId: String, offset: Int) = mutate {
        database.moveOpenTask(taskId, offset)
        refresh()
    }

    suspend fun applySuggestedOrder() = mutate {
        val tasks=database.openTasks()
        val eligible=database.eligibleDrawIds(tasks) ?: return@mutate
        val journal=LearningJournal(database)
        val candidates=tasks.filter {it.id in eligible}.sortedByDescending {journal.planning(it.id).importance}
        database.applyTaskOrder((candidates+tasks.filter {it.id !in eligible}).map {it.id})
        refresh()
    }

    suspend fun setTaskColor(taskId: String, color: TaskColor) = mutate {
        database.updateTaskColor(taskId, color)
        refresh()
    }

    suspend fun interrupt(note: String?) = mutate {
        val active = requireNotNull(database.activeFocus())
        database.interruptFocus(FocusTransitions.interrupt(active.session, now(), note))
        refresh()
    }

    suspend fun resume(firstStep: String? = null) = mutate {
        val active = requireNotNull(database.activeFocus())
        val session = FocusTransitions.resume(active.session, now())
        database.resumeFocus(session, firstStep ?: active.task.firstStep)
        refresh()
    }

    suspend fun postpone() = close(FocusStatus.POSTPONED, TaskStatus.READY)

    suspend fun complete() = close(FocusStatus.COMPLETED, TaskStatus.COMPLETED)

    private suspend fun close(focusStatus: FocusStatus, taskStatus: TaskStatus) = mutate {
        val active = requireNotNull(database.activeFocus())
        database.closeFocus(FocusTransitions.finish(active.session, now(), focusStatus), taskStatus)
        refresh()
    }

    private suspend fun mutate(block: () -> Unit) {
        withContext(ioDispatcher) {
            mutex.withLock {
                try {
                    block()
                } catch (error: Throwable) {
                    mutableSnapshot.value = mutableSnapshot.value.copy(loading = false)
                    throw error
                }
            }
        }
    }

    private fun refresh() {
        val tasks=database.openTasks()
        val eligible=database.eligibleDrawIds(tasks)
        val journal=LearningJournal(database)
        mutableSnapshot.value = AppSnapshot(
            tasks = tasks,
            activeFocus = database.activeFocus(),
            loading = false,
            eligibleDrawIds = eligible,
            suggestedTaskId = if(eligible==null) null else tasks.filter { it.id in eligible && it.status==TaskStatus.READY }.maxByOrNull { journal.planning(it.id).importance }?.id,
        )
    }

    private fun String?.normalizedOrNull(): String? = this?.trim()?.takeUnless { it.isEmpty() }
}
