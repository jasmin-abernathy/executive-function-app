package org.lepotager.executivefunction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TaskReminderIntentTest {
    @Test fun tappingTheReminderShowsDetailsWhileStartIsSeparate() {
        val context = RuntimeEnvironment.getApplication()
        val details = TaskReminder.taskIntent(context,"task-abc",false)
        val start = TaskReminder.taskIntent(context,"task-abc",true)
        assertEquals("task-abc",details.getStringExtra("open_task_details"))
        assertNull(details.getStringExtra("requested_task"))
        assertEquals("task-abc",start.getStringExtra("requested_task"))
        assertNull(start.getStringExtra("open_task_details"))
        assertNotEquals(details.data,start.data)
    }
}
