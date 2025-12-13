# Background Sync & Push Notifications Architecture

## Overview

This document describes the background data flow for message synchronization, attachment management, and push notification handling in the Compose Modular Android app.

## Architecture Components

### 1. WorkManager-based Periodic Sync

The app uses `androidx.work:work-runtime-ktx` to schedule three periodic background jobs:

#### 1.1 Message Backfill Worker (`BackfillMessagesWorker`)

**Purpose**: Fetches and caches messages from the server.

**Schedule**: Every 15 minutes
**Constraints**:
- Network connected (required)
- Battery not low (required)

**Backoff Policy**: Exponential backoff (1 minute initial delay)

**Flow**:
```
WorkManager triggers → BackfillMessagesWorker.doWork()
    → Fetch messages from API for all active conversations
    → Insert/update messages in Room database
    → MessageRepository.insertMessage() → MessageDao
    → Database triggers UI updates via Flow
```

**Error Handling**:
- Retries up to 3 times on transient failures
- Fails permanently after max retries exceeded
- Use case: Network timeouts, temporary server issues

#### 1.2 Attachment Cleanup Worker (`AttachmentCleanupWorker`)

**Purpose**: Removes stale attachments to manage storage.

**Schedule**: Daily
**Constraints**:
- Battery not low (required)

**Backoff Policy**: Linear backoff (1 hour initial delay)

**Flow**:
```
WorkManager triggers → AttachmentCleanupWorker.doWork()
    → Calculate cutoff timestamp (30 days old by default)
    → AttachmentRepository.deleteAttachmentsOlderThan(timestamp)
    → AttachmentDao.deleteAttachmentsOlderThan(timestamp)
    → Room executes batch delete query
```

**Configuration**:
- `KEY_RETENTION_DAYS`: 30 (days to retain before deletion)

#### 1.3 Send Queued Messages Worker (`SendQueuedMessagesWorker`)

**Purpose**: Delivers locally-queued messages when network becomes available.

**Schedule**: Every 15 minutes
**Constraints**:
- Network connected (required)
- Battery not low (not required - important for immediate sending)

**Backoff Policy**: Exponential backoff (1 minute initial delay)

**Flow**:
```
WorkManager triggers → SendQueuedMessagesWorker.doWork()
    → Fetch queued messages from database
    → For each message:
        → Send to API
        → On success: removeFromOutboundQueue()
        → On failure: Retry with exponential backoff
```

**Error Handling**:
- Individual message failures don't block other messages
- Failed messages remain in queue for next retry
- Max 3 overall retries before permanent failure

### 2. WorkScheduler Singleton

**Location**: `data/work/WorkScheduler.kt`
**Lifecycle**: Initialized in `ModularComposeApp.onCreate()`

**Responsibilities**:
- Enqueues all periodic work requests
- Uses `ExistingPeriodicWorkPolicy.KEEP` to prevent duplicate schedules
- Configures constraints (network, battery) for each worker
- Provides method to cancel all background work

**DI Registration**:
```kotlin
@Singleton
class WorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
)
```

Injected in `ModularComposeApp` as a lateinit property.

### 3. Firebase Cloud Messaging Integration

#### 3.1 PushNotificationService

**Location**: `data/messaging/PushNotificationService.kt`

**Lifecycle**: Registered in `AndroidManifest.xml` with `com.google.firebase.MESSAGING_EVENT` intent filter

**Responsibilities**:
- Receives push notifications from Firebase
- Extracts message metadata from notification payload
- Routes to `NotificationHandler`
- Handles FCM token refresh

**Payload Format** (expected):
```json
{
  "data": {
    "message_id": "msg-123",
    "conversation_id": "conv-456",
    "title": "User Name",
    "body": "Message content",
    "image_url": "https://..."
  }
}
```

#### 3.2 NotificationHandler

**Location**: `data/messaging/NotificationHandler.kt`

**Purpose**: Processes incoming notifications and manages system notifications.

**Key Features**:
1. **Push Processing**: `handlePushNotification()`
   - Stores incoming message in Room database
   - Triggers Room-backed flows to update UI
   - Displays system notification with deep link

2. **System Notification Creation**:
   - Uses `NotificationCompat.Builder`
   - Includes large icon with scaled bitmap
   - Provides pending intent deep link to conversation
   - Auto-cancels on tap

3. **Notification Channels** (Android 8.0+):
   - "Messages" channel (IMPORTANCE_HIGH) for message notifications

4. **Image Handling**:
   - Calls `BitmapScaler` to scale large images
   - Properly recycles bitmaps after use
   - Prevents memory leaks

