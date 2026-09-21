package org.lepotager.executivefunction.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TaskStepsPersistenceTest {
    @Test
    fun captureStoresAnyNumberOfOrderedSubtasksAndRestoresFirstStep() = runBlocking {
        val context: Context = RuntimeEnvironment.getApplication()
        var database = AppDatabase(context)
        context.deleteDatabase(database.databaseName)
        try {
            val repository = FocusRepository(database, Dispatchers.Unconfined)
            val expected = listOf("Ouvrir le document", "Écrire le brouillon", "Relire", "Envoyer")
            repository.capture("Préparer le dossier", steps = expected)

            val task = database.openTasks().single()
            assertEquals(expected.first(), task.firstStep)
            assertEquals(expected, LearningJournal(database).steps(task.id).map { it.title })

            database.close()
            database = AppDatabase(context)
            assertEquals(expected, LearningJournal(database).steps(task.id).map { it.title })
            assertEquals(expected.first(), database.taskById(task.id)?.firstStep)
        } finally {
            database.close()
        }
    }
}
