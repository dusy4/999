package com.ctonew.composemodular

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.ctonew.composemodular.ui.screen.HomeScreen
import com.ctonew.composemodular.ui.theme.ComposeModularTheme
import com.ctonew.composemodular.viewmodel.ThemeViewModel

@Composable
fun AppRoot(
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    val preferences = themeViewModel.preferences.collectAsStateWithLifecycle().value

    ComposeModularTheme(themePreferences = preferences) {
        HomeScreen(
            onCycleAccent = themeViewModel::cycleAccent,
        )
    }
}
