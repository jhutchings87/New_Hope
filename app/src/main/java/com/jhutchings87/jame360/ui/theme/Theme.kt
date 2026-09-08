package com.jhutchings87.jame360.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Jame360Teal = Color(0xFF4FD1C5)
private val Jame360Ink = Color(0xFF1B2430)

private val DarkColors = darkColorScheme(
    primary = Jame360Teal,
    secondary = Jame360Teal,
    background = Jame360Ink,
    surface = Color(0xFF24303F)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0F9C90),
    secondary = Color(0xFF0F9C90),
    background = Color(0xFFF5F7F8),
    surface = Color.White
)

@Composable
fun Jame360Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
