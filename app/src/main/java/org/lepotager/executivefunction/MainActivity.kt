package org.lepotager.executivefunction

import android.Manifest
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.lepotager.executivefunction.focus.FocusSurfacePreferences
import org.lepotager.executivefunction.model.FocusStatus
import org.lepotager.executivefunction.navigation.ExternalLaunchAction
import org.lepotager.executivefunction.ui.ErrorDialog
import org.lepotager.executivefunction.ui.FocusScreen
import org.lepotager.executivefunction.ui.HomeScreen
import org.lepotager.executivefunction.ui.PipFocusScreen
import org.lepotager.executivefunction.ui.QuickCaptureDialog
import org.lepotager.executivefunction.ui.ResumeScreen
import org.lepotager.executivefunction.ui.theme.ExecutiveFunctionTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()
    private val focusSurfacePreferences by lazy { FocusSurfacePreferences(this) }
    private var externalLaunchAction by mutableStateOf<String?>(null)
    private var captureInitialText by mutableStateOf("")
    private var inPictureInPicture by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resolveLaunchIntent(intent)
        enableEdgeToEdge()
        setContent {
            ExecutiveFunctionTheme {
                val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
                val error by viewModel.error.collectAsStateWithLifecycle()
                val activeFocus = snapshot.activeFocus
                val running = activeFocus?.session?.status == FocusStatus.RUNNING
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission(),
                ) { granted ->
                    // Focus itself never depends on this permission. If it is granted
                    // after the session has already started, surface that session now.
                    if (granted) {
                        viewModel.refreshFocusPresence()
                    }
                }

                LaunchedEffect(running) {
                    applyKeepScreenOn(running)
                }

                LaunchedEffect(externalLaunchAction, running) {
                    if (externalLaunchAction == ExternalLaunchAction.PICTURE_IN_PICTURE) {
                        if (running) {
                            enterFocusPictureInPicture()
                        }
                        externalLaunchAction = null
                    }
                }

                if (inPictureInPicture && running) {
                    PipFocusScreen(activeFocus = requireNotNull(activeFocus))
                } else {
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

                                running -> FocusScreen(
                                    activeFocus = requireNotNull(activeFocus),
                                    onQuickCapture = viewModel::capture,
                                    onInterrupt = viewModel::interrupt,
                                    onComplete = viewModel::complete,
                                )

                                activeFocus?.session?.status == FocusStatus.INTERRUPTED -> ResumeScreen(
                                    activeFocus = requireNotNull(activeFocus),
                                    onResume = viewModel::resume,
                                    onPostpone = viewModel::postpone,
                                )

                                else -> HomeScreen(
                                    tasks = snapshot.tasks,
                                    onCapture = viewModel::capture,
                                    onStart = { taskId ->
                                        if (
                                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                            ContextCompat.checkSelfPermission(
                                                this@MainActivity,
                                                Manifest.permission.POST_NOTIFICATIONS,
                                            ) != PackageManager.PERMISSION_GRANTED
                                        ) {
                                            notificationPermissionLauncher.launch(
                                                Manifest.permission.POST_NOTIFICATIONS,
                                            )
                                        }
                                        viewModel.start(taskId)
                                    },
                                )
                            }
                        }
                    }

                    if (
                        externalLaunchAction == ExternalLaunchAction.CAPTURE &&
                        !snapshot.loading
                    ) {
                        QuickCaptureDialog(
                            initialValue = captureInitialText,
                            onDismiss = ::clearCaptureLaunch,
                            onCapture = { title ->
                                viewModel.capture(title, null) {
                                    clearCaptureLaunch()
                                }
                            },
                        )
                    }
                }

                if (error != null) {
                    ErrorDialog(onDismiss = viewModel::clearError)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Notification actions may mutate the database while this Activity is away.
        viewModel.reload()
        val running = viewModel.snapshot.value.activeFocus?.session?.status == FocusStatus.RUNNING
        applyKeepScreenOn(running)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        resolveLaunchIntent(intent)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val running = viewModel.snapshot.value.activeFocus?.session?.status == FocusStatus.RUNNING
        if (running && focusSurfacePreferences.load().autoEnterPictureInPicture) {
            enterFocusPictureInPicture()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        inPictureInPicture = isInPictureInPictureMode
    }

    private fun enterFocusPictureInPicture() {
        if (viewModel.snapshot.value.activeFocus?.session?.status != FocusStatus.RUNNING) {
            return
        }
        runCatching {
            enterPictureInPictureMode(
                PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(1, 1))
                    .build(),
            )
        }
    }

    private fun applyKeepScreenOn(running: Boolean) {
        val keepScreenOn = running && focusSurfacePreferences.load().keepScreenOn
        if (keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun resolveLaunchIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            captureInitialText = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)
                ?.toString()
                ?.trim()
                .orEmpty()
            externalLaunchAction = ExternalLaunchAction.CAPTURE
            return
        }

        captureInitialText = ""
        externalLaunchAction = intent.getStringExtra(ExternalLaunchAction.EXTRA)
    }

    private fun clearCaptureLaunch() {
        externalLaunchAction = null
        captureInitialText = ""
    }
}
