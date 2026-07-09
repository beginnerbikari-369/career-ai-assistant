package com.careerai.data.database.dao

import androidx.room.*
import com.careerai.data.database.entities.SkillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {
    
    @Query("SELECT * FROM skills WHERE userId = :userId ORDER BY priority DESC, currentLevel DESC")
    fun getSkillsFlow(userId: String): Flow<List<SkillEntity>>
    
    @Query("SELECT * FROM skills WHERE userId = :userId AND category = :category ORDER BY currentLevel DESC")
    fun getSkillsByCategoryFlow(userId: String, category: String): Flow<List<SkillEntity>>
    
    @Query("SELECT * FROM skills WHERE id = :skillId")
    suspend fun getSkillById(skillId: String): SkillEntity?
    
    @Query("SELECT * FROM skills WHERE id = :skillId")
    fun getSkillByIdFlow(skillId: String): Flow<SkillEntity?>
    
    @Query("SELECT * FROM skills WHERE userId = :userId AND isCareerRelevant = 1 ORDER BY priority DESC, currentLevel ASC")
    fun getCareerRelevantSkillsFlow(userId: String): Flow<List<SkillEntity>>
    
    @Query("SELECT * FROM skills WHERE userId = :userId AND currentLevel < targetLevel ORDER BY priority DESC")
    fun getSkillsNeedingImprovementFlow(userId: String): Flow<List<SkillEntity>>
    
    @Query("SELECT * FROM skills WHERE userId = :userId AND priority = :priority ORDER BY currentLevel ASC")
    fun getSkillsByPriorityFlow(userId: String, priority: String): Flow<List<SkillEntity>>
    
    @Query("SELECT AVG(currentLevel) FROM skills WHERE userId = :userId AND category = :category")
    suspend fun getAverageSkillLevelByCategory(userId: String, category: String): Double?
    
    @Query("SELECT COUNT(*) FROM skills WHERE userId = :userId AND currentLevel >= targetLevel")
    suspend fun getTargetAchievedSkillsCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM skills WHERE userId = :userId")
    suspend fun getTotalSkillsCount(userId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: SkillEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<SkillEntity>)
    
    @Update
    suspend fun updateSkill(skill: SkillEntity)
    
    @Delete
    suspend fun deleteSkill(skill: SkillEntity)
    
    @Query("UPDATE skills SET currentLevel = :currentLevel, assessmentDate = :assessmentDate, updatedAt = :timestamp WHERE id = :skillId")
    suspend fun updateSkillLevel(skillId: String, currentLevel: Int, assessmentDate: Long, timestamp: Long)
    
    @Query("UPDATE skills SET targetLevel = :targetLevel, updatedAt = :timestamp WHERE id = :skillId")
    suspend fun updateTargetLevel(skillId: String, targetLevel: Int, timestamp: Long)
    
    @Query("UPDATE skills SET endorsements = endorsements + 1, updatedAt = :timestamp WHERE id = :skillId")
    suspend fun incrementEndorsements(skillId: String, timestamp: Long)
    
    @Query("UPDATE skills SET isVerified = :isVerified, updatedAt = :timestamp WHERE id = :skillId")
    suspend fun updateVerificationStatus(skillId: String, isVerified: Boolean, timestamp: Long)
    
    @Query("DELETE FROM skills WHERE userId = :userId")
    suspend fun deleteAllUserSkills(userId: String)
}