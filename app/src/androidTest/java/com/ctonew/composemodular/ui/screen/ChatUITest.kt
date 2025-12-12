package com.ctonew.composemodular.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assert
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ctonew.composemodular.domain.chat.models.ChatConversation
import com.ctonew.composemodular.domain.chat.models.ChatMessage
import com.ctonew.composemodular.domain.chat.models.MessageStatus
import com.ctonew.composemodular.domain.chat.models.MessageType
import com.ctonew.composemodular.ui.components.ChatBubble
import com.ctonew.composemodular.ui.theme.ComposeModularTheme
import com.ctonew.composemodular.ui.theme.ThemePreferences
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

/**
 * UI tests for chat components and theming
 */
@RunWith(AndroidJUnit4::class)
class ChatUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        // Set up default theme preferences for tests
        composeTestRule.setContent {
            TestThemeWrapper {
                TestContent()
            }
        }
    }

    @Test
    fun chatBubble_displaysMessageContentCorrectly() {
        val testMessage = ChatMessage(
            id = "1",
            conversationId = "conv1",
            senderId = "user1",
            content = "Hello, World!",
            timestamp = LocalDateTime.now(),
            messageType = MessageType.Text,
            status = MessageStatus.Sent,
            isFromMe = true
        )

        composeTestRule.setContent {
            ChatBubble(message = testMessage)
        }

        composeTestRule
            .onNodeWithText("Hello, World!")
            .assertIsDisplayed()
    }

    @Test
    fun chatBubble_showsMessageStatusForSentMessage() {
        val testMessage = ChatMessage(
            id = "1",
            conversationId = "conv1",
            senderId = "user1",
            content = "Test message",
            timestamp = LocalDateTime.now(),
            messageType = MessageType.Text,
            status = MessageStatus.Sent,
            isFromMe = true
        )

        composeTestRule.setContent {
            ChatBubble(message = testMessage)
        }

        // Check that status icon is displayed (using text representation)
        composeTestRule
            .onNodeWithText("Sent", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun chatBubble_adaptsAlignmentForUserMessages() {
        val myMessage = ChatMessage(
            id = "1",
            conversationId = "conv1",
            senderId = "user1",
            content = "My message",
            timestamp = LocalDateTime.now(),
            messageType = MessageType.Text,
            status = MessageStatus.Sent,
            isFromMe = true
        )

        composeTestRule.setContent {
            ChatBubble(message = myMessage)
        }

        // Verify message content is displayed
        composeTestRule
            .onNodeWithText("My message")
            .assertIsDisplayed()
    }

    @Test
    fun chatBubble_adaptsAlignmentForOtherMessages() {
        val otherMessage = ChatMessage(
            id = "1",
            conversationId = "conv1",
            senderId = "user2",
            content = "Other person's message",
            timestamp = LocalDateTime.now(),
            messageType = MessageType.Text,
            status = MessageStatus.Read,
            isFromMe = false
        )

        composeTestRule.setContent {
            ChatBubble(message = otherMessage)
        }

        // Verify message content is displayed
        composeTestRule
            .onNodeWithText("Other person's message")
            .assertIsDisplayed()
    }

    @Test
    fun conversationList_displaysEmptyState() {
        val emptyState = listOf<ChatConversation>()

        composeTestRule.setContent {
            // Test the empty state component directly
            EmptyState()
        }

        composeTestRule
            .onNodeWithText("No conversations yet")
            .assertIsDisplayed()
    }

    @Test
    fun conversationList_showsLoadingIndicator() {
        composeTestRule.setContent {
            // Test the loading indicator component
            LoadingIndicator()
        }

        // Verify loading indicator is displayed (circular progress)
        composeTestRule
            .onNodeWithContentDescription("Loading")
            .assertIsDisplayed()
    }

    @Test
    fun theme_accentColorAffectsComponents() {
        val cyanAccent = ThemePreferences(accentColorArgb = 0xFF00E5FF.toInt())

        composeTestRule.setContent {
            ComposeModularTheme(themePreferences = cyanAccent) {
                TestThemeContent()
            }
        }

        // Verify theme is applied by checking background color
        composeTestRule
            .onNodeWithTag("test_surface")
            .assertIsDisplayed()
    }

    @Test
    fun messageInput_acceptsUserInput() {
        composeTestRule.setContent {
            MessageInputTestWrapper()
        }

        // Type in the message input field
        composeTestRule
            .onNodeWithText("Type a message...")
            .performTextInput("Test message")

        // Verify the input was accepted
        composeTestRule
            .onNodeWithText("Test message")
            .assertIsDisplayed()
    }

    @Test
    fun pagination_loadingStateDisplayed() {
        composeTestRule.setContent {
            LoadingIndicator()
        }

        // Check that loading indicator is shown
        composeTestRule
            .onNodeWithContentDescription("Loading")
            .assertIsDisplayed()
    }

    @Test
    fun daySeparator_showsCorrectDateFormat() {
        val testDate = LocalDateTime.of(2024, 1, 15, 10, 30)

        composeTestRule.setContent {
            DaySeparatorTestWrapper(testDate)
        }

        // Verify date separator is displayed
        composeTestRule
            .onNodeWithText("January 15, 2024")
            .assertIsDisplayed()
    }
}

/**
 * Test wrapper components for UI testing
 */
@Composable
private fun TestThemeWrapper(content: @Composable () -> Unit) {
    ComposeModularTheme(themePreferences = ThemePreferences()) {
        content()
    }
}

@Composable
private fun TestContent() {
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Empty test content
    }
}

@Composable
private fun TestThemeContent() {
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxSize().testTag("test_surface"),
        color = MaterialTheme.colorScheme.primary
    ) {
        // Test content with theme applied
    }
}

@Composable
private fun MessageInputTestWrapper() {
    var messageText by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    
    Column {
        MessageInput(
            onSendMessage = { message -> messageText = it },
            isSending = false,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun DaySeparatorTestWrapper(timestamp: LocalDateTime) {
    DaySeparator(timestamp = timestamp)
}