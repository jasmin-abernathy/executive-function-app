package org.lepotager.executivefunction.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TaskOrderPersistenceTest {
    @Test
    fun orderSurvivesReloadAndInvalidOrderDoesNotLoseTasks() = runBlocking {
        val context: Context = RuntimeEnvironment.getApplication()
        var database = AppDatabase(context)
        context.deleteDatabase(database.databaseName)
        try {
            val repository = FocusRepository(database, Dispatchers.Unconfined)
            listOf("a", "b", "c").forEach { repository.capture(it) }
            val ids = database.openTasks().map { it.id }.reversed()
            repository.applyTaskOrder(ids)
            database.close()
            database = AppDatabase(context)
            FocusRepository(database, Dispatchers.Unconfined).load()
            assertEquals(ids, database.openTasks().map { it.id })
            assertThrows(IllegalArgumentException::class.java) {
                database.applyTaskOrder(listOf(ids[0], ids[0], ids[2]))
            }
            assertEquals(ids, database.openTasks().map { it.id })
        } finally {
            database.close()
        }
    }
}
