package com.careerai.data.database.dao

import androidx.room.*
import com.careerai.data.database.entities.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    
    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY date DESC")
    fun getJournalEntriesFlow(userId: String): Flow<List<JournalEntryEntity>>
    
    @Query("SELECT * FROM journal_entries WHERE id = :entryId")
    suspend fun getEntryById(entryId: String): JournalEntryEntity?
    
    @Query("SELECT * FROM journal_entries WHERE id = :entryId")
    fun getEntryByIdFlow(entryId: String): Flow<JournalEntryEntity?>
    
    @Query("SELECT * FROM journal_entries WHERE userId = :userId AND date = :date")
    suspend fun getEntryByDate(userId: String, date: String): JournalEntryEntity?
    
    @Query("SELECT * FROM journal_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getEntriesInDateRange(userId: String, startDate: String, endDate: String): List<JournalEntryEntity>
    
    @Query("SELECT * FROM journal_entries WHERE userId = :userId AND mood IS NOT NULL ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentEntriesWithMood(userId: String, limit: Int = 30): List<JournalEntryEntity>
    
    @Query("SELECT AVG(energy) FROM journal_entries WHERE userId = :userId AND energy IS NOT NULL AND date BETWEEN :startDate AND :endDate")
    suspend fun getAverageEnergyInRange(userId: String, startDate: String, endDate: String): Double?
    
    @Query("SELECT AVG(productivity) FROM journal_entries WHERE userId = :userId AND productivity IS NOT NULL AND date BETWEEN :startDate AND :endDate")
    suspend fun getAverageProductivityInRange(userId: String, startDate: String, endDate: String): Double?
    
    @Query("SELECT AVG(stress) FROM journal_entries WHERE userId = :userId AND stress IS NOT NULL AND date BETWEEN :startDate AND :endDate")
    suspend fun getAverageStressInRange(userId: String, startDate: String, endDate: String): Double?
    
    @Query("SELECT COUNT(*) FROM journal_entries WHERE userId = :userId")
    suspend fun getTotalEntriesCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM journal_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getEntriesCountInRange(userId: String, startDate: String, endDate: String): Int
    
    @Query("""
        SELECT * FROM journal_entries 
        WHERE userId = :userId 
        AND (content LIKE '%' || :searchQuery || '%' 
             OR title LIKE '%' || :searchQuery || '%'
             OR highlights LIKE '%' || :searchQuery || '%'
             OR learnings LIKE '%' || :searchQuery || '%')
        ORDER BY date DESC
        LIMIT :limit
    """)
    suspend fun searchEntries(userId: String, searchQuery: String, limit: Int = 50): List<JournalEntryEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntryEntity)
    
    @Update
    suspend fun updateEntry(entry: JournalEntryEntity)
    
    @Delete
    suspend fun deleteEntry(entry: JournalEntryEntity)
    
    @Query("DELETE FROM journal_entries WHERE userId = :userId")
    suspend fun deleteAllUserEntries(userId: String)
    
    @Query("DELETE FROM journal_entries WHERE date < :cutoffDate")
    suspend fun deleteOldEntries(cutoffDate: String)
}