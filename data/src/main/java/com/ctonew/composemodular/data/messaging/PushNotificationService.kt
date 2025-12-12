package com.ctonew.composemodular.data.messaging

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PushNotificationService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationHandler: NotificationHandler

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val conversationId = remoteMessage.data["conversation_id"]
        val messageId = remoteMessage.data["message_id"]
        val title = remoteMessage.data["title"] ?: "New Message"
        val body = remoteMessage.data["body"] ?: ""

        if (messageId != null && conversationId != null) {
            notificationHandler.handlePushNotification(
                messageId = messageId,
                conversationId = conversationId,
                title = title,
                body = body,
                data = remoteMessage.data,
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Save FCM token for server registration
    }
}
