# Background Sync Integration Guide

## Quick Start

This guide helps developers integrate the message sync and notification system with their API and business logic.

## 1. API Integration

### Backfill Messages API

Update `BackfillMessagesWorker.backfillMessages()`:

```kotlin
private suspend fun backfillMessages(conversationId: String, sinceTimestamp: Long) {
    try {
        // Inject your API service
        @Inject lateinit var messageApi: MessageApi
        
        // Fetch messages from server
        val response = messageApi.getMessages(
            conversationId = conversationId,
            since = sinceTimestamp
        )
        
        // Convert to domain models and insert
        response.messages.forEach { apiMessage ->
            messageRepository.insertMessage(apiMessage.toDomain())
        }
        
    } catch (e: Exception) {
        // Handle API errors
        throw e
    }
}
```

### Send Message API

Update `SendQueuedMessagesWorker.sendMessage()`:

```kotlin
private suspend fun sendMessage(messageId: String) {
    @Inject lateinit var messageApi: MessageApi
    
    val message = messageRepository.getMessage(messageId)
        ?: throw IllegalStateException("Message not found: $messageId")
    
    val response = messageApi.sendMessage(
        conversationId = message.conversationId,
        content = message.content
    )
    
    // Update message with server ID if needed
    if (response.serverId != messageId) {
        message.copy(id = response.serverId).let {
            messageRepository.updateMessage(it)
        }
    }
}
```

## 2. FCM Payload Configuration

Your backend should send FCM messages with this payload structure:

```json
{
  "data": {
    "message_id": "msg-unique-id",
    "conversation_id": "conv-unique-id",
    "title": "Sender Name",
    "body": "Message preview text",
    "image_url": "https://cdn.example.com/avatar.jpg",
    "custom_field": "optional-data"
  }
}
```

### Process Custom Fields

Update `NotificationHandler.handlePushNotification()`:

```kotlin
fun handlePushNotification(
    messageId: String,
    conversationId: String,
    title: String,
    body: String,
    data: Map<String, String>,
) {
    CoroutineScope(Dispatchers.Default).launch {
        try {
            // Extract custom fields
            val customField = data["custom_field"]
            
            // Process incoming message
            processIncomingMessage(messageId, conversationId, data)
            
            // Display notification with custom logic
            displayNotification(messageId, conversationId, title, body, data)
        } catch (e: Exception) {
            // Log error
        }
    }
}
```

## 3. Deep Link Handling

Update `MainActivity` to handle deep links from notifications:

```kotlin
@Composable
fun MainActivity() {
    val navController = rememberNavController()
    
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle
            ?.get<String>("message_id")?.let { messageId ->
            // Navigate to conversation with message
            navController.navigate("conversation/$messageId")
        }
    }
    
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("conversation/{messageId}") { backStackEntry ->
            val messageId = backStackEntry.arguments?.getString("messageId") ?: return@composable
            ConversationScreen(messageId = messageId)
        }
    }
}
```

Or update the intent handler in `PushNotificationService`:

```kotlin
private fun displayNotification(
    messageId: String,
    conversationId: String,
    title: String,
    body: String,
    data: Map<String, String>,
) {
    val intent = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        putExtra("conversation_id", conversationId)
        putExtra("message_id", messageId)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    
    val pendingIntent = PendingIntent.getActivity(
        context,
        messageId.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    
    // ... create and display notification with pendingIntent
}
```

## 4. FCM Token Registration

Update `PushNotificationService.onNewToken()`:

```kotlin
override fun onNewToken(token: String) {
    super.onNewToken(token)
    
    // Send token to your backend
    CoroutineScope(Dispatchers.IO).launch {
        try {
            @Inject lateinit var userApi: UserApi
            userApi.registerFcmToken(token)
        } catch (e: Exception) {
            // Log error, token registration failed
        }
    }
}
```

## 5. Custom Configuration

### Adjust Sync Frequency

Update `WorkScheduler` for different sync schedules:

```kotlin
// 5-minute backfill (more frequent)
val backfillWorkRequest = PeriodicWorkRequestBuilder<BackfillMessagesWorker>(
    5, TimeUnit.MINUTES,  // Changed from 15
)

// 12-hour attachment cleanup (less frequent)
val cleanupWorkRequest = PeriodicWorkRequestBuilder<AttachmentCleanupWorker>(
    12, TimeUnit.HOURS,  // Changed from 1 day
)
```

### Adjust Attachment Retention

When enqueueing the cleanup worker:

```kotlin
private fun scheduleAttachmentCleanupWorker() {
    val cleanupWorkRequest = PeriodicWorkRequestBuilder<AttachmentCleanupWorker>(
        1, TimeUnit.DAYS,
    )
        .setInputData(
            androidx.work.Data.Builder()
                .putInt(AttachmentCleanupWorker.KEY_RETENTION_DAYS, 60)  // 60 days
                .build()
        )
        .setConstraints(cleanupConstraints)
        .setBackoffPolicy(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS)
        .build()

    workManager.enqueueUniquePeriodicWork(
        "attachment_cleanup",
        ExistingPeriodicWorkPolicy.KEEP,
        cleanupWorkRequest,
    )
}
```