**Flow**:
```
Firebase Server → PushNotificationService.onMessageReceived()
    → NotificationHandler.handlePushNotification()
        → Process message: MessageRepository.insertMessage()
        → Room updates database → Flow emits to subscribers
        → Display notification with scaled image
        → User taps notification → PendingIntent routes to conversation
```

#### 3.3 BitmapScaler

**Location**: `data/messaging/BitmapScaler.kt`

**Purpose**: Efficiently scales bitmap images for notifications.

**Key Methods**:
- `scale(bitmap, maxSize)`: Scales to fit within maxSize (maintains aspect ratio)
- `scaleToSquare(bitmap, size)`: Scales to exact square dimensions

**Memory Management**:
- Reuses original bitmap when already correct size
- Recycles old bitmap after scaling (when not reusing)
- Prevents memory leaks and excessive GC pressure

**Usage in NotificationHandler**:
```kotlin
val largeIcon = if (imageUrl != null) {
    loadAndScaleBitmap(imageUrl)
} else {
    null
}
// ... create notification with largeIcon
largeIcon?.recycle()  // Always clean up
```

## Data Models & Persistence

### Domain Layer Interfaces

```
domain/message/
├── Message.kt              // Data class
├── Attachment.kt           // Data class
├── MessageRepository.kt    // Interface
└── AttachmentRepository.kt // Interface
```

### Data Layer Entities

```
data/db/
├── MessageEntity.kt       // @Entity with indices
├── OutboundQueueEntity.kt // @Entity with foreign key
├── AttachmentEntity.kt    // @Entity with foreign key
├── MessageDao.kt          // DAO with Flow queries
├── OutboundQueueDao.kt    // DAO for queue management
└── AttachmentDao.kt       // DAO with cleanup queries
```

### Entity Relationships

**Messages**: Unique by `id`, indexed by `conversationId` and `timestamp`
```sql
CREATE TABLE messages (
  id TEXT PRIMARY KEY,
  conversationId TEXT,
  content TEXT,
  senderId TEXT,
  timestamp LONG,
  isOutbound BOOLEAN
);
```

**Outbound Queue**: References `messages` with cascade delete
```sql
CREATE TABLE outbound_queue (
  messageId TEXT PRIMARY KEY,
  addedAt LONG,
  retryCount INT,
  lastRetryTime LONG,
  FOREIGN KEY(messageId) REFERENCES messages(id) ON DELETE CASCADE
);
```

**Attachments**: References `messages` with cascade delete, indexed by creation time for cleanup
```sql
CREATE TABLE attachments (
  id TEXT PRIMARY KEY,
  messageId TEXT,
  url TEXT,
  type TEXT,
  size LONG,
  createdAt LONG,
  FOREIGN KEY(messageId) REFERENCES messages(id) ON DELETE CASCADE
);
```

### Repository Implementations

**MessageRepositoryImpl** (`data/message/MessageRepositoryImpl.kt`):
- Implements `MessageRepository` interface
- Wraps DAO calls with entity-to-domain conversion
- Manages outbound queue via `OutboundQueueDao`

**AttachmentRepositoryImpl** (`data/message/AttachmentRepositoryImpl.kt`):
- Implements `AttachmentRepository` interface
- Provides async cleanup with timestamp filtering

**DI Binding** (`data/di/MessageModule.kt`):
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class MessageModule {
    @Binds
    @Singleton
    abstract fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository
    
    @Binds
    @Singleton
    abstract fun bindAttachmentRepository(impl: AttachmentRepositoryImpl): AttachmentRepository
}
```

## Complete Data Flow Diagrams

### Scenario 1: Receive Push Notification

```
Server sends notification
        ↓
Firebase Cloud Messaging
        ↓
PushNotificationService.onMessageReceived()
        ↓
NotificationHandler.handlePushNotification()
        ├→ processIncomingMessage()
        │   ↓
        │   MessageRepository.insertMessage()
        │   ↓
        │   MessageDao.insertMessage()
        │   ↓
        │   Room Database (messages table)
        │   ↓
        │   Flow observers notified (ViewModel, Compose)
        │
        └→ displayNotification()
            ├→ loadAndScaleBitmap() [if image_url in data]
            │  ↓
            │  BitmapScaler.scale()
            │
            ├→ NotificationCompat.Builder.setLargeIcon()
            ├→ PendingIntent with deep link (conversation_id)
            ├→ NotificationManager.notify()
            └→ largeIcon.recycle() [cleanup]

User taps notification → PendingIntent fired → Deep link to conversation
```

### Scenario 2: Periodic Message Backfill

```
WorkManager triggers timer (15 min)
        ↓
BackfillMessagesWorker.doWork()
        ↓
