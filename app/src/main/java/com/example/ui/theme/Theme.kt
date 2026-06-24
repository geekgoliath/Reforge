package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ReforgeColorScheme = darkColorScheme(
    primary = ReforgeLime,
    onPrimary = Color.Black,
    primaryContainer = ReforgeLimeMuted,
    onPrimaryContainer = ReforgeTextPrimary,
    secondary = ReforgeTextMuted,
    onSecondary = Color.Black,
    background = ReforgeBg,
    onBackground = ReforgeTextPrimary,
    surface = ReforgeSurface,
    onSurface = ReforgeTextPrimary,
    surfaceVariant = ReforgeSurfaceVariant,
    onSurfaceVariant = ReforgeTextMuted
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ReforgeColorScheme,
        typography = Typography,
        content = content
    )
}
