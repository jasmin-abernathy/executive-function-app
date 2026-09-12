package org.lepotager.executivefunction.ui

import androidx.compose.foundation.lazy.LazyListState
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.lepotager.executivefunction.model.TaskItem
import org.lepotager.executivefunction.model.TaskStatus

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TaskReorderTest {
    private fun task(id: String) = TaskItem(id, id, null, TaskStatus.READY, 0, 0)

    @Test
    fun releasePersistsOnceAndCancelNeverPersists() {
        val state = TaskReorder(LazyListState())
        state.tasks = listOf(task("a"), task("b"))
        var writes = 0
        var done: ((Boolean) -> Unit)? = null
        state.persist = { ids, callback ->
            assertEquals(listOf("b", "a"), ids)
            writes++
            done = callback
        }
        state.order = listOf("b", "a")
        state.dragged = "b"
        state.finish()
        state.finish()
        assertEquals(1, writes)
        done!!(true)
        state.order = listOf("b", "a")
        state.cancel()
        state.finish()
        assertEquals(1, writes)
    }

    @Test
    fun failedSaveRestoresRepositoryOrder() {
        val state = TaskReorder(LazyListState())
        state.tasks = listOf(task("a"), task("b"))
        state.persist = { _, done -> done(false) }
        state.order = listOf("b", "a")
        state.finish()
        assertEquals(state.tasks, state.visibleTasks)
        assertFalse(state.saving)
    }
}
