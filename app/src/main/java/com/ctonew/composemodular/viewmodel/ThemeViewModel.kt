package com.ctonew.composemodular.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ctonew.composemodular.domain.theme.ThemePreferences
import com.ctonew.composemodular.domain.theme.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    val preferences: StateFlow<ThemePreferences> = themeRepository.preferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemePreferences(),
        )

    fun cycleAccent() {
        val current = preferences.value.accentColorArgb
        val idx = AccentCycle.indexOf(current)
        val next = AccentCycle[(idx + 1).mod(AccentCycle.size)]

        viewModelScope.launch {
            themeRepository.setAccentColorArgb(next)
        }
    }

    fun setAccentColor(colorArgb: Int) {
        viewModelScope.launch {
            themeRepository.setAccentColorArgb(colorArgb)
        }
    }

    private companion object {
        val AccentCycle: List<Int> = listOf(
            0xFF00E5FF.toInt(),
            0xFFFF4081.toInt(),
            0xFFB2FF59.toInt(),
            0xFFFFD740.toInt(),
        )
    }
}
