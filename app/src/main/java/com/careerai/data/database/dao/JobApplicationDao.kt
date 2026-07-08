package com.careerai.data.database.dao

import androidx.room.*
import com.careerai.data.database.entities.JobApplicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobApplicationDao {
    
    @Query("SELECT * FROM job_applications WHERE userId = :userId AND isArchived = 0 ORDER BY appliedDate DESC")
    fun getActiveApplicationsFlow(userId: String): Flow<List<JobApplicationEntity>>
    
    @Query("SELECT * FROM job_applications WHERE userId = :userId AND status = :status AND isArchived = 0 ORDER BY appliedDate DESC")
    fun getApplicationsByStatusFlow(userId: String, status: String): Flow<List<JobApplicationEntity>>
    
    @Query("SELECT * FROM job_applications WHERE id = :applicationId")
    suspend fun getApplicationById(applicationId: String): JobApplicationEntity?
    
    @Query("SELECT * FROM job_applications WHERE id = :applicationId")
    fun getApplicationByIdFlow(applicationId: String): Flow<JobApplicationEntity?>
    
    @Query("SELECT * FROM job_applications WHERE userId = :userId AND followUpDate <= :currentDate AND status NOT IN ('offered', 'rejected', 'withdrawn') AND isArchived = 0")
    suspend fun getApplicationsNeedingFollowUp(userId: String, currentDate: Long): List<JobApplicationEntity>
    
    @Query("SELECT * FROM job_applications WHERE userId = :userId AND appliedDate BETWEEN :startDate AND :endDate AND isArchived = 0")
    suspend fun getApplicationsByDateRange(userId: String, startDate: Long, endDate: Long): List<JobApplicationEntity>
    
    @Query("SELECT COUNT(*) FROM job_applications WHERE userId = :userId AND status = :status AND isArchived = 0")
    suspend fun getApplicationsCountByStatus(userId: String, status: String): Int
    
    @Query("SELECT COUNT(*) FROM job_applications WHERE userId = :userId AND isArchived = 0")
    suspend fun getTotalApplicationsCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM job_applications WHERE userId = :userId AND appliedDate >= :startDate AND isArchived = 0")
    suspend fun getApplicationsCountSince(userId: String, startDate: Long): Int
    
    @Query("SELECT AVG(expectedSalary) FROM job_applications WHERE userId = :userId AND expectedSalary IS NOT NULL AND isArchived = 0")
    suspend fun getAverageExpectedSalary(userId: String): Double?
    
    @Query("SELECT company, COUNT(*) as count FROM job_applications WHERE userId = :userId AND isArchived = 0 GROUP BY company ORDER BY count DESC LIMIT :limit")
    suspend fun getTopCompanies(userId: String, limit: Int = 10): List<CompanyCount>
    
    @Query("""
        SELECT * FROM job_applications 
        WHERE userId = :userId 
        AND (jobTitle LIKE '%' || :searchQuery || '%' 
             OR company LIKE '%' || :searchQuery || '%'
             OR description LIKE '%' || :searchQuery || '%'
             OR notes LIKE '%' || :searchQuery || '%')
        AND isArchived = 0
        ORDER BY appliedDate DESC
        LIMIT :limit
    """)
    suspend fun searchApplications(userId: String, searchQuery: String, limit: Int = 50): List<JobApplicationEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: JobApplicationEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplications(applications: List<JobApplicationEntity>)
    
    @Update
    suspend fun updateApplication(application: JobApplicationEntity)
    
    @Delete
    suspend fun deleteApplication(application: JobApplicationEntity)
    
    @Query("UPDATE job_applications SET status = :status, updatedAt = :timestamp WHERE id = :applicationId")
    suspend fun updateStatus(applicationId: String, status: String, timestamp: Long)
    
    @Query("UPDATE job_applications SET followUpDate = :followUpDate, updatedAt = :timestamp WHERE id = :applicationId")
    suspend fun updateFollowUpDate(applicationId: String, followUpDate: Long?, timestamp: Long)
    
    @Query("UPDATE job_applications SET isArchived = :isArchived, updatedAt = :timestamp WHERE id = :applicationId")
    suspend fun updateArchivedStatus(applicationId: String, isArchived: Boolean, timestamp: Long)
    
    @Query("UPDATE job_applications SET actualSalary = :salary, updatedAt = :timestamp WHERE id = :applicationId")
    suspend fun updateActualSalary(applicationId: String, salary: Double?, timestamp: Long)
    
    @Query("DELETE FROM job_applications WHERE userId = :userId")
    suspend fun deleteAllUserApplications(userId: String)
}

data class CompanyCount(
    val company: String,
    val count: Int
)