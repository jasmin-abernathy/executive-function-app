package org.lepotager.executivefunction.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lepotager.executivefunction.data.*
import org.lepotager.executivefunction.model.TaskItem

@Composable
internal fun WorkspaceHome(
    tasks: List<TaskItem>,
    onChanged: () -> Unit,
    onStartStep: (String) -> Unit,
    content: @Composable (Set<String>?, String?, (String) -> Unit, @Composable () -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val french = LocalConfiguration.current.locales[0].language == "fr"
    fun label(fr: String, en: String) = if (french) fr else en
    val database = remember { AppDatabase(context.applicationContext) }
    val store = remember { WorkspaceStore(database) }
    val journal = remember { LearningJournal(database) }
    val scope = rememberCoroutineScope()
    var data by remember { mutableStateOf(WorkspaceContent()) }
    var loaded by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    var folder by rememberSaveable { mutableStateOf<String?>(null) }
    var folderEditor by rememberSaveable { mutableStateOf(false) }
    var folderName by rememberSaveable { mutableStateOf("") }
    var renameFolder by rememberSaveable { mutableStateOf(false) }
    var deleteFolder by rememberSaveable { mutableStateOf(false) }
    var noteEditor by rememberSaveable { mutableStateOf(false) }
    var noteId by rememberSaveable { mutableStateOf<String?>(null) }
    var noteText by rememberSaveable { mutableStateOf("") }
    var noteFolder by rememberSaveable { mutableStateOf<String?>(null) }
    var deleteNote by rememberSaveable { mutableStateOf<String?>(null) }
    var editTask by rememberSaveable { mutableStateOf<String?>(null) }
    var showAllNotes by rememberSaveable { mutableStateOf(false) }
    suspend fun refresh() { data = withContext(Dispatchers.IO) { store.load() }; loaded = true }
    fun run(action: () -> Unit, after: () -> Unit = {}) {
        if (busy) return
        busy = true
        scope.launch {
            try { withContext(Dispatchers.IO) { action() }; refresh(); onChanged(); after() }
            catch (_: Exception) { error = true }
            finally { busy = false }
        }
    }
    LaunchedEffect(tasks) { try { refresh() } catch (_: Exception) { error = true } }
    DisposableEffect(Unit) { onDispose { database.close() } }
    val selectedFolder = data.folders.firstOrNull { it.id == folder }
    val visible = if (folder == null || !loaded) null else tasks.filter { data.taskFolders[it.id] == folder }.map { it.id }.toSet()
    content(visible, folder, { editTask = it }) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(label("Mes dossiers", "My folders"), style = MaterialTheme.typography.titleMedium)
                    FolderPicker(data.folders, folder, label("Toutes les tâches et notes", "All tasks and notes"), !busy && loaded) { folder = it }
                    TextButton(enabled = !busy, onClick = { folderName = ""; renameFolder = false; folderEditor = true }) {
                        Text(label("Nouveau dossier", "New folder"))
                    }
                    if (selectedFolder != null) {
                        Text(label("Ajoute les tâches depuis leur fiche et les notes depuis leur édition.", "Assign tasks from their details and notes from their editor."), style = MaterialTheme.typography.bodySmall)
                        Row {
                            TextButton(enabled = !busy, onClick = { folderName = selectedFolder.name; renameFolder = true; folderEditor = true }) { Text(label("Renommer", "Rename")) }
                            TextButton(enabled = !busy, onClick = { deleteFolder = true }) { Text(label("Retirer le dossier", "Remove folder")) }
                        }
                    }
                }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(label("Mes notes", "My notes"), style = MaterialTheme.typography.titleMedium)
                    FilledTonalButton(enabled = !busy && loaded, onClick = { noteId = null; noteText = ""; noteFolder = folder; noteEditor = true }) {
                        Text(label("Noter une idée", "Capture a thought"))
                    }
                    val notes = data.notes.filter { folder == null || data.noteFolders[it.id] == folder }
                    if (!loaded) LinearProgressIndicator(Modifier.fillMaxWidth())
                    else if (notes.isEmpty()) Text(label("Une idée à garder pour plus tard ? Pose-la ici.", "Something to remember later? Leave it here."))
                    (if (showAllNotes) notes else notes.take(3)).forEach { note ->
                        OutlinedCard(Modifier.fillMaxWidth().clickable {
                            noteId = note.id; noteText = note.text; noteFolder = data.noteFolders[note.id]; noteEditor = true
                        }) {
                            Column(Modifier.padding(12.dp)) {
                                Text(note.text, maxLines = 3, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                data.folders.firstOrNull { it.id == data.noteFolders[note.id] }?.let { Text(it.name, style = MaterialTheme.typography.labelSmall) }
                            }
                        }
                    }
                    if (notes.size > 3) TextButton(onClick = { showAllNotes = !showAllNotes }) { Text(label(if (showAllNotes) "Réduire les notes" else "Voir toutes les notes", if (showAllNotes) "Show fewer notes" else "Show all notes")) }
                }
            }
        }
    }
    if (folderEditor) AlertDialog(
        onDismissRequest = { if (!busy) folderEditor = false },
        title = { Text(label(if (renameFolder) "Renommer le dossier" else "Nouveau dossier", if (renameFolder) "Rename folder" else "New folder")) },
        text = { OutlinedTextField(folderName, { folderName = it }, enabled = !busy, label = { Text(label("Nom", "Name")) }, singleLine = true) },
        confirmButton = { TextButton(enabled = !busy && folderName.isNotBlank(), onClick = {
            val name = folderName; val id = folder
            run({ if (renameFolder && id != null) store.renameFolder(id, name) else store.createFolder(name) }, { folderEditor = false })
        }) { Text(label("Enregistrer", "Save")) } },
        dismissButton = { TextButton(enabled = !busy, onClick = { folderEditor = false }) { Text(label("Annuler", "Cancel")) } },
    )
    if (deleteFolder) AlertDialog(
        onDismissRequest = { if (!busy) deleteFolder = false }, title = { Text(label("Retirer ce dossier ?", "Remove this folder?")) },
        text = { Text(label("Les tâches et notes seront conservées, sans dossier.", "Tasks and notes will be kept without a folder.")) },
        confirmButton = { TextButton(enabled = !busy, onClick = { val id = folder; run({ if (id != null) store.deleteFolder(id) }, { folder = null; deleteFolder = false }) }) { Text(label("Retirer", "Remove")) } },
        dismissButton = { TextButton(enabled = !busy, onClick = { deleteFolder = false }) { Text(label("Annuler", "Cancel")) } },
    )
    if (noteEditor) WorkspaceDialog(onDismiss = { if (!busy) noteEditor = false }) {
        Text(label("Ma note", "My note"), style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(noteText, { noteText = it }, enabled = !busy, modifier = Modifier.fillMaxWidth(), minLines = 3, label = { Text(label("Note", "Note")) })
        FolderPicker(data.folders, noteFolder, label("Sans dossier", "No folder"), !busy) { noteFolder = it }
        Button(enabled = !busy && noteText.isNotBlank(), onClick = {
            val id = noteId; val body = noteText; val target = noteFolder
            run({ store.saveNote(id, body, target) }, { noteEditor = false })
        }) { Text(label("Enregistrer", "Save")) }
        if (noteId != null) TextButton(enabled = !busy, onClick = { deleteNote = noteId }) { Text(label("Supprimer la note", "Delete note")) }
        TextButton(enabled = !busy, onClick = { noteEditor = false }) { Text(label("Fermer", "Close")) }
    }
    if (deleteNote != null) AlertDialog(
        onDismissRequest = { if (!busy) deleteNote = null }, title = { Text(label("Supprimer cette note ?", "Delete this note?")) },
        confirmButton = { TextButton(enabled = !busy, onClick = { val id = deleteNote!!; run({ journal.deleteNote(id) }, { deleteNote = null; noteEditor = false }) }) { Text(label("Supprimer", "Delete")) } },
        dismissButton = { TextButton(enabled = !busy, onClick = { deleteNote = null }) { Text(label("Annuler", "Cancel")) } },
    )
    editTask?.let { id ->
        TaskStepsDialog(id, database, data, onDismiss = { editTask = null }, onChanged = { onChanged(); scope.launch { refresh() } }, onStart = { editTask = null; onStartStep(it) })
    }
    if (error) AlertDialog(onDismissRequest = { error = false }, title = { Text(label("Enregistrement impossible", "Could not save")) }, text = { Text(label("Ton texte est conservé. Réessaie.", "Your text is still here. Please try again.")) }, confirmButton = { TextButton(onClick = { error = false }) { Text("OK") } })
}