## 6. Error Handling & Logging

Add proper error logging to workers:

```kotlin
override suspend fun doWork(): Result {
    return try {
        // Perform work
        Result.success()
    } catch (e: IOException) {
        // Network error - retry
        Log.w(TAG, "Network error, will retry", e)
        if (runAttemptCount < MAX_RETRIES) {
            Result.retry()
        } else {
            Result.failure()
        }
    } catch (e: Exception) {
        // Unexpected error - fail
        Log.e(TAG, "Unexpected error", e)
        Result.failure()
    }
}

companion object {
    private const val TAG = "BackfillMessagesWorker"
}
```

## 7. Testing API Integration

Mock the repository in tests:

```kotlin
@Test
fun testMessageSendingFlow() = runBlocking {
    // Mock repository
    val mockRepository = mock<MessageRepository>()
    coEvery { mockRepository.getOutboundQueue() } returns flowOf(listOf(
        Message(
            id = "msg-1",
            conversationId = "conv-1",
            content = "Test",
            senderId = "user-1",
            timestamp = System.currentTimeMillis(),
            isOutbound = true,
        )
    ))
    
    // Test worker behavior
    val worker = SendQueuedMessagesWorker(context, params, mockRepository)
    val result = worker.doWork()
    
    // Verify removal from queue
    coVerify { mockRepository.removeFromOutboundQueue("msg-1") }
    assertTrue(result is ListenableWorker.Result.Success)
}
```

## 8. Monitoring & Analytics

Track important metrics:

```kotlin
@Singleton
class SyncAnalytics @Inject constructor() {
    fun logMessageBackfilled(count: Int) {
        // Track sync success
        Firebase.analytics.logEvent("messages_backfilled") {
            param("count", count.toLong())
        }
    }
    
    fun logMessageSent(messageId: String, duration: Long) {
        Firebase.analytics.logEvent("message_sent") {
            param("message_id", messageId)
            param("duration_ms", duration)
        }
    }
    
    fun logSyncError(workerName: String, error: String) {
        Firebase.analytics.logEvent("sync_error") {
            param("worker", workerName)
            param("error", error)
        }
    }
}
```

## 9. Permissions Handling

Ensure runtime permissions are requested:

```kotlin
@Composable
fun AppContent() {
    val notificationPermission = rememberPermissionState(
        android.Manifest.permission.POST_NOTIFICATIONS
    )
    
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launchPermissionRequest()
        }
    }
    
    // ... rest of app
}
```

## 10. Debugging

Enable WorkManager debug logging:

```kotlin
// In ModularComposeApp.onCreate() for debugging
if (BuildConfig.DEBUG) {
    val configuration = Configuration.Builder()
        .setMinimumLoggingLevel(Log.DEBUG)
        .build()
    WorkManager.initialize(this, configuration)
}
```

## Common Integration Patterns

### Pattern 1: Sync on App Foreground

```kotlin
class SyncViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
) : ViewModel() {
    
    fun syncMessages(conversationId: String) {
        viewModelScope.launch {
            try {
                // Trigger immediate sync
                WorkManager.getInstance(context).enqueueUniqueWork(
                    "sync_$conversationId",
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequestBuilder<BackfillMessagesWorker>()
                        .setInputData(
                            androidx.work.Data.Builder()
                                .putString(KEY_CONVERSATION_ID, conversationId)
                                .putLong(KEY_SINCE_TIMESTAMP, lastSyncTime)
                                .build()
                        )
                        .build()
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
```

### Pattern 2: User-Initiated Message Send

```kotlin
class ConversationViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
) : ViewModel() {
    
    fun sendMessage(conversationId: String, content: String) {
        viewModelScope.launch {
            val message = Message(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                content = content,
                senderId = getCurrentUserId(),
                timestamp = System.currentTimeMillis(),
                isOutbound = true,
            )
            
            messageRepository.addToOutboundQueue(message)
            
            // Trigger immediate send attempt
            WorkManager.getInstance(context).enqueueUniqueWork(
                "send_immediate",
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<SendQueuedMessagesWorker>().build()
            )
        }
    }
}
```

### Pattern 3: Offline-First UI

```kotlin
@Composable
fun MessageItem(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(message.content)
        
        if (message.isOutbound) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Pending send",
                tint = Color.Gray,
            )
        }
    }
}
```

## Next Steps

1. Implement API integration following patterns above
2. Configure FCM payload structure with backend team
3. Add deep link handling in MainActivity
4. Set up FCM token registration
5. Add error logging and analytics
6. Test with real network conditions
7. Monitor sync metrics in production

See `BACKGROUND_SYNC.md` for complete architecture documentation.
