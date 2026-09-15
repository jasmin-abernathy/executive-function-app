package org.lepotager.executivefunction.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.lepotager.executivefunction.R
import org.lepotager.executivefunction.domain.FocusTimerChoice
import org.lepotager.executivefunction.domain.FocusTimerMode

/** Explicitly chooses how a new session displays time; it never mutates learned duration data. */
@Composable
internal fun FocusStartDialog(
    taskTitle: String,
    learnedTargetDurationMs: Long?,
    initialMode: FocusTimerMode,
    onDismiss: () -> Unit,
    onConfirm: (FocusTimerMode, Long?) -> Unit,
) {
    val suggestedMinutes = FocusTimerChoice.suggestedMinutes(learnedTargetDurationMs)
    var mode by remember(taskTitle) { mutableStateOf(initialMode) }
    var minutesText by remember(taskTitle, learnedTargetDurationMs) {
        mutableStateOf(suggestedMinutes?.toString().orEmpty())
    }
    val minutes = minutesText.toIntOrNull()
    val canStart = mode == FocusTimerMode.STOPWATCH || minutes?.let { it in 1..FocusTimerChoice.MAX_MINUTES } == true

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.focus_start_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(taskTitle)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = mode == FocusTimerMode.STOPWATCH,
                        onClick = { mode = FocusTimerMode.STOPWATCH },
                        label = { Text(stringResource(R.string.timer_mode_stopwatch)) },
                        modifier = Modifier.weight(1f),
                    )
                    FilterChip(
                        selected = mode == FocusTimerMode.COUNTDOWN,
                        onClick = {
                            mode = FocusTimerMode.COUNTDOWN
                            if (minutesText.isBlank()) minutesText = suggestedMinutes?.toString().orEmpty()
                        },
                        label = { Text(stringResource(R.string.timer_mode_countdown)) },
                        modifier = Modifier.weight(1f),
                    )
                }
                Text(
                    stringResource(
                        if (mode == FocusTimerMode.STOPWATCH) R.string.stopwatch_mode_support
                        else R.string.countdown_mode_support,
                    ),
                )
                if (mode == FocusTimerMode.COUNTDOWN) {
                    suggestedMinutes?.let {
                        Text(stringResource(R.string.learned_duration_reference, it))
                    }
                    OutlinedTextField(
                        value = minutesText,
                        onValueChange = { value -> minutesText = value.filter(Char::isDigit).take(5) },
                        label = { Text(stringResource(R.string.countdown_minutes_label)) },
                        supportingText = {
                            if (minutesText.isNotBlank() && !canStart) {
                                Text(stringResource(R.string.countdown_minutes_error))
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(stringResource(R.string.countdown_zero_support))
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canStart,
                onClick = {
                    onConfirm(
                        mode,
                        FocusTimerChoice.targetDurationMs(mode, minutes),
                    )
                },
            ) {
                Text(stringResource(R.string.start_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
