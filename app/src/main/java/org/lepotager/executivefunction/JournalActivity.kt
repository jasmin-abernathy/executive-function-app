package org.lepotager.executivefunction

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lepotager.executivefunction.data.*
import org.lepotager.executivefunction.ui.theme.ExecutiveFunctionTheme
import java.text.DateFormat
import java.util.Date

class JournalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ExecutiveFunctionTheme { Journal() } }
    }

    @Composable
    private fun Journal() {
        val french=resources.configuration.locales[0].language=="fr"
        fun label(fr: String,en: String)=if(french) fr else en
        val database=remember { AppDatabase(applicationContext) }
        val journal=remember { LearningJournal(database) }
        val scope=rememberCoroutineScope()
        var tasks by remember { mutableStateOf(emptyList<JournalTask>()) }
        var checks by remember { mutableStateOf(emptyList<CheckIn>()) }
        var selected by remember { mutableStateOf(intent.getStringExtra("task")) }
        var observations by remember { mutableStateOf(emptyList<Observation>()) }
        var steps by remember { mutableStateOf(emptyList<JournalStep>()) }
        var planning by remember { mutableStateOf(Planning()) }
        var target by remember { mutableStateOf<Long?>(null) }
        var manual by remember { mutableStateOf(false) }
        var reset by remember { mutableStateOf(false) }
        var section by remember { mutableStateOf(intent.getStringExtra("section") ?: "tasks") }
        var showCompleted by remember {mutableStateOf(false)}
        var notes by remember {mutableStateOf(emptyList<QuickNote>())}
        var noteText by remember {mutableStateOf("")}
        var editingCheck by remember {mutableStateOf<String?>(null)}
        var showCheckDetails by remember {mutableStateOf(false)}
        var checkMetric by remember { mutableStateOf(CheckMetric.MOOD) }
        var recurrence by remember {mutableStateOf<Int?>(null)}
        var stepRecommendation by remember {mutableStateOf<StepRecommendation?>(null)}
        var repeatDays by remember(selected) {mutableStateOf("")}
        var pendingDelete by remember {mutableStateOf<String?>(null)}
        var confirmClear by remember {mutableStateOf(false)}
        var previousKey by remember(selected) {mutableStateOf<String?>(null)}
        var calm by remember {mutableStateOf(getSharedPreferences("app_preferences",MODE_PRIVATE).getBoolean("calm",false))}
        var autoMini by remember {mutableStateOf(getSharedPreferences("app_preferences",MODE_PRIVATE).getBoolean("auto_pip",false))}
        var busy by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf(false) }
        var pendingImport by remember { mutableStateOf<String?>(null) }
        var title by remember(selected) { mutableStateOf("") }
        var stepText by remember(selected) { mutableStateOf("") }
        var minutes by remember(selected) { mutableStateOf("") }
        var mood by remember { mutableIntStateOf(1) }
        var motivation by remember { mutableIntStateOf(1) }
        var energy by remember { mutableIntStateOf(1) }
        var todayOnly by remember { mutableStateOf(false) }
        var planningMap by remember { mutableStateOf(emptyMap<String,Planning>()) }
        var checkEnabled by remember { mutableStateOf(getSharedPreferences("wellbeing",MODE_PRIVATE).getBoolean("enabled",true)) }
        val preferences=remember {getSharedPreferences("wellbeing",MODE_PRIVATE)}
        var adapt by remember {mutableStateOf(preferences.getBoolean("adapt",false))}
        var lowEnergy by remember {mutableStateOf(preferences.getBoolean("low_energy",false))}
        var availableMinutes by remember {mutableStateOf(preferences.getInt("available_minutes",0).toString())}
        var currentContext by remember {mutableStateOf(preferences.getString("context","").orEmpty())}
        var taskContext by remember(selected) {mutableStateOf("")}
        var keepScreen by remember {mutableStateOf(getSharedPreferences("app_preferences",MODE_PRIVATE).getBoolean("keep_screen_on",false))}
        suspend fun refresh() {
            val all=withContext(Dispatchers.IO) { journal.tasks() }
            tasks=all
            if(selected!=null && all.none {it.id==selected}) selected=null
            adapt=preferences.getBoolean("adapt",false)
            lowEnergy=preferences.getBoolean("low_energy",false)
            checkEnabled=preferences.getBoolean("enabled",true)
            checks=withContext(Dispatchers.IO) { journal.checkIns() }
            notes=withContext(Dispatchers.IO) {journal.notes()}
            planningMap=withContext(Dispatchers.IO) { all.associate { it.id to journal.planning(it.id) } }
            all.firstOrNull { it.id==selected }?.let { task ->
                observations=withContext(Dispatchers.IO) { journal.observations(task.key) }
                steps=withContext(Dispatchers.IO) { journal.steps(task.id) }
                planning=planningMap.getValue(task.id)
                target=withContext(Dispatchers.IO) { database.suggestedDurationMs(task.id) }
                manual=withContext(Dispatchers.IO) { journal.hasOverride(task.key) }
                reset=withContext(Dispatchers.IO) {journal.hasReset(task.key)}
                recurrence=withContext(Dispatchers.IO) {journal.recurrence(task.id)}
                stepRecommendation=withContext(Dispatchers.IO) {if(adapt) journal.recommendedSteps(task.id) else null}
            }
        }
        fun run(after: () -> Unit = {}, action: () -> Unit) {
            if(busy) return
            busy=true
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        action()
                        try { FocusPresence.sync(this@JournalActivity,database.activeFocus());TaskReminder.restore(this@JournalActivity,database) } catch (_: Exception) { }
                    }
                    refresh()
                    after()
                }
                catch (_: Exception) { error=true }
                finally { busy=false }
            }
        }
        fun returnHome() {
            startActivity(
                android.content.Intent(this@JournalActivity, MainActivity::class.java)
                    .addFlags(
                        android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP,
                    ),
            )
            finish()
        }
        LaunchedEffect(selected) { try { refresh() } catch (_: Exception) { error=true } }
        DisposableEffect(Unit) { onDispose { database.close() } }
        val export=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if(uri!=null) run {
                val data=LocalBackup(database).export()
                requireNotNull(contentResolver.openOutputStream(uri)).bufferedWriter().use { it.write(data) }
            }
        }
        val import=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if(uri!=null) scope.launch {
                try {
                    pendingImport=withContext(Dispatchers.IO) {
                        requireNotNull(contentResolver.openInputStream(uri)).use { input ->
                            val output=java.io.ByteArrayOutputStream()
                            val buffer=ByteArray(8192)
                            while(true) {
                                val count=input.read(buffer)
                                if(count<0) break
                                require(output.size()+count<=10_000_000)
                                output.write(buffer,0,count)
                            }
                            output.toString("UTF-8")
                        }
                    }
                } catch (_: Exception) { error=true }
            }
        }
        val task=tasks.firstOrNull { it.id==selected }
        if(confirmClear) AlertDialog(
            onDismissRequest={confirmClear=false},title={Text(label("Effacer toutes les données ?","Erase all data?"))},
            text={Text(label("Tâches, sessions, notes, états et apprentissage seront effacés. Exporte une sauvegarde avant si tu veux pouvoir les récupérer.","Tasks, sessions, notes, check-ins and learning will be erased. Export a backup first if you want to recover them."))},
            confirmButton={TextButton(onClick={confirmClear=false;run{LocalBackup(database).clear();preferences.edit().clear().apply();PauseSchedule.cancel(this@JournalActivity);FocusOverlayService.stop(this@JournalActivity);getSystemService(android.app.NotificationManager::class.java).cancelAll()}}){Text(label("Tout effacer","Erase all"))}},
            dismissButton={TextButton(onClick={confirmClear=false}){Text(label("Annuler","Cancel"))}},
        )
        if(pendingDelete!=null) AlertDialog(
            onDismissRequest={pendingDelete=null},
            title={Text(label("Supprimer cette occurrence ?","Delete this occurrence?"))},
            text={Text(label("Ses sessions et étapes seront supprimées. Termine ou reporte d’abord une occurrence active. Tu peux exporter tes données avant.","Its sessions and steps will be deleted. Finish or postpone an active occurrence first. You can export your data beforehand."))},
            confirmButton={TextButton(onClick={val id=pendingDelete!!;pendingDelete=null;run{journal.deleteTask(id)}}){Text(label("Supprimer","Delete"))}},
            dismissButton={TextButton(onClick={pendingDelete=null}){Text(label("Annuler","Cancel"))}},
        )
        Scaffold { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                item {
                    Text(label("Mon organisation et mon suivi","My planning and learning"),style=MaterialTheme.typography.headlineSmall)
                    TextButton(onClick={if(selected!=null) selected=null else finish()}) { Text(label("Retour","Back")) }
                    if(selected==null) Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        listOf("tasks" to label("Tâches et suivi","Tasks & learning"),"state" to label("Mon état","Check-in"),"notes" to label("Notes","Notes"),"settings" to label("Réglages","Settings")).forEach {(key,text) ->
                            FilterChip(selected=section==key,onClick={section=key},label={Text(text)})
                        }
                    }
                    if(busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                }
                if(task==null) {
                    if(section=="state") item {
                        Text(label("Comment ça va maintenant ? Facultatif.","How are you feeling now? Optional."),style=MaterialTheme.typography.titleMedium)
                        Text(label("0 : très bas · 1 : bas · 2 : moyen · 3 : haut","0: very low · 1: low · 2: medium · 3: high"))
                        Level(label("Humeur","Mood"),mood) { mood=it }
                        Level(label("Motivation","Motivation"),motivation) { motivation=it }
                        Level(label("Énergie","Energy"),energy) { energy=it }
                        Button(
                            enabled=!busy,
                            onClick={
                                val id=editingCheck
                                if(id==null) {
                                    run(after={ returnHome() }) {
                                        journal.checkIn(mood,motivation,energy)
                                    }
                                } else {
                                    run { journal.editCheckIn(id,mood,motivation,energy) }
                                    editingCheck=null
                                }
                            },
                        ) { Text(if(editingCheck==null) label("Enregistrer et revenir à l’accueil","Save and return home") else label("Corriger cet état","Update check-in")) }
                        if(editingCheck!=null) TextButton(onClick={editingCheck=null}) {Text(label("Annuler la correction","Cancel editing"))}
                        Row { Switch(checked=checkEnabled,onCheckedChange={checkEnabled=it;getSharedPreferences("wellbeing",MODE_PRIVATE).edit().putBoolean("enabled",it).apply()});Text(label("Proposer ce point toutes les 4 heures au maximum","Offer a check-in at most every 4 hours")) }
                        Text(label("Les états restent locaux. Ils ne constituent pas un diagnostic. Tu peux les supprimer ci-dessous.","Check-ins stay local. They are not a diagnosis. You can delete them below."))
                    }
                    if(section=="settings") item {
                        Text(label("Adaptation facultative","Optional adaptation"),style=MaterialTheme.typography.titleLarge)
                        Row {Switch(checked=adapt,onCheckedChange={adapt=it;preferences.edit().putBoolean("adapt",it).apply()});Text(label("Filtrer les tâches mélangées au dé et les propositions du widget","Filter tasks shuffled by the die and widget suggestions"))}
                        Row {Switch(checked=lowEnergy,onCheckedChange={lowEnergy=it;preferences.edit().putBoolean("low_energy",it).apply()});Text(label("Mode basse énergie","Low-energy mode"))}
                        OutlinedTextField(value=availableMinutes,onValueChange={availableMinutes=it},label={Text(label("Minutes disponibles (0 = sans limite)","Available minutes (0 = unlimited)"))})
                        OutlinedTextField(value=currentContext,onValueChange={currentContext=it},label={Text(label("Contexte actuel (vide = tous)","Current context (blank = all)"))})
                        TextButton(enabled=availableMinutes.toIntOrNull()?.let {it in 0..10080}==true,onClick={preferences.edit().putInt("available_minutes",availableMinutes.toInt()).putString("context",currentContext.trim()).apply()}) {Text(label("Appliquer les filtres","Apply filters"))}
                        Text(label("Ce sont des règles explicites, pas encore des conclusions apprises sur ton humeur. L’état énergétique expire après 4 heures. Un temps inconnu ne fait pas exclure une tâche.","These are explicit rules, not learned conclusions about your mood. Energy expires after 4 hours. Unknown duration does not exclude a task."))
                        Row {Switch(checked=keepScreen,onCheckedChange={keepScreen=it;getSharedPreferences("app_preferences",MODE_PRIVATE).edit().putBoolean("keep_screen_on",it).apply()});Text(label("Garder l’écran allumé pendant le focus","Keep screen on during focus"))}
                        Row {Switch(checked=calm,onCheckedChange={calm=it;getSharedPreferences("app_preferences",MODE_PRIVATE).edit().putBoolean("calm",it).apply()});Text(label("Mode calme : sans animation ni vibration du dé","Calm mode: no die animation or vibration"))}
                        Row {Switch(checked=autoMini,onCheckedChange={autoMini=it;getSharedPreferences("app_preferences",MODE_PRIVATE).edit().putBoolean("auto_pip",it).apply()});Text(label("Mini-fenêtre quand je quitte le focus","Mini window when I leave focus"))}
                        TextButton(onClick={startActivity(android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,packageName))}) {Text(label("Autorisations et notifications Android","Android notification settings"))}
                    }
                    if(section=="tasks") item {
                        Text(label("Tâches et occurrences","Tasks and occurrences"),style=MaterialTheme.typography.titleLarge)
                        Row { Switch(checked=todayOnly,onCheckedChange={todayOnly=it});Text(label("Aujourd’hui seulement","Today only")) }
                        Row {Switch(checked=showCompleted,onCheckedChange={showCompleted=it});Text(label("Afficher aussi les tâches terminées","Include completed tasks"))}
                    }
                    if(section=="tasks") items(tasks.filter { (!todayOnly || planningMap[it.id]?.today==true) && (showCompleted || !it.completed) },key={it.id}) { t ->
                        OutlinedButton(onClick={selected=t.id},modifier=Modifier.fillMaxWidth()) { Text(t.title+if(t.completed) label(" — terminée"," — completed") else "") }
                    }
                    if(section=="state") item {
                        Text(label("Évolution récente","Recent trend"),style=MaterialTheme.typography.titleLarge)
                        CheckInTrendCard(
                            checks = checks,
                            selected = checkMetric,
                            onSelected = { checkMetric = it },
                            moodLabel = label("Humeur","Mood"),
                            motivationLabel = label("Motivation","Motivation"),
                            energyLabel = label("Énergie","Energy"),
                            emptyLabel = label("Enregistre quelques points pour voir une évolution ici.","Add a few check-ins to see a trend here."),
                            accessibilityLabel = label("Évolution des derniers points enregistrés.","Trend of recent saved check-ins."),
                            veryLowLabel = label("Très bas","Very low"),
                            lowLabel = label("Bas","Low"),
                            mediumLabel = label("Moyen","Medium"),
                            highLabel = label("Haut","High"),
                            latestLabel = label("Dernier point","Latest"),
                        )
                        if(checks.isNotEmpty()) {
                            TextButton(onClick={showCheckDetails=!showCheckDetails}) {
                                Text(
                                    if(showCheckDetails) label("Masquer les détails","Hide details")
                                    else label("Voir les points enregistrés (${checks.size})","Show saved check-ins (${checks.size})"),
                                )
                            }
                        }
                    }
                    if(section=="state" && showCheckDetails) items(checks,key={it.id}) { c ->
                        Card(modifier=Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                                Text(DateFormat.getDateTimeInstance().format(Date(c.date)))
                                Text(label("Humeur / motivation / énergie : ","Mood / motivation / energy: ")+"${c.mood} / ${c.motivation} / ${c.energy}")
                                Row {
                                    TextButton(enabled=!busy,onClick={editingCheck=c.id;mood=c.mood;motivation=c.motivation;energy=c.energy}) {Text(label("Corriger","Edit"))}
                                    TextButton(enabled=!busy,onClick={run { journal.deleteCheckIn(c.id) }}) { Text(label("Supprimer","Delete")) }
                                }
                            }
                        }
                    }
                    if(section=="notes") item {
                        OutlinedTextField(value=noteText,onValueChange={noteText=it},label={Text(label("Une idée à garder","An idea to keep"))},modifier=Modifier.fillMaxWidth())
                        Button(enabled=noteText.isNotBlank()&&!busy,onClick={val text=noteText;run{journal.note(text)};noteText=""}) {Text(label("Garder cette note","Save note"))}
                    }
                    if(section=="notes") items(notes,key={it.id}) {note ->
                        Text(note.text)
                        TextButton(enabled=!busy,onClick={run {journal.deleteNote(note.id)}}) {Text(label("Supprimer","Delete"))}
                    }
                    if(section=="settings") item {
                        Text(label("Sauvegarde locale","Local backup"),style=MaterialTheme.typography.titleLarge)
                        Text(label("Le fichier contient tes tâches et tes états en clair. Choisis un emplacement sûr. Les réglages d’affichage ne sont pas inclus.","The file contains your tasks and check-ins in plaintext. Choose a safe location. Display preferences are not included."))
                        Button(enabled=!busy,onClick={export.launch("executive-function-backup.json")}) { Text(label("Exporter","Export")) }
                        OutlinedButton(enabled=!busy,onClick={import.launch(arrayOf("application/json"))}) { Text(label("Restaurer une sauvegarde","Restore backup")) }
                        TextButton(enabled=!busy,onClick={confirmClear=true}){Text(label("Effacer mes données","Erase my data"))}
                    }
                } else {
                    item {
                        Text(task.title,style=MaterialTheme.typography.headlineSmall)
                        OutlinedTextField(value=title,onValueChange={title=it},label={Text(label("Nouveau titre (famille conservée)","New title (keeps learning family)"))})
                        TextButton(enabled=title.isNotBlank()&&!busy,onClick={val value=title;run { journal.rename(task.id,value) };title=""}) { Text(label("Renommer","Rename")) }
                        Button(enabled=!busy,onClick={run { journal.repeat(task.id) }}) { Text(label("Créer une nouvelle occurrence","Create another occurrence")) }
                        Text(label("Répétition : ","Repeat: ")+(recurrence?.let {label("tous les $it jours","every $it days")} ?: label("manuelle","manual")))
                        OutlinedTextField(value=repeatDays,onValueChange={repeatDays=it},label={Text(label("Intervalle en jours (0 = manuel)","Days between occurrences (0 = manual)"))})
                        TextButton(enabled=repeatDays.toIntOrNull()?.let{it in 0..365}==true&&!busy,onClick={val days=repeatDays.toInt();run{journal.setRecurrence(task.id,days.takeIf{it>0})}}) {Text(label("Enregistrer la répétition","Save recurrence"))}
                        Text(label("Une occurrence apparaît à l’ouverture quand elle est due. Une absence ne crée jamais une pile de tâches en retard.","A due occurrence appears when you open the app. Absence never creates a backlog."))
                        Text(label("Me rappeler cette tâche dans…","Remind me about this task in…"))
                        Row(horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                            listOf(5,15,60).forEach {delay->TextButton(enabled=!busy&&!task.completed,onClick={run{TaskReminder.set(this@JournalActivity,database,task.id,delay)}}){Text("$delay min")}}
                        }
                        TextButton(enabled=!busy,onClick={run{TaskReminder.cancel(this@JournalActivity,database,task.id)}}){Text(label("Annuler le rappel","Cancel reminder"))}
                        Text(label("Android peut décaler légèrement le rappel. Autorise les notifications dans Réglages.","Android may delay the reminder slightly. Allow notifications in Settings."))
                        Row { Checkbox(checked=planning.today,onCheckedChange={value->run { journal.plan(task.id,planning.copy(today=value)) }});Text(label("À faire aujourd’hui","For today")) }
                        Level(label("Importance","Importance"),planning.importance) { value->run { journal.plan(task.id,planning.copy(importance=value)) } }
                        Level(label("Énergie nécessaire","Energy needed"),planning.energy) { value->run { journal.plan(task.id,planning.copy(energy=value)) } }
                        Text(label("Contexte : ","Context: ")+planning.context)
                        OutlinedTextField(value=taskContext,onValueChange={taskContext=it},label={Text(label("Lieu ou matériel nécessaire","Required place or equipment"))})
                        TextButton(enabled=!busy,onClick={run {journal.plan(task.id,planning.copy(context=taskContext.trim()))}}) {Text(label("Enregistrer le contexte","Save context"))}
                        Text(label("Étapes (ordre de saisie)","Steps (entry order)"),style=MaterialTheme.typography.titleLarge)
                        stepRecommendation?.let {recommendation ->
                            Text(label("${recommendation.observationCount} réalisations terminées avec une humeur, une motivation et une énergie de niveaux similaires. Proposition : reprendre le découpage le plus détaillé déjà utilisé.","${recommendation.observationCount} completions with similar mood, motivation and energy levels. Suggestion: reuse the most detailed breakdown previously used."))
                            recommendation.steps.forEach {Text("• $it")}
                            TextButton(enabled=!busy&&steps.isEmpty()&&!task.completed,onClick={run{journal.applyRecommendedSteps(task.id,recommendation.sourceSessionId)}}) {Text(label("Utiliser ces étapes","Use these steps"))}
                            Text(label("Une observation, pas une explication de ton comportement. Tu peux corriger les états ou exclure les séances dans le suivi.","An observation, not an explanation of your behavior. You can edit check-ins or exclude sessions in your history."))
                        }
                    }
                    items(steps,key={it.id}) { s ->
                        Row { Checkbox(checked=s.completed,enabled=!busy,onCheckedChange={value->run { journal.finishStep(s.id,value) }});Text(s.title) }
                        Row {
                            TextButton(enabled=!busy&&steps.firstOrNull()?.id!=s.id,onClick={run{journal.moveStep(task.id,s.id,-1)}}) {Text(label("Monter","Move up"))}
                            TextButton(enabled=!busy&&steps.lastOrNull()?.id!=s.id,onClick={run{journal.moveStep(task.id,s.id,1)}}) {Text(label("Descendre","Move down"))}
                        }
                    }
                    item {
                        OutlinedTextField(value=stepText,onValueChange={stepText=it},label={Text(label("Une petite action concrète","One small concrete action"))})
                        TextButton(enabled=stepText.isNotBlank()&&!busy,onClick={val value=stepText;run { journal.addStep(task.id,value) };stepText=""}) { Text(label("Ajouter l’étape","Add step")) }
                        Text(label("Ce que l’appli a appris","What the app has learned"),style=MaterialTheme.typography.titleLarge)
                        val eligible=observations.filter { it.included && !it.beforeReset && it.status=="COMPLETED" && it.duration>0 }
                        Text(label("Observations utilisables : ","Eligible observations: ")+eligible.size)
                        Text(label("Maximum actif : ","Longest active duration: ")+(eligible.maxOfOrNull { it.duration }?.let { "${it/60000}:${((it/1000)%60).toString().padStart(2,'0')}" } ?: "—"))
                        Text(label("Durée proposée : ","Suggested duration: ")+(target?.let { "${it/60000} min" } ?: label("apprentissage en cours (3 observations)","learning (3 observations required)")))
                        Text(if(manual) label("Référence manuelle active.","Manual override active.") else label("Règle : maximum actif arrondi à la minute supérieure + 1 minute. Les pauses ne comptent pas.","Rule: longest active duration rounded up to a minute + 1 minute. Pauses do not count."))
                        OutlinedTextField(value=minutes,onValueChange={minutes=it},label={Text(label("Référence manuelle en minutes","Manual reference in minutes"))})
                        TextButton(enabled=minutes.toLongOrNull()?.let { it in 1L..10080L }==true&&!busy,onClick={run { journal.override(task.key,minutes.toLong()) }}) { Text(label("Utiliser cette référence","Use this reference")) }
                        TextButton(enabled=!busy,onClick={run { journal.override(task.key,null) }}) { Text(label("Revenir au calcul automatique","Use automatic calculation")) }
                        TextButton(enabled=!busy,onClick={run{journal.resetLearning(task.key,!reset)}}) {Text(if(reset) label("Annuler la remise à zéro des observations","Undo observation reset") else label("Recommencer les observations (annulable)","Start observations afresh (reversible)"))}
                        Text(label("Les corrections s’appliquent aux prochaines sessions, jamais au timer déjà lancé.","Corrections apply to future sessions, never to a running timer."))
                    }
                    items(observations,key={it.id}) { o ->
                        Text(DateFormat.getDateTimeInstance().format(Date(o.date))+" · ${o.duration/60000}:${((o.duration/1000)%60).toString().padStart(2,'0')} · ${o.status}")
                        if(o.status=="COMPLETED") TextButton(enabled=!busy,onClick={run { journal.include(o.id,!o.included) }}) { Text(if(o.included) label("Cette fois ne me représente pas","Exclude this observation") else label("Réintégrer cette observation","Include again")) }
                        if(o.beforeReset) Text(label("Antérieure à la remise à zéro : conservée mais non utilisée.","Before the reset: preserved but not used."))
                    }
                    item {
                        Text(label("Corriger la famille d’apprentissage","Correct learning family"),style=MaterialTheme.typography.titleMedium)
                        Text(label("Ces actions changent les observations utilisées, pas leur contenu.","These actions change which observations are used, not their contents."))
                        TextButton(enabled=!busy,onClick={previousKey=task.key;run { journal.group(task.id,"task:"+task.id) }}) { Text(label("Séparer cette tâche des autres","Separate this task")) }
                        previousKey?.let {key->TextButton(enabled=!busy,onClick={run{journal.group(task.id,key)};previousKey=null}){Text(label("Annuler le dernier regroupement","Undo last grouping"))}}
                    }
                    items(tasks.filter { it.id!=task.id && it.key!=task.key }.distinctBy { it.key },key={"group:"+it.id}) { other ->
                        TextButton(enabled=!busy,onClick={previousKey=task.key;run { journal.group(task.id,other.key) }}) { Text(label("Même activité que : ","Same activity as: ")+other.title) }
                    }
                    item {TextButton(enabled=!busy,onClick={pendingDelete=task.id}){Text(label("Supprimer cette occurrence et son historique","Delete this occurrence and its history"))}}
                }
            }
        }
        if(error) AlertDialog(onDismissRequest={error=false},title={Text(label("Action non effectuée","Action failed"))},text={Text(label("Vérifie les valeurs ou le fichier puis réessaie. Une restauration invalide conserve les données précédentes.","Check the values or file and retry. An invalid restore preserves previous data."))},confirmButton={TextButton(onClick={error=false}) {Text("OK")}})
        if(pendingImport!=null) AlertDialog(onDismissRequest={pendingImport=null},title={Text(label("Remplacer les données locales ?","Replace local data?"))},text={Text(label("Exporte d’abord tes données actuelles. Cette restauration remplace tâches, sessions et états. Aucun envoi réseau.","Export your current data first. Restore replaces tasks, sessions and check-ins. No network upload."))},confirmButton={TextButton(onClick={val text=pendingImport!!;pendingImport=null;run {LocalBackup(database).restore(text)}}){Text(label("Remplacer","Replace"))}},dismissButton={TextButton(onClick={pendingImport=null}){Text(label("Annuler","Cancel"))}})
    }
}

