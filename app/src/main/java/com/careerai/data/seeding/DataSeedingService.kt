package com.careerai.data.seeding

import com.careerai.data.repository.GoalRepository
import com.careerai.data.repository.HabitRepository
import com.careerai.data.repository.SkillRepository
import com.careerai.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSeedingService @Inject constructor(
    private val goalRepository: GoalRepository,
    private val habitRepository: HabitRepository,
    private val skillRepository: SkillRepository
) {
    
    suspend fun seedSampleData(userId: String): Result<Unit> {
        return try {
            // Check if user already has data
            val existingGoals = goalRepository.getGoalsFlow(userId)
            // If data exists, don't reseed (implement a check here)
            
            createSampleGoals(userId)
            createSampleHabits(userId)
            createSampleSkills(userId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun createSampleGoals(userId: String) {
        val goals = listOf(
            // Career Goals
            Triple("Learn React Native", "Master React Native development for mobile apps", GoalCategory.SKILL to Priority.HIGH to GoalType.LONG_TERM),
            Triple("Complete AWS Certification", "Pass AWS Cloud Practitioner exam", GoalCategory.CAREER to Priority.HIGH to GoalType.SHORT_TERM),
            Triple("Improve Public Speaking", "Join Toastmasters and give 5 speeches", GoalCategory.PERSONAL to Priority.MEDIUM to GoalType.LONG_TERM),
            Triple("Network with 5 professionals", "Connect with industry professionals on LinkedIn", GoalCategory.CAREER to Priority.MEDIUM to GoalType.SHORT_TERM),
            
            // Personal Goals
            Triple("Read 12 books this year", "Read one book per month to expand knowledge", GoalCategory.EDUCATION to Priority.MEDIUM to GoalType.YEARLY),
            Triple("Complete a side project", "Build and deploy a personal web application", GoalCategory.SKILL to Priority.HIGH to GoalType.LONG_TERM),
            Triple("Learn a new language", "Achieve conversational level in Spanish", GoalCategory.EDUCATION to Priority.LOW to GoalType.LONG_TERM),
            Triple("Save $5000 for emergency fund", "Build financial security with emergency savings", GoalCategory.FINANCIAL to Priority.HIGH to GoalType.LONG_TERM)
        )
        
        goals.forEach { (title, description, categoryPriorityType) ->
            val (category, priority, type) = categoryPriorityType
            val targetDate = when (type) {
                GoalType.SHORT_TERM -> System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000) // 30 days
                GoalType.LONG_TERM -> System.currentTimeMillis() + (180L * 24 * 60 * 60 * 1000) // 6 months
                GoalType.YEARLY -> System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000) // 1 year
                else -> System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000) // 3 months
            }
            
            goalRepository.createGoal(
                userId = userId,
                title = title,
                description = description,
                category = category,
                priority = priority,
                type = type,
                targetDate = targetDate
            )
        }
    }
    
    private suspend fun createSampleHabits(userId: String) {
        val habits = listOf(
            // Health & Wellness
            Triple("Morning Exercise", "30 minutes of physical activity", HabitCategory.HEALTH to HabitFrequency.DAILY to HabitDifficulty.MEDIUM),
            Triple("Drink 8 glasses of water", "Stay hydrated throughout the day", HabitCategory.HEALTH to HabitFrequency.DAILY to HabitDifficulty.EASY),
            Triple("Meditate", "10 minutes of mindfulness meditation", HabitCategory.WELLNESS to HabitFrequency.DAILY to HabitDifficulty.MEDIUM),
            
            // Productivity
            Triple("Daily Planning", "Plan the day every morning", HabitCategory.PRODUCTIVITY to HabitFrequency.DAILY to HabitDifficulty.EASY),
            Triple("Read for 30 minutes", "Read technical or personal development books", HabitCategory.LEARNING to HabitFrequency.DAILY to HabitDifficulty.EASY),
            Triple("Code Practice", "Practice coding for at least 1 hour", HabitCategory.CAREER to HabitFrequency.DAILY to HabitDifficulty.HARD),
            
            // Personal Development
            Triple("Gratitude Journal", "Write down 3 things you're grateful for", HabitCategory.WELLNESS to HabitFrequency.DAILY to HabitDifficulty.EASY),
            Triple("Language Practice", "Practice Spanish for 20 minutes", HabitCategory.LEARNING to HabitFrequency.DAILY to HabitDifficulty.MEDIUM),
            
            // Weekly habits
            Triple("Weekly Review", "Review goals and progress weekly", HabitCategory.PRODUCTIVITY to HabitFrequency.WEEKLY to HabitDifficulty.MEDIUM),
            Triple("Networking", "Connect with one professional per week", HabitCategory.CAREER to HabitFrequency.WEEKLY to HabitDifficulty.MEDIUM)
        )
        
        habits.forEach { (name, description, categoryFrequencyDifficulty) ->
            val (category, frequency, difficulty) = categoryFrequencyDifficulty
            
            habitRepository.createHabit(
                userId = userId,
                name = name,
                description = description,
                category = category,
                frequency = frequency,
                targetCount = 1,
                difficulty = difficulty
            )
        }
    }
    
    private suspend fun createSampleSkills(userId: String) {
        val skills = listOf(
            // Technical Skills
            Triple("React Native", "Mobile Development", "Mobile app development with React Native" to (3 to 5)),
            Triple("AWS Cloud Services", "Cloud Computing", "Amazon Web Services cloud platform" to (2 to 4)),
            Triple("JavaScript", "Programming", "Modern JavaScript and ES6+" to (4 to 5)),
            Triple("TypeScript", "Programming", "Typed JavaScript for large applications" to (3 to 4)),
            Triple("Node.js", "Backend", "Server-side JavaScript development" to (3 to 4)),
            Triple("Docker", "DevOps", "Containerization and deployment" to (2 to 4)),
            Triple("Git", "Development", "Version control and collaboration" to (4 to 5)),
            
            // Soft Skills
            Triple("Public Speaking", "Communication", "Presenting and speaking to audiences" to (2 to 4)),
            Triple("Leadership", "Management", "Leading teams and projects" to (2 to 4)),
            Triple("Project Management", "Management", "Planning and executing projects" to (3 to 4)),
            Triple("Data Analysis", "Analytics", "Analyzing and interpreting data" to (2 to 4)),
            Triple("UI/UX Design", "Design", "User interface and experience design" to (2 to 4)),
            
            // Language Skills
            Triple("Spanish", "Languages", "Conversational Spanish language skills" to (1 to 3)),
            Triple("Technical Writing", "Communication", "Writing technical documentation" to (3 to 4))
        )
        
        skills.forEach { (name, category, descriptionToLevels) ->
            val (description, levels) = descriptionToLevels
            val (currentLevel, targetLevel) = levels
            
            skillRepository.addSkill(
                userId = userId,
                name = name,
                category = category,
                currentLevel = currentLevel,
                targetLevel = targetLevel,
                description = description
            )
        }
    }
    
    suspend fun seedProgressData(userId: String) {
        // Add some progress to make the dashboard more realistic
        try {
            // Update some goal progress
            val goals = goalRepository.getIncompleteGoalsFlow(userId)
            // This would be done in a real implementation to set some progress values
            
            // Create some habit completions for the past few days
            // This would involve creating HabitCompletion entities for recent dates
            
        } catch (e: Exception) {
            // Handle gracefully
        }
    }
}