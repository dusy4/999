package com.ctonew.composemodular.data.chat

import com.ctonew.composemodular.domain.chat.ChatRepository
import com.ctonew.composemodular.domain.chat.models.ChatConversation
import com.ctonew.composemodular.domain.chat.models.ChatMessage
import com.ctonew.composemodular.domain.chat.models.MessageStatus
import com.ctonew.composemodular.domain.chat.models.MessageType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor() : ChatRepository {

    private val conversations = mutableListOf<ChatConversation>()
    private val messages = mutableMapOf<String, MutableList<ChatMessage>>()

    init {
        // Initialize with some sample data
        initializeSampleData()
    }

    override fun getConversations(page: Int, pageSize: Int): Flow<List<ChatConversation>> = flow {
        delay(500) // Simulate network delay
        val start = page * pageSize
        val end = minOf(start + pageSize, conversations.size)
        val result = if (start < conversations.size) {
            conversations.subList(start, end)
        } else {
            emptyList()
        }
        emit(result)
    }

    override fun getMessages(conversationId: String, page: Int, pageSize: Int): Flow<List<ChatMessage>> = flow {
        delay(300) // Simulate network delay
        val conversationMessages = messages[conversationId]?.sortedByDescending { it.timestamp } ?: emptyList()
        val start = page * pageSize
        val end = minOf(start + pageSize, conversationMessages.size)
        val result = if (start < conversationMessages.size) {
            conversationMessages.subList(start, end).reversed()
        } else {
            emptyList()
        }
        emit(result)
    }

    override suspend fun sendMessage(message: ChatMessage): Result<Unit> = runCatching {
        delay(200) // Simulate network delay
        val messageList = messages.getOrPut(message.conversationId) { mutableListOf() }
        messageList.add(message)
    }

    override suspend fun markConversationAsRead(conversationId: String): Result<Unit> = runCatching {
        val conversation = conversations.find { it.id == conversationId } ?: return@runCatching
        val index = conversations.indexOf(conversation)
        conversations[index] = conversation.copy(unreadCount = 0)
    }

    override suspend fun getConversationById(conversationId: String): Result<ChatConversation?> = runCatching {
        conversations.find { it.id == conversationId }
    }

    override suspend fun searchConversations(query: String): Result<List<ChatConversation>> = runCatching {
        conversations.filter { it.title.contains(query, ignoreCase = true) }
    }

    override fun getConversationUpdates(): Flow<ChatConversation> = flow {
        // For demo purposes, emit updates periodically
        while (true) {
            delay(5000)
            if (conversations.isNotEmpty()) {
                val randomConversation = conversations.random()
                emit(randomConversation.copy(updatedAt = LocalDateTime.now()))
            }
        }
    }

    override fun getMessageUpdates(conversationId: String): Flow<ChatMessage> = flow {
        // For demo purposes, emit message updates periodically
        while (true) {
            delay(10000)
            if (messages.containsKey(conversationId) && messages[conversationId]?.isNotEmpty() == true) {
                val newMessage = createSampleMessage(conversationId, false)
                messages[conversationId]?.add(newMessage)
                emit(newMessage)
            }
        }
    }

    private fun initializeSampleData() {
        val sampleConversations = listOf(
            ChatConversation(
                id = "1",
                title = "Alice Johnson",
                participantIds = listOf("user1", "alice"),
                lastMessage = null,
                unreadCount = 2,
                isPinned = true,
                avatarUrl = "https://images.unsplash.com/photo-1494790108755-2616b612b786?w=150&h=150&fit=crop&crop=face"
            ),
            ChatConversation(
                id = "2",
                title = "Dev Team",
                participantIds = listOf("user1", "bob", "charlie", "diana"),
                lastMessage = null,
                unreadCount = 5,
                isMuted = false,
                isPinned = true,
                conversationType = com.ctonew.composemodular.domain.chat.models.ConversationType.Group,
                avatarUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=150&h=150&fit=crop&crop=face"
            ),
            ChatConversation(
                id = "3",
                title = "Bob Smith",
                participantIds = listOf("user1", "bob"),
                lastMessage = null,
                unreadCount = 0,
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&h=150&fit=crop&crop=face"
            ),
            ChatConversation(
                id = "4",
                title = "Charlie Brown",
                participantIds = listOf("user1", "charlie"),
                lastMessage = null,
                unreadCount = 1,
                isMuted = true,
                avatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&h=150&fit=crop&crop=face"
            )
        )

        conversations.addAll(sampleConversations)

        // Create sample messages for each conversation
        sampleConversations.forEach { conversation ->
            val conversationMessages = createSampleMessages(conversation.id)
            messages[conversation.id] = conversationMessages
        }
    }

    private fun createSampleMessages(conversationId: String): MutableList<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()
        
        val baseTime = LocalDateTime.now().minusDays(1)
        
        when (conversationId) {
            "1" -> { // Alice
                messages.addAll(listOf(
                    createMessage(conversationId, "alice", "Hey! How's it going?", baseTime.plusHours(1), isFromMe = false),
                    createMessage(conversationId, "user1", "Pretty good, working on some exciting projects!", baseTime.plusHours(1).plusMinutes(15), isFromMe = true),
                    createMessage(conversationId, "alice", "That sounds amazing! Tell me more about them", baseTime.plusHours(2), isFromMe = false),
                    createMessage(conversationId, "user1", "Working on a Compose chat app with navigation and cool UI features", baseTime.plusHours(2).plusMinutes(30), isFromMe = true),
                    createMessage(conversationId, "alice", "Nice! I love Jetpack Compose", baseTime.plusHours(3), isFromMe = false)
                ))
            }
            "2" -> { // Dev Team
                messages.addAll(listOf(
                    createMessage(conversationId, "bob", "Team standup in 10 minutes", baseTime.plusHours(8), isFromMe = false),
                    createMessage(conversationId, "charlie", "On my way to the meeting room", baseTime.plusHours(8).plusMinutes(5), isFromMe = false),
                    createMessage(conversationId, "diana", "I'll be there in 5", baseTime.plusHours(8).plusMinutes(8), isFromMe = false),
                    createMessage(conversationId, "user1", "Running late, be there in 10", baseTime.plusHours(8).plusMinutes(10), isFromMe = true),
                    createMessage(conversationId, "bob", "No problem, we'll wait", baseTime.plusHours(8).plusMinutes(12), isFromMe = false)
                ))
            }
            "3" -> { // Bob
                messages.addAll(listOf(
                    createMessage(conversationId, "bob", "Did you see the latest Compose update?", baseTime.plusHours(15), isFromMe = false),
                    createMessage(conversationId, "user1", "Not yet, what are the highlights?", baseTime.plusHours(15).plusMinutes(20), isFromMe = true),
                    createMessage(conversationId, "bob", "Some great performance improvements and new APIs", baseTime.plusHours(16), isFromMe = false),
                    createMessage(conversationId, "user1", "Thanks for letting me know!", baseTime.plusHours(16).plusMinutes(15), isFromMe = true)
                ))
            }
            "4" -> { // Charlie
                messages.addAll(listOf(
                    createMessage(conversationId, "charlie", "Working on some bugs today", baseTime.plusHours(20), isFromMe = false),
                    createMessage(conversationId, "user1", "Same here, what kind of bugs?", baseTime.plusHours(20).plusMinutes(25), isFromMe = true),
                    createMessage(conversationId, "charlie", "Some memory leaks in the image loading", baseTime.plusHours(21), isFromMe = false)
                ))
            }
        }
        
        return messages
    }

    private fun createMessage(
        conversationId: String,
        senderId: String,
        content: String,
        timestamp: LocalDateTime,
        isFromMe: Boolean = false
    ): ChatMessage {
        return ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = senderId,
            content = content,
            timestamp = timestamp,
            messageType = MessageType.Text,
            status = if (isFromMe) MessageStatus.Delivered else MessageStatus.Read,
            isFromMe = isFromMe
        )
    }

    private fun createSampleMessage(conversationId: String, isFromMe: Boolean): ChatMessage {
        val sampleTexts = listOf(
            "This is a new message",
            "Hello there!",
            "How are you doing?",
            "Working on some cool features",
            "Check out this new thing I found",
            "Did you finish the task?",
            "Let's catch up soon",
            "Thanks for the help!"
        )
        
        return ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = if (isFromMe) "user1" else "bot_${conversationId}",
            content = sampleTexts.random(),
            timestamp = LocalDateTime.now(),
            messageType = MessageType.Text,
            status = if (isFromMe) MessageStatus.Sent else MessageStatus.Read,
            isFromMe = isFromMe
        )
    }
}