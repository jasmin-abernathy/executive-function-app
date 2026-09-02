package org.lepotager.executivefunction.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Product colours agreed during the visual-direction discussions.
 *
 * Lavender, yellow, cream and anthracite are the source palette. Soft blue and
 * orange remain secondary implementation accents until the illustrator's final
 * identity pass. Keep those accents out of state semantics: colour never carries
 * meaning on its own.
 */
object AppPalette {
    val Lavender = Color(0xFF8B8DEB)
    val LavenderAccessible = Color(0xFF6567C7)
    val EnergyYellow = Color(0xFFFFD93D)
    val Cream = Color(0xFFFFF9F0)
    val Anthracite = Color(0xFF2D2D2D)
    val SoftBlue = Color(0xFF83C7E8)
    val SoftOrange = Color(0xFFF28A62)
}

private val LightColors = lightColorScheme(
    primary = AppPalette.LavenderAccessible,
    onPrimary = Color.White,
    primaryContainer = AppPalette.Lavender,
    onPrimaryContainer = Color(0xFF222238),
    secondary = AppPalette.EnergyYellow,
    onSecondary = AppPalette.Anthracite,
    secondaryContainer = Color(0xFFFFF1A6),
    onSecondaryContainer = Color(0xFF3C3100),
    tertiary = Color(0xFF4C7892),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD6F1FF),
    onTertiaryContainer = Color(0xFF17394B),
    background = AppPalette.Cream,
    onBackground = AppPalette.Anthracite,
    surface = Color(0xFFFFFDF9),
    onSurface = AppPalette.Anthracite,
    surfaceVariant = Color(0xFFF4F0FA),
    onSurfaceVariant = Color(0xFF565461),
    surfaceDim = Color(0xFFE5DFE8),
    surfaceBright = Color(0xFFFFFDF9),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFAF5FF),
    surfaceContainer = Color(0xFFF4F0FA),
    surfaceContainerHigh = Color(0xFFEEEAF5),
    surfaceContainerHighest = Color(0xFFE8E4EF),
    outline = Color(0xFF77727F),
    outlineVariant = Color(0xFFC8C3CF),
    inverseSurface = Color(0xFF303038),
    inverseOnSurface = Color(0xFFF7F1FA),
    inversePrimary = Color(0xFFC7C7FF),
    surfaceTint = AppPalette.LavenderAccessible,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC7C7FF),
    onPrimary = Color(0xFF303278),
    primaryContainer = Color(0xFF4B4DAA),
    onPrimaryContainer = Color(0xFFE5E5FF),
    secondary = AppPalette.EnergyYellow,
    onSecondary = Color(0xFF342B00),
    secondaryContainer = Color(0xFF675500),
    onSecondaryContainer = Color(0xFFFFF0A0),
    tertiary = Color(0xFFA8DFFF),
    onTertiary = Color(0xFF00344A),
    tertiaryContainer = Color(0xFF20566F),
    onTertiaryContainer = Color(0xFFD6F1FF),
    background = Color(0xFF191820),
    onBackground = Color(0xFFF4F0FA),
    surface = Color(0xFF211F29),
    onSurface = Color(0xFFF4F0FA),
    surfaceVariant = Color(0xFF45434E),
    onSurfaceVariant = Color(0xFFCAC5D1),
    surfaceDim = Color(0xFF191820),
    surfaceBright = Color(0xFF3B3943),
    surfaceContainerLowest = Color(0xFF14131A),
    surfaceContainerLow = Color(0xFF211F29),
    surfaceContainer = Color(0xFF25232D),
    surfaceContainerHigh = Color(0xFF302E38),
    surfaceContainerHighest = Color(0xFF3B3943),
    outline = Color(0xFF938F9B),
    outlineVariant = Color(0xFF494650),
    inverseSurface = Color(0xFFE6E1E9),
    inverseOnSurface = Color(0xFF303038),
    inversePrimary = AppPalette.LavenderAccessible,
    surfaceTint = Color(0xFFC7C7FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

@Composable
fun ExecutiveFunctionTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        shapes = AppShapes,
        content = content,
    )
}
