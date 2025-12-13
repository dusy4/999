package com.ctonew.composemodular

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.ctonew.composemodular.ui.navigation.ThreadsNavigationGraph
import com.ctonew.composemodular.ui.theme.ComposeModularTheme
import com.ctonew.composemodular.viewmodel.ThemeViewModel

@Composable
fun AppRoot(
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    val preferences = themeViewModel.preferences.collectAsStateWithLifecycle().value
    val navController = rememberNavController()

    ComposeModularTheme(themePreferences = preferences) {
        ThreadsNavigationGraph(
            navController = navController
        )
    }
}
