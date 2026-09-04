package org.lepotager.executivefunction.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.lepotager.executivefunction.R
import org.lepotager.executivefunction.ui.theme.ExecutiveFunctionTheme

/**
 * Optional launcher configuration for the companion widget.
 * It only controls behavior and information density; no visual companion design
 * is selected here.
 */
class CompanionWidgetConfigActivity : ComponentActivity() {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private var selectedAction by mutableStateOf(CompanionTapAction.OPEN)
    private var showTaskContext by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        )
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val stored = CompanionWidgetPreferences.load(this, appWidgetId)
        selectedAction = stored.compactTapAction
        showTaskContext = stored.showTaskContext

        setContent {
            ExecutiveFunctionTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Text(
                        text = stringResource(R.string.widget_config_title),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = stringResource(R.string.widget_config_tap_label),
                        style = MaterialTheme.typography.titleMedium,
                    )

                    CompanionTapAction.entries.forEach { action ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = selectedAction == action,
                                onClick = { selectedAction = action },
                            )
                            Text(
                                text = stringResource(action.labelRes()),
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.widget_config_show_context),
                            modifier = Modifier.weight(1f),
                        )
                        Switch(
                            checked = showTaskContext,
                            onCheckedChange = { showTaskContext = it },
                        )
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = ::saveAndFinish,
                    ) {
                        Text(stringResource(R.string.widget_config_save))
                    }
                }
            }
        }
    }

    private fun saveAndFinish() {
        CompanionWidgetPreferences.save(
            context = this,
            appWidgetId = appWidgetId,
            value = CompanionWidgetPreferencesData(
                compactTapAction = selectedAction,
                showTaskContext = showTaskContext,
            ),
        )

        lifecycleScope.launch {
            try {
                updateCompanionWidgets(this@CompanionWidgetConfigActivity)
            } finally {
                val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setResult(Activity.RESULT_OK, result)
                finish()
            }
        }
    }

    private fun CompanionTapAction.labelRes(): Int = when (this) {
        CompanionTapAction.OPEN -> R.string.widget_config_action_open
        CompanionTapAction.RESUME -> R.string.widget_config_action_resume
        CompanionTapAction.CAPTURE -> R.string.widget_config_action_capture
    }
}
