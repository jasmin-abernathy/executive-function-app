package org.lepotager.executivefunction.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import org.lepotager.executivefunction.R
import org.lepotager.executivefunction.domain.SessionClock
import org.lepotager.executivefunction.domain.FocusTimeFormat
import org.lepotager.executivefunction.domain.TaskDraw
import org.lepotager.executivefunction.model.ActiveFocus
import org.lepotager.executivefunction.model.TaskColor
import org.lepotager.executivefunction.model.TaskItem

@Composable
fun HomeScreen(
    tasks: List<TaskItem>,
    resumableTaskElapsedMs: Map<String, Long> = emptyMap(),
    onJournal: () -> Unit = {},
    onHelp: () -> Unit = {},
    eligibleDrawIds: Set<String>? = null,
    suggestedTaskId: String? = null,
    drawRequest: Int = 0,
    onApplySuggestedOrder: () -> Unit = {},
    drawEnabled: Boolean,
    pauseSuggestionsEnabled: Boolean,
    pauseAfterMinutes: Int,
    onCapture: (String, String?, TaskColor, () -> Unit) -> Unit,
    onStart: (String) -> Unit,
    onResumeTask: (String) -> Unit = {},
    onMoveTask: (String, Int) -> Unit,
    onApplyTaskOrder: (List<String>, (Boolean) -> Unit) -> Unit,
    onSetTaskColor: (String, TaskColor) -> Unit,
    onSetDrawEnabled: (Boolean) -> Unit,
    onSetPauseSuggestionsEnabled: (Boolean) -> Unit,
    onSetPauseAfterMinutes: (Int) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var firstStep by remember { mutableStateOf("") }
    var showCaptureOptions by remember { mutableStateOf(false) }
    var showSupportOptions by remember { mutableStateOf(false) }
    var drawnTaskId by remember { mutableStateOf<String?>(null) }
    var pendingDrawTaskId by remember { mutableStateOf<String?>(null) }
    var drawRollKey by remember { mutableIntStateOf(0) }
    var isDrawing by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(TaskColor.NEUTRAL) }
    var organizing by remember { mutableStateOf(false) }

    val reorder = rememberTaskReorder(tasks, onApplyTaskOrder)
    val drawResultRequester = remember { BringIntoViewRequester() }
    var proposalOnly by remember { mutableStateOf(false) }
    var handledDrawRequest by remember { mutableIntStateOf(0) }

    fun beginDraw(previousTaskId: String?, onlyPropose: Boolean = false) {
        if (isDrawing || reorder.saving || reorder.dragged != null) return
        val order = TaskDraw.permute(tasks, eligibleDrawIds, previousTaskId)
        val next = if (onlyPropose) TaskDraw.pick(
            tasks.filter { eligibleDrawIds == null || it.id in eligibleDrawIds }, previousTaskId,
        ) else order.proposed
        if (next == null) return
        fun present() {
            proposalOnly = onlyPropose
            pendingDrawTaskId = next.id
            isDrawing = true
            drawRollKey += 1
        }
        if (onlyPropose) present() else {
            reorder.saving = true
            onApplyTaskOrder(order.tasks.map { it.id }) { saved ->
                reorder.saving = false
                if (saved) present()
            }
        }
    }

    LaunchedEffect(drawRequest, tasks, drawEnabled, reorder.saving, reorder.dragged, isDrawing) {
        if (drawRequest > handledDrawRequest && drawEnabled && tasks.isNotEmpty() &&
            !reorder.saving && !isDrawing && reorder.dragged == null) {
            handledDrawRequest = drawRequest
            beginDraw(null, onlyPropose = true)
        }
    }
    LaunchedEffect(drawnTaskId, isDrawing) {
        if (!isDrawing && drawEnabled && tasks.any { it.id == drawnTaskId }) {
            delay(40)
            drawResultRequester.bringIntoView()
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = reorder.listState,
            userScrollEnabled = reorder.dragged == null,
            modifier = Modifier.fillMaxSize().testTag("home-task-list"),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                TextButton(onClick = onHelp, colors = appTextButtonColors()) {
                    UtilityGlyph(UtilityGlyphKind.HELP, Modifier.size(20.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.help_how_it_works))
                }
                TextButton(onClick = onJournal, colors = appTextButtonColors()) {
                    Text(stringResource(R.string.open_journal))
                }
                if (eligibleDrawIds != null) {
                    Text(stringResource(R.string.adaptation_rule))
                    tasks.firstOrNull { it.id == suggestedTaskId }?.let {
                        Text(stringResource(R.string.adaptation_suggestion, it.title))
                    }
                    if (eligibleDrawIds.isEmpty()) Text(stringResource(R.string.adaptation_empty))
                    if (eligibleDrawIds.isNotEmpty()) {
                        TextButton(onClick = onApplySuggestedOrder, colors = appTextButtonColors()) {
                            Text(stringResource(R.string.apply_suggested_order))
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.home_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = appCardColors(),
                    border = appBorder(),
                    shape = RoundedCornerShape(
                        topStart = 28.dp,
                        topEnd = 18.dp,
                        bottomEnd = 28.dp,
                        bottomStart = 18.dp,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.capture_label)) },
                            supportingText = { Text(stringResource(R.string.capture_support)) },
                            singleLine = true,
                            colors = appTextFieldColors(),
                        )
                        TextButton(
                            onClick = { showCaptureOptions = !showCaptureOptions },
                            colors = appTextButtonColors(),
                        ) {
                            Text(
                                stringResource(
                                    if (showCaptureOptions) R.string.capture_options_hide
                                    else R.string.capture_options_show,
                                ),
                            )
                        }
                        if (showCaptureOptions) {
                            OutlinedTextField(
                                value = firstStep,
                                onValueChange = { firstStep = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.first_step_optional)) },
                                singleLine = true,
                                colors = appTextFieldColors(),
                            )
                            Text(
                                text = stringResource(R.string.task_color_optional),
                                style = MaterialTheme.typography.labelLarge,
                            )
                            TaskColorPicker(selected = selectedColor, onSelect = { selectedColor = it })
                        }
                        Button(
                            onClick = {
                                onCapture(title, firstStep, selectedColor) {
                                    title = ""
                                    firstStep = ""
                                    showCaptureOptions = false
                                    selectedColor = TaskColor.NEUTRAL
                                }
                            },
                            enabled = title.isNotBlank(),
                            colors = appButtonColors(),
                            modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                        ) {
                            Text(stringResource(R.string.capture_action))
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.ready_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.semantics { heading() },
                    )
                    if (tasks.size > 1) {
                        TextButton(
                            onClick = { organizing = !organizing },
                            colors = appTextButtonColors(),
                        ) {
                            Text(
                                stringResource(
                                    if (organizing) R.string.finish_organizing
                                    else R.string.organize_tasks,
                                ),
                            )
                        }
                    }
                }
            }
            if (tasks.isEmpty()) {
                item { EmptyTasksPanel() }
            } else {
                itemsIndexed(reorder.visibleTasks, key = { _, task -> task.id }) { index, task ->
                    TaskCard(
                        task = task,
                        resumableElapsedMs = resumableTaskElapsedMs[task.id],
                        handleModifier = reorder.handle(task.id, !isDrawing && !reorder.saving),
                        organizing = organizing,
                        canMoveUp = index > 0 && !reorder.saving && reorder.dragged == null && !isDrawing,
                        canMoveDown = index < tasks.lastIndex && !reorder.saving && reorder.dragged == null && !isDrawing,
                        onMoveUp = { onMoveTask(task.id, -1) },
                        onMoveDown = { onMoveTask(task.id, 1) },
                        onSetColor = { onSetTaskColor(task.id, it) },
                        onStart = { onStart(task.id) },
                        onResume = { onResumeTask(task.id) },
                    )
                }
            }
            if (drawEnabled && tasks.isNotEmpty()) {
                item {
                    FilledTonalButton(
                        onClick = { beginDraw(drawnTaskId) },
                        enabled = !isDrawing && !reorder.saving && reorder.dragged == null && tasks.any {
                            it.status == org.lepotager.executivefunction.model.TaskStatus.READY &&
                                (eligibleDrawIds == null || it.id in eligibleDrawIds)
                        },
                        colors = appTonalButtonColors(),
                        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                    ) {
                        ToolGlyph(ToolGlyphKind.DRAW)
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(R.string.draw_action))
                    }
                }
                if (!isDrawing) tasks.firstOrNull { it.id == drawnTaskId }?.let { task ->
                    item(key = "drawn-${task.id}") {
                        Box(Modifier.bringIntoViewRequester(drawResultRequester)) {
                            TaskDrawPanel(
                                task = task,
                                proposalOnly = proposalOnly,
                                onRedraw = { beginDraw(task.id, proposalOnly) },
                                onStart = { onStart(task.id) },
                            )
                        }
                    }
                }
            }
            item {
                TextButton(
                    onClick = { showSupportOptions = !showSupportOptions },
                    colors = appTextButtonColors(),
                    modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                ) {
                    Text(
                        stringResource(
                            if (showSupportOptions) R.string.support_options_hide
                            else R.string.support_options_show,
                        ),
                    )
                }
            }
            if (showSupportOptions) {
                item {
                    SupportOptionsCard(
                        drawEnabled = drawEnabled,
                        pauseSuggestionsEnabled = pauseSuggestionsEnabled,
                        pauseAfterMinutes = pauseAfterMinutes,
                        onSetDrawEnabled = onSetDrawEnabled,
                        onSetPauseSuggestionsEnabled = onSetPauseSuggestionsEnabled,
                        onSetPauseAfterMinutes = onSetPauseAfterMinutes,
                    )
                }
            }
        }
        if (isDrawing) Box(
            modifier = Modifier.fillMaxSize().zIndex(10f),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedDieBadge(
                rollKey = drawRollKey,
                finalFace = pendingDrawTaskId?.let { stableDieFace(it) } ?: 0,
                description = stringResource(R.string.die_rolling_accessible),
                onRollFinished = {
                    drawnTaskId = pendingDrawTaskId
                    pendingDrawTaskId = null
                    isDrawing = false
                },
            )
        }
    }
}

