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
    private val automaticTaskColor: () -> TaskColor = {
        TaskColor.entries.filterNot { it == TaskColor.NEUTRAL }.random()
    },
) {
    private val mutex = Mutex()
    private val mutableSnapshot = MutableStateFlow(AppSnapshot())
    val snapshot: StateFlow<AppSnapshot> = mutableSnapshot.asStateFlow()

    suspend fun load() = mutate { LearningJournal(database).materializeRecurrences(); refresh() }

    suspend fun capture(
        title: String,
        firstStep: String? = null,
        color: TaskColor? = null,
        steps: List<String> = emptyList(),
    ) = mutate {
        val cleanTitle = title.trim()
        require(cleanTitle.isNotEmpty())
        val cleanSteps = steps.map { it.trim() }.filter { it.isNotEmpty() }
        val timestamp = now()
        val taskId = newId()
        val resolvedColor = color ?: automaticTaskColor()
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            database.insertTask(
                TaskItem(
                    id = taskId,
                    title = cleanTitle,
                    firstStep = cleanSteps.firstOrNull() ?: firstStep.normalizedOrNull(),
                    status = TaskStatus.READY,
                    createdAt = timestamp,
                    updatedAt = timestamp,
                    color = resolvedColor,
                    sortPosition = database.nextTaskPosition(),
                ),
            )
            if (cleanSteps.isNotEmpty()) {
                val journal = LearningJournal(database)
                cleanSteps.forEach { journal.addStep(taskId, it) }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        refresh()
    }

    suspend fun addQuickNote(text: String) = mutate {
        LearningJournal(database).note(text)
        refresh()
    }

    /** Legacy start path: keep the learned reference when no explicit timer choice is supplied. */
    suspend fun start(taskId: String) = mutate {
        startLocked(taskId, database.suggestedDurationMs(taskId))
    }

    /** Explicit start path. A null target is a stopwatch; a positive target is a countdown. */
    suspend fun start(taskId: String, targetDurationMs: Long?) = mutate {
        require(targetDurationMs == null || targetDurationMs > 0)
        startLocked(taskId, targetDurationMs)
    }

    suspend fun suggestedDurationMs(taskId: String): Long? = withContext(ioDispatcher) {
        mutex.withLock {
            requireNotNull(database.taskById(taskId))
            database.suggestedDurationMs(taskId)
        }
    }

    private fun startLocked(taskId: String, targetDurationMs: Long?) {
        requireNotNull(database.taskById(taskId))
        val timestamp = now()
        val previous = database.activeFocus()
        if (previous?.task?.id == taskId && previous.session.status == FocusStatus.RUNNING) return
        if (previous?.task?.id == taskId && previous.session.status == FocusStatus.INTERRUPTED) {
            // Resume preserves the existing persisted session target and mode.
            database.resumeFocus(FocusTransitions.resume(previous.session, timestamp), previous.task.firstStep)
            refresh()
            return
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
                targetDurationMs = targetDurationMs,
            ),
        )
        refresh()
    }

    suspend fun continuePostponed(taskId: String) = mutate {
        val postponed = requireNotNull(database.latestPostponedFocus(taskId))
        val continued = FocusTransitions.continuePostponed(postponed.session, now())
        database.resumePostponedFocus(continued, postponed.task.firstStep)
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
            resumableTaskIds = database.postponedTaskIds(),
        )
    }

    private fun String?.normalizedOrNull(): String? = this?.trim()?.takeUnless { it.isEmpty() }
}
