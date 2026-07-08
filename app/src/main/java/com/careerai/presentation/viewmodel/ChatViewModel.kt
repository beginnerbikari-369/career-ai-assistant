package com.careerai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.careerai.data.repository.ChatRepository
import com.careerai.domain.model.Conversation
import com.careerai.domain.model.ConversationContext
import com.careerai.domain.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private val _currentUserId = MutableStateFlow("") // Will be set from auth
    private val _currentConversationId = MutableStateFlow<String?>(null)
    
    init {
        // TODO: Get user ID from auth service
        _currentUserId.value = "dummy_user_id"
        loadConversations()
    }
    
    fun setUserId(userId: String) {
        _currentUserId.value = userId
        loadConversations()
    }
    
    fun loadConversations() {
        val userId = _currentUserId.value
        if (userId.isNotBlank()) {
            viewModelScope.launch {
                chatRepository.getConversationsFlow(userId)
                    .catch { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
                    .collect { conversations ->
                        _uiState.update { it.copy(conversations = conversations) }
                    }
            }
        }
    }
    
    fun selectConversation(conversationId: String) {
        _currentConversationId.value = conversationId
        _uiState.update { 
            it.copy(
                selectedConversationId = conversationId,
                messages = emptyList()
            )
        }
        
        viewModelScope.launch {
            chatRepository.getMessagesFlow(conversationId)
                .catch { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
                .collect { messages ->
                    _uiState.update { it.copy(messages = messages) }
                }
        }
    }
    
    fun createNewConversation(context: ConversationContext) {
        val userId = _currentUserId.value
        if (userId.isBlank()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val title = generateConversationTitle(context)
            val result = chatRepository.createConversation(userId, title, context)
            
            result.fold(
                onSuccess = { conversationId ->
                    selectConversation(conversationId)
                    switchContext(context)
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }
    
    fun sendMessage(content: String) {
        val conversationId = _currentConversationId.value
        val currentContext = _uiState.value.selectedContext
        
        if (conversationId == null || content.isBlank()) return
        
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true,
                    currentMessage = ""
                )
            }
            
            val result = chatRepository.sendMessage(conversationId, content, currentContext)
            
            result.fold(
                onSuccess = { aiMessage ->
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }
    
    fun switchContext(context: ConversationContext) {
        _uiState.update { it.copy(selectedContext = context) }
    }
    
    fun updateCurrentMessage(message: String) {
        _uiState.update { it.copy(currentMessage = message) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun archiveConversation(conversationId: String) {
        viewModelScope.launch {
            chatRepository.archiveConversation(conversationId)
                .fold(
                    onSuccess = {
                        if (_currentConversationId.value == conversationId) {
                            _currentConversationId.value = null
                            _uiState.update { 
                                it.copy(
                                    selectedConversationId = null,
                                    messages = emptyList()
                                )
                            }
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
                )
        }
    }
    
    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            chatRepository.deleteConversation(conversationId)
                .fold(
                    onSuccess = {
                        if (_currentConversationId.value == conversationId) {
                            _currentConversationId.value = null
                            _uiState.update { 
                                it.copy(
                                    selectedConversationId = null,
                                    messages = emptyList()
                                )
                            }
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
                )
        }
    }
    
    fun searchMessages(query: String) {
        val userId = _currentUserId.value
        if (userId.isBlank() || query.isBlank()) return
        
        viewModelScope.launch {
            chatRepository.searchMessages(userId, query)
                .fold(
                    onSuccess = { messages ->
                        _uiState.update { it.copy(searchResults = messages) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
                )
        }
    }
    
    fun clearSearch() {
        _uiState.update { it.copy(searchResults = null) }
    }
    
    private fun generateConversationTitle(context: ConversationContext): String {
        return when (context) {
            ConversationContext.GENERAL -> "General Chat"
            ConversationContext.CAREER -> "Career Discussion"
            ConversationContext.GOALS -> "Goal Planning"
            ConversationContext.HABITS -> "Habit Coaching"
            ConversationContext.PLANNING -> "Productivity Planning"
        }
    }
    
    fun isAIConfigured(): Boolean {
        return chatRepository.isAIConfigured()
    }
}

data class ChatUiState(
    val conversations: List<Conversation> = emptyList(),
    val selectedConversationId: String? = null,
    val messages: List<Message> = emptyList(),
    val currentMessage: String = "",
    val selectedContext: ConversationContext = ConversationContext.GENERAL,
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchResults: List<Message>? = null
)