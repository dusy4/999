package com.ctonew.composemodular.domain.theme

import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    val preferences: Flow<ThemePreferences>

    suspend fun setAccentColorArgb(argb: Int)
}
