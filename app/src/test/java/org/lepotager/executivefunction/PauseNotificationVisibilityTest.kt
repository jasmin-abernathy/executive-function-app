package org.lepotager.executivefunction

import android.app.Notification
import android.app.NotificationManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.lepotager.executivefunction.model.ActiveFocus
import org.lepotager.executivefunction.model.FocusSession
import org.lepotager.executivefunction.model.FocusStatus
import org.lepotager.executivefunction.model.TaskItem
import org.lepotager.executivefunction.model.TaskStatus
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PauseNotificationVisibilityTest {
    @Test fun focusUpdatesDoNotDismissThePendingPauseDecision() {
        val context = RuntimeEnvironment.getApplication()
        PauseSchedule.cancel(context)
        val manager = context.getSystemService(NotificationManager::class.java)
        val now = System.currentTimeMillis()
        val task = TaskItem("task", "Private title", null, TaskStatus.IN_PROGRESS, now, now)
        val session = FocusSession("pause-test", task.id, FocusStatus.RUNNING, 0,
            now - 26 * 60_000L, null, now - 26 * 60_000L, now)
        val active = ActiveFocus(task, session)
        PauseSchedule.show(context, active)
        assertTrue(manager.activeNotifications.any { it.id == 3108 })
        PauseSchedule.sync(context, active)
        assertTrue(manager.activeNotifications.any { it.id == 3108 })
        assertEquals(Notification.VISIBILITY_PUBLIC, FocusPresence.build(context, active).visibility)
        PauseSchedule.cancel(context)
    }
}