@Composable
private fun SupportOptionsCard(
    drawEnabled: Boolean,
    pauseSuggestionsEnabled: Boolean,
    pauseAfterMinutes: Int,
    onSetDrawEnabled: (Boolean) -> Unit,
    onSetPauseSuggestionsEnabled: (Boolean) -> Unit,
    onSetPauseAfterMinutes: (Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = appCardColors(),
        border = appBorder(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.support_options_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.random_draw_option))
                    Text(
                        text = stringResource(R.string.random_draw_option_support),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = drawEnabled, onCheckedChange = onSetDrawEnabled)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.pause_suggestions_option))
                    Text(
                        text = stringResource(R.string.pause_suggestions_support),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = pauseSuggestionsEnabled,
                    onCheckedChange = onSetPauseSuggestionsEnabled,
                )
            }
            if (pauseSuggestionsEnabled) {
                Text(
                    text = stringResource(R.string.pause_after_label),
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(15, 25, 45, 60).forEach { minutes ->
                        FilterChip(
                            selected = pauseAfterMinutes == minutes,
                            onClick = { onSetPauseAfterMinutes(minutes) },
                            label = { Text(stringResource(R.string.minutes_short, minutes)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskColorPicker(selected: TaskColor, onSelect: (TaskColor) -> Unit) {
    TaskColor.entries.chunked(3).forEach { rowColors ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            rowColors.forEach { color ->
                val palette = taskPalette(color)
                val name = taskColorName(color)
                Surface(
                    onClick = { onSelect(color) },
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = name },
                    shape = CircleShape,
                    color = palette.container,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(
                        if (selected == color) 3.dp else 1.dp,
                        if (selected == color) MaterialTheme.colorScheme.onSurface else palette.border,
                    ),
                ) {
                    if (selected == color) {
                        Box(contentAlignment = Alignment.Center) {
                            ToolGlyph(ToolGlyphKind.COMPLETE, glyphSize = 18.dp)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}

private data class TaskPalette(val container: Color, val border: Color)

@Composable
private fun taskPalette(color: TaskColor): TaskPalette {
    val dark = isSystemInDarkTheme()
    return when (color) {
        TaskColor.NEUTRAL -> TaskPalette(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.outline,
        )
        TaskColor.SAGE -> if (dark) TaskPalette(Color(0xFF28342C), Color(0xFF71917A))
            else TaskPalette(Color(0xFFEDF6EF), Color(0xFFA9CBB2))
        TaskColor.BLUE -> if (dark) TaskPalette(Color(0xFF27313A), Color(0xFF7189A0))
            else TaskPalette(Color(0xFFEEF4FA), Color(0xFFA8C4DD))
        TaskColor.TERRACOTTA -> if (dark) TaskPalette(Color(0xFF392D29), Color(0xFFA57B6C))
            else TaskPalette(Color(0xFFF9F0EC), Color(0xFFD6AD9D))
        TaskColor.LAVENDER -> if (dark) TaskPalette(Color(0xFF322B39), Color(0xFF8E79A0))
            else TaskPalette(Color(0xFFF4EFF9), Color(0xFFC4ADD8))
        TaskColor.SAND -> if (dark) TaskPalette(Color(0xFF393428), Color(0xFF9C8B61))
            else TaskPalette(Color(0xFFFAF5E8), Color(0xFFD9C493))
    }
}

@Composable
private fun taskColorName(color: TaskColor): String = stringResource(
    when (color) {
        TaskColor.NEUTRAL -> R.string.task_color_neutral
        TaskColor.SAGE -> R.string.task_color_sage
        TaskColor.BLUE -> R.string.task_color_blue
        TaskColor.TERRACOTTA -> R.string.task_color_terracotta
        TaskColor.LAVENDER -> R.string.task_color_lavender
        TaskColor.SAND -> R.string.task_color_sand
    },
)

@Composable
private fun taskCardColors(color: TaskColor) = CardDefaults.cardColors(
    containerColor = taskPalette(color).container,
    contentColor = MaterialTheme.colorScheme.onSurface,
)

@Composable
private fun taskBorder(color: TaskColor) = BorderStroke(1.dp, taskPalette(color).border)

@Composable
private fun TaskDrawPanel(
    task: TaskItem,
    proposalOnly: Boolean,
    onRedraw: () -> Unit,
    onStart: () -> Unit,
) {
    val startLabel = stringResource(R.string.start_action)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = taskCardColors(task.color),
        border = taskBorder(task.color),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp)
                    .clickable(onClickLabel = startLabel, role = Role.Button, onClick = onStart)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ToolBadge(
                    kind = ToolGlyphKind.DRAW,
                    dieFace = stableDieFace(task.id),
                )
                Text(
                    text = stringResource(R.string.draw_result_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.semantics { heading() },
                )
                task.firstStep?.let {
                    Text(
                        text = stringResource(R.string.first_step_value, it),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = stringResource(if (proposalOnly) R.string.draw_result_support else R.string.draw_order_result_support),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onRedraw,
                    colors = appOutlinedButtonColors(),
                    border = appBorder(),
                    modifier = Modifier.weight(1f).sizeIn(minHeight = 48.dp),
                ) {
                    ToolGlyph(ToolGlyphKind.DRAW, dieFace = stableDieFace(task.id))
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.redraw_action))
                }
                Button(
                    onClick = onStart,
                    colors = appButtonColors(),
                    modifier = Modifier.weight(1f).sizeIn(minHeight = 48.dp),
                ) {
                    ToolGlyph(ToolGlyphKind.START)
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.start_action))
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskItem,
    resumableElapsedMs: Long? = null,
    handleModifier: Modifier = Modifier,
    organizing: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onSetColor: (TaskColor) -> Unit,
    onStart: () -> Unit,
    onResume: () -> Unit = {},
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = taskCardColors(task.color),
        border = taskBorder(task.color),
        shape = RoundedCornerShape(
            topStart = 22.dp,
            topEnd = 14.dp,
            bottomEnd = 22.dp,
            bottomStart = 14.dp,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val handleLabel = stringResource(R.string.reorder_handle, task.title)
            val upLabel = stringResource(R.string.move_task_up)
            val downLabel = stringResource(R.string.move_task_down)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    task.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                Box(
                    Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).then(handleModifier)
                        .semantics(mergeDescendants = true) {
                            contentDescription = handleLabel
                            customActions = buildList {
                                if (canMoveUp) add(CustomAccessibilityAction(upLabel) { onMoveUp(); true })
                                if (canMoveDown) add(CustomAccessibilityAction(downLabel) { onMoveDown(); true })
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) { DragHandleGlyph() }
            }
            task.firstStep?.let {
                Text(
                    text = stringResource(R.string.first_step_value, it),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (resumableElapsedMs != null) {
                Text(
                    stringResource(R.string.resume_saved_time, formatElapsed(resumableElapsedMs)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (organizing) {
                Text(
                    text = stringResource(R.string.choose_task_color),
                    style = MaterialTheme.typography.labelLarge,
                )
                TaskColorPicker(selected = task.color, onSelect = onSetColor)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onMoveUp,
                        enabled = canMoveUp,
                        colors = appOutlinedButtonColors(),
                        border = appBorder(),
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(R.string.move_task_up)) }
                    OutlinedButton(
                        onClick = onMoveDown,
                        enabled = canMoveDown,
                        colors = appOutlinedButtonColors(),
                        border = appBorder(),
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(R.string.move_task_down)) }
                }
            }
            Button(
                onClick = if (resumableElapsedMs != null) onResume else onStart,
                colors = appButtonColors(),
                modifier = Modifier.sizeIn(minHeight = 48.dp),
            ) {
                ToolGlyph(ToolGlyphKind.START)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(if (resumableElapsedMs != null) R.string.resume_action else R.string.start_action))
            }
        }
    }
}

@Composable
fun FocusScreen(
    activeFocus: ActiveFocus,
    modalBlocked: Boolean = false,
    onJournal: () -> Unit = {},
    onNotes: () -> Unit = {},
    onMini: () -> Unit = {},
    onPip: () -> Unit = {},
    onNote: () -> Unit = {},
    overlayEnabled: Boolean,
    pauseSuggestionsEnabled: Boolean,
    pauseAfterMinutes: Int,
    onToggleOverlay: () -> Unit,
    onQuickCapture: (String, String?, () -> Unit) -> Unit,
    onInterrupt: (String?) -> Unit,
    onComplete: () -> Unit,
) {
    var showCapture by remember { mutableStateOf(false) }
    var showInterrupt by remember { mutableStateOf(false) }
    var showMoreTools by remember { mutableStateOf(false) }
    var showPauseSuggestion by remember(activeFocus.session.id) { mutableStateOf(false) }
    var now by remember(activeFocus.session.id) { mutableLongStateOf(System.currentTimeMillis()) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    var nextPausePromptAtMs by remember(
        activeFocus.session.id,
        activeFocus.session.segmentStartedAt,
        pauseAfterMinutes,
    ) {
        mutableLongStateOf(
            org.lepotager.executivefunction.PauseSchedule.deadline(context, activeFocus, pauseAfterMinutes),
        )
    }
    LaunchedEffect(activeFocus.session.id, activeFocus.session.segmentStartedAt) {
        lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
            while (true) {
                now = System.currentTimeMillis()
                delay(1_000)
            }
        }
    }
    val elapsed = SessionClock.elapsedMs(activeFocus.session, now)
    LaunchedEffect(elapsed, pauseSuggestionsEnabled, nextPausePromptAtMs) {
        if (
            pauseSuggestionsEnabled &&
            nextPausePromptAtMs != Long.MAX_VALUE &&
            elapsed >= nextPausePromptAtMs
        ) {
            showPauseSuggestion = true
        }
    }
    LaunchedEffect(pauseSuggestionsEnabled) {
        if (!pauseSuggestionsEnabled) showPauseSuggestion = false
    }
    val targetDuration = activeFocus.session.targetDurationMs
    val overtime = targetDuration?.let { (elapsed - it).coerceAtLeast(0) } ?: 0L
    val displayedTime = when {
        targetDuration == null -> elapsed
        overtime > 0 -> overtime
        else -> (targetDuration - elapsed).coerceAtLeast(0)
    }
    val displayedSeconds = displayedTime / 1_000
    val accessibleElapsed = stringResource(
        R.string.timer_accessible,
        displayedSeconds / 3_600,
        (displayedSeconds % 3_600) / 60,
        displayedSeconds % 60,
    )

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.focus_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = activeFocus.task.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            activeFocus.task.firstStep?.let {
                Text(
                    text = stringResource(R.string.first_step_value, it),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ToolBadge(ToolGlyphKind.CLOCK)
            Text(
                text = stringResource(
                    when {
                        targetDuration == null -> R.string.stopwatch_learning_label
                        overtime > 0 -> R.string.timer_overtime_label
                        else -> R.string.timer_suggested_label
                    },
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = (if (overtime > 0) "+" else "") + formatElapsed(displayedTime),
                fontSize = 54.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.semantics { contentDescription = accessibleElapsed },
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = onNote, modifier = Modifier.weight(1f).sizeIn(minHeight = 48.dp)) {
                    Text(stringResource(R.string.focus_tool_note))
                }
                Spacer(Modifier.size(8.dp))
                OutlinedButton(onClick = { showCapture = true }, modifier = Modifier.weight(1f).sizeIn(minHeight = 48.dp)) {
                    Text(stringResource(R.string.create_task_action))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onNotes) { Text(stringResource(R.string.view_saved_notes)) }
                UtilityIconButton(
                    UtilityGlyphKind.MINI_WINDOW,
                    stringResource(R.string.focus_tool_mini_window),
                    onMini,
                )
                UtilityIconButton(
                    UtilityGlyphKind.MORE,
                    stringResource(if (showMoreTools) R.string.focus_tools_less else R.string.focus_tools_more),
                    { showMoreTools = !showMoreTools },
                )
            }
            if (showMoreTools) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = appCardColors(),
                    border = appBorder(),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TextButton(onClick = onJournal, colors = appTextButtonColors()) {
                            Text(stringResource(R.string.open_journal))
                        }
                        TextButton(onClick = onPip, colors = appTextButtonColors()) {
                            Text(stringResource(R.string.focus_tool_pip))
                        }
                        val context = androidx.compose.ui.platform.LocalContext.current
                        TextButton(
                            onClick = {
                                context.startActivity(
                                    android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                        .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                                )
                            },
                            colors = appTextButtonColors(),
                        ) { Text(stringResource(R.string.lock_screen_notifications_settings)) }
                        FilledTonalButton(
                            onClick = onToggleOverlay,
                            colors = appTonalButtonColors(),
                            modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                        ) {
                            ToolGlyph(ToolGlyphKind.CLOCK)
                            Spacer(Modifier.size(8.dp))
                            Text(
                                stringResource(
                                    if (overlayEnabled) R.string.hide_floating_timer
                                    else R.string.show_floating_timer,
                                ),
                            )
                        }
                    }
                }
            }
            OutlinedButton(
                onClick = { showInterrupt = true },
                colors = appOutlinedButtonColors(),
                border = appBorder(),
                modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
            ) {
                ToolGlyph(ToolGlyphKind.PAUSE)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.focus_pause_short))
            }
            Button(
                onClick = onComplete,
                colors = appButtonColors(),
                modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
            ) {
                ToolGlyph(ToolGlyphKind.COMPLETE)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.focus_finish_short))
            }
        }
    }

    if (showCapture && !modalBlocked) {
        CaptureDialog(
            onDismiss = { showCapture = false },
            onCapture = { title -> onQuickCapture(title, null) { showCapture = false } },
        )
    }
    if (showInterrupt && !showCapture && !modalBlocked) {
        InterruptDialog(
            onDismiss = { showInterrupt = false },
            onConfirm = { note ->
                showInterrupt = false
                onInterrupt(note)
            },
        )
    }
    if (showPauseSuggestion && !showCapture && !showInterrupt && !modalBlocked) {
        PauseSuggestionDialog(
            minutes = pauseAfterMinutes,
            onPause = {
                showPauseSuggestion = false
                org.lepotager.executivefunction.PauseSchedule.set(
                    context,
                    activeFocus,
                    elapsed + pauseAfterMinutes * 60_000L,
                )
                onInterrupt(null)
            },
            onContinue = {
                showPauseSuggestion = false
                nextPausePromptAtMs = org.lepotager.executivefunction.domain.PauseReminderTimes.afterContinue(elapsed, pauseAfterMinutes)
                org.lepotager.executivefunction.PauseSchedule.set(context, activeFocus, nextPausePromptAtMs)
            },
            onRemindLater = {
                showPauseSuggestion = false
                nextPausePromptAtMs = org.lepotager.executivefunction.domain.PauseReminderTimes.afterSnooze(elapsed)
                org.lepotager.executivefunction.PauseSchedule.set(context, activeFocus, nextPausePromptAtMs)
            },
        )
    }
}

