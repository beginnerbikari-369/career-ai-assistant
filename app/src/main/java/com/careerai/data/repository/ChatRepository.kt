package com.careerai.data.repository

import com.careerai.data.api.AIService
import com.careerai.data.api.ChatMessage as ApiChatMessage
import com.careerai.data.database.dao.ConversationDao
import com.careerai.data.database.dao.MessageDao
import com.careerai.data.database.entities.ConversationEntity
import com.careerai.data.database.entities.MessageEntity
import com.careerai.domain.model.Conversation
import com.careerai.domain.model.ConversationContext
import com.careerai.domain.model.Message
import com.careerai.domain.model.MessageType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val aiService: AIService
) {
    
    fun getConversationsFlow(userId: String): Flow<List<Conversation>> {
        return conversationDao.getActiveConversationsFlow(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    fun getConversationWithMessagesFlow(conversationId: String): Flow<Conversation?> {
        return conversationDao.getConversationByIdFlow(conversationId)
            .map { entity -> entity?.toDomain() }
    }
    
    fun getMessagesFlow(conversationId: String): Flow<List<Message>> {
        return messageDao.getMessagesFlow(conversationId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    suspend fun createConversation(
        userId: String,
        title: String,
        context: ConversationContext
    ): Result<String> {
        return try {
            val conversationId = UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis()
            
            val conversation = ConversationEntity(
                id = conversationId,
                userId = userId,
                title = title,
                context = context.name.lowercase(),
                createdAt = timestamp,
                updatedAt = timestamp,
                lastMessageAt = timestamp
            )
            
            conversationDao.insertConversation(conversation)
            Result.success(conversationId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendMessage(
        conversationId: String,
        content: String,
        context: ConversationContext
    ): Result<Message> {
        return try {
            val timestamp = System.currentTimeMillis()
            
            // Save user message
            val userMessageId = UUID.randomUUID().toString()
            val userMessage = MessageEntity(
                id = userMessageId,
                conversationId = conversationId,
                content = content,
                isFromUser = true,
                timestamp = timestamp
            )
            messageDao.insertMessage(userMessage)
            
            // Update conversation
            conversationDao.incrementMessageCount(conversationId, timestamp)
            
            // Get conversation history for AI context
            val recentMessages = messageDao.getRecentMessages(conversationId, 10)
            val apiMessages = recentMessages.map { message ->
                ApiChatMessage(
                    role = if (message.isFromUser) "user" else "assistant",
                    content = message.content
                )
            }
            
            // Send to AI service
            val aiResponse = aiService.sendMessage(
                messages = apiMessages,
                context = context
            )
            
            aiResponse.fold(
                onSuccess = { response ->
                    val aiMessageContent = response.choices.firstOrNull()?.message?.content
                        ?: "I'm sorry, I couldn't generate a response."
                    
                    val aiMessageId = UUID.randomUUID().toString()
                    val aiMessage = MessageEntity(
                        id = aiMessageId,
                        conversationId = conversationId,
                        content = aiMessageContent,
                        isFromUser = false,
                        timestamp = System.currentTimeMillis(),
                        tokens = response.usage?.total_tokens,
                        model = response.model
                    )
                    
                    messageDao.insertMessage(aiMessage)
                    conversationDao.incrementMessageCount(conversationId, aiMessage.timestamp)
                    
                    Result.success(aiMessage.toDomain())
                },
                onFailure = { error ->
                    // Create an error message for the user
                    val errorMessageId = UUID.randomUUID().toString()
                    val errorMessage = MessageEntity(
                        id = errorMessageId,
                        conversationId = conversationId,
                        content = "I'm sorry, I encountered an error: ${error.message}",
                        isFromUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                    
                    messageDao.insertMessage(errorMessage)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateConversationTitle(conversationId: String, title: String): Result<Unit> {
        return try {
            conversationDao.updateTitle(conversationId, title, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun archiveConversation(conversationId: String): Result<Unit> {
        return try {
            conversationDao.updateArchivedStatus(conversationId, true, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteConversation(conversationId: String): Result<Unit> {
        return try {
            messageDao.deleteAllConversationMessages(conversationId)
            val conversation = conversationDao.getConversationById(conversationId)
            conversation?.let { conversationDao.deleteConversation(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchMessages(userId: String, query: String): Result<List<Message>> {
        return try {
            val messages = messageDao.searchMessages(userId, query, 50)
            Result.success(messages.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun isAIConfigured(): Boolean {
        return aiService.isApiKeyConfigured()
    }
    
    suspend fun testAIConnection(): Result<Boolean> {
        return aiService.testConnection()
    }
}

// Extension functions for domain mapping
private fun ConversationEntity.toDomain(): Conversation {
    return Conversation(
        id = id,
        userId = userId,
        title = title,
        context = ConversationContext.valueOf(context.uppercase()),
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastMessageAt = lastMessageAt,
        messageCount = messageCount,
        isArchived = isArchived
    )
}

private fun MessageEntity.toDomain(): Message {
    return Message(
        id = id,
        conversationId = conversationId,
        content = content,
        isFromUser = isFromUser,
        timestamp = timestamp,
        messageType = MessageType.valueOf(messageType.uppercase()),
        tokens = tokens,
        model = model
    )
}