private enum class CheckMetric { MOOD, MOTIVATION, ENERGY }

@Composable
private fun CheckInTrendCard(
    checks: List<CheckIn>,
    selected: CheckMetric,
    onSelected: (CheckMetric) -> Unit,
    moodLabel: String,
    motivationLabel: String,
    energyLabel: String,
    emptyLabel: String,
    accessibilityLabel: String,
    veryLowLabel: String,
    lowLabel: String,
    mediumLabel: String,
    highLabel: String,
    latestLabel: String,
) {
    if (checks.isEmpty()) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                emptyLabel,
                modifier = Modifier.padding(18.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val points = checks.take(14).asReversed()
    val metricLabel = when (selected) {
        CheckMetric.MOOD -> moodLabel
        CheckMetric.MOTIVATION -> motivationLabel
        CheckMetric.ENERGY -> energyLabel
    }
    fun value(check: CheckIn): Int = when (selected) {
        CheckMetric.MOOD -> check.mood
        CheckMetric.MOTIVATION -> check.motivation
        CheckMetric.ENERGY -> check.energy
    }
    val values = points.map(::value)
    val latest = points.last()
    val latestValue = value(latest)
    val latestLevel = listOf(veryLowLabel, lowLabel, mediumLabel, highLabel)[latestValue.coerceIn(0, 3)]
    val trend = latestValue - values.first()
    val trendText = when {
        trend > 0 -> "↑ +$trend"
        trend < 0 -> "↓ $trend"
        else -> "→"
    }
    val lineColor = when (selected) {
        CheckMetric.MOOD -> MaterialTheme.colorScheme.primary
        CheckMetric.MOTIVATION -> MaterialTheme.colorScheme.secondary
        CheckMetric.ENERGY -> MaterialTheme.colorScheme.tertiary
    }
    val surfaceColor = MaterialTheme.colorScheme.surface
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT)
    val chartSummary = buildString {
        append(accessibilityLabel)
        append(" ")
        append(metricLabel)
        append(": ")
        append(values.joinToString(", "))
        append(". ")
        append(latestLabel)
        append(": ")
        append(latestLevel)
        append(".")
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    CheckMetric.MOOD to moodLabel,
                    CheckMetric.MOTIVATION to motivationLabel,
                    CheckMetric.ENERGY to energyLabel,
                ).forEach { (metric, label) ->
                    FilterChip(
                        selected = selected == metric,
                        onClick = { onSelected(metric) },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.Bottom,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(metricLabel, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${latestLabel} · $latestLevel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                    color = lineColor.copy(alpha = 0.12f),
                    contentColor = lineColor,
                ) {
                    Text(
                        "$latestValue / 3   $trendText",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .semantics { contentDescription = chartSummary },
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight().width(62.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(highLabel, style = MaterialTheme.typography.labelSmall)
                    Text(mediumLabel, style = MaterialTheme.typography.labelSmall)
                    Text(lowLabel, style = MaterialTheme.typography.labelSmall)
                    Text(veryLowLabel, style = MaterialTheme.typography.labelSmall)
                }
                Canvas(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    val left = 8.dp.toPx()
                    val right = size.width - 8.dp.toPx()
                    val top = 8.dp.toPx()
                    val bottom = size.height - 8.dp.toPx()

                    fun x(index: Int): Float =
                        if (values.size == 1) (left + right) / 2f
                        else left + (right - left) * index / values.lastIndex.toFloat()
                    fun y(v: Int): Float =
                        bottom - (bottom - top) * (v.coerceIn(0, 3) / 3f)

                    repeat(4) { level ->
                        val gy = y(level)
                        drawLine(
                            color = gridColor,
                            start = Offset(left, gy),
                            end = Offset(right, gy),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }

                    values.zipWithNext().forEachIndexed { index, pair ->
                        drawLine(
                            color = lineColor,
                            start = Offset(x(index), y(pair.first)),
                            end = Offset(x(index + 1), y(pair.second)),
                            strokeWidth = 4.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        )
                    }
                    values.forEachIndexed { index, v ->
                        drawCircle(
                            color = surfaceColor,
                            radius = 6.dp.toPx(),
                            center = Offset(x(index), y(v)),
                        )
                        drawCircle(
                            color = lineColor,
                            radius = 4.dp.toPx(),
                            center = Offset(x(index), y(v)),
                        )
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    dateFormat.format(Date(points.first().date)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (points.size > 1) {
                    Text(
                        dateFormat.format(Date(points.last().date)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun Level(title: String,value: Int,onChange: (Int)->Unit) {
    Text("$title : $value")
    Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
        (0..3).forEach { n -> FilterChip(selected=n==value,onClick={onChange(n)},label={Text(n.toString())}) }
    }
}
