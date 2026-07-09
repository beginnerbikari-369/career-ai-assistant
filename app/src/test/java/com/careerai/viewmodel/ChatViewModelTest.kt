package com.careerai.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.careerai.data.repository.ChatRepository
import com.careerai.domain.model.*
import com.careerai.presentation.viewmodel.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ChatViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = UnconfinedTestDispatcher()
    
    @Mock
    private lateinit var chatRepository: ChatRepository
    
    private lateinit var viewModel: ChatViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Setup default mock responses
        whenever(chatRepository.getConversationsFlow(any())).thenReturn(flowOf(emptyList()))
        whenever(chatRepository.getMessagesFlow(any())).thenReturn(flowOf(emptyList()))
        whenever(chatRepository.isAIConfigured()).thenReturn(true)
        
        viewModel = ChatViewModel(chatRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should have default values`() {
        val uiState = viewModel.uiState.value
        
        assertEquals(emptyList<Conversation>(), uiState.conversations)
        assertEquals(null, uiState.selectedConversationId)
        assertEquals(emptyList<Message>(), uiState.messages)
        assertEquals("", uiState.currentMessage)
        assertEquals(ConversationContext.GENERAL, uiState.selectedContext)
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
    }
    
    @Test
    fun `switchContext should update selected context`() = runTest {
        val newContext = ConversationContext.CAREER
        
        viewModel.switchContext(newContext)
        
        val uiState = viewModel.uiState.value
        assertEquals(newContext, uiState.selectedContext)
    }
    
    @Test
    fun `updateCurrentMessage should update current message`() = runTest {
        val testMessage = "Hello, AI assistant!"
        
        viewModel.updateCurrentMessage(testMessage)
        
        val uiState = viewModel.uiState.value
        assertEquals(testMessage, uiState.currentMessage)
    }
    
    @Test
    fun `createNewConversation should call repository and update state`() = runTest {
        val context = ConversationContext.GOALS
        val conversationId = "test_conversation_123"
        
        whenever(chatRepository.createConversation(any(), any(), eq(context)))
            .thenReturn(Result.success(conversationId))
        whenever(chatRepository.getMessagesFlow(conversationId))
            .thenReturn(flowOf(emptyList()))
        
        viewModel.createNewConversation(context)
        
        verify(chatRepository).createConversation(any(), any(), eq(context))
        
        val uiState = viewModel.uiState.value
        assertEquals(conversationId, uiState.selectedConversationId)
        assertEquals(context, uiState.selectedContext)
        assertFalse(uiState.isLoading)
    }
    
    @Test
    fun `sendMessage should call repository when conversation is selected`() = runTest {
        val conversationId = "test_conversation_123"
        val messageContent = "Test message"
        val testMessage = Message(
            id = "msg_123",
            conversationId = conversationId,
            content = "AI response",
            isFromUser = false,
            timestamp = System.currentTimeMillis()
        )
        
        // Setup state
        viewModel.selectConversation(conversationId)
        viewModel.updateCurrentMessage(messageContent)
        
        whenever(chatRepository.sendMessage(conversationId, messageContent, ConversationContext.GENERAL))
            .thenReturn(Result.success(testMessage))
        
        viewModel.sendMessage(messageContent)
        
        verify(chatRepository).sendMessage(conversationId, messageContent, ConversationContext.GENERAL)
        
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals("", uiState.currentMessage) // Should be cleared after sending
    }
    
    @Test
    fun `sendMessage should handle repository error`() = runTest {
        val conversationId = "test_conversation_123"
        val messageContent = "Test message"
        val errorMessage = "Network error"
        
        viewModel.selectConversation(conversationId)
        viewModel.updateCurrentMessage(messageContent)
        
        whenever(chatRepository.sendMessage(any(), any(), any()))
            .thenReturn(Result.failure(Exception(errorMessage)))
        
        viewModel.sendMessage(messageContent)
        
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(errorMessage, uiState.error)
    }
    
    @Test
    fun `clearError should reset error state`() = runTest {
        // Set an error first
        viewModel.updateCurrentMessage("test")
        whenever(chatRepository.sendMessage(any(), any(), any()))
            .thenReturn(Result.failure(Exception("Test error")))
        viewModel.selectConversation("test_id")
        viewModel.sendMessage("test")
        
        // Clear the error
        viewModel.clearError()
        
        val uiState = viewModel.uiState.value
        assertNull(uiState.error)
    }
}