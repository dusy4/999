package com.ctonew.composemodular.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ctonew.composemodular.ui.screen.ConversationListScreen
import com.ctonew.composemodular.ui.screen.ConversationScreen
import com.ctonew.composemodular.ui.screen.SettingsScreen

/**
 * Destinations for the chat app navigation
 */
object ChatDestinations {
    const val CONVERSATION_LIST = "conversation_list"
    const val CONVERSATION_DETAIL = "conversation_detail"
    const val SETTINGS = "settings"
}

/**
 * Bottom navigation items for the app
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Messages : BottomNavItem(
        route = ChatDestinations.CONVERSATION_LIST,
        title = "Messages",
        icon = Icons.Default.Message
    )

    data object Settings : BottomNavItem(
        route = ChatDestinations.SETTINGS,
        title = "Settings",
        icon = Icons.Default.Settings
    )
}

/**
 * Navigation arguments
 */
object NavArguments {
    const val CONVERSATION_ID = "conversationId"
}

/**
 * Build the chat navigation graph
 */
@Composable
fun ChatNavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = ChatDestinations.CONVERSATION_LIST,
        modifier = modifier
    ) {
        // Conversation List Screen
        conversationListDestination(
            onNavigateToConversation = { conversationId ->
                navController.navigate("${ChatDestinations.CONVERSATION_DETAIL}/$conversationId")
            },
            onNavigateToSettings = {
                navController.navigate(ChatDestinations.SETTINGS)
            }
        )

        // Conversation Detail Screen
        conversationDetailDestination(
            onNavigateBack = {
                navController.popBackStack()
            }
        )

        // Settings Screen
        settingsDestination(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
}

@Composable
private fun conversationListDestination(
    onNavigateToConversation: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    composable(ChatDestinations.CONVERSATION_LIST) {
        ConversationListScreen(
            onNavigateToConversation = onNavigateToConversation,
            onNavigateToSettings = onNavigateToSettings
        )
    }
}

@Composable
private fun conversationDetailDestination(
    onNavigateBack: () -> Unit
) {
    composable(
        route = "${ChatDestinations.CONVERSATION_DETAIL}/{$NavArguments.CONVERSATION_ID}"
    ) { backStackEntry ->
        val conversationId = backStackEntry.arguments?.getString(NavArguments.CONVERSATION_ID) ?: ""
        ConversationScreen(
            conversationId = conversationId,
            onNavigateBack = onNavigateBack
        )
    }
}

@Composable
private fun settingsDestination(
    onNavigateBack: () -> Unit
) {
    composable(ChatDestinations.SETTINGS) {
        SettingsScreen(
            onNavigateBack = onNavigateBack
        )
    }
}