@Composable
fun ResumeScreen(
    activeFocus: ActiveFocus,
    onResume: (String?) -> Unit,
    onPostpone: () -> Unit,
) {
    var showSmaller by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = stringResource(R.string.resume_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.resume_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = appCardColors(),
            border = appBorder(),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(activeFocus.task.title, style = MaterialTheme.typography.titleLarge)
                activeFocus.session.interruptionNote?.let {
                    Text(stringResource(R.string.resume_context, it))
                }
                activeFocus.task.firstStep?.let {
                    Text(stringResource(R.string.first_step_value, it))
                }
            }
        }
        Button(
            onClick = { onResume(null) },
            colors = appButtonColors(),
            modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
        ) {
            ToolGlyph(ToolGlyphKind.START)
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.resume_action))
        }
        FilledTonalButton(
            onClick = { showSmaller = true },
            colors = appTonalButtonColors(),
            modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
        ) {
            ToolGlyph(ToolGlyphKind.REDUCE)
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.make_smaller_action))
        }
        OutlinedButton(
            onClick = onPostpone,
            colors = appOutlinedButtonColors(),
            border = appBorder(),
            modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
        ) {
            ToolGlyph(ToolGlyphKind.POSTPONE)
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.choose_another_action))
        }
    }

    if (showSmaller) {
        SmallerStepDialog(
            initialValue = activeFocus.task.firstStep.orEmpty(),
            onDismiss = { showSmaller = false },
            onConfirm = { onResume(it) },
        )
    }
}

