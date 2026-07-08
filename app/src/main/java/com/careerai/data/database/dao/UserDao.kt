package com.careerai.data.database.dao

import androidx.room.*
import com.careerai.data.database.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdFlow(userId: String): Flow<UserEntity?>
    
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("UPDATE users SET preferences = :preferences, updatedAt = :timestamp WHERE id = :userId")
    suspend fun updateUserPreferences(userId: String, preferences: String, timestamp: Long)
    
    @Query("UPDATE users SET isOnboardingCompleted = :isCompleted, updatedAt = :timestamp WHERE id = :userId")
    suspend fun updateOnboardingStatus(userId: String, isCompleted: Boolean, timestamp: Long)
}