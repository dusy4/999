package com.ctonew.composemodular.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.ctonew.composemodular.domain.theme.DEFAULT_ACCENT_COLOR_ARGB
import com.ctonew.composemodular.domain.theme.ThemePreferences

val LocalAccentColor = staticCompositionLocalOf { Color(DEFAULT_ACCENT_COLOR_ARGB) }

@Composable
fun ComposeModularTheme(
    themePreferences: ThemePreferences,
    content: @Composable () -> Unit,
) {
    val accent = remember(themePreferences.accentColorArgb) {
        Color(themePreferences.accentColorArgb)
    }

    val colorScheme = remember(accent) { amoledColorScheme(accent) }

    CompositionLocalProvider(
        LocalAccentColor provides accent,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}

private fun amoledColorScheme(accent: Color) = darkColorScheme(
    primary = accent,
    onPrimary = Color.Black,
    secondary = accent.copy(alpha = 0.85f),
    onSecondary = Color.Black,
    tertiary = accent.copy(alpha = 0.65f),
    onTertiary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF0A0A0A),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF141414),
    onSurfaceVariant = Color(0xFFCCCCCC),
)