API call: getMessages(conversationId, since)
        ↓
Parse response → Message objects
        ↓
MessageRepository.insertMessage() [for each]
        ↓
MessageDao.insertMessage()
        ↓
Room Database (messages table)
        ↓
Flow<List<Message>> emits update
        ↓
ViewModel observes flow
        ↓
Compose recomposes with new messages
```

### Scenario 3: Send Queued Outbound Message

```
User sends message offline
        ↓
ViewModel creates Message (isOutbound=true)
        ↓
MessageRepository.addToOutboundQueue()
        ├→ MessageDao.insertMessage() [message stored]
        └→ OutboundQueueDao.enqueue() [added to queue]
        ↓
Room Database updated
        ↓
Flow notifies UI of unsent message
        ↓
[Network becomes available]
        ↓
WorkManager triggers SendQueuedMessagesWorker (15 min)
        ↓
Get queued messages: MessageRepository.getOutboundQueue()
        ↓
API call: sendMessage(messageId)
        ↓
On success:
    MessageRepository.removeFromOutboundQueue()
    ↓
    OutboundQueueDao.dequeue()
    ↓
    Message removed from queue view

On failure:
    Retry count incremented
    ↓
    lastRetryTime updated
    ↓
    Message remains in queue
    ↓
    Next worker execution retries
```

### Scenario 4: Attachment Cleanup

```
WorkManager triggers timer (daily)
        ↓
AttachmentCleanupWorker.doWork()
        ↓
Calculate cutoff: now - 30 days
        ↓
AttachmentRepository.deleteAttachmentsOlderThan(timestamp)
        ↓
AttachmentDao.deleteAttachmentsOlderThan()
        ↓
Room executes: DELETE FROM attachments WHERE createdAt < timestamp
        ↓
Flow observers notified of changed data
        ↓
