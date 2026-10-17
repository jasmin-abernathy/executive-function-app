package org.lepotager.executivefunction.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class RecentPostponedTaskTest {
    @Test fun anOpenPostponedTaskRemainsEasyToFind() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("executive-function.db")
        val db = AppDatabase(context)
        try {
            val repo = FocusRepository(db, Dispatchers.Unconfined)
            val id = repo.capture("Lire le dossier")
            assertNull(db.recentPostponedTaskId())
            repo.start(id, null)
            repo.postpone()
            assertEquals(id, db.recentPostponedTaskId())
            repo.start(id, null)
            repo.complete()
            assertNull(db.recentPostponedTaskId())
        } finally { db.close() }
    }
}
