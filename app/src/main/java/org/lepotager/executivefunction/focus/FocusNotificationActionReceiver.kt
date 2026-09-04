package org.lepotager.executivefunction.focus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.lepotager.executivefunction.data.AppDatabase
import org.lepotager.executivefunction.data.FocusRepository
import org.lepotager.executivefunction.model.FocusStatus

/**
 * Handles explicit notification actions without keeping a process alive.
 *
 * The local database stays authoritative. Actions are ignored safely when the
 * notification is stale and the expected session state no longer exists.
 */
class FocusNotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repository = FocusRepository(AppDatabase(appContext))
                repository.load()
                val status = repository.snapshot.value.activeFocus?.session?.status

                runCatching {
                    when (intent.action) {
                        ACTION_INTERRUPT -> if (status == FocusStatus.RUNNING) {
                            repository.interrupt(null)
                        }

                        ACTION_RESUME -> if (status == FocusStatus.INTERRUPTED) {
                            repository.resume()
                        }

                        ACTION_COMPLETE -> if (
                            status == FocusStatus.RUNNING || status == FocusStatus.INTERRUPTED
                        ) {
                            repository.complete()
                        }
                    }
                }

                FocusPresenceNotifier(appContext).sync(repository.snapshot.value.activeFocus)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_INTERRUPT = "org.lepotager.executivefunction.focus.INTERRUPT"
        const val ACTION_RESUME = "org.lepotager.executivefunction.focus.RESUME"
        const val ACTION_COMPLETE = "org.lepotager.executivefunction.focus.COMPLETE"
    }
}
