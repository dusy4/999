package com.ctonew.composemodular.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ctonew.composemodular.ui.screen.ConversationListScreen
import com.ctonew.composemodular.ui.screen.ConversationScreen
import com.ctonew.composemodular.ui.screen.ProfileScreen
import com.ctonew.composemodular.ui.screen.SettingsScreen

object MessengerDestinations {
    const val CONVERSATIONS = "conversations"
    const val CONVERSATION = "conversation"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
}

object MessengerNavArguments {
    const val CONVERSATION_ID = "conversationId"
    const val USER_ID = "userId"
}

sealed class MessengerBottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Conversations : MessengerBottomNavItem(
        route = MessengerDestinations.CONVERSATIONS,
        title = "Chats",
        icon = Icons.Default.Chat
    )

    data object Profile : MessengerBottomNavItem(
        route = MessengerDestinations.PROFILE,
        title = "Profile",
        icon = Icons.Default.Person
    )

    data object Settings : MessengerBottomNavItem(
        route = MessengerDestinations.SETTINGS,
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
        startDestination = MessengerDestinations.CONVERSATIONS,
        modifier = modifier
    ) {
        composable(MessengerDestinations.CONVERSATIONS) {
            ConversationListScreen(
                onNavigateToConversation = { conversationId ->
                    navController.navigate("${MessengerDestinations.CONVERSATION}/$conversationId")
                },
                onNavigateToSettings = {
                    navController.navigate(MessengerDestinations.SETTINGS)
                }
            )
        }

        composable(
            route = "${MessengerDestinations.CONVERSATION}/{${MessengerNavArguments.CONVERSATION_ID}}"
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString(MessengerNavArguments.CONVERSATION_ID) ?: ""
            ConversationScreen(
                conversationId = conversationId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "${MessengerDestinations.PROFILE}/{${MessengerNavArguments.USER_ID}}"
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString(MessengerNavArguments.USER_ID) ?: ""
            ProfileScreen(
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(MessengerDestinations.SETTINGS) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
