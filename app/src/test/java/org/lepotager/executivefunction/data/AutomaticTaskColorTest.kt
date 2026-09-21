package org.lepotager.executivefunction.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.lepotager.executivefunction.model.TaskColor
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AutomaticTaskColorTest {
    @Test
    fun missingColorUsesAutomaticPaletteButExplicitNeutralStaysNeutral() = runBlocking {
        val context: Context = RuntimeEnvironment.getApplication()
        var database = AppDatabase(context)
        context.deleteDatabase(database.databaseName)
        try {
            val repository = FocusRepository(
                database,
                Dispatchers.Unconfined,
                automaticTaskColor = { TaskColor.RED },
            )

            repository.capture("Automatique")
            repository.capture("Neutre", color = TaskColor.NEUTRAL)

            val tasks = database.openTasks()
            assertEquals(TaskColor.RED, tasks.first { it.title == "Automatique" }.color)
            assertEquals(TaskColor.NEUTRAL, tasks.first { it.title == "Neutre" }.color)
        } finally {
            database.close()
        }
    }
}
