package org.lepotager.executivefunction.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import org.lepotager.executivefunction.ui.theme.LightColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import org.lepotager.executivefunction.data.JournalStep
import org.lepotager.executivefunction.domain.SessionClock
import org.lepotager.executivefunction.domain.HomeViewMode
import org.lepotager.executivefunction.domain.TaskDraw
import org.lepotager.executivefunction.model.ActiveFocus
import org.lepotager.executivefunction.model.TaskColor
import org.lepotager.executivefunction.model.TaskItem

@Composable
fun CheckInDialog(
    onDismiss: () -> Unit,
    onSave: (mood: Int, motivation: Int, energy: Int) -> Unit,
) {
    var mood by remember { mutableIntStateOf(1) }
    var motivation by remember { mutableIntStateOf(1) }
    var energy by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.check_in_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.check_in_body))
                Text(
                    text = stringResource(R.string.check_in_scale),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CheckInLevel(stringResource(R.string.check_in_mood), mood) { mood = it }
                CheckInLevel(stringResource(R.string.check_in_motivation), motivation) { motivation = it }
                CheckInLevel(stringResource(R.string.check_in_energy), energy) { energy = it }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(mood, motivation, energy) }) {
                Text(stringResource(R.string.check_in_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.not_now))
            }
        },
    )
}

