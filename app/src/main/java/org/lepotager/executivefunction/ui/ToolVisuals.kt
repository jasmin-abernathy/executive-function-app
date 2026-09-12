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
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
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
    if (kind == ToolGlyphKind.DRAW) {
        D10Glyph(
            face = dieFace,
            modifier = modifier.size(glyphSize),
            fillColor = color.copy(alpha = 0.14f),
            outlineColor = color,
            numberSize = glyphSize * 0.38f,
        )
        return
    }
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
            ToolGlyphKind.DRAW -> Unit
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
    val duration = org.lepotager.executivefunction.domain.DieMotion.durationMillis(
        org.lepotager.executivefunction.ui.theme.LocalCalmMode.current, ValueAnimator.areAnimatorsEnabled(),
    )

    LaunchedEffect(rollKey) {
        if (rollKey <= 0) return@LaunchedEffect
        progress.snapTo(0f)
        if (duration > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing),
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
        ((fraction * 29).toInt() + rollKey) % 10
    } else finalFace.coerceIn(0, 9)
    val jump = if (rolling) sin(PI * fraction).toFloat() else 0f

    Box(
        modifier = modifier
            .semantics { contentDescription = description }
            .graphicsLayer {
                rotationZ = if (rolling) 720f * fraction else 0f
                rotationX = if (rolling) 24f * sin(PI * fraction * 2).toFloat() else 0f
                rotationY = if (rolling) 18f * sin(PI * fraction * 3).toFloat() else 0f
                translationY = -32.dp.toPx() * jump
                val scale = 1f + 0.12f * jump
                scaleX = scale
                scaleY = scale
                shadowElevation = (4.dp + 10.dp * jump).toPx()
            }
            .size(180.dp),
        contentAlignment = Alignment.Center,
    ) {
        D10Mesh(fraction, shownFace, Modifier.size(170.dp))
    }
}

internal fun stableDieFace(taskId: String): Int = (taskId.hashCode() and Int.MAX_VALUE) % 10

@Composable
private fun D10Glyph(
    face: Int,
    modifier: Modifier,
    fillColor: androidx.compose.ui.graphics.Color,
    outlineColor: androidx.compose.ui.graphics.Color,
    shadowColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Transparent,
    numberSize: Dp,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            fun polygon(points: List<Offset>) = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
                close()
            }
            val w = size.width
            val h = size.height
            val top = Offset(w * 0.50f, h * 0.05f)
            val upperRight = Offset(w * 0.82f, h * 0.29f)
            val right = Offset(w * 0.91f, h * 0.55f)
            val bottom = Offset(w * 0.50f, h * 0.95f)
            val left = Offset(w * 0.09f, h * 0.55f)
            val upperLeft = Offset(w * 0.18f, h * 0.29f)
            val centerLeft = Offset(w * 0.31f, h * 0.36f)
            val centerRight = Offset(w * 0.69f, h * 0.36f)
            val centerBottom = Offset(w * 0.50f, h * 0.83f)
            val outer = polygon(listOf(top, upperRight, right, bottom, left, upperLeft))
            if (shadowColor.alpha > 0f) {
                translate(top = h * 0.045f) { drawPath(outer, shadowColor) }
            }
            drawPath(outer, fillColor)
            drawPath(polygon(listOf(top, upperRight, centerRight, centerLeft, upperLeft)), androidx.compose.ui.graphics.Color.White.copy(alpha = 0.12f))
            drawPath(polygon(listOf(upperRight, right, bottom, centerBottom, centerRight)), outlineColor.copy(alpha = 0.13f))
            drawPath(polygon(listOf(left, upperLeft, centerLeft, centerBottom, bottom)), androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.10f))
            val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
            drawPath(outer, outlineColor, style = stroke)
            listOf(
                top to centerLeft,
                top to centerRight,
                centerLeft to centerRight,
                centerLeft to centerBottom,
                centerRight to centerBottom,
                centerBottom to bottom,
            ).forEach { (start, end) -> drawLine(outlineColor.copy(alpha = 0.62f), start, end, stroke.width) }
        }
        Text(
            text = face.coerceIn(0, 9).toString(),
            color = outlineColor,
            fontSize = numberSize.value.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
