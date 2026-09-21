package org.lepotager.executivefunction.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.lepotager.executivefunction.model.FocusStatus
import org.lepotager.executivefunction.model.TaskColor
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PostponedResumeTest {
    @Test
    fun continueReactivatesSameSessionWithElapsedTimeAndTarget() = runBlocking {
        val context: Context = RuntimeEnvironment.getApplication()
        var database = AppDatabase(context)
        context.deleteDatabase(database.databaseName)
        var now = 1_000_000L
        try {
            val repository = FocusRepository(
                database,
                Dispatchers.Unconfined,
                now = { now },
                automaticTaskColor = { TaskColor.SAGE },
            )
            repository.capture("Lire")
            val taskId = repository.snapshot.value.tasks.single().id
            val target = 20 * 60_000L

            repository.start(taskId, target)
            val original = requireNotNull(database.activeFocus()).session
            now += 95_000L
            repository.postpone()

            assertEquals(null, database.activeFocus())
            val postponed = requireNotNull(database.latestPostponedFocus(taskId)).session
            assertEquals(original.id, postponed.id)
            assertEquals(FocusStatus.POSTPONED, postponed.status)
            assertEquals(95_000L, postponed.elapsedBeforeSegmentMs)
            assertEquals(target, postponed.targetDurationMs)

            repository.continuePostponed(taskId)
            val continued = assertNotNull(database.activeFocus()).let { requireNotNull(database.activeFocus()).session }
            assertEquals(original.id, continued.id)
            assertEquals(FocusStatus.RUNNING, continued.status)
            assertEquals(95_000L, continued.elapsedBeforeSegmentMs)
            assertEquals(target, continued.targetDurationMs)
        } finally {
            database.close()
        }
    }
}
