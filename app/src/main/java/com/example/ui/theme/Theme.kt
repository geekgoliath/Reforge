package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ReforgeColorScheme = darkColorScheme(
    primary = ReforgeLime,
    onPrimary = ReforgeBg,
    primaryContainer = ReforgeLimeMuted,
    onPrimaryContainer = ReforgeTextPrimary,
    secondary = ReforgeLavender,
    onSecondary = ReforgeBg,
    secondaryContainer = ReforgeLavender.copy(alpha = 0.16f),
    onSecondaryContainer = ReforgeTextPrimary,
    tertiary = ReforgeSuccess,
    onTertiary = ReforgeBg,
    error = ReforgeWarning,
    onError = ReforgeBg,
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
