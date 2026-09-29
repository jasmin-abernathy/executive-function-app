package org.lepotager.executivefunction

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.lepotager.executivefunction.domain.FocusTimerMode
import org.lepotager.executivefunction.model.FocusStatus
import org.lepotager.executivefunction.model.TaskColor
import org.lepotager.executivefunction.ui.ErrorDialog
import org.lepotager.executivefunction.ui.FirstRunSetupConfig
import org.lepotager.executivefunction.ui.FirstRunSetupFlow
import org.lepotager.executivefunction.ui.FocusScreen
import org.lepotager.executivefunction.ui.FocusStartDialog
import org.lepotager.executivefunction.ui.HomeScreen
import org.lepotager.executivefunction.ui.ResumeScreen
import org.lepotager.executivefunction.ui.theme.ExecutiveFunctionTheme

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { viewModel.reload() }
    private val viewModel: AppViewModel by viewModels()
    private var overlayEnabled by mutableStateOf(false)
    private var drawEnabled by mutableStateOf(true)
    private var pauseSuggestionsEnabled by mutableStateOf(true)
    private var pauseAfterMinutes by mutableStateOf(25)
    private var waitingForOverlayPermission = false
    private var showCheckIn by mutableStateOf(false)
    private var miniWindow by mutableStateOf(false)
    private var requestedTask by mutableStateOf<String?>(null)
    private var showQuickNote by mutableStateOf(false)
    private var returnToMiniWindow = false
    private var externalCapture by mutableStateOf(false)
    private var drawRequest by mutableStateOf(0)
    private var introSeen by mutableStateOf(false)
    private var showIntro by mutableStateOf(false)
    private var lastTimerMode by mutableStateOf<FocusTimerMode?>(null)
    private val focusChanges=object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context,intent: Intent) { viewModel.reload() }
    }

    override fun onStart() {
        super.onStart()
        androidx.core.content.ContextCompat.registerReceiver(
            this,
            focusChanges,
            android.content.IntentFilter(FocusPresence.CHANGED),
            androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    override fun onStop() {
        unregisterReceiver(focusChanges)
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            requestedTask = it.getString("pending_task")
            externalCapture = it.getBoolean("external_capture")
            showQuickNote = it.getBoolean("quick_note")
            returnToMiniWindow = it.getBoolean("return_to_pip")
            showIntro = it.getBoolean("show_intro")
            showCheckIn = it.getBoolean("show_check_in")
            drawRequest = it.getInt("draw_request")
            waitingForOverlayPermission = it.getBoolean("waiting_overlay")
        }
        miniWindow = isInPictureInPictureMode
        consumeIntent(intent)
        overlayEnabled = overlayPreference() && Settings.canDrawOverlays(this)
        val preferences = appPreferences()
        drawEnabled = preferences.getBoolean(KEY_DRAW_ENABLED, true)
        pauseSuggestionsEnabled = preferences.getBoolean(KEY_PAUSE_ENABLED, true)
        pauseAfterMinutes = preferences.getInt(KEY_PAUSE_MINUTES, 25)
        introSeen = preferences.getBoolean(KEY_INTRO_SEEN, false)
        lastTimerMode = preferences.getString(KEY_TIMER_MODE, null)?.let { value ->
            runCatching { FocusTimerMode.valueOf(value) }.getOrNull()
        }
        enableEdgeToEdge()
        setContent {
            ExecutiveFunctionTheme {
                val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
                val error by viewModel.error.collectAsStateWithLifecycle()
                val completionLeadMinutes by viewModel.completionLeadMinutes.collectAsStateWithLifecycle()
                val pendingStart by viewModel.pendingStart.collectAsStateWithLifecycle()

                LaunchedEffect(requestedTask, snapshot.loading) {
                    val taskId = requestedTask ?: return@LaunchedEffect
                    if (snapshot.loading || pendingStart?.taskId == taskId) return@LaunchedEffect
                    if (snapshot.tasks.any { it.id == taskId }) viewModel.requestStart(taskId)
                    else requestedTask = null
                }
                LaunchedEffect(pendingStart?.taskId, requestedTask) {
                    if (pendingStart?.taskId != null && pendingStart?.taskId == requestedTask) requestedTask = null
                }
                LaunchedEffect(error) {
                    if (error != null) requestedTask = null
                }
                LaunchedEffect(
                    snapshot.loading,
                    snapshot.activeFocus?.session?.id,
                    externalCapture,
                    showQuickNote,
                    miniWindow,
                    requestedTask,
                    pendingStart,
                    introSeen,
                ) {
                    if (
                        !snapshot.loading && !introSeen && snapshot.activeFocus == null &&
                        !externalCapture && !showQuickNote && !miniWindow && requestedTask == null && pendingStart == null
                    ) {
                        showCheckIn = false
                        showIntro = true
                    }
                }
                LaunchedEffect(
                    snapshot.loading,
                    snapshot.activeFocus?.session?.id,
                    showIntro,
                    introSeen,
                    externalCapture,
                    showQuickNote,
                    miniWindow,
                    requestedTask,
                    pendingStart,
                ) {
                    val prefs=getSharedPreferences("wellbeing",MODE_PRIVATE)
                    val now=System.currentTimeMillis()
                    if(
                        !snapshot.loading && introSeen && !showIntro && snapshot.activeFocus==null &&
                        !externalCapture && !showQuickNote && !miniWindow && requestedTask==null && pendingStart==null &&
                        prefs.getBoolean("enabled",true) && now-prefs.getLong("last_prompt",0L)>=14_400_000L
                    ) {
                        prefs.edit().putLong("last_prompt",now).apply()
                        showCheckIn=true
                    }
                }

                if (
                    showIntro && !snapshot.loading && snapshot.activeFocus == null && !externalCapture && !showQuickNote && !miniWindow &&
                    requestedTask == null && pendingStart == null
                ) {
                    FirstRunSetupFlow(
                        initial = currentSetupConfig(),
                        onApply = ::applyFirstRunSetup,
                        onSkip = ::markIntroSeen,
                    )
                } else {
                    if(externalCapture && !miniWindow) {
                        org.lepotager.executivefunction.ui.ExternalCaptureDialog(
                            onDismiss={externalCapture=false},
                            onCapture={title->viewModel.capture(title,null,TaskColor.NEUTRAL){externalCapture=false}},
                        )
                    }
                    if (showQuickNote && !externalCapture && !miniWindow) {
                        org.lepotager.executivefunction.ui.QuickNoteDialog(
                            onDismiss = ::closeQuickNote,
                            onSave = { text -> viewModel.addQuickNote(text, ::closeQuickNote) },
                        )
                    }
                    if (!externalCapture && !showQuickNote && !miniWindow) pendingStart?.let { request ->
                        key(request.taskId) {
                            FocusStartDialog(
                                taskTitle = request.title,
                                learnedTargetDurationMs = request.learnedTargetDurationMs,
                                initialMode = lastTimerMode ?: if (request.learnedTargetDurationMs == null) {
                                    FocusTimerMode.STOPWATCH
                                } else {
                                    FocusTimerMode.COUNTDOWN
                                },
                                willPostponeExisting = snapshot.activeFocus?.task?.id?.let { it != request.taskId } == true,
                                onDismiss = viewModel::cancelStart,
                                onConfirm = { mode, targetDurationMs ->
                                    saveTimerMode(mode)
                                    requestNotificationPermissionIfNeeded()
                                    viewModel.confirmStart(targetDurationMs)
                                },
                            )
                        }
                    }
                    if(showCheckIn && !snapshot.loading && snapshot.activeFocus == null &&
                        !externalCapture && !showQuickNote && !miniWindow && requestedTask == null && pendingStart == null
                    ) androidx.compose.material3.AlertDialog(
                        onDismissRequest={showCheckIn=false},
                        title={androidx.compose.material3.Text(getString(R.string.check_in_title))},
                        text={androidx.compose.material3.Text(getString(R.string.check_in_body))},
                        confirmButton={androidx.compose.material3.TextButton(onClick={showCheckIn=false;startActivity(Intent(this@MainActivity,JournalActivity::class.java).putExtra("section","state"))}) {androidx.compose.material3.Text(getString(R.string.check_in_answer))}},
                        dismissButton={androidx.compose.material3.TextButton(onClick={showCheckIn=false}) {androidx.compose.material3.Text(getString(R.string.not_now))}},
                    )

                    LaunchedEffect(snapshot.loading, snapshot.activeFocus, miniWindow) {
                        if (snapshot.loading) return@LaunchedEffect
                        if (miniWindow) {
                            val active = snapshot.activeFocus
                            if (active != null) setPictureInPictureParams(pipParams(active))
                            else setPictureInPictureParams(android.app.PictureInPictureParams.Builder().setActions(emptyList()).build())
                        }
                        if(snapshot.activeFocus!=null) drawRequest=0
                        if(snapshot.activeFocus?.session?.status == FocusStatus.RUNNING && appPreferences().getBoolean("keep_screen_on",false)) window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        else window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        if (snapshot.activeFocus?.session?.status != FocusStatus.RUNNING && overlayEnabled) {
                            updateOverlayEnabled(false)
                        }
                    }

                    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                        Box(
                            modifier = Modifier.fillMaxSize().padding(padding),
                        ) {
                            when {
                                miniWindow && snapshot.activeFocus!=null -> org.lepotager.executivefunction.ui.MiniTimer(requireNotNull(snapshot.activeFocus))
                                snapshot.loading -> CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                )

                                snapshot.activeFocus?.session?.status == FocusStatus.RUNNING -> FocusScreen(
                                    activeFocus = requireNotNull(snapshot.activeFocus),
                                    modalBlocked = externalCapture || showQuickNote || pendingStart != null,
                                    onJournal = { startActivity(Intent(this@MainActivity, JournalActivity::class.java).putExtra("task",snapshot.activeFocus?.task?.id)) },
                                    onNote = { showQuickNote = true },
                                    onMini = ::enterMiniWindow,
                                    overlayEnabled = overlayEnabled,
                                    pauseSuggestionsEnabled = pauseSuggestionsEnabled,
                                    pauseAfterMinutes = pauseAfterMinutes,
                                    onToggleOverlay = ::toggleOverlay,
                                    onQuickCapture = { title, firstStep, after ->
                                        viewModel.capture(title, firstStep, TaskColor.NEUTRAL, after)
                                    },
                                    onInterrupt = viewModel::interrupt,
                                    onComplete = viewModel::complete,
                                )

                                snapshot.activeFocus?.session?.status == FocusStatus.INTERRUPTED -> ResumeScreen(
                                    activeFocus = requireNotNull(snapshot.activeFocus),
                                    onResume = viewModel::resume,
                                    onPostpone = viewModel::postpone,
                                )

                                else -> HomeScreen(
                                    tasks = snapshot.tasks,
                                    drawRequest = drawRequest,
                                    onApplySuggestedOrder = viewModel::applySuggestedOrder,
                                    eligibleDrawIds = snapshot.eligibleDrawIds,
                                    suggestedTaskId = snapshot.suggestedTaskId,
                                    onHelp = { refreshPreferences(); showCheckIn=false; showIntro=true },
                                    onJournal = { startActivity(Intent(this@MainActivity, JournalActivity::class.java)) },
                                    drawEnabled = drawEnabled,
                                    pauseSuggestionsEnabled = pauseSuggestionsEnabled,
                                    pauseAfterMinutes = pauseAfterMinutes,
                                    onCapture = viewModel::capture,
                                    onStart = viewModel::requestStart,
                                    onMoveTask = viewModel::moveTask,
                                    onApplyTaskOrder = viewModel::applyTaskOrder,
                                    onSetTaskColor = viewModel::setTaskColor,
                                    onSetDrawEnabled = ::saveDrawPreference,
                                    onSetPauseSuggestionsEnabled = ::savePausePreference,
                                    onSetPauseAfterMinutes = ::savePauseInterval,
                                )
                            }
                        }
                    }

                    if (error != null) {
                        ErrorDialog(onDismiss = viewModel::clearError)
                    }
                    completionLeadMinutes?.let { minutes ->
                        org.lepotager.executivefunction.ui.CompletionFeedbackDialog(
                            minutesAhead = minutes,
                            onDismiss = viewModel::clearCompletionFeedback,
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.reload()
        refreshPreferences()
        miniWindow = isInPictureInPictureMode
        if (appPreferences().getBoolean("keep_screen_on", false) && viewModel.snapshot.value.activeFocus?.session?.status==FocusStatus.RUNNING) window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (waitingForOverlayPermission) {
            waitingForOverlayPermission = false
            if (!miniWindow && Settings.canDrawOverlays(this)) updateOverlayEnabled(true)
        } else {
            val shouldBeEnabled = !miniWindow && overlayPreference() && Settings.canDrawOverlays(this)
            overlayEnabled = shouldBeEnabled
            if (shouldBeEnabled) FocusOverlayService.start(this)
            else FocusOverlayService.stop(this)
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: android.content.res.Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode,newConfig)
        miniWindow=isInPictureInPictureMode
        if (isInPictureInPictureMode) {
            if (overlayEnabled) updateOverlayEnabled(false)
            viewModel.snapshot.value.activeFocus?.let { setPictureInPictureParams(pipParams(it)) }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if(!waitingForOverlayPermission && !showQuickNote && !externalCapture &&
            !showIntro && viewModel.pendingStart.value == null && requestedTask == null &&
            appPreferences().getBoolean("auto_pip",false)) enterMiniWindow()
    }

    private fun pipParams(active: org.lepotager.executivefunction.model.ActiveFocus) =
        PipFocusSurface.params(
            this,
            active,
            PendingIntent.getActivity(
                this, 3109,
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .putExtra("quick_action", "pip_note"),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )

    private fun enterMiniWindow() {
        val active = viewModel.snapshot.value.activeFocus ?: return
        if (active.session.status !in listOf(FocusStatus.RUNNING, FocusStatus.INTERRUPTED) ||
            !packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_PICTURE_IN_PICTURE)
        ) return
        // PiP may be disabled in system settings even on a capable device.
        val entered = runCatching { enterPictureInPictureMode(pipParams(active)) }.getOrDefault(false)
        if (entered && overlayEnabled) updateOverlayEnabled(false)
    }

    private fun closeQuickNote() {
        showQuickNote = false
        val returnToPip = returnToMiniWindow
        returnToMiniWindow = false
        if (returnToPip) enterMiniWindow()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("pending_task", requestedTask ?: viewModel.pendingStart.value?.taskId)
        outState.putBoolean("external_capture", externalCapture)
        outState.putBoolean("quick_note", showQuickNote)
        outState.putBoolean("return_to_pip", returnToMiniWindow)
        outState.putBoolean("show_intro", showIntro)
        outState.putBoolean("show_check_in", showCheckIn)
        outState.putInt("draw_request", drawRequest)
        outState.putBoolean("waiting_overlay", waitingForOverlayPermission)
        super.onSaveInstanceState(outState)
    }

    private fun toggleOverlay() {
        if (overlayEnabled) {
            updateOverlayEnabled(false)
        } else if (Settings.canDrawOverlays(this)) {
            updateOverlayEnabled(true)
        } else {
            waitingForOverlayPermission = true
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"),
                ),
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeIntent(intent)
        viewModel.reload()
    }

    private fun consumeIntent(intent: Intent?) {
        intent?.getStringExtra("requested_task")?.let { requestedTask = it; showCheckIn = false }
        when(intent?.getStringExtra("quick_action")) {
            "capture" -> { externalCapture=true; showCheckIn=false }
            "pip_note" -> { showQuickNote=true; returnToMiniWindow=true; showCheckIn=false }
            "draw" -> drawRequest+=1
        }
        intent?.removeExtra("requested_task")
        intent?.removeExtra("quick_action")
    }

    private fun updateOverlayEnabled(enabled: Boolean) {
        overlayEnabled = enabled
        getSharedPreferences(FocusOverlayService.PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(FocusOverlayService.KEY_ENABLED, enabled)
            .apply()
        if (enabled) FocusOverlayService.start(this) else FocusOverlayService.stop(this)
    }

    private fun overlayPreference(): Boolean =
        getSharedPreferences(FocusOverlayService.PREFERENCES, Context.MODE_PRIVATE)
            .getBoolean(FocusOverlayService.KEY_ENABLED, false)

    private fun saveDrawPreference(enabled: Boolean) {
        drawEnabled = enabled
        appPreferences().edit().putBoolean(KEY_DRAW_ENABLED, enabled).apply()
    }

    private fun savePausePreference(enabled: Boolean) {
        pauseSuggestionsEnabled = enabled
        appPreferences().edit().putBoolean(KEY_PAUSE_ENABLED, enabled).apply()
        viewModel.snapshot.value.activeFocus?.let { FocusPresence.sync(this, it) }
    }

    private fun savePauseInterval(minutes: Int) {
        pauseAfterMinutes = minutes.coerceIn(5, 120)
        appPreferences().edit().putInt(KEY_PAUSE_MINUTES, pauseAfterMinutes).apply()
        viewModel.snapshot.value.activeFocus?.let { active ->
            PauseSchedule.set(
                this,
                active,
                org.lepotager.executivefunction.domain.PauseReminderTimes.afterContinue(
                    org.lepotager.executivefunction.domain.SessionClock.elapsedMs(active.session, System.currentTimeMillis()),
                    pauseAfterMinutes,
                ),
            )
        }
    }

    private fun markIntroSeen() {
        introSeen = true
        showIntro = false
        appPreferences().edit().putBoolean(KEY_INTRO_SEEN, true).apply()
    }

    private fun refreshPreferences() {
        val prefs = appPreferences()
        drawEnabled = prefs.getBoolean(KEY_DRAW_ENABLED, true)
        pauseSuggestionsEnabled = prefs.getBoolean(KEY_PAUSE_ENABLED, true)
        pauseAfterMinutes = prefs.getInt(KEY_PAUSE_MINUTES, 25).coerceIn(5, 120)
        lastTimerMode = prefs.getString(KEY_TIMER_MODE, null)?.let {
            runCatching { FocusTimerMode.valueOf(it) }.getOrNull()
        }
    }

    private fun currentSetupConfig(): FirstRunSetupConfig {
        val wellbeing = getSharedPreferences("wellbeing", Context.MODE_PRIVATE)
        val app = appPreferences()
        return FirstRunSetupConfig(
            timerMode = lastTimerMode ?: FocusTimerMode.STOPWATCH,
            drawEnabled = drawEnabled,
            pauseSuggestionsEnabled = pauseSuggestionsEnabled,
            pauseAfterMinutes = pauseAfterMinutes,
            checkInEnabled = wellbeing.getBoolean("enabled", true),
            adaptationEnabled = wellbeing.getBoolean("adapt", false),
            calmMode = app.getBoolean("calm", false),
            autoMiniWindow = app.getBoolean("auto_pip", false),
        )
    }

    private fun applyFirstRunSetup(config: FirstRunSetupConfig) {
        drawEnabled = config.drawEnabled
        pauseSuggestionsEnabled = config.pauseSuggestionsEnabled
        pauseAfterMinutes = config.pauseAfterMinutes.coerceIn(5, 120)
        lastTimerMode = config.timerMode
        appPreferences().edit()
            .putBoolean(KEY_DRAW_ENABLED, drawEnabled)
            .putBoolean(KEY_PAUSE_ENABLED, pauseSuggestionsEnabled)
            .putInt(KEY_PAUSE_MINUTES, pauseAfterMinutes)
            .putString(KEY_TIMER_MODE, config.timerMode.name)
            .putBoolean("calm", config.calmMode)
            .putBoolean("auto_pip", config.autoMiniWindow)
            .putBoolean(KEY_INTRO_SEEN, true)
            .apply()
        getSharedPreferences("wellbeing", Context.MODE_PRIVATE).edit()
            .putBoolean("enabled", config.checkInEnabled)
            .putBoolean("adapt", config.adaptationEnabled)
            .apply()
        introSeen = true
        showIntro = false
        viewModel.reload()
    }

    private fun saveTimerMode(mode: FocusTimerMode) {
        lastTimerMode = mode
        appPreferences().edit().putString(KEY_TIMER_MODE, mode.name).apply()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (
            android.os.Build.VERSION.SDK_INT >= 33 &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED &&
            !appPreferences().getBoolean("notification_permission_asked", false)
        ) {
            appPreferences().edit().putBoolean("notification_permission_asked", true).apply()
            notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun appPreferences() = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    private companion object {
        const val PREFERENCES = "app_preferences"
        const val KEY_DRAW_ENABLED = "draw_enabled"
        const val KEY_PAUSE_ENABLED = "pause_suggestions_enabled"
        const val KEY_PAUSE_MINUTES = "pause_after_minutes"
        const val KEY_INTRO_SEEN = "local_algorithm_intro_seen"
        const val KEY_TIMER_MODE = "focus_timer_mode"
    }
}
