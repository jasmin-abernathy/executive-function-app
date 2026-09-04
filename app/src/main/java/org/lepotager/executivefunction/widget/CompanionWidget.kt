package org.lepotager.executivefunction.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.defaultWeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lepotager.executivefunction.MainActivity
import org.lepotager.executivefunction.R
import org.lepotager.executivefunction.data.AppDatabase
import java.time.LocalDate

/**
 * Static home-screen presence for the companion.
 *
 * No animation and no aNeko-style overlay are used. Until the illustrator's
 * official assets arrive, the widget only reserves a neutral artwork area.
 */
class CompanionWidget : GlanceAppWidget() {
    companion object {
        val SMALL = DpSize(72.dp, 72.dp)
        val MEDIUM = DpSize(180.dp, 110.dp)

        const val EXTRA_WIDGET_ACTION = "companion_widget_action"
        const val ACTION_OPEN = "open"
        const val ACTION_RESUME = "resume"
        const val ACTION_CAPTURE = "capture"
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SMALL, MEDIUM))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = withContext(Dispatchers.IO) {
            val database = AppDatabase(context.applicationContext)
            CompanionWidgetStateFactory.create(
                activeFocus = database.activeFocus(),
                openTasks = database.openTasks(),
                dayOfYear = LocalDate.now().dayOfYear,
            )
        }
        provideContent { CompanionWidgetContent(state) }
    }
}

private val WidgetSurface = ColorProvider(
    day = Color(0xFFF6FBF7),
    night = Color(0xFF252E29),
)
private val WidgetText = ColorProvider(
    day = Color(0xFF2D2D2D),
    night = Color(0xFFEEF3EF),
)
private val WidgetMutedText = ColorProvider(
    day = Color(0xFF59655E),
    night = Color(0xFFC8D2CB),
)

@Composable
private fun CompanionWidgetContent(state: CompanionWidgetState) {
    val context = LocalContext.current
    val size = LocalSize.current
    val isSmall = size.width < 130.dp || size.height < 96.dp

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(WidgetSurface)
            .cornerRadius(20.dp)
            .padding(if (isSmall) 7.dp else 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isSmall) {
            SmallCompanion(
                context = context,
                openIntent = widgetIntent(context, CompanionWidget.ACTION_OPEN),
            )
        } else {
            MediumCompanion(context, state)
        }
    }
}

@Composable
private fun SmallCompanion(context: Context, openIntent: Intent) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionStartActivity(openIntent)),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        // Neutral technical placeholder: no temporary companion design.
        Text(
            text = context.getString(R.string.companion_widget_art_pending_short),
            style = TextStyle(
                color = WidgetMutedText,
                fontWeight = FontWeight.Medium,
            ),
            maxLines = 2,
        )
    }
}

@Composable
private fun MediumCompanion(context: Context, state: CompanionWidgetState) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            // This column becomes the official static artwork area later.
            Column(
                modifier = GlanceModifier.width(72.dp),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            ) {
                Text(
                    text = context.getString(R.string.companion_widget_art_pending),
                    style = TextStyle(color = WidgetMutedText),
                    maxLines = 2,
                )
            }

            Spacer(GlanceModifier.width(10.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = context.getString(R.string.companion_widget_title),
                    style = TextStyle(
                        color = WidgetText,
                        fontWeight = FontWeight.Bold,
                    ),
                    maxLines = 1,
                )
                Spacer(GlanceModifier.height(3.dp))
                Text(
                    text = statusText(context, state),
                    style = TextStyle(color = WidgetMutedText),
                    maxLines = 2,
                )
            }
        }

        Spacer(GlanceModifier.defaultWeight())

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
                modifier = GlanceModifier.defaultWeight(),
            )
            Spacer(GlanceModifier.width(8.dp))
            Button(
                text = context.getString(R.string.companion_widget_capture),
                onClick = actionStartActivity(widgetIntent(context, CompanionWidget.ACTION_CAPTURE)),
                modifier = GlanceModifier.defaultWeight(),
            )
        }
    }
}

private fun statusText(context: Context, state: CompanionWidgetState): String = when (state.status) {
    CompanionWidgetStatus.FOCUSING -> context.getString(
        R.string.companion_widget_status_focusing,
        state.taskTitle.orEmpty(),
    )
    CompanionWidgetStatus.RESUMABLE -> context.getString(
        R.string.companion_widget_status_resumable,
        state.taskTitle.orEmpty(),
    )
    CompanionWidgetStatus.READY -> context.getString(
        R.string.companion_widget_status_ready,
        state.taskTitle.orEmpty(),
    )
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
