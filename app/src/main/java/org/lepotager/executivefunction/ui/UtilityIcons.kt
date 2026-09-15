package org.lepotager.executivefunction.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class UtilityGlyphKind { NOTE, MINI_WINDOW, HELP, MORE }

@Composable
internal fun UtilityIconButton(
    kind: UtilityGlyphKind,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .semantics { contentDescription = description },
    ) {
        UtilityGlyph(kind, Modifier.size(24.dp))
    }
}

@Composable
internal fun UtilityGlyph(kind: UtilityGlyphKind, modifier: Modifier = Modifier) {
    val color = LocalContentColor.current
    if (kind == UtilityGlyphKind.HELP) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Canvas(Modifier.matchParentSize()) {
                drawCircle(color, radius = size.minDimension * .42f, style = Stroke(1.8.dp.toPx()))
            }
            Text("?", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
        }
        return
    }
    Canvas(modifier) {
        val strokeWidth = 1.8.dp.toPx()
        val stroke = Stroke(strokeWidth)
        when (kind) {
            UtilityGlyphKind.NOTE -> {
                val left = size.width * .20f
                val top = size.height * .12f
                val right = size.width * .80f
                val bottom = size.height * .88f
                val fold = size.width * .18f
                val path = Path().apply {
                    moveTo(left, top)
                    lineTo(right - fold, top)
                    lineTo(right, top + fold)
                    lineTo(right, bottom)
                    lineTo(left, bottom)
                    close()
                    moveTo(right - fold, top)
                    lineTo(right - fold, top + fold)
                    lineTo(right, top + fold)
                }
                drawPath(path, color, style = stroke)
                drawLine(color, Offset(size.width * .36f, size.height * .55f), Offset(size.width * .64f, size.height * .55f), strokeWidth)
                drawLine(color, Offset(size.width * .50f, size.height * .41f), Offset(size.width * .50f, size.height * .69f), strokeWidth)
            }
            UtilityGlyphKind.MINI_WINDOW -> {
                drawRoundRect(
                    color,
                    topLeft = Offset(size.width * .10f, size.height * .16f),
                    size = Size(size.width * .80f, size.height * .68f),
                    cornerRadius = CornerRadius(size.minDimension * .10f),
                    style = stroke,
                )
                drawRoundRect(
                    color,
                    topLeft = Offset(size.width * .50f, size.height * .47f),
                    size = Size(size.width * .30f, size.height * .25f),
                    cornerRadius = CornerRadius(size.minDimension * .05f),
                    style = stroke,
                )
            }
            UtilityGlyphKind.MORE -> {
                listOf(.28f, .50f, .72f).forEach { x ->
                    drawCircle(color, radius = size.minDimension * .07f, center = Offset(size.width * x, size.height * .5f))
                }
            }
            UtilityGlyphKind.HELP -> Unit
        }
    }
}

@Composable
internal fun DragHandleGlyph(modifier: Modifier = Modifier) {
    val color = LocalContentColor.current
    Canvas(modifier.size(24.dp)) {
        val radius = size.minDimension * .065f
        val xs = listOf(size.width * .38f, size.width * .62f)
        val ys = listOf(size.height * .28f, size.height * .50f, size.height * .72f)
        xs.forEach { x -> ys.forEach { y -> drawCircle(color, radius, Offset(x, y)) } }
    }
}
