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
import org.lepotager.executivefunction.focus.FocusPresenceNotifier

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FocusRepository(AppDatabase(application))
    private val focusPresenceNotifier = FocusPresenceNotifier(application)
    val snapshot = repository.snapshot

    private val mutableError = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = mutableError.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        mutableError.value = throwable
    }

    init {
        reload()
    }

    fun reload() = launch {
        repository.load()
        syncFocusPresence()
    }

    fun capture(title: String, firstStep: String? = null, after: () -> Unit = {}) =
        launch(after) { repository.capture(title, firstStep) }

    fun start(taskId: String) = launch {
        repository.start(taskId)
        syncFocusPresence()
    }

    fun interrupt(note: String?) = launch {
        repository.interrupt(note)
        syncFocusPresence()
    }

    fun resume(firstStep: String? = null) = launch {
        repository.resume(firstStep)
        syncFocusPresence()
    }

    fun postpone() = launch {
        repository.postpone()
        syncFocusPresence()
    }

    fun complete() = launch {
        repository.complete()
        syncFocusPresence()
    }

    fun refreshFocusPresence() {
        syncFocusPresence()
    }

    fun clearError() {
        mutableError.value = null
    }

    private fun syncFocusPresence() {
        // Local task/session state is authoritative. A SystemUI/permission issue must
        // never turn a successful task transition into an app error.
        try {
            focusPresenceNotifier.sync(repository.snapshot.value.activeFocus)
        } catch (_: Throwable) {
            // The next state change or app opening will try again.
        }
    }

    private fun launch(after: () -> Unit = {}, block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            block()
            after()
        }
    }
}
