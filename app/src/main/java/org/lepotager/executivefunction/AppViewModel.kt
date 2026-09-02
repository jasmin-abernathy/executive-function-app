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

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FocusRepository(AppDatabase(application))
    val snapshot = repository.snapshot

    private val mutableError = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = mutableError.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        mutableError.value = throwable
    }

    init {
        launch { repository.load() }
    }

    fun capture(title: String, firstStep: String? = null, after: () -> Unit = {}) =
        launch(after) { repository.capture(title, firstStep) }

    fun start(taskId: String) = launch { repository.start(taskId) }

    fun interrupt(note: String?) = launch { repository.interrupt(note) }

    fun resume(firstStep: String? = null) = launch { repository.resume(firstStep) }

    fun postpone() = launch { repository.postpone() }

    fun complete() = launch { repository.complete() }

    fun clearError() {
        mutableError.value = null
    }

    private fun launch(after: () -> Unit = {}, block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            block()
            after()
        }
    }
}
