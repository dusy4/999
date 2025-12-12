package com.ctonew.composemodular.data.theme

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.ctonew.composemodular.domain.theme.DEFAULT_ACCENT_COLOR_ARGB
import com.ctonew.composemodular.domain.theme.ThemePreferences
import com.ctonew.composemodular.domain.theme.ThemeRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Singleton
class DataStoreThemeRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : ThemeRepository {

    override val preferences: Flow<ThemePreferences> = dataStore.data
        .map { prefs ->
            ThemePreferences(
                accentColorArgb = prefs[Keys.AccentColorArgb] ?: DEFAULT_ACCENT_COLOR_ARGB,
            )
        }
        .distinctUntilChanged()

    override suspend fun setAccentColorArgb(argb: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.AccentColorArgb] = argb
        }
    }

    private object Keys {
        val AccentColorArgb = intPreferencesKey("accent_color_argb")
    }
}
