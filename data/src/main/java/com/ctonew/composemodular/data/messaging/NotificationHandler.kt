package com.ctonew.composemodular.data.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ctonew.composemodular.domain.message.MessageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messageRepository: MessageRepository,
    private val bitmapScaler: BitmapScaler,
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    fun handlePushNotification(
        messageId: String,
        conversationId: String,
        title: String,
        body: String,
        data: Map<String, String>,
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                // Process and store the message
                processIncomingMessage(messageId, conversationId, data)

                // Display notification
                displayNotification(messageId, conversationId, title, body, data)
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    private suspend fun processIncomingMessage(
        messageId: String,
        conversationId: String,
        data: Map<String, String>,
    ) {
        // In production, process and store the message in the database
        // Example: messageRepository.insertMessage(...)
    }

    private fun displayNotification(
        messageId: String,
        conversationId: String,
        title: String,
        body: String,
        data: Map<String, String>,
    ) {
        val intent = Intent().apply {
            action = "com.ctonew.composemodular.MESSAGE_NOTIFICATION"
            putExtra("message_id", messageId)
            putExtra("conversation_id", conversationId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            messageId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val imageUrl = data["image_url"]
        val largeIcon = if (imageUrl != null) {
            loadAndScaleBitmap(imageUrl)
        } else {
            null
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_MESSAGES)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .apply {
                if (largeIcon != null) {
                    setLargeIcon(largeIcon)
                }
            }
            .build()

        notificationManager.notify(
            messageId.hashCode(),
            notification,
        )

        largeIcon?.recycle()
    }

    private fun loadAndScaleBitmap(url: String): Bitmap? {
        return try {
            // In production, use a library like Coil to load the image
            // val bitmap = // load from url
            // bitmapScaler.scale(bitmap, MAX_ICON_SIZE)
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val messageChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notifications for incoming messages"
            }

            notificationManager.createNotificationChannel(messageChannel)
        }
    }

    companion object {
        const val CHANNEL_MESSAGES = "messages"
        const val MAX_ICON_SIZE = 384
    }
}