@Composable
private fun EmptyTasksPanel() {
    Surface(
        shape = RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 16.dp,
            bottomEnd = 24.dp,
            bottomStart = 16.dp,
        ),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = appBorder(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                border = appBorder(),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    ToolGlyph(ToolGlyphKind.COMPLETE)
                }
            }
            Text(
                text = stringResource(R.string.empty_tasks),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CaptureDialog(onDismiss: () -> Unit, onCapture: (String) -> Unit) {
    var value by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_task_action)) },
        text = {
            Column {
                Text(stringResource(R.string.create_task_explanation))
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(stringResource(R.string.capture_label)) },
                    singleLine = true,
                    colors = appTextFieldColors(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCapture(value) },
                enabled = value.isNotBlank(),
                colors = appTextButtonColors(),
            ) {
                Text(stringResource(R.string.capture_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = appTextButtonColors()) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun InterruptDialog(onDismiss: () -> Unit, onConfirm: (String?) -> Unit) {
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.interrupt_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.interrupt_explanation))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.interrupt_note_optional)) },
                    colors = appTextFieldColors(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(note) }, colors = appTextButtonColors()) {
                Text(stringResource(R.string.save_and_stop))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = appTextButtonColors()) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun PauseSuggestionDialog(
    minutes: Int,
    onPause: () -> Unit,
    onContinue: () -> Unit,
    onRemindLater: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onContinue,
        title = { Text(stringResource(R.string.pause_suggestion_title)) },
        text = { Text(stringResource(R.string.pause_suggestion_message, minutes)) },
        confirmButton = {
            TextButton(onClick = onPause, colors = appTextButtonColors()) {
                Text(stringResource(R.string.take_a_pause))
            }
        },
        dismissButton = {
            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = onRemindLater, colors = appTextButtonColors()) {
                    Text(stringResource(R.string.remind_pause_later))
                }
                TextButton(onClick = onContinue, colors = appTextButtonColors()) {
                    Text(stringResource(R.string.continue_without_pause))
                }
            }
        },
    )
}

