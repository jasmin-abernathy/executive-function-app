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
import org.lepotager.executivefunction.focus.FocusPresenceService

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FocusRepository(AppDatabase(application))
    val snapshot = repository.snapshot

    private val mutableError = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = mutableError.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        mutableError.value = throwable
    }

    init {
        launch {
            repository.load()
            syncFocusPresence()
        }
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

    fun clearError() {
        mutableError.value = null
    }

    private fun syncFocusPresence() {
        // The database state is authoritative. A notification/launcher failure must
        // never turn a successful task transition into an app error.
        try {
            FocusPresenceService.sync(getApplication(), repository.snapshot.value.activeFocus)
        } catch (_: Throwable) {
            // The service will reconcile next time the app is opened or state changes.
        }
    }

    private fun launch(after: () -> Unit = {}, block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            block()
            after()
        }
    }
}
