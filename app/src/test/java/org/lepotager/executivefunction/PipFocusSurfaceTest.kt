package org.lepotager.executivefunction

import android.app.PendingIntent
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
@Config(sdk = [28])
class PipFocusSurfaceTest {
    @Test fun pausedSessionReplacesPauseWithResumeAndKeepsNoteAction() {
        val context = RuntimeEnvironment.getApplication()
        val note = PendingIntent.getActivity(context, 42, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val task = TaskItem("task", "Private task", null, TaskStatus.IN_PROGRESS, 0, 0)
        val session = FocusSession("session", task.id, FocusStatus.RUNNING, 0, 1L, null, 0, 0)
        val running = PipFocusSurface.params(context, ActiveFocus(task, session), note).actions
        val paused = PipFocusSurface.params(context, ActiveFocus(task,
            session.copy(status = FocusStatus.INTERRUPTED, segmentStartedAt = null)), note).actions
        assertEquals(listOf(context.getString(R.string.pip_pause), context.getString(R.string.pip_add_note)),
            running.map { it.title.toString() })
        assertEquals(listOf(context.getString(R.string.pip_resume), context.getString(R.string.pip_add_note)),
            paused.map { it.title.toString() })
        assertEquals("resume", shadowOf(paused[0].actionIntent).savedIntent.action)
        assertEquals(session.id, shadowOf(paused[0].actionIntent).savedIntent.getStringExtra("session"))
        assertEquals(note, paused[1].actionIntent)
    }
}