@Composable
private fun SmallerStepDialog(
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.make_smaller_title)) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(stringResource(R.string.smaller_step_label)) },
                colors = appTextFieldColors(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(value) },
                enabled = value.isNotBlank(),
                colors = appTextButtonColors(),
            ) {
                Text(stringResource(R.string.save_and_resume))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = appTextButtonColors()) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
fun ErrorDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.error_title)) },
        text = { Text(stringResource(R.string.error_message)) },
        confirmButton = {
            TextButton(onClick = onDismiss, colors = appTextButtonColors()) {
                Text(stringResource(R.string.ok))
            }
        },
    )
}

@Composable
fun CompletionFeedbackDialog(minutesAhead: Long, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.completion_ahead_title)) },
        text = {
            Text(
                pluralStringResource(
                    R.plurals.completion_ahead_message,
                    minutesAhead.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                    minutesAhead,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss, colors = appTextButtonColors()) {
                Text(stringResource(R.string.ok))
            }
        },
    )
}

@Composable
private fun appCardColors() = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface,
)

@Composable
private fun appButtonColors() = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
    disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.45f),
)

@Composable
private fun appTonalButtonColors() = ButtonDefaults.filledTonalButtonColors(
    containerColor = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
    disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.45f),
)

@Composable
private fun appOutlinedButtonColors() = ButtonDefaults.outlinedButtonColors(
    contentColor = MaterialTheme.colorScheme.onSurface,
    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
)

@Composable
private fun appTextButtonColors() = ButtonDefaults.textButtonColors(
    contentColor = MaterialTheme.colorScheme.onSurface,
    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
)

@Composable
private fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = MaterialTheme.colorScheme.onSurface,
    focusedBorderColor = MaterialTheme.colorScheme.outline,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.onSurface,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

@Composable
private fun appBorder() = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)

private fun formatElapsed(milliseconds: Long): String {
    return FocusTimeFormat.format(milliseconds)
}
