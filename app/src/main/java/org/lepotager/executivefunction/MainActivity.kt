package org.lepotager.executivefunction

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.lepotager.executivefunction.model.FocusStatus
import org.lepotager.executivefunction.ui.ErrorDialog
import org.lepotager.executivefunction.ui.FocusScreen
import org.lepotager.executivefunction.ui.HomeScreen
import org.lepotager.executivefunction.ui.ResumeScreen
import org.lepotager.executivefunction.ui.WidgetCaptureDialog
import org.lepotager.executivefunction.ui.theme.ExecutiveFunctionTheme
import org.lepotager.executivefunction.widget.CompanionWidget

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()
    private var widgetAction by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widgetAction = intent.widgetAction()
        enableEdgeToEdge()

        setContent {
            ExecutiveFunctionTheme {
                val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
                val error by viewModel.error.collectAsStateWithLifecycle()

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
                                onQuickCapture = viewModel::capture,
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
                                onCapture = viewModel::capture,
                                onStart = viewModel::start,
                            )
                        }
                    }
                }

                if (widgetAction == CompanionWidget.ACTION_CAPTURE && !snapshot.loading) {
                    WidgetCaptureDialog(
                        onDismiss = { widgetAction = null },
                        onCapture = { title ->
                            viewModel.capture(title, null) {
                                widgetAction = null
                            }
                        },
                    )
                }

                if (error != null) {
                    ErrorDialog(onDismiss = viewModel::clearError)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        widgetAction = intent.widgetAction()
    }

    private fun Intent.widgetAction(): String? = getStringExtra(CompanionWidget.EXTRA_WIDGET_ACTION)
}
