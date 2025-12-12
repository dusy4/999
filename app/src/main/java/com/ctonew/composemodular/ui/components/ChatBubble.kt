package com.ctonew.composemodular.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ctonew.composemodular.domain.chat.models.Attachment
import com.ctonew.composemodular.domain.chat.models.ChatMessage
import com.ctonew.composemodular.domain.chat.models.MessageStatus
import com.ctonew.composemodular.domain.chat.models.MessageType
import com.ctonew.composemodular.ui.theme.LocalAccentColor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Telegram-style chat bubble that adapts alignment, shape, and color
 */
@Composable
fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier,
    showTimestamp: Boolean = true,
    showAvatar: Boolean = false,
    avatarUrl: String? = null,
    onImageClick: ((Attachment.Image) -> Unit)? = null,
    onFileClick: ((Attachment.File) -> Unit)? = null
) {
    val isFromMe = message.isFromMe
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
        ) {
            // Message bubble with proper alignment and shape
            ChatBubbleContent(
                message = message,
                isFromMe = isFromMe,
                onImageClick = onImageClick,
                onFileClick = onFileClick
            )
            
            // Timestamp and status
            if (showTimestamp) {
                Spacer(modifier = Modifier.height(4.dp))
                MessageStatusRow(
                    message = message,
                    isFromMe = isFromMe
                )
            }
        }
    }
}

@Composable
private fun ChatBubbleContent(
    message: ChatMessage,
    isFromMe: Boolean,
    onImageClick: ((Attachment.Image) -> Unit)? = null,
    onFileClick: ((Attachment.File) -> Unit)? = null
) {
    val bubbleColors = remember(isFromMe) {
        if (isFromMe) {
            // My messages - use accent color
            ChatBubbleColors(
                background = LocalAccentColor.current,
                textColor = Color.Black,
                borderColor = LocalAccentColor.current.copy(alpha = 0.8f)
            )
        } else {
            // Others' messages - dark gray
            ChatBubbleColors(
                background = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                borderColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }

    Column(
        modifier = Modifier
            .clip(chatBubbleShape(isFromMe))
            .background(bubbleColors.background)
            .then(
                if (isFromMe) {
                    Modifier.padding(start = 12.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
                } else {
                    Modifier.padding(start = 8.dp, top = 8.dp, end = 12.dp, bottom = 8.dp)
                }
            )
    ) {
        // Message content based on type
        when (val messageType = message.messageType) {
            is MessageType.Text -> {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = bubbleColors.textColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            is MessageType.Image -> {
                MessageImage(
                    imageUrl = messageType.imageUrl,
                    contentDescription = "Image message",
                    onClick = { onImageClick?.invoke(messageType) }
                )
            }
            is MessageType.File -> {
                MessageFile(
                    fileName = messageType.fileName,
                    fileSize = messageType.fileSize,
                    onClick = { onFileClick?.invoke(messageType) }
                )
            }
            is MessageType.Audio -> {
                MessageAudio(
                    audioUrl = messageType.audioUrl,
                    duration = messageType.duration
                )
            }
            is MessageType.Video -> {
                MessageVideo(
                    videoUrl = messageType.videoUrl,
                    duration = messageType.duration,
                    onClick = { onImageClick?.invoke(Attachment.Image(messageType.videoUrl, 0, 0, 0)) }
                )
            }
        }

        // Edited indicator
        if (message.isEdited) {
            Text(
                text = "edited",
                style = MaterialTheme.typography.labelSmall,
                color = bubbleColors.textColor.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Custom shapes for different message alignments (Telegram-style)
 */
@Composable
private fun chatBubbleShape(isFromMe: Boolean): androidx.compose.ui.graphics.Shape {
    val cornerRadius = 18.dp
    return remember(isFromMe) {
        RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
            bottomStart = if (isFromMe) cornerRadius else 4.dp,
            bottomEnd = if (isFromMe) 4.dp else cornerRadius
        )
    }
}

/**
 * Row showing timestamp and message status
 */
@Composable
private fun MessageStatusRow(
    message: ChatMessage,
    isFromMe: Boolean
) {
    val timeText = remember(message.timestamp) {
        formatTime(message.timestamp)
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = timeText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        
        if (isFromMe) {
            MessageStatusIcon(status = message.status)
        }
    }
}

@Composable
private fun MessageStatusIcon(status: MessageStatus) {
    val iconAndTint = remember(status) {
        when (status) {
            MessageStatus.Sending -> Icons.Default.Schedule to MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            MessageStatus.Sent -> Icons.Default.Check to MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            MessageStatus.Delivered -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            MessageStatus.Read -> Icons.Default.CheckCircle to LocalAccentColor.current
            is MessageStatus.Failed -> Icons.Default.Error to Color.Red
        }
    }
    
    Icon(
        imageVector = iconAndTint.first,
        contentDescription = "Message status",
        tint = iconAndTint.second,
        modifier = Modifier.size(16.dp)
    )
}

/**
 * Image message with lifecycle-aware sizing
 */
@Composable
private fun MessageImage(
    imageUrl: String,
    contentDescription: String,
    onClick: () -> Unit
) {
    val lifecycleAwareSize = remember {
        // Provide reasonable default sizes for different contexts
        200.dp
    }

    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = Modifier
            .size(lifecycleAwareSize)
            .clip(RoundedCornerShape(8.dp))
            .then(Modifier),
        onSuccess = { /* Coil handles lifecycle-aware sizing */ }
    )
}

/**
 * File message attachment
 */
@Composable
private fun MessageFile(
    fileName: String,
    fileSize: Long,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // File icon would go here - using text as placeholder
        Text(
            text = "📄",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(end = 8.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatFileSize(fileSize),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Audio message attachment
 */
@Composable
private fun MessageAudio(
    audioUrl: String,
    duration: Long
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🎵",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(end = 8.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Audio message",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Video message attachment
 */
@Composable
private fun MessageVideo(
    videoUrl: String,
    duration: Long,
    onClick: () -> Unit
) {
    Box {
        Text(
            text = "🎬",
            style = MaterialTheme.typography.titleMedium
        )
        
        Text(
            text = formatDuration(duration),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
        )
    }
}

/**
 * Data class for chat bubble colors
 */
private data class ChatBubbleColors(
    val background: Color,
    val textColor: Color,
    val borderColor: Color
)

/**
 * Format time for message timestamps
 */
private fun formatTime(timestamp: LocalDateTime): String {
    val now = LocalDateTime.now()
    val isToday = timestamp.toLocalDate() == now.toLocalDate()
    
    return if (isToday) {
        DateTimeFormatter.ofPattern("HH:mm").format(timestamp)
    } else {
        DateTimeFormatter.ofPattern("MMM dd, HH:mm").format(timestamp)
    }
}

/**
 * Format file size for display
 */
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}

/**
 * Format duration for audio/video messages
 */
private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}