@Composable
private fun FolderPicker(folders: List<WorkspaceFolder>, selected: String?, emptyLabel: String, enabled: Boolean, onSelect: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(enabled = enabled, onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(folders.firstOrNull { it.id == selected }?.name ?: emptyLabel) }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text(emptyLabel) }, onClick = { onSelect(null); expanded = false })
            folders.forEach { f -> DropdownMenuItem(text = { Text(f.name) }, onClick = { onSelect(f.id); expanded = false }) }
        }
    }
}

@Composable
private fun WorkspaceDialog(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(Modifier.fillMaxWidth().heightIn(max = 620.dp).imePadding().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
        }
    }
}

@Composable
private fun TaskStepsDialog(taskId: String, database: AppDatabase, data: WorkspaceContent, onDismiss: () -> Unit, onChanged: () -> Unit, onStart: (String) -> Unit) {
    val french = LocalConfiguration.current.locales[0].language == "fr"
    fun label(fr: String, en: String) = if (french) fr else en
    val store = remember(database) { WorkspaceStore(database) }
    val journal = remember(database) { LearningJournal(database) }
    val scope = rememberCoroutineScope()
    var task by remember(taskId) { mutableStateOf<TaskItem?>(null) }
    var steps by remember(taskId) { mutableStateOf(emptyList<JournalStep>()) }
    var text by rememberSaveable(taskId) { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    var selectedFolder by remember(taskId) { mutableStateOf(data.taskFolders[taskId]) }
    var notice by remember { mutableStateOf(false) }
    suspend fun refresh() {
        val result = withContext(Dispatchers.IO) { store.ensureSteps(taskId); database.taskById(taskId) to journal.steps(taskId) }
        task = result.first; steps = result.second
    }
    fun run(action: () -> Unit, after: () -> Unit = {}) {
        if (busy) return
        busy = true
        scope.launch {
            try { withContext(Dispatchers.IO) { action() }; refresh(); onChanged(); after() }
            catch (_: Exception) { error = true }
            finally { busy = false }
        }
    }
    LaunchedEffect(taskId) { try { refresh() } catch (_: Exception) { error = true } }
    WorkspaceDialog(onDismiss = { if (!busy) onDismiss() }) {
        Text(task?.title ?: label("Ma tâche", "My task"), style = MaterialTheme.typography.titleLarge)
        FolderPicker(data.folders, selectedFolder, label("Sans dossier", "No folder"), !busy) { target -> run({ store.assignTask(taskId, target) }, { selectedFolder = target }) }
        Text(label("Une étape à la fois", "One step at a time"), style = MaterialTheme.typography.titleMedium)
        Text(label("Ajoute de petites actions. Lance une étape seule, ou transforme les étapes restantes en tâches séparées.", "Add small actions. Start one step, or turn the remaining steps into separate tasks."))
        if (steps.isEmpty()) Text(label("Pas encore d’étape.", "No steps yet."), style = MaterialTheme.typography.bodySmall)
        steps.forEachIndexed { index, step ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${index + 1}. ${step.title}", style = MaterialTheme.typography.titleSmall)
                    if (step.completed) Text(label("Terminée", "Completed"))
                    else TextButton(enabled = !busy, onClick = {
                        var child: String? = null
                        run({ child = store.decomposeStep(taskId, step.id) }, { child?.let(onStart) })
                    }) { Text(label("Lancer le timer de cette étape", "Start this step’s timer")) }
                }
            }
        }
        OutlinedTextField(text, { text = it }, enabled = !busy, modifier = Modifier.fillMaxWidth(), minLines = 2, label = { Text(label("Étapes : une par ligne", "Steps: one per line")) })
        FilledTonalButton(enabled = !busy && text.isNotBlank(), onClick = {
            val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
            run({ database.writableDatabase.beginTransaction(); try { lines.forEach { journal.addStep(taskId, it) }; database.writableDatabase.setTransactionSuccessful() } finally { database.writableDatabase.endTransaction() } }, { text = "" })
        }) { Text(label("Ajouter les étapes", "Add steps")) }
        OutlinedButton(enabled = !busy && steps.any { !it.completed }, onClick = { run({ store.decomposeAll(taskId) }, { notice = true }) }) { Text(label("Décomposer en tâches", "Split into tasks")) }
        if (notice) Text(label("Les tâches sont prêtes sur l’accueil. Les étapes déjà décomposées ont été conservées.", "The tasks are ready on the home screen. Previously created tasks were reused."))
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (error) Text(label("Impossible d’enregistrer. Ton texte est conservé ; réessaie.", "Could not save. Your text is still here; please try again."), color = MaterialTheme.colorScheme.error)
        TextButton(enabled = !busy, onClick = onDismiss) { Text(label("Revenir à l’accueil", "Back to home")) }
    }
}
