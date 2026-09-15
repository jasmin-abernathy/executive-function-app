package org.lepotager.executivefunction

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.lepotager.executivefunction.model.ActiveFocus
import org.lepotager.executivefunction.model.FocusSession
import org.lepotager.executivefunction.model.FocusStatus
import org.lepotager.executivefunction.model.TaskItem
import org.lepotager.executivefunction.model.TaskStatus
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "fr")
class PipFocusSurfaceTest {
    @Test
    fun actionsFollowSessionStateAndNoteOpensTheActivity() {
        val context: Context = RuntimeEnvironment.getApplication()
        val note = PendingIntent.getActivity(
            context, 3109,
            Intent(context, MainActivity::class.java).putExtra("quick_action", "note"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val task = TaskItem("task", "Private task title", null, TaskStatus.IN_PROGRESS, 0, 0)
        val session = FocusSession("session", task.id, FocusStatus.RUNNING, 0, 0L, null, 0, 0, 300_000L)
        for ((status, label, command) in listOf(
            Triple(FocusStatus.RUNNING, "Mettre en pause", "pause"),
            Triple(FocusStatus.INTERRUPTED, "Reprendre", "resume"),
        )) {
            val params = PipFocusSurface.params(context, ActiveFocus(task, session.copy(status = status)), note)
            assertEquals(listOf(label, "Ajouter une note"), params.actions.map { it.title.toString() })
            val action = shadowOf(params.actions.first().actionIntent).savedIntent
            assertEquals(command, action.action)
            assertEquals("session", action.getStringExtra("session"))
            assertEquals(note, params.actions.last().actionIntent)
            assertEquals("note", shadowOf(params.actions.last().actionIntent).savedIntent.getStringExtra("quick_action"))
        }
    }
}
