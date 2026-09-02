package org.lepotager.executivefunction.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF3A6652),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBCEED3),
    onPrimaryContainer = Color(0xFF002115),
    secondary = Color(0xFF506458),
    surface = Color(0xFFFAFDF9),
    surfaceVariant = Color(0xFFDEE5DF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA1D2B7),
    onPrimary = Color(0xFF073824),
    primaryContainer = Color(0xFF224E3B),
    onPrimaryContainer = Color(0xFFBCEED3),
    secondary = Color(0xFFB7CCBE),
)

@Composable
fun ExecutiveFunctionTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val context = LocalContext.current
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}
