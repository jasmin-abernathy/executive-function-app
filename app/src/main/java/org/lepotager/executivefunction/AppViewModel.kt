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
import org.lepotager.executivefunction.notification.FocusNotificationManager

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FocusRepository(AppDatabase(application))
    private val focusNotificationManager = FocusNotificationManager(application)
    val snapshot = repository.snapshot

    private val mutableError = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = mutableError.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        mutableError.value = throwable
    }

    init {
        launch(::syncFocusNotification) { repository.load() }
    }

    fun capture(title: String, firstStep: String? = null, after: () -> Unit = {}) =
        launch(after) { repository.capture(title, firstStep) }

    fun start(taskId: String) = launch(::syncFocusNotification) { repository.start(taskId) }

    fun interrupt(note: String?) = launch(::syncFocusNotification) { repository.interrupt(note) }

    fun resume(firstStep: String? = null) =
        launch(::syncFocusNotification) { repository.resume(firstStep) }

    fun postpone() = launch(::syncFocusNotification) { repository.postpone() }

    fun complete() = launch(::syncFocusNotification) { repository.complete() }

    fun clearError() {
        mutableError.value = null
    }

    private fun syncFocusNotification() {
        focusNotificationManager.sync(snapshot.value.activeFocus)
    }

    private fun launch(after: () -> Unit = {}, block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            block()
            after()
        }
    }
}
