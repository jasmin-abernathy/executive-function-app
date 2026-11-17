package org.lepotager.executivefunction.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.lepotager.executivefunction.R
import org.lepotager.executivefunction.data.AppDatabase
import org.lepotager.executivefunction.data.RegulationCardRules
import org.lepotager.executivefunction.data.RegulationCardStore

private data class SupportCard(val id: String?, val title: String, val steps: List<String>)

/** Opening a card never changes tasks, starts a timer or records a check-in. */
@Composable
fun OverloadSupportDialog(
    hasRunningFocus: Boolean,
    onKeepPlace: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current.applicationContext
    val store = remember(context) { RegulationCardStore(AppDatabase(context)) }
    var customCards by remember { mutableStateOf(store.list()) }
    var selected by remember { mutableStateOf<SupportCard?>(null) }
    var editing by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<String?>(null) }
    var cardTitle by remember { mutableStateOf("") }
    var stepsText by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf(false) }

    val builtIns = listOf(
        SupportCard(null, stringResource(R.string.overload_builtin_senses_title), listOf(
            stringResource(R.string.overload_builtin_senses_step1),
            stringResource(R.string.overload_builtin_senses_step2),
            stringResource(R.string.overload_builtin_senses_step3),
        )),
        SupportCard(null, stringResource(R.string.overload_builtin_restart_title), listOf(
            stringResource(R.string.overload_builtin_restart_step1),
            stringResource(R.string.overload_builtin_restart_step2),
            stringResource(R.string.overload_builtin_restart_step3),
        )),
    )
    val allCards = builtIns + customCards.map { SupportCard(it.id, it.title, it.steps) }
    val normalized = runCatching {
        RegulationCardRules.normalize(cardTitle, stepsText.lines())
    }.getOrNull()

    fun edit(card: SupportCard?) {
        editing = true
        editingId = card?.id
        cardTitle = card?.title.orEmpty()
        stepsText = card?.steps?.joinToString("\n").orEmpty()
        confirmDelete = false
        saveError = false
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp).heightIn(max = 640.dp),
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    stringResource(R.string.overload_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.semantics { heading() },
                )
                if (editing) {
                    Text(stringResource(R.string.overload_editor_hint), style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = cardTitle,
                        onValueChange = { cardTitle = it },
                        label = { Text(stringResource(R.string.overload_card_title)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = stepsText,
                        onValueChange = { stepsText = it },
                        label = { Text(stringResource(R.string.overload_card_steps)) },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (saveError) Text(
                        stringResource(R.string.overload_save_error), color = MaterialTheme.colorScheme.error,
                    )
                    Button(
                        enabled = normalized != null,
                        onClick = {
                            val value = normalized ?: return@Button
                            val saved = runCatching {
                                store.save(editingId, value.first, value.second)
                            }.getOrNull()
                            if (saved == null) saveError = true else {
                                customCards = store.list()
                                selected = SupportCard(saved.id, saved.title, saved.steps)
                                editing = false
                                saveError = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                    ) { Text(stringResource(R.string.overload_save_card)) }
                    TextButton(
                        onClick = { editing = false },
                        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                    ) { Text(stringResource(R.string.cancel)) }
                } else if (selected != null) {
                    val card = requireNotNull(selected)
                    Text(card.title, style = MaterialTheme.typography.titleLarge)
                    card.steps.forEachIndexed { index, step ->
                        Text((index + 1).toString() + ". " + step, style = MaterialTheme.typography.bodyLarge)
                    }
                    if (card.id != null) {
                        OutlinedButton(
                            onClick = { edit(card) },
                            modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                        ) { Text(stringResource(R.string.overload_edit_card)) }
                        if (confirmDelete) {
                            Text(stringResource(R.string.overload_confirm_delete))
                            Button(
                                onClick = {
                                    store.delete(card.id)
                                    customCards = store.list()
                                    selected = null
                                    confirmDelete = false
                                },
                                modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                            ) { Text(stringResource(R.string.overload_delete_card)) }
                            TextButton(onClick = { confirmDelete = false }) {
                                Text(stringResource(R.string.cancel))
                            }
                        } else TextButton(
                            onClick = { confirmDelete = true },
                            modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                        ) { Text(stringResource(R.string.overload_delete_card)) }
                    }
                    OutlinedButton(
                        onClick = { selected = null; confirmDelete = false },
                        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                    ) { Text(stringResource(R.string.intro_back)) }
                } else {
                    Text(
                        stringResource(R.string.overload_intro),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (hasRunningFocus) Button(
                        onClick = { onDismiss(); onKeepPlace() },
                        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 56.dp),
                    ) { Text(stringResource(R.string.overload_keep_place)) }
                    Text(
                        stringResource(R.string.overload_choose_card),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    allCards.forEach { card ->
                        OutlinedButton(
                            onClick = { selected = card },
                            modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 52.dp),
                        ) { Text(card.title) }
                    }
                    OutlinedButton(
                        enabled = customCards.size < RegulationCardRules.MAX_CARDS,
                        onClick = { edit(null) },
                        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                    ) { Text(stringResource(R.string.overload_add_card)) }
                    Text(
                        stringResource(R.string.overload_privacy),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                ) { Text(stringResource(R.string.overload_close)) }
            }
        }
    }
}
