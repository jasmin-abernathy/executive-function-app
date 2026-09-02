package org.lepotager.executivefunction.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.lepotager.executivefunction.R

/**
 * Behavioural states are intentionally small and replaceable. The illustrator's
 * final transparent assets can take over [CompanionFigure] without changing the
 * product logic or the screen layouts.
 */
internal enum class CompanionMood {
    Idle,
    Listening,
    Safekeeping,
    WelcomeBack,
}

@Composable
internal fun CompanionPanel(
    mood: CompanionMood,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val interactionLabel = stringResourceCompat(R.string.companion_interaction_description)
    val interactiveModifier = if (onClick == null) {
        modifier
    } else {
        modifier.clickable(
            role = Role.Button,
            onClickLabel = interactionLabel,
            onClick = onClick,
        )
    }

    Surface(
        modifier = interactiveModifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 30.dp,
            topEnd = 18.dp,
            bottomEnd = 30.dp,
            bottomStart = 20.dp,
        ),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            CompanionFigure(
                mood = mood,
                modifier = Modifier.size(88.dp),
            )
            Spacer(Modifier.width(4.dp))
            val copy = companionCopy(mood)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = copy.first,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = copy.second,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(16.dp))
        }
    }
}

@Composable
internal fun CompanionFigure(
    mood: CompanionMood,
    modifier: Modifier = Modifier,
) {
    val tilt by animateFloatAsState(
        targetValue = when (mood) {
            CompanionMood.Listening -> -6f
            CompanionMood.Safekeeping -> 4f
            CompanionMood.WelcomeBack -> -2f
            CompanionMood.Idle -> 0f
        },
        label = "companion-tilt",
    )
    val bodyColor = MaterialTheme.colorScheme.onSurface
    val detailColor = MaterialTheme.colorScheme.primaryContainer
    val outlineColor = MaterialTheme.colorScheme.outline

    Canvas(
        modifier = modifier.graphicsLayer {
            rotationZ = tilt
            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.85f)
        },
    ) {
        val width = size.width
        val height = size.height

        drawCircle(
            color = detailColor,
            radius = width * 0.39f,
            center = Offset(width * 0.5f, height * 0.52f),
        )

        val body = Path().apply {
            moveTo(width * 0.24f, height * 0.38f)
            lineTo(width * 0.18f, height * 0.14f)
            lineTo(width * 0.37f, height * 0.27f)
            cubicTo(
                width * 0.43f,
                height * 0.19f,
                width * 0.57f,
                height * 0.19f,
                width * 0.64f,
                height * 0.27f,
            )
            lineTo(width * 0.82f, height * 0.14f)
            lineTo(width * 0.76f, height * 0.39f)
            cubicTo(
                width * 0.86f,
                height * 0.51f,
                width * 0.82f,
                height * 0.72f,
                width * 0.72f,
                height * 0.79f,
            )
            cubicTo(
                width * 0.66f,
                height * 0.85f,
                width * 0.61f,
                height * 0.77f,
                width * 0.56f,
                height * 0.82f,
            )
            cubicTo(
                width * 0.51f,
                height * 0.88f,
                width * 0.46f,
                height * 0.78f,
                width * 0.40f,
                height * 0.82f,
            )
            cubicTo(
                width * 0.32f,
                height * 0.87f,
                width * 0.19f,
                height * 0.74f,
                width * 0.20f,
                height * 0.57f,
            )
            cubicTo(
                width * 0.20f,
                height * 0.49f,
                width * 0.21f,
                height * 0.43f,
                width * 0.24f,
                height * 0.38f,
            )
            close()
        }
        drawPath(body, bodyColor)

        val eyeY = height * 0.48f
        if (mood == CompanionMood.WelcomeBack) {
            drawLine(
                color = detailColor,
                start = Offset(width * 0.38f, eyeY),
                end = Offset(width * 0.45f, eyeY),
                strokeWidth = width * 0.035f,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = detailColor,
                start = Offset(width * 0.56f, eyeY),
                end = Offset(width * 0.63f, eyeY),
                strokeWidth = width * 0.035f,
                cap = StrokeCap.Round,
            )
        } else {
            val eyeRadius = if (mood == CompanionMood.Listening) width * 0.042f else width * 0.034f
            drawCircle(detailColor, eyeRadius, Offset(width * 0.41f, eyeY))
            drawCircle(detailColor, eyeRadius, Offset(width * 0.60f, eyeY))
        }

        drawCircle(
            color = outlineColor,
            radius = width * 0.055f,
            center = Offset(width * 0.51f, height * 0.59f),
            style = Stroke(width = width * 0.018f),
        )
    }
}

@Composable
private fun companionCopy(mood: CompanionMood): Pair<String, String> = when (mood) {
    CompanionMood.Idle -> stringResourceCompat(R.string.companion_idle_title) to
        stringResourceCompat(R.string.companion_idle_body)
    CompanionMood.Listening -> stringResourceCompat(R.string.companion_listening_title) to
        stringResourceCompat(R.string.companion_listening_body)
    CompanionMood.Safekeeping -> stringResourceCompat(R.string.companion_saved_title) to
        stringResourceCompat(R.string.companion_saved_body)
    CompanionMood.WelcomeBack -> stringResourceCompat(R.string.companion_welcome_title) to
        stringResourceCompat(R.string.companion_welcome_body)
}

@Composable
private fun stringResourceCompat(id: Int): String = androidx.compose.ui.res.stringResource(id)
