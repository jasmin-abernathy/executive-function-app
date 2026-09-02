package org.lepotager.executivefunction.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF59664F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE2CF),
    onPrimaryContainer = Color(0xFF1D281A),
    secondary = Color(0xFF9B4E35),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF2D2C3),
    onSecondaryContainer = Color(0xFF3B0B00),
    tertiary = Color(0xFF8A6520),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF3DCA9),
    onTertiaryContainer = Color(0xFF2A1C00),
    background = Color(0xFFFAF3E5),
    onBackground = Color(0xFF2F2924),
    surface = Color(0xFFFFF9EE),
    onSurface = Color(0xFF2F2924),
    surfaceVariant = Color(0xFFEFE3CF),
    onSurfaceVariant = Color(0xFF51483F),
    outline = Color(0xFF7B6D60),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFBEC8AA),
    onPrimary = Color(0xFF293422),
    primaryContainer = Color(0xFF414D39),
    onPrimaryContainer = Color(0xFFDDE2CF),
    secondary = Color(0xFFFFB59A),
    onSecondary = Color(0xFF5B1B08),
    secondaryContainer = Color(0xFF7B3520),
    onSecondaryContainer = Color(0xFFFFDBCF),
    tertiary = Color(0xFFE3BC67),
    onTertiary = Color(0xFF3D2E00),
    background = Color(0xFF1D1A17),
    onBackground = Color(0xFFEAE1D8),
    surface = Color(0xFF231F1B),
    onSurface = Color(0xFFEAE1D8),
    surfaceVariant = Color(0xFF4A4139),
    onSurfaceVariant = Color(0xFFD3C4B7),
    outline = Color(0xFF9C8D80),
)

@Composable
fun ExecutiveFunctionTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
