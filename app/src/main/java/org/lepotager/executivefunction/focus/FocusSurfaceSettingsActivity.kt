package org.lepotager.executivefunction.focus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.lepotager.executivefunction.R
import org.lepotager.executivefunction.ui.theme.ExecutiveFunctionTheme

/**
 * Small standalone settings surface for experimental persistence features.
 * Defaults remain conservative; this exists mainly so every prepared option can
 * be exercised on a real device without wiring a full settings architecture yet.
 */
class FocusSurfaceSettingsActivity : ComponentActivity() {
    private val preferences by lazy { FocusSurfacePreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExecutiveFunctionTheme {
                var settings by remember { mutableStateOf(preferences.load()) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Text(
                        text = stringResource(R.string.focus_surface_settings_title),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = stringResource(R.string.focus_surface_settings_explanation),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    FocusSurfaceToggle(
                        title = stringResource(R.string.focus_surface_keep_screen_on),
                        description = stringResource(R.string.focus_surface_keep_screen_on_detail),
                        checked = settings.keepScreenOn,
                        onCheckedChange = { enabled ->
                            preferences.setKeepScreenOn(enabled)
                            settings = settings.copy(keepScreenOn = enabled)
                        },
                    )
                    FocusSurfaceToggle(
                        title = stringResource(R.string.focus_surface_auto_pip),
                        description = stringResource(R.string.focus_surface_auto_pip_detail),
                        checked = settings.autoEnterPictureInPicture,
                        onCheckedChange = { enabled ->
                            preferences.setAutoEnterPictureInPicture(enabled)
                            settings = settings.copy(autoEnterPictureInPicture = enabled)
                        },
                    )
                    FocusSurfaceToggle(
                        title = stringResource(R.string.focus_surface_notification_actions),
                        description = stringResource(R.string.focus_surface_notification_actions_detail),
                        checked = settings.notificationActions,
                        onCheckedChange = { enabled ->
                            preferences.setNotificationActions(enabled)
                            settings = settings.copy(notificationActions = enabled)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusSurfaceToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
