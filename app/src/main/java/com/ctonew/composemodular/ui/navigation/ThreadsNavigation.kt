package com.ctonew.composemodular.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ctonew.composemodular.ui.screen.FeedScreen
import com.ctonew.composemodular.ui.screen.ThreadDetailScreen
import com.ctonew.composemodular.ui.screen.ProfileScreen
import com.ctonew.composemodular.ui.screen.SettingsScreen

object ThreadDestinations {
    const val FEED = "feed"
    const val THREAD_DETAIL = "thread_detail"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
}

object ThreadNavArguments {
    const val THREAD_ID = "threadId"
    const val USER_ID = "userId"
}

sealed class ThreadBottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Feed : ThreadBottomNavItem(
        route = ThreadDestinations.FEED,
        title = "Home",
        icon = Icons.Default.Home
    )

    data object Search : ThreadBottomNavItem(
        route = "search",
        title = "Search",
        icon = Icons.Default.Search
    )

    data object Profile : ThreadBottomNavItem(
        route = ThreadDestinations.PROFILE,
        title = "Profile",
        icon = Icons.Default.Person
    )

    data object Settings : ThreadBottomNavItem(
        route = ThreadDestinations.SETTINGS,
        title = "Settings",
        icon = Icons.Default.Settings
    )
}

@Composable
fun ThreadsNavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = ThreadDestinations.FEED,
        modifier = modifier
    ) {
        composable(ThreadDestinations.FEED) {
            FeedScreen(
                onNavigateToThread = { threadId ->
                    navController.navigate("${ThreadDestinations.THREAD_DETAIL}/$threadId")
                },
                onNavigateToSettings = {
                    navController.navigate(ThreadDestinations.SETTINGS)
                },
                onNavigateToProfile = { userId ->
                    navController.navigate("${ThreadDestinations.PROFILE}/$userId")
                }
            )
        }

        composable(
            route = "${ThreadDestinations.THREAD_DETAIL}/{${ThreadNavArguments.THREAD_ID}}"
        ) { backStackEntry ->
            val threadId = backStackEntry.arguments?.getString(ThreadNavArguments.THREAD_ID) ?: ""
            ThreadDetailScreen(
                threadId = threadId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProfile = { userId ->
                    navController.navigate("${ThreadDestinations.PROFILE}/$userId")
                }
            )
        }

        composable(
            route = "${ThreadDestinations.PROFILE}/{${ThreadNavArguments.USER_ID}}"
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString(ThreadNavArguments.USER_ID) ?: ""
            ProfileScreen(
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToThread = { threadId ->
                    navController.navigate("${ThreadDestinations.THREAD_DETAIL}/$threadId")
                },
                onNavigateToSettings = {
                    navController.navigate(ThreadDestinations.SETTINGS)
                }
            )
        }

        composable(ThreadDestinations.SETTINGS) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
