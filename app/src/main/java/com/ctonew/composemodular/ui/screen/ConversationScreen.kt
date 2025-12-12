package com.ctonew.composemodular.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ctonew.composemodular.domain.chat.models.Attachment
import com.ctonew.composemodular.domain.chat.models.ChatMessage
import com.ctonew.composemodular.ui.components.ChatBubble
import com.ctonew.composemodular.viewmodel.ConversationViewModel

/**
 * Screen showing individual conversation with messages
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    conversationId: String,
    onNavigateBack: () -> Unit,
    viewModel: ConversationViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Mark conversation as read when screen appears
    LaunchedEffect(conversationId) {
        viewModel.markAsRead()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Conversation $conversationId", // TODO: Get actual title from conversation
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Messages list with pagination
            MessageList(
                messages = uiState.messages,
                isLoading = uiState.isLoading,
                hasMore = uiState.hasMore,
                error = uiState.error,
                onLoadMore = viewModel::loadMoreMessages,
                onClearError = viewModel::clearError,
                modifier = Modifier.weight(1f)
            )
            
            // Message input at bottom
            MessageInput(
                onSendMessage = viewModel::sendMessage,
                isSending = uiState.isSending,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MessageList(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    hasMore: Boolean,
    error: String?,
    onLoadMore: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    // Implement pagination with prefetching
    LaunchedEffect(messages.size, hasMore) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { firstVisibleIndex ->
                // Load more when scrolled to top
                if (firstVisibleIndex < 3 && hasMore && !isLoading) {
                    onLoadMore()
                }
            }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (messages.isEmpty() && !isLoading) {
            // Empty state
            EmptyMessageState()
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Day separators using sticky headers (implemented as items for simplicity)
                items(
                    items = messages,
                    key = { it.id }
                ) { message ->
                    ChatBubble(
                        message = message,
                        showTimestamp = true,
                        onImageClick = { imageAttachment ->
                            // Handle image click
                        },
                        onFileClick = { fileAttachment ->
                            // Handle file click
                        }
                    )
                }
                
                // Show day separators for messages from different days
                items(
                    items = messages,
                    key = { "separator-${it.timestamp.toLocalDate()}" }
                ) { message ->
                    val shouldShowSeparator = shouldShowDaySeparator(messages, message)
                    if (shouldShowSeparator) {
                        DaySeparator(timestamp = message.timestamp)
                    }
                }
                
                // Loading indicator for pagination
                if (isLoading) {
                    item {
                        LoadingIndicator()
                    }
                }
            }
        }
        
        // Error snackbar would go here
        if (error != null) {
            // TODO: Implement error handling
            LaunchedEffect(error) {
                onClearError()
            }
        }
    }
}

@Composable
private fun EmptyMessageState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "💭",
            style = MaterialTheme.typography.displaySmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No messages yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Start the conversation!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun DaySeparator(
    timestamp: java.time.LocalDateTime,
    modifier: Modifier = Modifier
) {
    val dateText = remember(timestamp) {
        formatDaySeparator(timestamp)
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.clip(RoundedCornerShape(16.dp))
        ) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun MessageInput(
    onSendMessage: (String) -> Unit,
    isSending: Boolean,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }
    
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Type a message...") },
                singleLine = false,
                maxLines = 4,
                modifier = Modifier
                    .weight(1f)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(24.dp)
                    ),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .then(
                        if (messageText.isNotBlank() && !isSending) {
                            Modifier.clickable {
                                onSendMessage(messageText.trim())
                                messageText = ""
                            }
                        } else {
                            Modifier
                        }
                    )
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    if (isSending) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.Black
                        )
                    } else {
                        Text(
                            text = if (messageText.isNotBlank()) "📤" else "🎤",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Determine if day separator should be shown for this message
 */
private fun shouldShowDaySeparator(messages: List<ChatMessage>, currentMessage: ChatMessage): Boolean {
    if (messages.isEmpty()) return false
    
    val messageIndex = messages.indexOf(currentMessage)
    if (messageIndex <= 0) return true // Always show for first message in visible list
    
    val previousMessage = messages[messageIndex - 1]
    val currentDate = currentMessage.timestamp.toLocalDate()
    val previousDate = previousMessage.timestamp.toLocalDate()
    
    return currentDate != previousDate
}

/**
 * Format date for day separator
 */
private fun formatDaySeparator(timestamp: java.time.LocalDateTime): String {
    val now = java.time.LocalDateTime.now()
    val messageDate = timestamp.toLocalDate()
    val today = now.toLocalDate()
    val yesterday = now.minusDays(1).toLocalDate()
    
    return when (messageDate) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> {
            java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy").format(timestamp)
        }
    }
}