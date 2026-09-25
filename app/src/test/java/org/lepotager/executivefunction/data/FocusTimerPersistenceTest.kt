package org.lepotager.executivefunction.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.lepotager.executivefunction.domain.SessionClock
import org.lepotager.executivefunction.model.FocusStatus
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class FocusTimerPersistenceTest {
    @Test fun stopwatchSurvivesPauseResumeAndDatabaseReopen() = verifyPersistence(null)
    @Test fun countdownSurvivesPauseResumeAndDatabaseReopen() = verifyPersistence(300_000L)

    @Test fun postponedTaskResumesSameSessionAndKeepsContext() = runBlocking {
        val context: Context = RuntimeEnvironment.getApplication()
        val database = AppDatabase(context)
        context.deleteDatabase(database.databaseName)
        var now = 1_000_000L
        try {
            val repository = FocusRepository(database, Dispatchers.Unconfined, now = { now })
            repository.capture("Lire", "Page 3")
            val taskId = repository.snapshot.value.tasks.single().id
            repository.start(taskId, 600_000L)
            val originalId = database.activeFocus()!!.session.id
            now += 120_000L
            repository.interrupt("Paragraphe du milieu")
            repository.postpone()
            assertEquals(120_000L, repository.snapshot.value.resumableTaskElapsedMs[taskId])
            now += 900_000L
            repository.resumePostponed(taskId)
            val resumed = database.activeFocus()!!
            assertEquals(originalId, resumed.session.id)
            assertEquals(600_000L, resumed.session.targetDurationMs)
            assertEquals(120_000L, SessionClock.elapsedMs(resumed.session, now))
            assertEquals("Paragraphe du milieu", resumed.session.interruptionNote)
            assertEquals("Page 3", resumed.task.firstStep)
            repository.addQuickNote("Idée à garder")
            assertEquals("Idée à garder", LearningJournal(database).notes().single().text)
        } finally {
            database.close()
            context.deleteDatabase(database.databaseName)
        }
    }

    private fun verifyPersistence(target: Long?) = runBlocking {
        val context: Context = RuntimeEnvironment.getApplication()
        var database = AppDatabase(context)
        val databaseName = database.databaseName
        context.deleteDatabase(databaseName)
        var now = 1_000_000L
        try {
            var repository = FocusRepository(database, Dispatchers.Unconfined, now = { now })
            repository.capture("Lire")
            val taskId = repository.snapshot.value.tasks.single().id
            // A reference exists even for the explicit stopwatch case.
            LearningJournal(database).override("lire", 14)
            assertEquals(840_000L, repository.suggestedDurationMs(taskId))
            repository.start(taskId, target)
            val sessionId = database.activeFocus()!!.session.id
            assertEquals(target, database.activeFocus()!!.session.targetDurationMs)
            now += 120_000L
            repository.interrupt("Reprendre ici")
            assertEquals(target, database.activeFocus()!!.session.targetDurationMs)
            // Neither global defaults nor a new learned reference may rewrite the session.
            context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE).edit()
                .putString("focus_timer_mode", if (target == null) "COUNTDOWN" else "STOPWATCH").commit()
            LearningJournal(database).override("lire", 22)
            now += 600_000L
            repository.resume()
            database.close()
            database = AppDatabase(context)
            repository = FocusRepository(database, Dispatchers.Unconfined, now = { now })
            repository.load()
            val restored = repository.snapshot.value.activeFocus!!.session
            assertEquals(sessionId, restored.id)
            assertEquals(target, restored.targetDurationMs)
            assertEquals(120_000L, SessionClock.elapsedMs(restored, now))
            // Both start entry points must preserve a paused session, including legacy callers.
            repository.interrupt(null)
            repository.start(taskId)
            assertEquals(target, database.activeFocus()!!.session.targetDurationMs)
            repository.interrupt(null)
            repository.start(taskId, if (target == null) 60_000L else null)
            assertEquals(target, database.activeFocus()!!.session.targetDurationMs)
            now += 300_000L
            repository.load()
            val overtime = database.activeFocus()!!.session
            assertEquals(FocusStatus.RUNNING, overtime.status)
            assertEquals(420_000L, SessionClock.elapsedMs(overtime, now))
            assertEquals(target, overtime.targetDurationMs)
            if (target != null) assertTrue(SessionClock.elapsedMs(overtime, now) > target)
            // The existing v4 backup also round-trips the persisted timer mode.
            val backup = LocalBackup(database).export()
            LocalBackup(database).restore(backup)
            assertEquals(target, database.activeFocus()!!.session.targetDurationMs)
            database.readableDatabase.rawQuery("PRAGMA foreign_key_check", null).use {
                assertEquals(0, it.count)
            }
        } finally {
            database.close()
            context.deleteDatabase(databaseName)
        }
    }
}
