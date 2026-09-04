package org.lepotager.executivefunction.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
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
import org.lepotager.executivefunction.MainActivity

/**
 * Static home-screen presence for the companion.
 *
 * The widget intentionally contains no animation. Artwork is represented by a
 * neutral development slot until the illustrator's production assets arrive.
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
        provideContent {
            CompanionWidgetContent()
        }
    }
}

@Composable
private fun CompanionWidgetContent() {
    val context = LocalContext.current
    val size = LocalSize.current
    val isSmall = size.width < 130.dp || size.height < 96.dp

    val openIntent = widgetIntent(context, CompanionWidget.ACTION_OPEN)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(ColorProvider(Color(0xFFF7F7F2)))
            .cornerRadius(20.dp)
            .padding(if (isSmall) 8.dp else 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isSmall) {
            SmallCompanion(openIntent)
        } else {
            MediumCompanion(context)
        }
    }
}

@Composable
private fun SmallCompanion(openIntent: Intent) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        // Artwork slot only. Do not replace with temporary generated character art.
        Text(
            text = "•••",
            style = TextStyle(
                color = ColorProvider(Color(0xFF5D665F)),
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(GlanceModifier.height(4.dp))
        androidx.glance.Button(
            text = "Ouvrir",
            onClick = actionStartActivity(openIntent),
        )
    }
}

@Composable
private fun MediumCompanion(context: Context) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "Ton compagnon est là",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF303A34)),
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(GlanceModifier.height(3.dp))
                Text(
                    text = "Illustration statique à venir",
                    style = TextStyle(color = ColorProvider(Color(0xFF667068))),
                )
            }
            Spacer(GlanceModifier.width(8.dp))
            Text(
                text = "•••",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF5D665F)),
                    fontWeight = FontWeight.Bold,
                ),
            )
        }

        Spacer(GlanceModifier.defaultWeight())

        Row(modifier = GlanceModifier.fillMaxWidth()) {
            androidx.glance.Button(
                text = "Reprendre",
                onClick = actionStartActivity(widgetIntent(context, CompanionWidget.ACTION_RESUME)),
                modifier = GlanceModifier.defaultWeight(),
            )
            Spacer(GlanceModifier.width(8.dp))
            androidx.glance.Button(
                text = "Noter",
                onClick = actionStartActivity(widgetIntent(context, CompanionWidget.ACTION_CAPTURE)),
                modifier = GlanceModifier.defaultWeight(),
            )
        }
    }
}

private fun widgetIntent(context: Context, action: String): Intent =
    Intent(context, MainActivity::class.java)
        .putExtra(CompanionWidget.EXTRA_WIDGET_ACTION, action)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
