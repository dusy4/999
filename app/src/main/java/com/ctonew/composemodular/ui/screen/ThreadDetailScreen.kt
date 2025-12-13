package com.ctonew.composemodular.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ctonew.composemodular.domain.models.Thread
import com.ctonew.composemodular.domain.models.User
import com.ctonew.composemodular.ui.components.ThreadCard
import com.ctonew.composemodular.viewmodel.ThreadDetailViewModel

@Composable
fun ThreadDetailScreen(
    threadId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    viewModel: ThreadDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.getThreadDetail(threadId).collectAsStateWithLifecycle(initial = null)
    val users by viewModel.users.collectAsStateWithLifecycle()
    val replies by viewModel.getReplies(threadId).collectAsStateWithLifecycle(initial = emptyList())

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            ThreadDetailHeader(onNavigateBack = onNavigateBack)

            // Content
            if (uiState == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                ThreadDetailContent(
                    thread = uiState!!,
                    author = users[uiState!!.userId],
                    replies = replies,
                    users = users,
                    onLikeClick = { viewModel.toggleLike(threadId) },
                    onReplyClick = { /* Open reply composer */ },
                    onRepostClick = { viewModel.toggleRepost(threadId) },
                    onShareClick = { /* Share */ },
                )
            }
        }
    }
}

@Composable
private fun ThreadDetailHeader(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Thread",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun ThreadDetailContent(
    thread: Thread,
    author: User?,
    replies: List<Thread>,
    users: Map<String, User>,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    onRepostClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        item {
            ExpandedThreadCard(
                thread = thread,
                author = author,
                onLikeClick = { onLikeClick() },
                onReplyClick = { onReplyClick() },
                onRepostClick = { onRepostClick() },
                onShareClick = { onShareClick() }
            )
        }

        if (replies.isNotEmpty()) {
            item {
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    thickness = 0.5.dp
                )
            }

            item {
                Text(
                    text = "Replies",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }

            items(
                items = replies,
                key = { it.id }
            ) { reply ->
                val replyAuthor = users[reply.userId]
                ThreadCard(
                    thread = reply,
                    author = replyAuthor ?: User(
                        id = reply.userId,
                        name = "Unknown",
                        email = "",
                        createdAt = 0
                    ),
                    onThreadClick = {},
                    onLikeClick = { /* Like reply */ },
                    onReplyClick = { /* Reply to reply */ },
                    onRepostClick = { /* Repost reply */ },
                    onShareClick = { /* Share reply */ },
                )
            }
        }
    }
}

@Composable
private fun ExpandedThreadCard(
    thread: Thread,
    author: User?,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    onRepostClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (author == null) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Expanded author section
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Avatar (larger in expanded view)
            coil.compose.AsyncImage(
                model = author.avatarUrl.ifEmpty { "https://i.pravatar.cc/150?img=${author.id.hashCode() % 70}" },
                contentDescription = author.name,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = author.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "@${author.username.ifEmpty { author.id.take(8) }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Content (larger in expanded view)
        if (thread.content.isNotEmpty()) {
            Text(
                text = thread.content,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // Media if available
        if (thread.mediaUrls.isNotEmpty()) {
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                items(thread.mediaUrls) { imageUrl ->
                    coil.compose.AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            ),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            }
        }

        // Timestamp
        Text(
            text = formatThreadTime(thread.createdAt),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Stats
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            StatItem(label = "Replies", count = thread.replyCount)
            StatItem(label = "Reposts", count = thread.repostCount)
            StatItem(label = "Likes", count = thread.likeCount)
        }

        Divider(
            color = MaterialTheme.colorScheme.surfaceVariant,
            thickness = 0.5.dp
        )

        // Interaction buttons (larger)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExpandedActionButton(
                label = "Reply",
                onClick = onReplyClick,
                modifier = Modifier.weight(1f)
            )
            ExpandedActionButton(
                label = "Repost",
                onClick = onRepostClick,
                modifier = Modifier.weight(1f)
            )
            ExpandedActionButton(
                label = "Like",
                onClick = onLikeClick,
                modifier = Modifier.weight(1f)
            )
            ExpandedActionButton(
                label = "Share",
                onClick = onShareClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    count: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExpandedActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

private fun formatThreadTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val date = java.util.Date(timestamp)
    
    return when {
        diff < 60000 -> "now"
        diff < 3600000 -> "${diff / 60000} minutes ago"
        diff < 86400000 -> "${diff / 3600000} hours ago"
        diff < 604800000 -> "${diff / 86400000} days ago"
        else -> java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault()).format(date)
    }
}