Storage freed up
```

## Lifecycle & Constraints

### WorkManager Constraints in Detail

**Network Constraints**:
- `CONNECTED`: Device has any network connection
- Required for: Backfill, Send queued messages
- Not required for: Attachment cleanup (can happen offline)

**Battery Constraints**:
- `requiresBatteryNotLow(true)`: Don't run when battery critical
- Applied to: Backfill, Cleanup, Send (conservative approach)
- Prevents: Excessive drain during low-battery situations

**Backoff Policies**:
- **Exponential** (1 min initial): For network-dependent tasks
  - Delays: 1 min, 2 min, 4 min, ...
  - Used by: Backfill, Send queued
- **Linear** (1 hour initial): For scheduled tasks
  - Delays: 1 hr, 2 hrs, 3 hrs, ...
  - Used by: Attachment cleanup

### Lifecycle Scopes

**NotificationHandler Coroutine Scope**:
```kotlin
CoroutineScope(Dispatchers.Default).launch { ... }
```
- Uses `Dispatchers.Default` (CPU-bound work thread pool)
- Not tied to Activity/Fragment lifecycle
- Appropriate for background FCM processing
- Does not block notification delivery

**BitmapScaler Memory Management**:
- Bitmap scaling happens on `Dispatchers.Default`
- Lifecycle-aware: Bitmap recycled immediately after notification display
- Prevents memory leaks during notification display

## Testing & Instrumentation

### Worker Testing Strategy

Create instrumentation tests in `app/src/androidTest/`:

1. **BackfillMessagesWorker Test**:
   - Mock API responses
   - Verify messages inserted into database
   - Assert database state before/after
   - Test constraint conditions (network availability)

2. **SendQueuedMessagesWorker Test**:
   - Pre-populate outbound queue
   - Mock API success/failure responses
   - Verify queue emptied on success
   - Verify queue retained on failure

3. **AttachmentCleanupWorker Test**:
   - Create attachments with various timestamps
   - Trigger worker
   - Assert old attachments deleted, recent retained

### Mock Strategies

**MessageRepository Mock**:
```kotlin
@HiltAndroidTest
class WorkerTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Test
    fun testBackfill() = runBlocking {
        // Use WorkManager.getWorkInfoById() to check status
        // Use Repository mock to verify interactions
    }
}
```

**WorkManager Testing**:
- Use `androidx.work:work-testing` for synchronous testing
- Set `SynchronousExecutor` for deterministic test runs
- Query work status: `workManager.getWorkInfoById()`

## Performance Considerations

### Periodic Sync Frequency

- **15-minute window**: Backfill and Send queued messages
  - Network-dependent, respects constraints
  - Configurable via `PeriodicWorkRequestBuilder()`
- **Daily window**: Attachment cleanup
  - Can be less frequent, non-critical

### Battery Impact Mitigation

1. **Constraints**: `requiresBatteryNotLow(true)` prevents runs during low battery
2. **Backoff**: Exponential backoff reduces retry frequency
3. **Coroutine Scope**: FCM processing on `Dispatchers.Default` (limited thread pool)
4. **Bitmap Recycling**: Immediate cleanup prevents memory pressure

### Database Indexing

Optimizes query performance:
- `messages (conversationId)`: For backfill queries
- `messages (timestamp)`: For sorting and filtering
- `attachments (createdAt)`: For cleanup queries
- `attachments (messageId)`: For foreign key traversals

## Integration with ViewModel & UI

### ViewModel Observes Message Flow

```kotlin
class ConversationViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
) : ViewModel() {
    val messages: Flow<List<Message>> = 
        messageRepository.getAllMessages(conversationId)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
```

### Compose Observes ViewModel

```kotlin
@Composable
fun ConversationScreen(viewModel: ConversationViewModel) {
    val messages by viewModel.messages.collectAsState()
    LazyColumn {
        items(messages) { MessageItem(it) }
    }
}
```

### Data Flow: WorkManager → Room → ViewModel → Compose

```
BackfillMessagesWorker
    ↓
MessageRepository.insertMessage()
    ↓
Room Database (emit Flow)
    ↓
ViewModel.messages (Flow transformation)
    ↓
Compose UI recomposes
    ↓
User sees new messages
```

## Security & Privacy Considerations

1. **FCM Token**: Obtained in `PushNotificationService.onNewToken()`
   - Should be sent to backend for registration
   - Enables targeted push delivery

2. **Message Content**: Encrypted end-to-end (should be)
   - Decrypted before display
   - Notification body may be summary only

3. **Deep Links**: Verified in `PendingIntent`
   - Intent action: `com.ctonew.composemodular.MESSAGE_NOTIFICATION`
   - Passed data: `message_id`, `conversation_id`
   - Should validate before routing

## Configuration & Future Enhancements

### Configurable Parameters

**WorkScheduler**:
- Periodic sync intervals (15 min, 1 day, etc.)
- Backoff policies
- Constraint combinations

**AttachmentCleanupWorker**:
- `KEY_RETENTION_DAYS`: Days before deletion (default 30)

**NotificationHandler**:
- `MAX_ICON_SIZE`: Notification icon max dimension (384 px)
- Notification channel configuration

### Planned Enhancements

1. **Batch Operations**
   - Bulk insert messages in BackfillMessagesWorker
   - Reduces database transaction overhead

2. **Sync Optimization**
   - Track last sync timestamp per conversation
   - Only fetch new messages since last sync

3. **Priority Queuing**
   - Prioritize user-initiated messages in send queue
   - Attempt high-priority sends before scheduled worker runs

4. **Analytics**
   - Track delivery success rates
   - Monitor sync failures and retry patterns
   - Measure push notification latency

5. **Bandwidth Awareness**
   - Disable image loading on slow networks
   - Reduce sync frequency on cellular networks

## File Structure Summary

```
domain/
└── src/main/kotlin/com/ctonew/composemodular/domain/message/
    ├── Message.kt                    # Domain model
    ├── Attachment.kt                 # Domain model
    ├── MessageRepository.kt          # Interface
    └── AttachmentRepository.kt       # Interface

data/
└── src/main/java/com/ctonew/composemodular/data/
    ├── db/
    │   ├── MessageEntity.kt          # Room entity
    │   ├── MessageDao.kt             # Room DAO
    │   ├── AttachmentEntity.kt       # Room entity
    │   ├── AttachmentDao.kt          # Room DAO
    │   ├── OutboundQueueEntity.kt    # Room entity
    │   └── AppDatabase.kt            # Updated with new DAOs
    ├── message/
    │   ├── MessageRepositoryImpl.kt   # Repository impl
    │   └── AttachmentRepositoryImpl.kt# Repository impl
    ├── messaging/
    │   ├── PushNotificationService.kt# FCM service
    │   ├── NotificationHandler.kt    # Notification processing
    │   └── BitmapScaler.kt           # Image scaling utility
    ├── work/
    │   ├── BackfillMessagesWorker.kt # Periodic backfill
    │   ├── AttachmentCleanupWorker.kt# Periodic cleanup
    │   ├── SendQueuedMessagesWorker.kt# Periodic send
    │   └── WorkScheduler.kt          # Work orchestration
    └── di/
        └── MessageModule.kt          # Hilt bindings

app/
└── src/main/
    ├── AndroidManifest.xml          # Updated with permissions & service
    └── java/com/ctonew/composemodular/
        └── ModularComposeApp.kt     # Updated to init WorkScheduler
```
