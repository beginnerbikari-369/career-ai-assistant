package com.careerai.data.database.dao

import androidx.room.*
import com.careerai.data.database.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesFlow(conversationId: String): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessages(conversationId: String): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessage(conversationId: String): MessageEntity?
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(conversationId: String, limit: Int): List<MessageEntity>
    
    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId")
    suspend fun getMessageCount(conversationId: String): Int
    
    @Query("SELECT SUM(tokens) FROM messages WHERE conversationId = :conversationId AND tokens IS NOT NULL")
    suspend fun getTotalTokens(conversationId: String): Int?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)
    
    @Update
    suspend fun updateMessage(message: MessageEntity)
    
    @Delete
    suspend fun deleteMessage(message: MessageEntity)
    
    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteAllConversationMessages(conversationId: String)
    
    @Query("DELETE FROM messages WHERE conversationId IN (SELECT id FROM conversations WHERE userId = :userId)")
    suspend fun deleteAllUserMessages(userId: String)
    
    @Query("""
        SELECT * FROM messages 
        WHERE conversationId IN (SELECT id FROM conversations WHERE userId = :userId)
        AND content LIKE '%' || :searchQuery || '%'
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    suspend fun searchMessages(userId: String, searchQuery: String, limit: Int = 50): List<MessageEntity>
}