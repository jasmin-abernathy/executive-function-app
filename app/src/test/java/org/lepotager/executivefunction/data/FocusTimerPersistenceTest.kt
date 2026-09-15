package org.lepotager.executivefunction.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.lepotager.executivefunction.model.FocusStatus
import org.lepotager.executivefunction.domain.SessionClock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class FocusTimerPersistenceTest {
    @Test
    fun stopwatchSurvivesInterruptionResumeAndDatabaseReload() = verifyPersistence(null)

    @Test
    fun countdownSurvivesInterruptionResumeAndDatabaseReload() = verifyPersistence(5 * 60_000L)

    private fun verifyPersistence(target: Long?) = runBlocking {
        val context: Context = RuntimeEnvironment.getApplication()
        var database = AppDatabase(context)
        val databaseName = database.databaseName
        context.deleteDatabase(databaseName)
        var timestamp = 1_000_000L
        try {
            val repository = FocusRepository(database, Dispatchers.Unconfined, now = { timestamp })
            repository.capture("Persistence test")
            val taskId = repository.snapshot.value.tasks.single().id
            repository.start(taskId, target)
            val sessionId = requireNotNull(repository.snapshot.value.activeFocus).session.id

            timestamp += 30_000L
            repository.interrupt("A short pause")
            val interrupted = requireNotNull(repository.snapshot.value.activeFocus).session
            assertEquals(FocusStatus.INTERRUPTED, interrupted.status)
            assertEquals(target, interrupted.targetDurationMs)
            assertEquals(30_000L, interrupted.elapsedBeforeSegmentMs)

            timestamp += 120_000L
            repository.resume()
            val resumed = requireNotNull(repository.snapshot.value.activeFocus).session
            assertEquals(sessionId, resumed.id)
            assertEquals(FocusStatus.RUNNING, resumed.status)
            assertEquals(target, resumed.targetDurationMs)
            assertEquals(30_000L, resumed.elapsedBeforeSegmentMs)

            database.close()
            database = AppDatabase(context)
            val reloaded = FocusRepository(database, Dispatchers.Unconfined, now = { timestamp })
            reloaded.load()
            val restored = requireNotNull(reloaded.snapshot.value.activeFocus).session
            assertEquals(resumed, restored)

            // Passing another target for the same interrupted session must not replace its mode.
            reloaded.interrupt(null)
            reloaded.start(taskId, if (target == null) 600_000L else null)
            assertEquals(target, requireNotNull(reloaded.snapshot.value.activeFocus).session.targetDurationMs)

            // Exceeding a countdown only changes elapsed time; it never completes the task.
            timestamp += (target ?: 60_000L) + 10_000L
            reloaded.load()
            val continuing = requireNotNull(reloaded.snapshot.value.activeFocus).session
            assertEquals(FocusStatus.RUNNING, continuing.status)
            assertEquals(30_000L + (target ?: 60_000L) + 10_000L, SessionClock.elapsedMs(continuing, timestamp))
        } finally {
            database.close()
            context.deleteDatabase(databaseName)
        }
    }
}
