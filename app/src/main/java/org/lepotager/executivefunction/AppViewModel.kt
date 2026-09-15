package org.lepotager.executivefunction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.lepotager.executivefunction.data.AppDatabase
import org.lepotager.executivefunction.data.FocusRepository
import org.lepotager.executivefunction.domain.SessionClock
import org.lepotager.executivefunction.model.TaskColor

class AppViewModel(application: Application) : AndroidViewModel(application) {
    data class PendingStart(
        val taskId: String,
        val title: String,
        val learnedTargetDurationMs: Long?,
    )

    private val repository = FocusRepository(AppDatabase(application))
    val snapshot = repository.snapshot

    private val mutableError = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = mutableError.asStateFlow()
    private val mutableCompletionLeadMinutes = MutableStateFlow<Long?>(null)
    val completionLeadMinutes: StateFlow<Long?> = mutableCompletionLeadMinutes.asStateFlow()
    private val mutablePendingStart = MutableStateFlow<PendingStart?>(null)
    val pendingStart: StateFlow<PendingStart?> = mutablePendingStart.asStateFlow()
    private var requestedStartTaskId: String? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        mutableError.value = throwable
    }

    init {
        launch { repository.load() }
    }

    fun capture(
        title: String,
        firstStep: String? = null,
        color: TaskColor = TaskColor.NEUTRAL,
        after: () -> Unit = {},
    ) = launch(after) { repository.capture(title, firstStep, color) }

    fun addQuickNote(text: String, after: () -> Unit = {}) =
        launch(after) { repository.addQuickNote(text) }

    /** Kept for non-interactive/internal callers. Normal UI starts go through requestStart(). */
    fun start(taskId: String) = launch { repository.start(taskId) }

    fun requestStart(taskId: String) {
        val task = snapshot.value.tasks.firstOrNull { it.id == taskId } ?: return
        requestedStartTaskId = taskId
        viewModelScope.launch(exceptionHandler) {
            val learnedTarget = repository.suggestedDurationMs(taskId)
            if (requestedStartTaskId == taskId) {
                mutablePendingStart.value = PendingStart(taskId, task.title, learnedTarget)
            }
        }
    }

    fun cancelStart() {
        requestedStartTaskId = null
        mutablePendingStart.value = null
    }

    fun confirmStart(targetDurationMs: Long?) {
        val request = mutablePendingStart.value ?: return
        launch(
            after = {
                requestedStartTaskId = null
                mutablePendingStart.value = null
            },
        ) { repository.start(request.taskId, targetDurationMs) }
    }

    fun reload() = launch { repository.load() }

    fun moveTask(taskId: String, offset: Int) = launch { repository.moveTask(taskId, offset) }
    fun applyTaskOrder(ids: List<String>, onSettled: (Boolean) -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            var saved = false
            try {
                repository.applyTaskOrder(ids)
                saved = true
            } finally {
                onSettled(saved)
            }
        }
    }

    fun applySuggestedOrder() = launch { repository.applySuggestedOrder() }

    fun setTaskColor(taskId: String, color: TaskColor) = launch {
        repository.setTaskColor(taskId, color)
    }

    fun interrupt(note: String?) = launch { repository.interrupt(note) }

    fun resume(firstStep: String? = null) = launch { repository.resume(firstStep) }

    fun postpone() = launch { repository.postpone() }

    fun complete() {
        val active = snapshot.value.activeFocus
        val remainingMs = active?.session?.targetDurationMs?.let { target ->
            target - SessionClock.elapsedMs(active.session, System.currentTimeMillis())
        }
        launch(
            after = {
                if (remainingMs != null && remainingMs > 0) {
                    mutableCompletionLeadMinutes.value = (remainingMs + 59_999L) / 60_000L
                }
            },
        ) { repository.complete() }
    }

    fun clearError() {
        mutableError.value = null
    }

    fun clearCompletionFeedback() {
        mutableCompletionLeadMinutes.value = null
    }

    private fun launch(after: () -> Unit = {}, block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            block()
            // Notification permission/OS failures must not invalidate a saved task.
            try { FocusPresence.sync(getApplication(), snapshot.value.activeFocus) } catch (_: Exception) { }
            after()
        }
    }
}
