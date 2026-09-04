package org.lepotager.executivefunction

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.lepotager.executivefunction.model.FocusStatus
import org.lepotager.executivefunction.ui.ErrorDialog
import org.lepotager.executivefunction.ui.FocusScreen
import org.lepotager.executivefunction.ui.HomeScreen
import org.lepotager.executivefunction.ui.ResumeScreen
import org.lepotager.executivefunction.ui.theme.ExecutiveFunctionTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExecutiveFunctionTheme {
                val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
                val error by viewModel.error.collectAsStateWithLifecycle()
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission(),
                ) {
                    // Focus itself never depends on the permission result.
                    // If denied, the in-app timer remains fully usable.
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

                if (error != null) {
                    ErrorDialog(onDismiss = viewModel::clearError)
                }
            }
        }
    }
}