@Composable
private fun CheckInLevel(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
) {
    val choices = listOf(
        R.string.check_in_level_0,
        R.string.check_in_level_1,
        R.string.check_in_level_2,
        R.string.check_in_level_3,
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        choices.chunked(2).forEachIndexed { rowIndex, rowChoices ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                rowChoices.forEachIndexed { columnIndex, choice ->
                    val level = rowIndex * 2 + columnIndex
                    FilterChip(
                        selected = value == level,
                        onClick = { onChange(level) },
                        label = { Text(stringResource(choice)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    tasks: List<TaskItem>,
    visibleTaskIds: Set<String>? = null,
    onEditTask: (String) -> Unit = {},
    onAddTask: () -> Unit = {},
    onAddNote: () -> Unit = {},
    taskSteps: Map<String, List<JournalStep>> = emptyMap(),
    onToggleStep: (String, String, Boolean) -> Unit = { _, _, _ -> },
    workspace: @Composable () -> Unit = {},
    onJournal: () -> Unit = {},
    onCheckIn: () -> Unit = {},
    onHelp: () -> Unit = {},
    eligibleDrawIds: Set<String>? = null,
    suggestedTaskId: String? = null,
    recentTaskId: String? = null,
    lowEnergy: Boolean = false,
    drawRequest: Int = 0,
    onApplySuggestedOrder: () -> Unit = {},
    drawEnabled: Boolean,
    pauseSuggestionsEnabled: Boolean,
    pauseAfterMinutes: Int,
    onStart: (String) -> Unit,
    homeViewMode: HomeViewMode = HomeViewMode.LIST,
    customStartAvailable: Boolean = true,
    onStartCustom: (String) -> Unit = {},
    onMoveTask: (String, Int) -> Unit,
    onApplyTaskOrder: (List<String>, (Boolean) -> Unit) -> Unit,
    onSetTaskColor: (String, TaskColor) -> Unit,
    onSetDrawEnabled: (Boolean) -> Unit,
    onSetPauseSuggestionsEnabled: (Boolean) -> Unit,
    onSetPauseAfterMinutes: (Int) -> Unit,
) {
    var showSupportOptions by remember { mutableStateOf(false) }
    var showMoreTools by rememberSaveable { mutableStateOf(false) }
    var drawnTaskId by remember { mutableStateOf<String?>(null) }
    var pendingDrawTaskId by remember { mutableStateOf<String?>(null) }
    var drawRollKey by remember { mutableIntStateOf(0) }
    var isDrawing by remember { mutableStateOf(false) }
    var organizing by remember { mutableStateOf(false) }
    var showAllTasks by rememberSaveable { mutableStateOf(homeViewMode == HomeViewMode.LIST) }
    var difficultDay by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(lowEnergy) { if (lowEnergy) showAllTasks = false }
    val effectiveHomeMode = if (lowEnergy) HomeViewMode.ONE_NEXT else homeViewMode
    val visibleTasks = tasks.filter { visibleTaskIds == null || it.id in visibleTaskIds }
    val drawEligibleIds = TaskDraw.eligibleForView(eligibleDrawIds, visibleTaskIds)
    val reorder = rememberTaskReorder(tasks, onApplyTaskOrder)
    val drawResultRequester = remember { BringIntoViewRequester() }
    var proposalOnly by remember { mutableStateOf(false) }
    var handledDrawRequest by remember { mutableIntStateOf(0) }

    fun beginDraw(previousTaskId: String?, onlyPropose: Boolean = false) {
        if (isDrawing || reorder.saving || reorder.dragged != null) return
        // Inside a folder, suggest from that folder only; don't reorder other folders.
        val scopedProposal = onlyPropose || visibleTaskIds != null
        val order = if (scopedProposal) null else TaskDraw.permute(tasks, drawEligibleIds, previousTaskId)
        val next = if (scopedProposal) TaskDraw.pick(
            tasks.filter { drawEligibleIds == null || it.id in drawEligibleIds }, previousTaskId,
        ) else order?.proposed
        if (next == null) return
        fun present() {
            proposalOnly = scopedProposal
            pendingDrawTaskId = next.id
            isDrawing = true
            drawRollKey += 1
        }
        if (scopedProposal) present() else {
            reorder.saving = true
            onApplyTaskOrder(requireNotNull(order).tasks.map { it.id }) { saved ->
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
                Spacer(Modifier.size(14.dp))
                OutlinedButton(
                    onClick = onCheckIn,
                    modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp).testTag("home-check-in"),
                ) { Text(stringResource(R.string.home_check_in)) }
                Spacer(Modifier.size(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = onAddTask,
                        modifier = Modifier.weight(1f).sizeIn(minHeight = 64.dp).testTag("home-add-task"),
                        colors = appButtonColors(),
                    ) {
                        Text(
                            stringResource(R.string.capture_new),
                            textAlign = TextAlign.Center,
                        )
                    }
                    OutlinedButton(
                        onClick = onAddNote,
                        modifier = Modifier.weight(1f).sizeIn(minHeight = 64.dp).testTag("home-add-note"),
                    ) {
                        Text(
                            stringResource(R.string.home_add_note),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                if (lowEnergy) {
                    Text(stringResource(R.string.low_energy_active), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.low_energy_explanation))
                }
                if (eligibleDrawIds != null) {
                    if (!lowEnergy) Text(stringResource(R.string.adaptation_rule))
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
            }
            val recent = visibleTasks.firstOrNull { it.id == recentTaskId && it.status == org.lepotager.executivefunction.model.TaskStatus.READY }
            if (recent != null) item {
                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(R.string.return_to_task_title), style = MaterialTheme.typography.titleMedium)
                        Text(recent.title, style = MaterialTheme.typography.bodyLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(onClick = { onEditTask(recent.id) }) {
                                Text(stringResource(R.string.return_to_task_details))
                            }
                            FilledTonalButton(onClick = { onStart(recent.id) }) {
                                Text(stringResource(R.string.return_to_task_action))
                            }
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
                    if (tasks.size > 1 && visibleTaskIds == null) {
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
            if (effectiveHomeMode != HomeViewMode.LIST && tasks.isNotEmpty()) {
                item {
                    TodayFocusPanel(
                        tasks = visibleTasks.filter { it.status == org.lepotager.executivefunction.model.TaskStatus.READY && (!lowEnergy || eligibleDrawIds == null || it.id in eligibleDrawIds) },
                        mode = effectiveHomeMode,
                        showAll = showAllTasks,
                        noSuggestedTask = lowEnergy,
                        difficultDay = difficultDay,
                        customStartAvailable = customStartAvailable,
                        onToggleAll = { showAllTasks = !showAllTasks },
                        onToggleDifficultDay = {
                            difficultDay = !difficultDay
                            if (difficultDay) showAllTasks = false
                        },
                        onStart = onStart,
                        onStartCustom = onStartCustom,
                        onEditTask = onEditTask,
                        stepsByTask = taskSteps,
                        onToggleStep = onToggleStep,
                    )
                }
            }
            if (visibleTasks.isEmpty()) {
                item { if (visibleTaskIds == null) EmptyTasksPanel() else Text(stringResource(R.string.folder_empty_tasks)) }
            } else if (effectiveHomeMode == HomeViewMode.LIST || showAllTasks) {
                itemsIndexed(reorder.visibleTasks.filter { visibleTaskIds == null || it.id in visibleTaskIds }, key = { _, task -> task.id }) { index, task ->
                    TaskCard(
                        task = task,
                        handleModifier = reorder.handle(task.id, visibleTaskIds == null && !isDrawing && !reorder.saving),
                        organizing = organizing && visibleTaskIds == null,
                        canMoveUp = visibleTaskIds == null && index > 0 && !reorder.saving && reorder.dragged == null && !isDrawing,
                        canMoveDown = visibleTaskIds == null && index < tasks.lastIndex && !reorder.saving && reorder.dragged == null && !isDrawing,
                        onMoveUp = { onMoveTask(task.id, -1) },
                        onMoveDown = { onMoveTask(task.id, 1) },
                        onSetColor = { onSetTaskColor(task.id, it) },
                        onEdit = { onEditTask(task.id) },
                        steps = taskSteps[task.id].orEmpty(),
                        onToggleStep = { stepId, checked -> onToggleStep(task.id, stepId, checked) },
                        onStart = { onStart(task.id) },
                        customStartAvailable = customStartAvailable,
                        onStartCustom = { onStartCustom(task.id) },
                    )
                }
            }
            if (drawEnabled && tasks.isNotEmpty()) {
                item {
                    FilledTonalButton(
                        onClick = { beginDraw(drawnTaskId) },
                        enabled = !isDrawing && !reorder.saving && reorder.dragged == null && tasks.any {
                            it.status == org.lepotager.executivefunction.model.TaskStatus.READY &&
                                (drawEligibleIds == null || it.id in drawEligibleIds)
                        },
                        colors = appTonalButtonColors(),
                        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                    ) {
                        ToolGlyph(ToolGlyphKind.DRAW)
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(R.string.draw_action))
                    }
                }
                if (!isDrawing) tasks.firstOrNull { it.id == drawnTaskId && (visibleTaskIds == null || it.id in visibleTaskIds) }?.let { task ->
                    item(key = "drawn-${task.id}") {
                        Box(Modifier.bringIntoViewRequester(drawResultRequester)) {
                            TaskDrawPanel(
                                task = task,
                                proposalOnly = proposalOnly,
                                onRedraw = { beginDraw(task.id, proposalOnly) },
                                onStart = { onStart(task.id) },
                                steps = taskSteps[task.id].orEmpty(),
                                onToggleStep = { stepId, checked -> onToggleStep(task.id, stepId, checked) },
                                customStartAvailable = customStartAvailable,
                                onStartCustom = { onStartCustom(task.id) },
                            )
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { showMoreTools = !showMoreTools },
                        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                    ) {
                        Text(stringResource(if (showMoreTools) R.string.home_hide_tools else R.string.home_more_tools))
                    }
                    if (showMoreTools) {
                        OutlinedButton(onClick = onJournal, modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp)) {
                            Text(stringResource(R.string.open_journal))
                        }
                        TextButton(onClick = onHelp) {
                            UtilityGlyph(UtilityGlyphKind.HELP, Modifier.size(20.dp))
                            Spacer(Modifier.size(8.dp))
                            Text(stringResource(R.string.help_how_it_works))
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
            // Notes and themes remain optional; their detail is the last home block.
            item(key = "home-workspace-notes") { workspace() }
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
private fun TodayFocusPanel(
    tasks: List<TaskItem>,
    mode: HomeViewMode,
    showAll: Boolean,
    noSuggestedTask: Boolean = false,
    difficultDay: Boolean,
    customStartAvailable: Boolean,
    onToggleAll: () -> Unit,
    onToggleDifficultDay: () -> Unit,
    onEditTask: (String) -> Unit,
    onStart: (String) -> Unit,
    onStartCustom: (String) -> Unit,
    stepsByTask: Map<String, List<JournalStep>> = emptyMap(),
    onToggleStep: (String, String, Boolean) -> Unit = { _, _, _ -> },
) {
    val current = tasks.firstOrNull()
    Card(modifier = Modifier.fillMaxWidth(), colors = appCardColors(), border = current?.let { taskBorder(it) } ?: appBorder(), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.home_now_label), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (current == null) {
                Text(stringResource(if (noSuggestedTask) R.string.low_energy_no_suggestion else R.string.empty_tasks), style = MaterialTheme.typography.bodyLarge)
            } else {
                Text(current.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = { onEditTask(current.id) }) { Text(stringResource(R.string.task_steps_folder)) }
                TaskDeadlineLabel(current)
                if (stepsByTask[current.id].orEmpty().isNotEmpty()) {
                    TaskStepChecklist(stepsByTask[current.id].orEmpty()) { stepId, checked ->
                        onToggleStep(current.id, stepId, checked)
                    }
                } else current.firstStep?.let {
                    Text(stringResource(R.string.first_step_value, it), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = { onStart(current.id) }, modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp).testTag("home-primary-start"), colors = appButtonColors()) {
                    ToolGlyph(ToolGlyphKind.START); Spacer(Modifier.size(8.dp)); Text(stringResource(R.string.start_action))
                }
                if (customStartAvailable) TextButton(onClick = { onStartCustom(current.id) }, modifier = Modifier.fillMaxWidth().testTag("home-custom-start")) {
                    Text(stringResource(R.string.start_custom_once))
                }
            }
            if (mode == HomeViewMode.NOW_NEXT && !difficultDay && tasks.size > 1) {
                Text(stringResource(R.string.home_next_label), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                tasks.drop(1).take(2).forEach { task ->
                    TextButton(onClick = { onStart(task.id) }, modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp)) {
                        Text(task.title, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                        Text(stringResource(R.string.start_action))
                    }
                }
            }
            if (difficultDay) Text(stringResource(R.string.home_difficult_day_support), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (mode == HomeViewMode.NOW_NEXT && tasks.size > 1) TextButton(onClick = onToggleDifficultDay, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(if (difficultDay) R.string.home_difficult_day_active else R.string.home_difficult_day_action))
            }
            TextButton(onClick = onToggleAll, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(if (showAll) R.string.home_hide_all else R.string.home_open_all))
            }
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
/**
 * Put six cheerful warm shades first while keeping the original persisted enum
 * identifiers (and a real NEUTRAL option) available for existing tasks/backups.
 */
internal val taskColorPickerOrder: List<TaskColor> = listOf(
    TaskColor.ROSE,
    TaskColor.TERRACOTTA,
    TaskColor.CORAL,
    TaskColor.APRICOT,
    TaskColor.SAND,
    TaskColor.LEMON,
).let { warmColors -> warmColors + TaskColor.entries.filterNot { it in warmColors } }

private fun TaskColorPicker(selected: TaskColor, onSelect: (TaskColor) -> Unit) {
    taskColorPickerOrder.chunked(3).forEach { rowColors ->
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
                    contentColor = Color(0xFF2D2D2D),
                    border = BorderStroke(
                        if (selected == color) 3.dp else 1.dp,
                        if (selected == color) Color(0xFF2D2D2D) else palette.border,
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
        Spacer(Modifier.height(12.dp))
    }
}

internal data class TaskPalette(val container: Color, val border: Color)

internal fun taskPalette(color: TaskColor): TaskPalette {
    return when (color) {
        TaskColor.NEUTRAL -> TaskPalette(Color.White, Color(0xFFB7D8BF))
        TaskColor.SAGE -> TaskPalette(Color(0xFFEDF6EF), Color(0xFFA9CBB2))
        TaskColor.BLUE -> TaskPalette(Color(0xFFEEF4FA), Color(0xFFA8C4DD))
        TaskColor.TERRACOTTA -> TaskPalette(Color(0xFFE99883), Color(0xFFAA5848))
        TaskColor.LAVENDER -> TaskPalette(Color(0xFFF4EFF9), Color(0xFFC4ADD8))
        TaskColor.SAND -> TaskPalette(Color(0xFFFFE992), Color(0xFFB99220))
        TaskColor.MINT -> TaskPalette(Color(0xFFD8F5E5), Color(0xFF83C8A4))
        TaskColor.TURQUOISE -> TaskPalette(Color(0xFFD4F3F1), Color(0xFF79C4BF))
        TaskColor.SKY -> TaskPalette(Color(0xFFD9EEFF), Color(0xFF8DBDE2))
        TaskColor.PERIWINKLE -> TaskPalette(Color(0xFFE1E5FF), Color(0xFFA0ACE0))
        TaskColor.LILAC -> TaskPalette(Color(0xFFEEDBFA), Color(0xFFC09AD8))
        TaskColor.ROSE -> TaskPalette(Color(0xFFF4A0B6), Color(0xFFC45478))
        TaskColor.CORAL -> TaskPalette(Color(0xFFFFB79B), Color(0xFFD6724C))
        TaskColor.APRICOT -> TaskPalette(Color(0xFFFFCA82), Color(0xFFCC822D))
        TaskColor.LEMON -> TaskPalette(Color(0xFFE3F08B), Color(0xFF94AC29))
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
        TaskColor.MINT -> R.string.task_color_mint
        TaskColor.TURQUOISE -> R.string.task_color_turquoise
        TaskColor.SKY -> R.string.task_color_sky
        TaskColor.PERIWINKLE -> R.string.task_color_periwinkle
        TaskColor.LILAC -> R.string.task_color_lilac
        TaskColor.ROSE -> R.string.task_color_rose
        TaskColor.CORAL -> R.string.task_color_coral
        TaskColor.APRICOT -> R.string.task_color_apricot
        TaskColor.LEMON -> R.string.task_color_lemon
    },
)

@Composable
private fun taskCardColors(color: TaskColor) = CardDefaults.cardColors(
    containerColor = taskPalette(color).container,
    contentColor = MaterialTheme.colorScheme.onSurface,
)

@Composable
private fun taskBorder(task: TaskItem): BorderStroke {
    val today = deadlineToday()
    val strength = task.deadlineDay?.let { org.lepotager.executivefunction.domain.TaskDeadline.intensity(it - today.toEpochDay(), task.deadlineWarningDays) } ?: 0f
    return BorderStroke((1f + 3f * strength).dp, androidx.compose.ui.graphics.lerp(taskPalette(task.color).border, Color(0xFF7761B3), strength))
}

@Composable
private fun deadlineToday(): java.time.LocalDate {
    var today by remember { mutableStateOf(java.time.LocalDate.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            val zone = java.time.ZoneId.systemDefault()
            val nextDay = java.time.LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            delay((nextDay - System.currentTimeMillis()).coerceAtLeast(1_000L))
            today = java.time.LocalDate.now()
        }
    }
    return today
}

@Composable
private fun TaskDeadlineLabel(task: TaskItem) {
    val day = task.deadlineDay ?: return
    val today = deadlineToday()
    val days = day - today.toEpochDay()
    val date = java.time.LocalDate.ofEpochDay(day).format(java.time.format.DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM))
    Text(
        stringResource(when { days < 0 -> R.string.task_date_passed; days == 0L -> R.string.task_date_today; else -> R.string.task_date_future }, date),
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun TaskDrawPanel(
    task: TaskItem,
    proposalOnly: Boolean,
    onRedraw: () -> Unit,
    onStart: () -> Unit,
    steps: List<JournalStep> = emptyList(),
    onToggleStep: (String, Boolean) -> Unit = { _, _ -> },
    customStartAvailable: Boolean,
    onStartCustom: () -> Unit,
) {
    MaterialTheme(colorScheme = LightColors) {
    val startLabel = stringResource(R.string.start_action)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = taskCardColors(task.color),
        border = taskBorder(task),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp)
                    .clickable(onClickLabel = startLabel, role = Role.Button, onClick = onStart)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                verticalArrangement = Arrangement.spacedBy(16.dp),
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
                TaskDeadlineLabel(task)
                if (steps.isNotEmpty()) TaskStepChecklist(steps, onToggleStep)
                else task.firstStep?.let {
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
            if (customStartAvailable) TextButton(onClick = onStartCustom, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.start_custom_once))
            }
        }
    }
    }
}

@Composable
private fun TaskStepChecklist(
    steps: List<JournalStep>,
    onToggleStep: (String, Boolean) -> Unit,
) {
    val firstOpen = steps.indexOfFirst { !it.completed }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, step ->
            Row(
                Modifier.fillMaxWidth()
                    .toggleable(value = step.completed, role = Role.Checkbox) {
                        onToggleStep(step.id, it)
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = step.completed, onCheckedChange = null)
                Text(
                    step.title,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    style = if (index == firstOpen) MaterialTheme.typography.titleSmall
                    else MaterialTheme.typography.bodyMedium,
                    color = if (step.completed) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskItem,
    onEdit: () -> Unit,
    steps: List<JournalStep> = emptyList(),
    onToggleStep: (String, Boolean) -> Unit = { _, _ -> },
    handleModifier: Modifier = Modifier,
    organizing: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onSetColor: (TaskColor) -> Unit,
    onStart: () -> Unit,
    customStartAvailable: Boolean,
    onStartCustom: () -> Unit,
) {
    MaterialTheme(colorScheme = LightColors) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = taskCardColors(task.color),
        border = taskBorder(task),
        shape = RoundedCornerShape(
            topStart = 22.dp,
            topEnd = 14.dp,
            bottomEnd = 22.dp,
            bottomStart = 14.dp,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
            TaskDeadlineLabel(task)
            if (steps.isNotEmpty()) TaskStepChecklist(steps, onToggleStep)
            else task.firstStep?.let {
                Text(
                    text = stringResource(R.string.first_step_value, it),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.task_steps_folder))
            }
            if (organizing) {
                Text(
                    text = stringResource(R.string.choose_task_color),
                    style = MaterialTheme.typography.labelLarge,
                )
                TaskColorPicker(selected = task.color, onSelect = onSetColor)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                onClick = onStart,
                colors = appButtonColors(),
                modifier = Modifier.sizeIn(minHeight = 48.dp),
            ) {
                ToolGlyph(ToolGlyphKind.START)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.start_action))
            }
            if (customStartAvailable) TextButton(onClick = onStartCustom, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.start_custom_once))
            }
        }
    }
    }
}

@Composable
fun FocusScreen(
    activeFocus: ActiveFocus,
    modalBlocked: Boolean = false,
    onJournal: () -> Unit = {},
    onHome: () -> Unit = {},
    onMini: () -> Unit = {},
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
    val captureDescription = stringResource(R.string.focus_tool_capture)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth().testTag("focus-back-home")) {
                Text(stringResource(R.string.focus_back_home))
            }
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
            TaskDeadlineLabel(activeFocus.task)
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
                UtilityIconButton(
                    UtilityGlyphKind.NOTE,
                    stringResource(R.string.focus_tool_note),
                    onNote,
                )
                IconButton(
                    onClick = { showCapture = true },
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .semantics { contentDescription = captureDescription },
                ) { ToolGlyph(ToolGlyphKind.CAPTURE) }
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
            FocusFolderNotes(activeFocus.task.id)
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
                nextPausePromptAtMs = Long.MAX_VALUE
                org.lepotager.executivefunction.PauseSchedule.set(context, activeFocus, nextPausePromptAtMs)
            },
            onRemindLater = {
                showPauseSuggestion = false
                nextPausePromptAtMs = elapsed + 10 * 60_000L
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
        title = { Text(stringResource(R.string.quick_capture_title)) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(stringResource(R.string.capture_label)) },
                singleLine = true,
                colors = appTextFieldColors(),
            )
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
    val totalSeconds = milliseconds / 1_000
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
}

@Composable
internal fun RunningFocusBanner(active: ActiveFocus, onReturn: () -> Unit) {
    var now by remember(active.session.id) { mutableLongStateOf(System.currentTimeMillis()) }
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(active.session.id) {
        lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
            while (true) { now = System.currentTimeMillis(); delay(1_000) }
        }
    }
    val elapsed = SessionClock.elapsedMs(active.session, now)
    val target = active.session.targetDurationMs
    val remaining = target?.minus(elapsed)
    val clock = if (remaining == null) formatElapsed(elapsed)
        else if (remaining >= 0) formatElapsed(remaining) else "+" + formatElapsed(-remaining)
    Card(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(active.task.title, style = MaterialTheme.typography.titleMedium)
            TaskDeadlineLabel(active.task)
            Text(clock, style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onReturn, modifier = Modifier.fillMaxWidth().testTag("home-return-timer")) {
                Text(stringResource(R.string.focus_return_timer))
            }
        }
    }
}
