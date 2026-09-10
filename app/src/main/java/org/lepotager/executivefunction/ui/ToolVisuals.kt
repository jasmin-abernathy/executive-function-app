package org.lepotager.executivefunction.ui

import android.animation.ValueAnimator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * Functional marks only. They do not define the companion, its objects,
 * the logo or the future illustration language.
 */
internal enum class ToolGlyphKind {
    CAPTURE,
    DRAW,
    START,
    CLOCK,
    PAUSE,
    COMPLETE,
    REDUCE,
    POSTPONE,
}

@Composable
internal fun ToolBadge(
    kind: ToolGlyphKind,
    modifier: Modifier = Modifier,
    dieFace: Int = 5,
) {
    Surface(
        modifier = modifier.size(44.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Box(contentAlignment = Alignment.Center) {
            ToolGlyph(kind = kind, dieFace = dieFace)
        }
    }
}

@Composable
internal fun ToolGlyph(
    kind: ToolGlyphKind,
    modifier: Modifier = Modifier,
    dieFace: Int = 5,
    glyphSize: Dp = 22.dp,
) {
    val color = LocalContentColor.current
    Canvas(modifier = modifier.size(glyphSize)) {
        val strokeWidth = 2.dp.toPx()
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        val left = size.width * 0.18f
        val right = size.width * 0.82f
        val top = size.height * 0.18f
        val bottom = size.height * 0.82f
        val center = Offset(size.width / 2f, size.height / 2f)

        when (kind) {
            ToolGlyphKind.CAPTURE -> {
                drawLine(color, Offset(center.x, top), Offset(center.x, bottom), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(left, center.y), Offset(right, center.y), strokeWidth, StrokeCap.Round)
            }
            ToolGlyphKind.DRAW -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    cornerRadius = CornerRadius(size.minDimension * 0.12f),
                    style = stroke,
                )
                diceDots(dieFace.coerceIn(1, 6)).forEach { dot ->
                    drawCircle(
                        color = color,
                        radius = size.minDimension * 0.045f,
                        center = Offset(
                            left + (right - left) * dot.x,
                            top + (bottom - top) * dot.y,
                        ),
                    )
                }
            }
            ToolGlyphKind.START -> {
                val path = Path().apply {
                    moveTo(size.width * 0.34f, top)
                    lineTo(right, center.y)
                    lineTo(size.width * 0.34f, bottom)
                    close()
                }
                drawPath(path = path, color = color, style = stroke)
            }
            ToolGlyphKind.CLOCK, ToolGlyphKind.POSTPONE -> {
                drawCircle(color, size.minDimension * 0.32f, center, style = stroke)
                drawLine(color, center, Offset(center.x, size.height * 0.31f), strokeWidth, StrokeCap.Round)
                drawLine(color, center, Offset(size.width * 0.66f, size.height * 0.60f), strokeWidth, StrokeCap.Round)
                if (kind == ToolGlyphKind.POSTPONE) {
                    drawLine(color, Offset(left, top), Offset(size.width * 0.36f, top), strokeWidth, StrokeCap.Round)
                    drawLine(color, Offset(left, top), Offset(left, size.height * 0.36f), strokeWidth, StrokeCap.Round)
                }
            }
            ToolGlyphKind.PAUSE -> {
                drawLine(color, Offset(size.width * 0.39f, top), Offset(size.width * 0.39f, bottom), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.61f, top), Offset(size.width * 0.61f, bottom), strokeWidth, StrokeCap.Round)
            }
            ToolGlyphKind.COMPLETE -> {
                val path = Path().apply {
                    moveTo(left, size.height * 0.52f)
                    lineTo(size.width * 0.43f, bottom)
                    lineTo(right, top)
                }
                drawPath(path = path, color = color, style = stroke)
            }
            ToolGlyphKind.REDUCE -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    cornerRadius = CornerRadius(size.minDimension * 0.08f),
                    style = stroke,
                )
                drawLine(color, Offset(size.width * 0.31f, center.y), Offset(size.width * 0.69f, center.y), strokeWidth, StrokeCap.Round)
            }
        }
    }
}

/** A functional die animation; it deliberately does not define illustration or companion style. */
@Composable
internal fun AnimatedDieBadge(
    rollKey: Int,
    finalFace: Int,
    description: String,
    onRollFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(1f) }
    val haptic = LocalHapticFeedback.current
    val latestOnFinished by rememberUpdatedState(onRollFinished)
    val animationsEnabled = remember { ValueAnimator.areAnimatorsEnabled() }

    LaunchedEffect(rollKey) {
        if (rollKey <= 0) return@LaunchedEffect
        progress.snapTo(0f)
        if (animationsEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
            )
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } else {
            progress.snapTo(1f)
        }
        latestOnFinished()
    }

    val fraction = progress.value
    val rolling = rollKey > 0 && fraction < 1f
    val shownFace = if (rolling) {
        (((fraction * 17).toInt() + rollKey) % 6) + 1
    } else {
        finalFace.coerceIn(1, 6)
    }
    val jump = if (rolling) sin(PI * fraction).toFloat() else 0f

    Surface(
        modifier = modifier
            .semantics { contentDescription = description }
            .graphicsLayer {
                rotationZ = if (rolling) 720f * fraction else 0f
                translationY = -32.dp.toPx() * jump
                val scale = 1f + 0.12f * jump
                scaleX = scale
                scaleY = scale
                shadowElevation = (4.dp + 10.dp * jump).toPx()
            }
            .size(72.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 4.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            ToolGlyph(
                kind = ToolGlyphKind.DRAW,
                dieFace = shownFace,
                glyphSize = 40.dp,
            )
        }
    }
}

internal fun stableDieFace(taskId: String): Int = ((taskId.hashCode() and Int.MAX_VALUE) % 6) + 1

private fun diceDots(face: Int): List<Offset> {
    val tl = Offset(0.27f, 0.27f)
    val tr = Offset(0.73f, 0.27f)
    val ml = Offset(0.27f, 0.50f)
    val c = Offset(0.50f, 0.50f)
    val mr = Offset(0.73f, 0.50f)
    val bl = Offset(0.27f, 0.73f)
    val br = Offset(0.73f, 0.73f)
    return when (face) {
        1 -> listOf(c)
        2 -> listOf(tl, br)
        3 -> listOf(tl, c, br)
        4 -> listOf(tl, tr, bl, br)
        5 -> listOf(tl, tr, c, bl, br)
        else -> listOf(tl, tr, ml, mr, bl, br)
    }
}
