package org.lepotager.executivefunction.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.lepotager.executivefunction.R
import org.lepotager.executivefunction.ui.theme.ExecutiveFunctionTheme

/** Optional Android AppWidget configuration. No companion artwork is rendered here. */
class CompanionWidgetConfigureActivity : ComponentActivity() {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        )
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            ExecutiveFunctionTheme {
                val scope = rememberCoroutineScope()
                var settings by remember {
                    mutableStateOf(CompanionWidgetPreferences.load(this, appWidgetId))
                }

                CompanionWidgetConfiguration(
                    settings = settings,
                    onSettingsChange = { settings = it },
                    onSave = {
                        CompanionWidgetPreferences.save(this, appWidgetId, settings)
                        scope.launch {
                            updateCompanionWidgets(this@CompanionWidgetConfigureActivity)
                            val result = Intent().putExtra(
                                AppWidgetManager.EXTRA_APPWIDGET_ID,
                                appWidgetId,
                            )
                            setResult(RESULT_OK, result)
                            finish()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun CompanionWidgetConfiguration(
    settings: CompanionWidgetPreferencesData,
    onSettingsChange: (CompanionWidgetPreferencesData) -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = stringResource(R.string.companion_widget_config_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.companion_widget_config_explanation),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = stringResource(R.string.companion_widget_config_tap_title),
            style = MaterialTheme.typography.titleMedium,
        )
        CompanionTapAction.entries.forEach { action ->
            ChoiceRow(
                label = stringResource(action.labelRes()),
                selected = settings.compactTapAction == action,
                onSelect = {
                    onSettingsChange(settings.copy(compactTapAction = action))
                },
            )
        }

        SwitchRow(
            title = stringResource(R.string.companion_widget_config_context),
            checked = settings.showTaskContext,
            onCheckedChange = {
                onSettingsChange(settings.copy(showTaskContext = it))
            },
        )
        SwitchRow(
            title = stringResource(R.string.companion_widget_config_buttons),
            checked = settings.showButtons,
            onCheckedChange = {
                onSettingsChange(settings.copy(showButtons = it))
            },
        )

        Text(
            text = stringResource(R.string.companion_widget_config_scene_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.companion_widget_config_scene_explanation),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CompanionScenePreference.entries.forEach { scene ->
            ChoiceRow(
                label = stringResource(scene.labelRes()),
                selected = settings.scenePreference == scene,
                onSelect = {
                    onSettingsChange(settings.copy(scenePreference = scene))
                },
            )
        }

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.companion_widget_config_save))
        }
    }
}

@Composable
private fun ChoiceRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun CompanionTapAction.labelRes(): Int = when (this) {
    CompanionTapAction.OPEN -> R.string.companion_widget_config_tap_open
    CompanionTapAction.RESUME -> R.string.companion_widget_config_tap_resume
    CompanionTapAction.CAPTURE -> R.string.companion_widget_config_tap_capture
}

private fun CompanionScenePreference.labelRes(): Int = when (this) {
    CompanionScenePreference.AUTO -> R.string.companion_widget_config_scene_auto
    CompanionScenePreference.RESTING -> R.string.companion_widget_config_scene_resting
    CompanionScenePreference.CRAFTING -> R.string.companion_widget_config_scene_crafting
    CompanionScenePreference.SEWING -> R.string.companion_widget_config_scene_sewing
    CompanionScenePreference.OBSERVING -> R.string.companion_widget_config_scene_observing
}
