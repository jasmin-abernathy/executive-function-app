package org.lepotager.executivefunction.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class Vertex(val x: Float, val y: Float, val z: Float)

/** Perspective projection of ten kite faces; original code, no external artwork. */
@Composable
internal fun D10Mesh(progress: Float, face: Int, modifier: Modifier = Modifier) {
    val base = MaterialTheme.colorScheme.primaryContainer
    val ink = MaterialTheme.colorScheme.onPrimaryContainer
    Canvas(modifier) {
        val turn = progress * 4 * PI + .45
        val tilt = -.35 + .45 * sin(progress * PI)
        fun rotate(v: Vertex): Vertex {
            val x = v.x * cos(turn) + v.z * sin(turn)
            val z = -v.x * sin(turn) + v.z * cos(turn)
            return Vertex(x.toFloat(), (v.y * cos(tilt) - z * sin(tilt)).toFloat(),
                (v.y * sin(tilt) + z * cos(tilt)).toFloat())
        }
        val belt = (0..9).map {
            val angle = it * PI / 5
            rotate(Vertex(cos(angle).toFloat(), if (it % 2 == 0) -.28f else .28f, sin(angle).toFloat()))
        }
        val top = rotate(Vertex(0f, -1.35f, 0f))
        val bottom = rotate(Vertex(0f, 1.35f, 0f))
        val faces = (0..9).map { i ->
            i to listOf(if (i % 2 == 0) top else bottom, belt[i], belt[(i + 1) % 10], belt[(i + 2) % 10])
        }.sortedBy { (_, vertices) -> vertices.sumOf { it.z.toDouble() } }
        fun project(v: Vertex): Offset {
            val scale = size.minDimension * .30f * 4f / (4f - v.z)
            return Offset(center.x + v.x * scale, center.y + v.y * scale)
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ink.toArgb()
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            textSize = size.minDimension * .10f
        }
        faces.forEach { (index, vertices) ->
            val points = vertices.map(::project)
            val path = Path().apply {
                moveTo(points[0].x, points[0].y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
                close()
            }
            val light = (.64f + (index % 3) * .12f + vertices.sumOf { it.z.toDouble() }.toFloat() * .0325f).coerceIn(.4f, 1f)
            drawPath(path, Color(base.red * light, base.green * light, base.blue * light))
            drawPath(path, ink.copy(alpha = .65f), style = Stroke(size.minDimension * .007f))
            val x = points.sumOf { it.x.toDouble() }.toFloat() / 4
            val y = points.sumOf { it.y.toDouble() }.toFloat() / 4
            drawContext.canvas.nativeCanvas.drawText(((index + face) % 10).toString(), x, y + paint.textSize * .3f, paint)
        }
    }
}
