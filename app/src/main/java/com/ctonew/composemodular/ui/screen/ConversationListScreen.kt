package com.ctonew.composemodular.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ctonew.composemodular.domain.chat.models.ChatConversation
import com.ctonew.composemodular.viewmodel.ConversationListViewModel

/**
 * Screen showing the list of chat conversations (thread list)
 */
@Composable
fun ConversationListScreen(
    onNavigateToConversation: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ConversationListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with title and settings
            ConversationListHeader(
                onNavigateToSettings = onNavigateToSettings
            )
            
            // Search bar
            ConversationSearchBar(
                onSearch = { query ->
                    if (query.isBlank()) {
                        viewModel.refreshConversations()
                    } else {
                        viewModel.searchConversations(query)
                    }
                }
            )
            
            // Conversation list with pagination
            ConversationList(
                conversations = uiState.conversations,
                isLoading = uiState.isLoading,
                isRefreshing = uiState.isRefreshing,
                hasMore = uiState.hasMore,
                error = uiState.error,
                onLoadMore = viewModel::loadMoreConversations,
                onRefresh = viewModel::refreshConversations,
                onConversationClick = { conversation ->
                    viewModel.markConversationAsRead(conversation.id)
                    onNavigateToConversation(conversation.id)
                },
                onRetry = {
                    viewModel.refreshConversations()
                }
            )
        }
    }
}

@Composable
private fun ConversationListHeader(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Messages",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                // Settings button with better styling
                androidx.compose.material3.IconButton(
                    onClick = { onNavigateToSettings() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationSearchBar(
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔍",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(end = 8.dp)
            )
            
            androidx.compose.material3.OutlinedTextField(
                value = searchQuery,
                onValueChange = { newValue ->
                    searchQuery = newValue
                    onSearch(newValue)
                },
                placeholder = { Text("Search conversations...") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun ConversationList(
    conversations: List<ChatConversation>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    hasMore: Boolean,
    error: String?,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onConversationClick: (ChatConversation) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    // Implement pagination prefetching
    LaunchedEffect(listState, conversations.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val shouldLoadMore = lastVisibleIndex != null && 
                    lastVisibleIndex >= conversations.size - 5 && // Prefetch when 5 items from end
                    hasMore && 
                    !isLoading
                
                if (shouldLoadMore) {
                    onLoadMore()
                }
            }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            error != null -> {
                // Error state with retry
                ErrorState(
                    error = error,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            conversations.isEmpty() && !isLoading -> {
                // Empty state
                EmptyState(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                // Main conversation list
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp), // Space for input
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    // Day separators using sticky headers
                    items(
                        items = conversations,
                        key = { it.id }
                    ) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            onClick = { onConversationClick(conversation) }
                        )
                        
                        Divider(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    
                    // Loading indicator for pagination
                    if (isLoading && conversations.isNotEmpty()) {
                        item {
                            LoadingIndicator()
                        }
                    }
                }
            }
        }
        
        // Pull to refresh indicator
        if (isRefreshing) {
            LoadingIndicator(
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun ConversationItem(
    conversation: ChatConversation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar with unread indicator
            Box(modifier = Modifier.size(56.dp)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(conversation.avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "${conversation.title} avatar",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                // Unread count badge
                if (conversation.unreadCount > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Muted indicator
                if (conversation.isMuted) {
                    Text(
                        text = "🔇",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Title with pinned indicator
                    Text(
                        text = conversation.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (conversation.isPinned) {
                        Text(
                            text = "📌",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    
                    // Time of last message
                    conversation.lastMessage?.timestamp?.let { timestamp ->
                        Text(
                            text = formatTime(timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                
                // Last message preview
                val lastMessageText = conversation.lastMessage?.content?.take(40) ?: "No messages yet"
                Text(
                    text = lastMessageText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "❌",
            style = MaterialTheme.typography.displaySmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        androidx.compose.material3.Button(
            onClick = onRetry
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "💬",
            style = MaterialTheme.typography.displaySmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No conversations yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Start a new conversation to see it here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Format time for conversation list
 */
private fun formatTime(timestamp: java.time.LocalDateTime): String {
    val now = java.time.LocalDateTime.now()
    val isToday = timestamp.toLocalDate() == now.toLocalDate()
    val isYesterday = timestamp.toLocalDate() == now.minusDays(1).toLocalDate()
    
    return when {
        isToday -> {
            java.time.format.DateTimeFormatter.ofPattern("HH:mm").format(timestamp)
        }
        isYesterday -> {
            "Yesterday"
        }
        else -> {
            java.time.format.DateTimeFormatter.ofPattern("MMM dd").format(timestamp)
        }
    }
}