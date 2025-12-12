package com.ctonew.composemodular.domain.theme

data class ThemePreferences(
    val accentColorArgb: Int = DEFAULT_ACCENT_COLOR_ARGB,
)

const val DEFAULT_ACCENT_COLOR_ARGB: Int = 0xFF00E5FF.toInt()
