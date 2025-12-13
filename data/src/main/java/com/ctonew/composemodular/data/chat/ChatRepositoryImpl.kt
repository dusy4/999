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
        val baseTime = LocalDateTime.now()
        
        val sampleConversations = listOf(
            ChatConversation(
                id = "conv_1",
                title = "Sarah Chen",
                participantIds = listOf("user1", "user2"),
                lastMessage = null,
                unreadCount = 2,
                isPinned = true,
                avatarUrl = "https://i.pravatar.cc/150?img=1",
                createdAt = baseTime.minusDays(30),
                updatedAt = baseTime.minusMinutes(5)
            ),
            ChatConversation(
                id = "conv_2",
                title = "Design Team",
                participantIds = listOf("user1", "user3", "user4", "user5"),
                lastMessage = null,
                unreadCount = 3,
                isMuted = false,
                isPinned = true,
                conversationType = com.ctonew.composemodular.domain.chat.models.ConversationType.Group,
                avatarUrl = "https://i.pravatar.cc/150?img=5",
                createdAt = baseTime.minusDays(25),
                updatedAt = baseTime.minusHours(2)
            ),
            ChatConversation(
                id = "conv_3",
                title = "Alex Rivera",
                participantIds = listOf("user1", "user3"),
                lastMessage = null,
                unreadCount = 0,
                avatarUrl = "https://i.pravatar.cc/150?img=2",
                createdAt = baseTime.minusDays(20),
                updatedAt = baseTime.minusHours(3)
            ),
            ChatConversation(
                id = "conv_4",
                title = "Jordan Kim",
                participantIds = listOf("user1", "user4"),
                lastMessage = null,
                unreadCount = 1,
                isMuted = false,
                avatarUrl = "https://i.pravatar.cc/150?img=3",
                createdAt = baseTime.minusDays(15),
                updatedAt = baseTime.minusDays(1)
            ),
            ChatConversation(
                id = "conv_5",
                title = "Taylor Tech",
                participantIds = listOf("user1", "user5"),
                lastMessage = null,
                unreadCount = 0,
                avatarUrl = "https://i.pravatar.cc/150?img=4",
                createdAt = baseTime.minusDays(10),
                updatedAt = baseTime.minusDays(2)
            )
        )

        conversations.addAll(sampleConversations)

        // Create sample messages for each conversation
        sampleConversations.forEach { conversation ->
            val conversationMessages = createSampleMessages(conversation.id)
            messages[conversation.id] = conversationMessages
            
            // Update conversation with last message
            if (conversationMessages.isNotEmpty()) {
                val lastMsg = conversationMessages.last()
                val index = conversations.indexOf(conversation)
                conversations[index] = conversation.copy(
                    lastMessage = lastMsg,
                    updatedAt = lastMsg.timestamp
                )
            }
        }
    }

    private fun createSampleMessages(conversationId: String): MutableList<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()
        val baseTime = LocalDateTime.now()
        
        when (conversationId) {
            "conv_1" -> { // Sarah Chen
                messages.addAll(listOf(
                    createMessage(conversationId, "user2", "Hey! How's the new project going?", baseTime.minusHours(5), isFromMe = false),
                    createMessage(conversationId, "user1", "It's going great! Just finished the UI design phase", baseTime.minusHours(4).plusMinutes(30), isFromMe = true),
                    createMessage(conversationId, "user2", "That's awesome! I'd love to see the designs 🎨", baseTime.minusHours(4).plusMinutes(15), isFromMe = false),
                    createMessage(conversationId, "user1", "I'll send them over tomorrow. Still refining a few things.", baseTime.minusHours(3).plusMinutes(45), isFromMe = true),
                    createMessage(conversationId, "user2", "Perfect! Let me know if you need any feedback", baseTime.minusHours(3).plusMinutes(20), isFromMe = false),
                    createMessage(conversationId, "user1", "Will do, thanks! 😊", baseTime.minusMinutes(5), isFromMe = true)
                ))
            }
            "conv_2" -> { // Design Team
                messages.addAll(listOf(
                    createMessage(conversationId, "user3", "Morning everyone! Quick update on the redesign", baseTime.minusHours(2), isFromMe = false),
                    createMessage(conversationId, "user4", "Hey! I'm excited about the new direction", baseTime.minusHours(1).plusMinutes(50), isFromMe = false),
                    createMessage(conversationId, "user1", "Looking forward to seeing the mockups", baseTime.minusHours(1).plusMinutes(45), isFromMe = true),
                    createMessage(conversationId, "user5", "I've started on the component library", baseTime.minusHours(1).plusMinutes(30), isFromMe = false),
                    createMessage(conversationId, "user3", "Great! Can you share the progress by EOD?", baseTime.minusHours(1).plusMinutes(15), isFromMe = false),
                    createMessage(conversationId, "user5", "Absolutely, will have something ready", baseTime.minusMinutes(45), isFromMe = false)
                ))
            }
            "conv_3" -> { // Alex Rivera
                messages.addAll(listOf(
                    createMessage(conversationId, "user3", "Did you check out the new design system docs?", baseTime.minusHours(3), isFromMe = false),
                    createMessage(conversationId, "user1", "Not yet, I'll take a look now", baseTime.minusHours(2).plusMinutes(50), isFromMe = true),
                    createMessage(conversationId, "user3", "They're really comprehensive. Let me know what you think", baseTime.minusHours(2).plusMinutes(30), isFromMe = false),
                    createMessage(conversationId, "user1", "Impressive work! Really well organized", baseTime.minusHours(1).plusMinutes(20), isFromMe = true),
                    createMessage(conversationId, "user3", "Thanks! Much more to come", baseTime.minusHours(1), isFromMe = false)
                ))
            }
            "conv_4" -> { // Jordan Kim
                messages.addAll(listOf(
                    createMessage(conversationId, "user4", "Just captured some great shots today! 📸", baseTime.minusDays(1), isFromMe = false),
                    createMessage(conversationId, "user1", "That's awesome! I'd love to see them", baseTime.minusDays(1).plusHours(1), isFromMe = true),
                    createMessage(conversationId, "user4", "Will send them your way soon", baseTime.minusDays(1).plusHours(2), isFromMe = false)
                ))
            }
            "conv_5" -> { // Taylor Tech
                messages.addAll(listOf(
                    createMessage(conversationId, "user5", "The latest commit is ready for review", baseTime.minusDays(2), isFromMe = false),
                    createMessage(conversationId, "user1", "I'll check it out and provide feedback", baseTime.minusDays(2).plusHours(2), isFromMe = true),
                    createMessage(conversationId, "user5", "Thanks! Looking forward to your thoughts", baseTime.minusDays(2).plusHours(3), isFromMe = false)
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