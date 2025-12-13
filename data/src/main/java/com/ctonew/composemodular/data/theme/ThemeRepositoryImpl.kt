package com.ctonew.composemodular.data.theme

import com.ctonew.composemodular.domain.theme.ThemePreferences
import com.ctonew.composemodular.domain.theme.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeRepositoryImpl @Inject constructor() : ThemeRepository {

    private val _preferences = MutableStateFlow(ThemePreferences())
    override val preferences: Flow<ThemePreferences> = _preferences.asStateFlow()

    override suspend fun setAccentColorArgb(argb: Int) {
        _preferences.value = _preferences.value.copy(accentColorArgb = argb)
    }
}