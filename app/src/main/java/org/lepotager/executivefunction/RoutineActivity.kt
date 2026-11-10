package org.lepotager.executivefunction

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lepotager.executivefunction.data.AppDatabase
import org.lepotager.executivefunction.data.RoutineClock
import org.lepotager.executivefunction.data.RoutineStep
import org.lepotager.executivefunction.data.RoutineStore
import org.lepotager.executivefunction.data.RoutineTemplate
import org.lepotager.executivefunction.ui.theme.ExecutiveFunctionTheme

class RoutineActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ExecutiveFunctionTheme { RoutineScreen(onExit = ::finish) } }
    }
}

@Composable
private fun RoutineScreen(onExit: () -> Unit) {
    val context = LocalContext.current
    val french = LocalConfiguration.current.locales[0].language == "fr"
    fun label(fr: String, en: String) = if (french) fr else en
    val db = remember { AppDatabase(context.applicationContext) }
    val store = remember(db) { RoutineStore(db) }
    val scope = rememberCoroutineScope()
    var routines by remember { mutableStateOf(emptyList<RoutineTemplate>()) }
    var busy by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var creating by rememberSaveable { mutableStateOf(false) }
    var newName by rememberSaveable { mutableStateOf("") }
    var renameMode by rememberSaveable { mutableStateOf(false) }
    var renameText by rememberSaveable { mutableStateOf("") }
    var stepText by rememberSaveable { mutableStateOf("") }
    var stepClock by rememberSaveable { mutableStateOf("") }
    var editingStepId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingRemove by remember { mutableStateOf<String?>(null) }
    var lastCompleted by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try { routines = withContext(Dispatchers.IO) { store.load() } }
        catch (_: Exception) { error = true }
        finally { loading = false }
    }
    DisposableEffect(db) { onDispose { db.close() } }

    // All database mutations and refreshes happen off the Compose/UI thread.
    fun act(action: () -> String?, onSuccess: (String?) -> Unit = {}) {
        if (busy) return
        busy = true
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val value = action()
                    value to store.load()
                }
                routines = result.second
                onSuccess(result.first)
            } catch (_: Exception) {
                // Never discard the user's draft on a failed save.
                error = true
            } finally { busy = false }
        }
    }

    val selected = routines.firstOrNull { it.id == selectedId }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(label("Mes routines", "My routines"), style = MaterialTheme.typography.headlineMedium)
        Text(
            label(
                "Des étapes à suivre dans l'ordre, avec une heure seulement si elle est utile. Aucun retard à rattraper.",
                "Steps in order, with a clock time only where it helps. Nothing to catch up on.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        TextButton(
            onClick = {
                if (selectedId == null) onExit()
                else {
                    selectedId = null
                    editingStepId = null
                    stepText = ""
                    stepClock = ""
                    renameMode = false
                }
            },
            modifier = Modifier.sizeIn(minHeight = 48.dp),
        ) {
            Text(if (selectedId == null) label("Retour à l'accueil", "Back home") else label("Toutes les routines", "All routines"))
        }
        if (loading) {
            CircularProgressIndicator()
        } else if (selected == null) {
            if (lastCompleted != null) {
                Text(label("Routine terminée. Tu peux en commencer une nouvelle quand tu le souhaites.",
                    "Routine finished. Start a new one whenever you like."))
            }
            if (routines.isEmpty()) {
                Text(label("Aucune routine pour l'instant. Crée simplement un nom, puis ajoute tes étapes.",
                    "No routines yet. Give one a name, then add its steps."))
            }
            routines.forEach { routine ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(routine.title, style = MaterialTheme.typography.titleLarge)
                        val run = routine.openRun
                        Text(
                            if (run == null) label("Étapes : ", "Steps: ") + routine.steps.size
                            else label("Reprise possible : ", "Ready to resume: ") +
                                run.steps.count { it.state != "PENDING" } + "/" + run.steps.size,
                        )
                        Button(
                            onClick = { selectedId = routine.id; creating = false; lastCompleted = null },
                            modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                        ) {
                            Text(if (run == null) label("Ouvrir cette routine", "Open routine")
                                else label("Continuer cette routine", "Continue routine"))
                        }
                    }
                }
            }
            if (!creating) {
                OutlinedButton(
                    onClick = { creating = true },
                    modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp).testTag("routine-create-open"),
                ) { Text(label("Créer une routine", "Create a routine")) }
            } else {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it.take(120) },
                    modifier = Modifier.fillMaxWidth().testTag("routine-name"),
                    label = { Text(label("Nom de la routine", "Routine name")) },
                    singleLine = true,
                )
                Button(
                    enabled = !busy && newName.isNotBlank(),
                    onClick = {
                        act({ store.create(newName) }) { newId ->
                            selectedId = newId
                            creating = false
                            newName = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                ) { Text(label("Créer et ajouter les étapes", "Create and add steps")) }
                TextButton(onClick = { creating = false }) { Text(label("Annuler", "Cancel")) }
            }
        } else {
            Text(selected.title, style = MaterialTheme.typography.headlineSmall)
            val run = selected.openRun
            if (run != null) {
                val decided = run.steps.count { it.state != "PENDING" }
                val next = run.steps.firstOrNull { it.state == "PENDING" }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(label("Routine en cours", "Routine in progress"), style = MaterialTheme.typography.titleLarge)
                        Text(label("Étapes traitées : ", "Steps handled: ") + decided + "/" + run.steps.size)
                        if (run.status == "PAUSED") {
                            Text(label("En pause. Tu peux reprendre n'importe quand.", "Paused. You can resume whenever you like."))
                            Button(
                                enabled = !busy,
                                modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                                onClick = { act({ store.startOrResume(selected.id) }) },
                            ) { Text(label("Reprendre", "Resume")) }
                        } else if (next != null) {
                            Text(next.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag("routine-next-step"))
                            if (next.clockMinute != null) {
                                Text(label("Repère horaire : ", "Time cue: ") + RoutineClock.format(next.clockMinute))
                            }
                            Button(
                                enabled = !busy,
                                modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp).testTag("routine-step-done"),
                                onClick = {
                                    val isLast = decided + 1 == run.steps.size
                                    act({ store.decide(run.id, next.position, false); null }) {
                                        if (isLast) { lastCompleted = selected.title; selectedId = null }
                                    }
                                },
                            ) { Text(label("Étape terminée", "Step done")) }
                            OutlinedButton(
                                enabled = !busy,
                                modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                                onClick = {
                                    val isLast = decided + 1 == run.steps.size
                                    act({ store.decide(run.id, next.position, true); null }) {
                                        if (isLast) { lastCompleted = selected.title; selectedId = null }
                                    }
                                },
                            ) { Text(label("Passer cette étape", "Skip this step")) }
                            TextButton(
                                enabled = !busy,
                                onClick = { act({ store.pause(run.id); null }) },
                                modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                            ) { Text(label("Mettre en pause", "Pause routine")) }
                        }
                        run.steps.filter { it.state != "PENDING" }.forEach { item ->
                            Text(
                                (if (item.state == "DONE") "✓ " else "– ") + item.title,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            } else {
                Button(
                    enabled = !busy && selected.steps.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp).testTag("routine-start"),
                    onClick = { act({ store.startOrResume(selected.id) }) },
                ) { Text(label("Commencer cette routine", "Start this routine")) }
                if (selected.steps.isEmpty()) {
                    Text(label("Ajoute au moins une étape avant de commencer.", "Add at least one step before starting."))
                }
            }
            Text(label("Préparer les prochaines utilisations", "Set up future runs"), style = MaterialTheme.typography.titleMedium)
            if (run != null) {
                Text(label(
                    "Les changements ci-dessous n'affectent pas la routine déjà commencée.",
                    "Changes below won't affect the run already in progress.",
                ))
            }
            if (!renameMode) {
                TextButton(onClick = { renameText = selected.title; renameMode = true }) {
                    Text(label("Renommer la routine", "Rename routine"))
                }
            } else {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it.take(120) },
                    label = { Text(label("Nouveau nom", "New name")) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(enabled = !busy && renameText.isNotBlank(), onClick = {
                        act({ store.rename(selected.id, renameText); null }) { renameMode = false }
                    }) { Text(label("Enregistrer", "Save")) }
                    TextButton(onClick = { renameMode = false }) { Text(label("Annuler", "Cancel")) }
                }
            }
            selected.steps.forEachIndexed { index, step ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(step.title, style = MaterialTheme.typography.bodyLarge)
                        if (step.clockMinute != null) {
                            Text(label("Repère : ", "Cue: ") + RoutineClock.format(step.clockMinute))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(enabled = !busy && index > 0, onClick = {
                                act({ store.moveStep(selected.id, step.id, -1); null })
                            }, modifier = Modifier.sizeIn(minHeight = 48.dp)) { Text(label("Monter", "Move up")) }
                            TextButton(enabled = !busy && index < selected.steps.lastIndex, onClick = {
                                act({ store.moveStep(selected.id, step.id, 1); null })
                            }, modifier = Modifier.sizeIn(minHeight = 48.dp)) { Text(label("Descendre", "Move down")) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                editingStepId = step.id
                                stepText = step.title
                                stepClock = RoutineClock.format(step.clockMinute)
                            }, enabled = !busy, modifier = Modifier.sizeIn(minHeight = 48.dp)) {
                                Text(label("Modifier", "Edit"))
                            }
                            TextButton(onClick = { pendingRemove = step.id }, enabled = !busy,
                                modifier = Modifier.sizeIn(minHeight = 48.dp)) {
                                Text(label("Retirer", "Remove"))
                            }
                        }
                    }
                }
            }
            Text(
                if (editingStepId == null) label("Ajouter une étape", "Add a step")
                else label("Modifier cette étape", "Edit this step"),
                style = MaterialTheme.typography.titleMedium,
            )
            OutlinedTextField(
                value = stepText,
                onValueChange = { stepText = it.take(120) },
                label = { Text(label("Action à faire", "Action to do")) },
                modifier = Modifier.fillMaxWidth().testTag("routine-step-title"),
                singleLine = true,
            )
            val parsedTime = runCatching { RoutineClock.parse(stepClock) }
            OutlinedTextField(
                value = stepClock,
                onValueChange = { stepClock = it.take(5) },
                label = { Text(label("Heure facultative (HH:mm)", "Optional time (HH:mm)")) },
                supportingText = {
                    Text(label("Simple repère, sans alarme automatique.", "A visual cue, not an automatic alarm."))
                },
                isError = parsedTime.isFailure,
                modifier = Modifier.fillMaxWidth().testTag("routine-step-clock"),
                singleLine = true,
            )
            Button(
                enabled = !busy && stepText.isNotBlank() && parsedTime.isSuccess &&
                    (editingStepId != null || selected.steps.size < 30),
                onClick = {
                    val editing = editingStepId
                    val clock = parsedTime.getOrNull()
                    act({
                        if (editing == null) store.addStep(selected.id, stepText, clock)
                        else { store.editStep(selected.id, editing, stepText, clock); null }
                    }) {
                        editingStepId = null
                        stepText = ""
                        stepClock = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp).testTag("routine-save-step"),
            ) {
                Text(if (editingStepId == null) label("Ajouter cette étape", "Add this step")
                    else label("Enregistrer les modifications", "Save changes"))
            }
            if (editingStepId != null) {
                TextButton(onClick = {
                    editingStepId = null
                    stepText = ""
                    stepClock = ""
                }) { Text(label("Annuler la modification", "Cancel editing")) }
            }
        }
    }

    if (pendingRemove != null && selected != null) {
        val toRemove = selected.steps.firstOrNull { it.id == pendingRemove }
        if (toRemove != null) {
            AlertDialog(
                onDismissRequest = { pendingRemove = null },
                title = { Text(label("Retirer cette étape ?", "Remove this step?")) },
                text = { Text(label(
                    "Elle disparaîtra des prochaines routines. Une routine déjà commencée reste inchangée.",
                    "It will disappear from future runs. An already-started run will remain unchanged.",
                )) },
                confirmButton = {
                    TextButton(onClick = {
                        val stepId = toRemove.id
                        act({ store.deleteStep(selected.id, stepId); null }) { pendingRemove = null }
                    }) { Text(label("Retirer", "Remove")) }
                },
                dismissButton = {
                    TextButton(onClick = { pendingRemove = null }) { Text(label("Annuler", "Cancel")) }
                },
            )
        }
    }
    if (error) {
        AlertDialog(
            onDismissRequest = { error = false },
            title = { Text(label("Action non effectuée", "Action failed")) },
            text = { Text(label(
                "Aucune donnée existante n'a été volontairement effacée. Vérifie les champs et réessaie.",
                "Existing data was not deliberately erased. Check the fields and try again.",
            )) },
            confirmButton = { TextButton(onClick = { error = false }) { Text("OK") } },
        )
    }
}
