package org.lepotager.executivefunction.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.lepotager.executivefunction.R
import org.lepotager.executivefunction.domain.SessionClock
import org.lepotager.executivefunction.model.ActiveFocus
import org.lepotager.executivefunction.model.TaskItem

@Composable
fun HomeScreen(
    tasks: List<TaskItem>,
    onCapture: (String, String?, () -> Unit) -> Unit,
    onStart: (String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var firstStep by remember { mutableStateOf("") }
    var showFirstStep by remember { mutableStateOf(false) }
    var companionMood by remember { mutableStateOf(CompanionMood.Idle) }

    LaunchedEffect(companionMood) {
        if (companionMood != CompanionMood.Idle) {
            delay(3_800)
            companionMood = CompanionMood.Idle
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
            Spacer(Modifier.height(14.dp))
            CompanionPanel(
                mood = companionMood,
                onClick = { companionMood = CompanionMood.Listening },
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
                    if (showFirstStep) {
                        OutlinedTextField(
                            value = firstStep,
                            onValueChange = { firstStep = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.first_step_optional)) },
                            singleLine = true,
                            colors = appTextFieldColors(),
                        )
                    } else {
                        TextButton(
                            onClick = { showFirstStep = true },
                            colors = appTextButtonColors(),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text(stringResource(R.string.add_first_step))
                        }
                    }
                    Button(
                        onClick = {
                            onCapture(title, firstStep) {
                                title = ""
                                firstStep = ""
                                showFirstStep = false
                                companionMood = CompanionMood.Safekeeping
                            }
                        },
                        enabled = title.isNotBlank(),
                        colors = appButtonColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .sizeIn(minHeight = 48.dp),
                    ) {
                        Text(stringResource(R.string.capture_action))
                    }
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.ready_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics { heading() },
            )
        }
        if (tasks.isEmpty()) {
            item {
                EmptyTasksPanel()
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                TaskCard(task = task, onStart = { onStart(task.id) })
            }
        }
    }
}

@Composable
private fun TaskCard(task: TaskItem, onStart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = appCardColors(),
        border = appBorder(),
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
            Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            task.firstStep?.let {
                Text(
                    text = stringResource(R.string.first_step_value, it),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = onStart,
                colors = appButtonColors(),
                modifier = Modifier.sizeIn(minHeight = 48.dp),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Text(stringResource(R.string.start_action))
            }
        }
    }
}

@Composable
fun FocusScreen(
    activeFocus: ActiveFocus,
    onQuickCapture: (String, String?, () -> Unit) -> Unit,
    onInterrupt: (String?) -> Unit,
    onComplete: () -> Unit,
) {
    var showCapture by remember { mutableStateOf(false) }
    var showInterrupt by remember { mutableStateOf(false) }
    var now by remember(activeFocus.session.id) { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(activeFocus.session.id, activeFocus.session.segmentStartedAt) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }
    val elapsed = SessionClock.elapsedMs(activeFocus.session, now)
    val elapsedSeconds = elapsed / 1_000
    val accessibleElapsed = stringResource(
        R.string.timer_accessible,
        elapsedSeconds / 3_600,
        (elapsedSeconds % 3_600) / 60,
        elapsedSeconds % 60,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
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

        Text(
            text = formatElapsed(elapsed),
            fontSize = 54.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.semantics {
                contentDescription = accessibleElapsed
            },
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FilledTonalButton(
                onClick = { showCapture = true },
                colors = appTonalButtonColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(minHeight = 48.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(stringResource(R.string.quick_capture_action))
            }
            OutlinedButton(
                onClick = { showInterrupt = true },
                colors = appOutlinedButtonColors(),
                border = appBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(minHeight = 48.dp),
            ) {
                Text(stringResource(R.string.interrupt_action))
            }
            Button(
                onClick = onComplete,
                colors = appButtonColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(minHeight = 48.dp),
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Text(stringResource(R.string.complete_action))
            }
        }
    }

    if (showCapture) {
        CaptureDialog(
            onDismiss = { showCapture = false },
            onCapture = { title -> onQuickCapture(title, null) { showCapture = false } },
        )
    }
    if (showInterrupt) {
        InterruptDialog(
            onDismiss = { showInterrupt = false },
            onConfirm = { note ->
                showInterrupt = false
                onInterrupt(note)
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
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
    ) {
        CompanionPanel(
            mood = CompanionMood.WelcomeBack,
            onClick = null,
        )
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
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 48.dp),
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Text(stringResource(R.string.resume_action))
        }
        FilledTonalButton(
            onClick = { showSmaller = true },
            colors = appTonalButtonColors(),
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 48.dp),
        ) {
            Text(stringResource(R.string.make_smaller_action))
        }
        OutlinedButton(
            onClick = onPostpone,
            colors = appOutlinedButtonColors(),
            border = appBorder(),
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 48.dp),
        ) {
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
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                    )
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
