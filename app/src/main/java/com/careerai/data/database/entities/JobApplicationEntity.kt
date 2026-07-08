package com.careerai.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "job_applications")
@Serializable
data class JobApplicationEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val jobTitle: String,
    val company: String,
    val jobUrl: String?,
    val status: String, // applied, interview_scheduled, interview_completed, offered, rejected, withdrawn
    val appliedDate: Long,
    val expectedSalary: Double?,
    val actualSalary: Double?,
    val location: String?,
    val workType: String?, // remote, hybrid, onsite
    val jobType: String?, // full_time, part_time, contract, internship
    val description: String?,
    val requirements: String?,
    val notes: String?,
    val contactPerson: String?,
    val contactEmail: String?,
    val referralSource: String?, // linkedin, company_website, referral, job_board
    val interviewDates: String? = null, // JSON array of interview timestamps
    val followUpDate: Long? = null,
    val rejectionReason: String?,
    val offerDetails: String?, // JSON object with offer details
    val priority: String = "medium", // high, medium, low
    val tags: String? = null, // JSON array of tags
    val attachments: String? = null, // JSON array of document paths
    val createdAt: Long,
    val updatedAt: Long,
    val isArchived: Boolean = false
)