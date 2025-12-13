package com.ctonew.composemodular.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ctonew.composemodular.domain.models.Thread
import com.ctonew.composemodular.domain.models.User
import com.ctonew.composemodular.ui.components.ThreadCard
import com.ctonew.composemodular.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onNavigateToThread: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val user by viewModel.getUser(userId).collectAsStateWithLifecycle(initial = null)
    val threads by viewModel.getUserThreads(userId).collectAsStateWithLifecycle(initial = emptyList())
    val users by viewModel.users.collectAsStateWithLifecycle()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            ProfileHeader(onNavigateBack = onNavigateBack)

            if (user == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        ProfileHeaderCard(
                            user = user!!,
                            onFollowClick = { viewModel.toggleFollow(userId) }
                        )
                    }

                    item {
                        Text(
                            text = "Posts",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    items(
                        items = threads,
                        key = { it.id }
                    ) { thread ->
                        val author = users[thread.userId]
                        ThreadCard(
                            thread = thread,
                            author = author ?: user!!,
                            onThreadClick = onNavigateToThread,
                            onLikeClick = { /* Like thread */ },
                            onReplyClick = onNavigateToThread,
                            onRepostClick = { /* Repost */ },
                            onShareClick = { /* Share */ },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
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
                text = "Profile",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun ProfileHeaderCard(
    user: User,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner area (simplified - just background color)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {}

        // Avatar and follow button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-40).dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.avatarUrl.ifEmpty { "https://i.pravatar.cc/150?img=${user.id.hashCode() % 70}" })
                    .crossfade(true)
                    .build(),
                contentDescription = user.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                contentScale = ContentScale.Crop
            )

            if (!user.isFollowing) {
                Button(onClick = onFollowClick) {
                    Text("Follow")
                }
            } else {
                OutlinedButton(onClick = onFollowClick) {
                    Text("Following")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User info
        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            text = "@${user.username.ifEmpty { user.id.take(8) }}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (user.bio.isNotEmpty()) {
            Text(
                text = user.bio,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Divider(
            color = MaterialTheme.colorScheme.surfaceVariant,
            thickness = 0.5.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
