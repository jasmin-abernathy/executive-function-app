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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.lepotager.executivefunction.model.FocusStatus
import org.lepotager.executivefunction.model.TaskColor
import org.lepotager.executivefunction.ui.ErrorDialog
import org.lepotager.executivefunction.ui.FocusScreen
import org.lepotager.executivefunction.ui.HomeScreen
import org.lepotager.executivefunction.ui.ResumeScreen
import org.lepotager.executivefunction.ui.theme.ExecutiveFunctionTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()
    private var overlayEnabled by mutableStateOf(false)
    private var drawEnabled by mutableStateOf(true)
    private var pauseSuggestionsEnabled by mutableStateOf(true)
    private var pauseAfterMinutes by mutableStateOf(25)
    private var waitingForOverlayPermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overlayEnabled = overlayPreference() && Settings.canDrawOverlays(this)
        val preferences = appPreferences()
        drawEnabled = preferences.getBoolean(KEY_DRAW_ENABLED, true)
        pauseSuggestionsEnabled = preferences.getBoolean(KEY_PAUSE_ENABLED, true)
        pauseAfterMinutes = preferences.getInt(KEY_PAUSE_MINUTES, 25)
        enableEdgeToEdge()
        setContent {
            ExecutiveFunctionTheme {
                val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
                val error by viewModel.error.collectAsStateWithLifecycle()
                val completionLeadMinutes by viewModel.completionLeadMinutes.collectAsStateWithLifecycle()

                LaunchedEffect(snapshot.activeFocus?.session?.status) {
                    if (snapshot.activeFocus?.session?.status != FocusStatus.RUNNING && overlayEnabled) {
                        updateOverlayEnabled(false)
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                    ) {
                        when {
                            snapshot.loading -> CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                            )

                            snapshot.activeFocus?.session?.status == FocusStatus.RUNNING -> FocusScreen(
                                activeFocus = requireNotNull(snapshot.activeFocus),
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
                                drawEnabled = drawEnabled,
                                pauseSuggestionsEnabled = pauseSuggestionsEnabled,
                                pauseAfterMinutes = pauseAfterMinutes,
                                onCapture = viewModel::capture,
                                onStart = viewModel::start,
                                onMoveTask = viewModel::moveTask,
                                onSetTaskColor = viewModel::setTaskColor,
                                onSetDrawEnabled = ::setDrawEnabled,
                                onSetPauseSuggestionsEnabled = ::setPauseSuggestionsEnabled,
                                onSetPauseAfterMinutes = ::setPauseAfterMinutes,
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

    override fun onResume() {
        super.onResume()
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

    private fun setDrawEnabled(enabled: Boolean) {
        drawEnabled = enabled
        appPreferences().edit().putBoolean(KEY_DRAW_ENABLED, enabled).apply()
    }

    private fun setPauseSuggestionsEnabled(enabled: Boolean) {
        pauseSuggestionsEnabled = enabled
        appPreferences().edit().putBoolean(KEY_PAUSE_ENABLED, enabled).apply()
    }

    private fun setPauseAfterMinutes(minutes: Int) {
        pauseAfterMinutes = minutes.coerceIn(5, 120)
        appPreferences().edit().putInt(KEY_PAUSE_MINUTES, pauseAfterMinutes).apply()
    }

    private fun appPreferences() = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    private companion object {
        const val PREFERENCES = "app_preferences"
        const val KEY_DRAW_ENABLED = "draw_enabled"
        const val KEY_PAUSE_ENABLED = "pause_suggestions_enabled"
        const val KEY_PAUSE_MINUTES = "pause_after_minutes"
    }
}
