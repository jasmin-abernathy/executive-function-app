package org.lepotager.executivefunction.ui

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.lepotager.executivefunction.model.TaskColor
import kotlin.math.sqrt

class TaskColorPaletteTest {
    private val warmColors = listOf(
        TaskColor.ROSE, TaskColor.TERRACOTTA, TaskColor.CORAL,
        TaskColor.APRICOT, TaskColor.SAND, TaskColor.LEMON,
    )

    @Test fun firstSixSwatchesAreCheerfulWarmShadesAndAllColorsRemainAvailable() {
        assertEquals(warmColors, taskColorPickerOrder.take(6))
        assertEquals(TaskColor.entries.size, taskColorPickerOrder.size)
        assertEquals(TaskColor.entries.toSet(), taskColorPickerOrder.toSet())
        assertEquals(Color.White, taskPalette(TaskColor.NEUTRAL).container)
    }

    @Test fun firstSixSwatchesAreDistinctAndNotAlmostWhite() {
        val colors = warmColors.map { taskPalette(it).container }
        for (color in colors) {
            // A meaningful hue rather than the original near-white pastels.
            val chroma = maxOf(color.red, color.green, color.blue) -
                minOf(color.red, color.green, color.blue)
            assertTrue("Color is too desaturated: $color", chroma >= 0.25f)
            assertFalse("Color is practically white: $color",
                color.red >= 0.95f && color.green >= 0.95f && color.blue >= 0.95f)
        }
        colors.forEachIndexed { index, color ->
            colors.drop(index + 1).forEach { other ->
                val distance = sqrt(
                    (color.red - other.red) * (color.red - other.red) +
                    (color.green - other.green) * (color.green - other.green) +
                    (color.blue - other.blue) * (color.blue - other.blue)
                )
                assertTrue("Two warm swatches look too similar: $color and $other", distance >= 0.10f)
            }
        }
    }
}
