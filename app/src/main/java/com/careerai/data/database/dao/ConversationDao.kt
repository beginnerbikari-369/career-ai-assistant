package com.careerai.data.database.dao

import androidx.room.*
import com.careerai.data.database.entities.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    
    @Query("SELECT * FROM conversations WHERE userId = :userId AND isArchived = 0 ORDER BY lastMessageAt DESC")
    fun getActiveConversationsFlow(userId: String): Flow<List<ConversationEntity>>
    
    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    suspend fun getConversationById(conversationId: String): ConversationEntity?
    
    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    fun getConversationByIdFlow(conversationId: String): Flow<ConversationEntity?>
    
    @Query("SELECT * FROM conversations WHERE userId = :userId AND context = :context AND isArchived = 0 ORDER BY lastMessageAt DESC LIMIT 1")
    suspend fun getLatestConversationByContext(userId: String, context: String): ConversationEntity?
    
    @Query("SELECT * FROM conversations WHERE userId = :userId AND isArchived = 1 ORDER BY lastMessageAt DESC")
    fun getArchivedConversationsFlow(userId: String): Flow<List<ConversationEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)
    
    @Update
    suspend fun updateConversation(conversation: ConversationEntity)
    
    @Delete
    suspend fun deleteConversation(conversation: ConversationEntity)
    
    @Query("UPDATE conversations SET messageCount = messageCount + 1, lastMessageAt = :timestamp, updatedAt = :timestamp WHERE id = :conversationId")
    suspend fun incrementMessageCount(conversationId: String, timestamp: Long)
    
    @Query("UPDATE conversations SET isArchived = :isArchived, updatedAt = :timestamp WHERE id = :conversationId")
    suspend fun updateArchivedStatus(conversationId: String, isArchived: Boolean, timestamp: Long)
    
    @Query("UPDATE conversations SET title = :title, updatedAt = :timestamp WHERE id = :conversationId")
    suspend fun updateTitle(conversationId: String, title: String, timestamp: Long)
    
    @Query("DELETE FROM conversations WHERE userId = :userId")
    suspend fun deleteAllUserConversations(userId: String)
}