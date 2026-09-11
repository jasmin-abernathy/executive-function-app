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
 * Quiet, deliberately narrow product palette.
 *
 * The light interface is white, pale green and anthracite only. The dark theme
 * follows the system setting with neutral charcoal surfaces. Green remains an
 * accent for actions and borders instead of tinting the whole interface.
 */
object AppPalette {
    val White = Color(0xFFFFFFFF)
    val Anthracite = Color(0xFF2D2D2D)
    val GreenBorder = Color(0xFFB7D8BF)
    val GreenButton = Color(0xFFEAF6ED)
    val GreenSurface = Color(0xFFF6FBF7)

    val DarkBackground = Color(0xFF18191A)
    val DarkSurface = Color(0xFF202224)
    val DarkSurfaceRaised = Color(0xFF292C2E)
    val DarkButton = Color(0xFF26362D)
    val DarkBorder = Color(0xFF718779)
    val DarkText = Color(0xFFF1F3F2)
    val DarkMutedText = Color(0xFFC9CECB)
}

private val LightColors = lightColorScheme(
    primary = AppPalette.Anthracite,
    onPrimary = AppPalette.White,
    primaryContainer = AppPalette.GreenButton,
    onPrimaryContainer = AppPalette.Anthracite,
    secondary = AppPalette.GreenBorder,
    onSecondary = AppPalette.Anthracite,
    secondaryContainer = AppPalette.GreenButton,
    onSecondaryContainer = AppPalette.Anthracite,
    tertiary = AppPalette.GreenBorder,
    onTertiary = AppPalette.Anthracite,
    tertiaryContainer = AppPalette.GreenSurface,
    onTertiaryContainer = AppPalette.Anthracite,
    background = AppPalette.White,
    onBackground = AppPalette.Anthracite,
    surface = AppPalette.White,
    onSurface = AppPalette.Anthracite,
    surfaceVariant = AppPalette.GreenSurface,
    onSurfaceVariant = AppPalette.Anthracite,
    surfaceDim = AppPalette.GreenSurface,
    surfaceBright = AppPalette.White,
    surfaceContainerLowest = AppPalette.White,
    surfaceContainerLow = AppPalette.White,
    surfaceContainer = AppPalette.White,
    surfaceContainerHigh = AppPalette.GreenSurface,
    surfaceContainerHighest = AppPalette.GreenButton,
    outline = AppPalette.GreenBorder,
    outlineVariant = AppPalette.GreenBorder,
    inverseSurface = AppPalette.Anthracite,
    inverseOnSurface = AppPalette.White,
    inversePrimary = AppPalette.GreenButton,
    surfaceTint = AppPalette.GreenBorder,
    error = AppPalette.Anthracite,
    onError = AppPalette.White,
    errorContainer = AppPalette.GreenButton,
    onErrorContainer = AppPalette.Anthracite,
)

private val DarkColors = darkColorScheme(
    primary = AppPalette.DarkText,
    onPrimary = AppPalette.DarkBackground,
    primaryContainer = AppPalette.DarkButton,
    onPrimaryContainer = AppPalette.DarkText,
    secondary = AppPalette.DarkBorder,
    onSecondary = AppPalette.DarkBackground,
    secondaryContainer = AppPalette.DarkButton,
    onSecondaryContainer = AppPalette.DarkText,
    tertiary = AppPalette.DarkBorder,
    onTertiary = AppPalette.DarkBackground,
    tertiaryContainer = AppPalette.DarkSurfaceRaised,
    onTertiaryContainer = AppPalette.DarkText,
    background = AppPalette.DarkBackground,
    onBackground = AppPalette.DarkText,
    surface = AppPalette.DarkSurface,
    onSurface = AppPalette.DarkText,
    surfaceVariant = AppPalette.DarkSurfaceRaised,
    onSurfaceVariant = AppPalette.DarkMutedText,
    surfaceDim = AppPalette.DarkBackground,
    surfaceBright = AppPalette.DarkSurfaceRaised,
    surfaceContainerLowest = AppPalette.DarkBackground,
    surfaceContainerLow = AppPalette.DarkSurface,
    surfaceContainer = AppPalette.DarkSurface,
    surfaceContainerHigh = AppPalette.DarkSurfaceRaised,
    surfaceContainerHighest = AppPalette.DarkButton,
    outline = AppPalette.DarkBorder,
    outlineVariant = AppPalette.DarkBorder,
    inverseSurface = AppPalette.DarkText,
    inverseOnSurface = AppPalette.DarkBackground,
    inversePrimary = AppPalette.DarkButton,
    surfaceTint = AppPalette.DarkBorder,
    error = AppPalette.DarkText,
    onError = AppPalette.DarkBackground,
    errorContainer = AppPalette.DarkButton,
    onErrorContainer = AppPalette.DarkText,
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
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        shapes = AppShapes,
        content = content,
    )
}
