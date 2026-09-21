package org.lepotager.executivefunction

import android.app.Notification
import android.content.Context
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
class FocusPresenceNotificationTest {
    @Test
    fun runningFocusBuildsPublicOngoingChronometerForLockScreen() {
        val context: Context = RuntimeEnvironment.getApplication()
        val now = System.currentTimeMillis()
        val task = TaskItem(
            id = "task",
            title = "Lire",
            firstStep = null,
            status = TaskStatus.IN_PROGRESS,
            createdAt = now,
            updatedAt = now,
        )
        val session = FocusSession(
            id = "session",
            taskId = task.id,
            status = FocusStatus.RUNNING,
            elapsedBeforeSegmentMs = 65_000L,
            segmentStartedAt = now,
            interruptionNote = null,
            createdAt = now,
            updatedAt = now,
        )

        val notification = FocusPresence.build(context, ActiveFocus(task, session))

        assertEquals(Notification.VISIBILITY_PUBLIC, notification.visibility)
        assertEquals(androidx.core.app.NotificationCompat.CATEGORY_PROGRESS, notification.category)
        assertTrue(notification.flags and Notification.FLAG_ONGOING_EVENT != 0)
        assertTrue(notification.extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER))
    }
}
