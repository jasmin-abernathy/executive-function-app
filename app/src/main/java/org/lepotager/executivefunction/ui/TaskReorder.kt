package org.lepotager.executivefunction.ui

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import org.lepotager.executivefunction.model.TaskItem

@Stable
internal class TaskReorder(val listState: LazyListState) {
    var tasks: List<TaskItem> = emptyList()
    var persist: (List<String>, (Boolean) -> Unit) -> Unit = { _, done -> done(false) }
    var order by mutableStateOf<List<String>?>(null)
    var dragged by mutableStateOf<String?>(null)
    var saving by mutableStateOf(false)
    private var center = 0f
    val visibleTasks: List<TaskItem>
        get() = order?.mapNotNull { id -> tasks.firstOrNull { it.id == id } } ?: tasks

    fun start(id: String) {
        if (saving) return
        val item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == id } ?: return
        order = tasks.map { it.id }
        dragged = id
        center = item.offset + item.size / 2f
    }

    fun move(delta: Float) {
        val id = dragged ?: return
        center += delta
        val ids = order?.toMutableList() ?: return
        val target = listState.layoutInfo.visibleItemsInfo.firstOrNull {
            it.key in ids && center >= it.offset && center < it.offset + it.size
        }?.key ?: return
        val from = ids.indexOf(id)
        val to = ids.indexOf(target)
        if (from >= 0 && to >= 0 && from != to) {
            ids.add(to, ids.removeAt(from))
            order = ids
        }
    }

    fun cancel() { dragged = null; if (!saving) order = null }

    fun finish() {
        if (saving) return
        dragged = null
        val ids = order ?: return
        if (ids == tasks.map { it.id }) { order = null; return }
        saving = true
        persist(ids) { order = null; saving = false }
    }

    suspend fun autoScroll() {
        val layout = listState.layoutInfo
        val delta = when {
            center < layout.viewportStartOffset + 72 -> -14f
            center > layout.viewportEndOffset - 72 -> 14f
            else -> 0f
        }
        if (delta != 0f) { listState.scrollBy(delta); move(0f) }
    }

    fun handle(id: String, enabled: Boolean) = Modifier.pointerInput(id, enabled) {
        if (enabled) detectDragGesturesAfterLongPress(
            onDragStart = { start(id) },
            onDragEnd = ::finish,
            onDragCancel = ::cancel,
            onDrag = { change, amount -> change.consume(); move(amount.y) },
        )
    }
}

@Composable
internal fun rememberTaskReorder(
    tasks: List<TaskItem>,
    persist: (List<String>, (Boolean) -> Unit) -> Unit,
): TaskReorder {
    val list = rememberLazyListState()
    val state = remember(list) { TaskReorder(list) }
    state.tasks = tasks
    state.persist = persist
    LaunchedEffect(tasks.map { it.id }.toSet()) {
        if (state.order?.toSet()?.let { it != tasks.map { task -> task.id }.toSet() } == true) state.cancel()
    }
    LaunchedEffect(state.dragged) {
        while (state.dragged != null) { state.autoScroll(); delay(16) }
    }
    return state
}
