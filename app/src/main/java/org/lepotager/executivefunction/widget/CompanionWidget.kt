package org.lepotager.executivefunction.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.AppWidgetId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lepotager.executivefunction.MainActivity
import org.lepotager.executivefunction.R
import org.lepotager.executivefunction.data.AppDatabase

/**
 * Static home-screen presence for the companion.
 *
 * No animation and no aNeko-style overlay are used. Until the illustrator's
 * official assets arrive, the widget only reserves an empty artwork area.
 */
class CompanionWidget : GlanceAppWidget() {
    companion object {
        val SMALL = DpSize(56.dp, 56.dp)
        val MEDIUM = DpSize(180.dp, 110.dp)

        const val EXTRA_WIDGET_ACTION = "companion_widget_action"
        const val ACTION_OPEN = "open"
        const val ACTION_RESUME = "resume"
        const val ACTION_CAPTURE = "capture"
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SMALL, MEDIUM))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = (id as? AppWidgetId)?.appWidgetId
        val preferences = CompanionWidgetPreferences.load(context, appWidgetId)
        val state = withContext(Dispatchers.IO) {
            val database = AppDatabase(context.applicationContext)
            CompanionWidgetStateFactory.create(
                activeFocus = database.activeFocus(),
                openTasks = database.openTasks(),
                dayOfYear = LocalDate.now().dayOfYear,
            )
        }
        provideContent { CompanionWidgetContent(state, preferences) }
    }
}

private val WidgetSurface = ColorProvider(R.color.widget_surface)
private val WidgetText = ColorProvider(R.color.widget_text)
private val WidgetMutedText = ColorProvider(R.color.widget_muted_text)

@Composable
private fun CompanionWidgetContent(
    state: CompanionWidgetState,
    preferences: CompanionWidgetPreferencesData,
) {
    val context = LocalContext.current
    val size = LocalSize.current
    val isSmall = size.width < 130.dp || size.height < 96.dp

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(WidgetSurface)
            .cornerRadius(20.dp)
            .padding(if (isSmall) 6.dp else 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isSmall) {
            SmallCompanion(context, preferences.compactTapAction)
        } else {
            MediumCompanion(context, state, preferences.showTaskContext)
        }
    }
}

@Composable
private fun SmallCompanion(context: Context, tapAction: CompanionTapAction) {
    // Empty reserved slot: the official static illustration will fill this area later.
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionStartActivity(widgetIntent(context, tapAction.intentAction()))),
        contentAlignment = Alignment.Center,
    ) {
        Spacer(
            modifier = GlanceModifier
                .width(44.dp)
                .height(44.dp),
        )
    }
}

@Composable
private fun MediumCompanion(
    context: Context,
    state: CompanionWidgetState,
    showTaskContext: Boolean,
) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            // Reserved official-artwork slot. Intentionally blank during development.
            Spacer(
                modifier = GlanceModifier
                    .width(48.dp)
                    .height(48.dp),
            )
            Spacer(GlanceModifier.width(10.dp))
            Column(modifier = GlanceModifier.width(108.dp)) {
                Text(
                    text = context.getString(R.string.companion_widget_title),
                    style = TextStyle(
                        color = WidgetText,
                        fontWeight = FontWeight.Bold,
                    ),
                    maxLines = 1,
                )
                if (showTaskContext) {
                    Spacer(GlanceModifier.height(3.dp))
                    Text(
                        text = statusText(context, state),
                        style = TextStyle(color = WidgetMutedText),
                        maxLines = 2,
                    )
                }
            }
        }

        Spacer(GlanceModifier.height(8.dp))

        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Button(
                text = primaryActionText(context, state.status),
                onClick = actionStartActivity(
                    widgetIntent(
                        context,
                        if (state.status == CompanionWidgetStatus.RESUMABLE) {
                            CompanionWidget.ACTION_RESUME
                        } else {
                            CompanionWidget.ACTION_OPEN
                        },
                    ),
                ),
                modifier = GlanceModifier.width(82.dp),
            )
            Spacer(GlanceModifier.width(8.dp))
            Button(
                text = context.getString(R.string.companion_widget_capture),
                onClick = actionStartActivity(widgetIntent(context, CompanionWidget.ACTION_CAPTURE)),
                modifier = GlanceModifier.width(70.dp),
            )
        }
    }
}

private fun CompanionTapAction.intentAction(): String = when (this) {
    CompanionTapAction.OPEN -> CompanionWidget.ACTION_OPEN
    CompanionTapAction.RESUME -> CompanionWidget.ACTION_RESUME
    CompanionTapAction.CAPTURE -> CompanionWidget.ACTION_CAPTURE
}

private fun statusText(context: Context, state: CompanionWidgetState): String = when (state.status) {
    CompanionWidgetStatus.FOCUSING -> state.taskTitle?.let {
        context.getString(R.string.companion_widget_status_focusing, it)
    } ?: context.getString(R.string.companion_widget_status_quiet)

    CompanionWidgetStatus.RESUMABLE -> state.taskTitle?.let {
        context.getString(R.string.companion_widget_status_resumable, it)
    } ?: context.getString(R.string.companion_widget_status_quiet)

    CompanionWidgetStatus.READY -> state.taskTitle?.let {
        context.getString(R.string.companion_widget_status_ready, it)
    } ?: context.getString(R.string.companion_widget_status_quiet)

    CompanionWidgetStatus.QUIET -> context.getString(R.string.companion_widget_status_quiet)
}

private fun primaryActionText(context: Context, status: CompanionWidgetStatus): String = when (status) {
    CompanionWidgetStatus.RESUMABLE -> context.getString(R.string.companion_widget_resume)
    CompanionWidgetStatus.FOCUSING,
    CompanionWidgetStatus.READY,
    CompanionWidgetStatus.QUIET,
    -> context.getString(R.string.companion_widget_open)
}

private fun widgetIntent(context: Context, action: String): Intent =
    Intent(context, MainActivity::class.java)
        .putExtra(CompanionWidget.EXTRA_WIDGET_ACTION, action)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

suspend fun updateCompanionWidgets(context: Context) {
    CompanionWidget().updateAll(context.applicationContext)
}
