package org.lepotager.executivefunction

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
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
import org.lepotager.executivefunction.ui.FocusScreen
import org.lepotager.executivefunction.ui.FocusStartDialog
import org.lepotager.executivefunction.ui.HomeScreen
import org.lepotager.executivefunction.ui.LocalAlgorithmIntro
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
                    if (snapshot.loading) return@LaunchedEffect
                    if (snapshot.tasks.any { it.id == taskId }) viewModel.requestStart(taskId)
                    else requestedTask = null
                }
                LaunchedEffect(pendingStart?.taskId) {
                    if (pendingStart?.taskId != null && pendingStart?.taskId == requestedTask) requestedTask = null
                }
                LaunchedEffect(
                    snapshot.loading,
                    snapshot.activeFocus?.session?.id,
                    externalCapture,
                    requestedTask,
                    pendingStart,
                    introSeen,
                ) {
                    if (
                        !snapshot.loading && !introSeen && snapshot.activeFocus == null &&
                        !externalCapture && requestedTask == null && pendingStart == null
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
                    requestedTask,
                    pendingStart,
                ) {
                    val prefs=getSharedPreferences("wellbeing",MODE_PRIVATE)
                    val now=System.currentTimeMillis()
                    if(
                        !snapshot.loading && introSeen && !showIntro && snapshot.activeFocus==null &&
                        !externalCapture && requestedTask==null && pendingStart==null &&
                        prefs.getBoolean("enabled",true) && now-prefs.getLong("last_prompt",0L)>=14_400_000L
                    ) {
                        prefs.edit().putLong("last_prompt",now).apply()
                        showCheckIn=true
                    }
                }

                if (
                    showIntro && snapshot.activeFocus == null && !externalCapture &&
                    requestedTask == null && pendingStart == null
                ) {
                    LocalAlgorithmIntro(
                        onEnableAdaptation = ::enableAdaptation,
                        onFinish = ::markIntroSeen,
                        onSkip = ::markIntroSeen,
                    )
                } else {
                    if(externalCapture) {
                        org.lepotager.executivefunction.ui.ExternalCaptureDialog(
                            onDismiss={externalCapture=false},
                            onCapture={title->viewModel.capture(title,null,TaskColor.NEUTRAL){externalCapture=false}},
                        )
                    }
                    pendingStart?.let { request ->
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
                    if(showCheckIn) androidx.compose.material3.AlertDialog(
                        onDismissRequest={showCheckIn=false},
                        title={Text(getString(R.string.check_in_title))},
                        text={Text(getString(R.string.check_in_body))},
                        confirmButton={TextButton(onClick={showCheckIn=false;startActivity(Intent(this@MainActivity,JournalActivity::class.java).putExtra("section","state"))}) {Text(getString(R.string.check_in_answer))}},
                        dismissButton={TextButton(onClick={showCheckIn=false}) {Text(getString(R.string.not_now))}},
                    )

                    LaunchedEffect(snapshot.activeFocus?.session?.status) {
                        if(snapshot.activeFocus!=null) drawRequest=0
                        if(snapshot.activeFocus?.session?.status == FocusStatus.RUNNING && appPreferences().getBoolean("keep_screen_on",false)) window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        else window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        if (snapshot.activeFocus?.session?.status != FocusStatus.RUNNING && overlayEnabled) {
                            updateOverlayEnabled(false)
                        }
                    }

                    val homeVisible = !snapshot.loading && snapshot.activeFocus == null
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            if (homeVisible) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    TextButton(onClick = { showCheckIn=false; showIntro=true }) {
                                        Text(getString(R.string.help_how_it_works))
                                    }
                                }
                            }
                        },
                    ) { padding ->
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
                                    onJournal = { startActivity(Intent(this@MainActivity, JournalActivity::class.java).putExtra("task",snapshot.activeFocus?.task?.id)) },
                                    onNote = { startActivity(Intent(this@MainActivity, JournalActivity::class.java).putExtra("section","notes")) },
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
        if (appPreferences().getBoolean("keep_screen_on", false) && viewModel.snapshot.value.activeFocus?.session?.status==FocusStatus.RUNNING) window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (waitingForOverlayPermission) {
            waitingForOverlayPermission = false
            if (Settings.canDrawOverlays(this)) updateOverlayEnabled(true)
        } else {
            val shouldBeEnabled = overlayPreference() && Settings.canDrawOverlays(this)
            overlayEnabled = shouldBeEnabled
            if (shouldBeEnabled) FocusOverlayService.start(this)
            else FocusOverlayService.stop(this)
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: android.content.res.Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode,newConfig)
        miniWindow=isInPictureInPictureMode
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if(appPreferences().getBoolean("auto_pip",false)) enterMiniWindow()
    }

    private fun enterMiniWindow() {
        val active=viewModel.snapshot.value.activeFocus ?: return
        if(active.session.status!=FocusStatus.RUNNING || !packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_PICTURE_IN_PICTURE)) return
        val pause=android.app.RemoteAction(android.graphics.drawable.Icon.createWithResource(this,R.drawable.ic_timer_notification),getString(R.string.interrupt_action),getString(R.string.interrupt_action),FocusPresence.action(this,active,"pause"))
        if(overlayEnabled) updateOverlayEnabled(false)
        enterPictureInPictureMode(android.app.PictureInPictureParams.Builder().setAspectRatio(android.util.Rational(4,3)).setActions(listOf(pause)).build())
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
        requestedTask=intent?.getStringExtra("requested_task")
        when(intent?.getStringExtra("quick_action")) {
            "capture" -> externalCapture=true
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
    }

    private fun savePauseInterval(minutes: Int) {
        pauseAfterMinutes = minutes.coerceIn(5, 120)
        appPreferences().edit().putInt(KEY_PAUSE_MINUTES, pauseAfterMinutes).apply()
    }

    private fun markIntroSeen() {
        introSeen = true
        showIntro = false
        appPreferences().edit().putBoolean(KEY_INTRO_SEEN, true).apply()
    }

    private fun enableAdaptation() {
        getSharedPreferences("wellbeing", Context.MODE_PRIVATE).edit().putBoolean("adapt", true).apply()
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
