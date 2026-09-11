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
    private val repository = FocusRepository(AppDatabase(application))
    val snapshot = repository.snapshot

    private val mutableError = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = mutableError.asStateFlow()
    private val mutableCompletionLeadMinutes = MutableStateFlow<Long?>(null)
    val completionLeadMinutes: StateFlow<Long?> = mutableCompletionLeadMinutes.asStateFlow()

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

    fun start(taskId: String) = launch { repository.start(taskId) }
    fun reload() = launch { repository.load() }

    fun moveTask(taskId: String, offset: Int) = launch { repository.moveTask(taskId, offset) }
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
