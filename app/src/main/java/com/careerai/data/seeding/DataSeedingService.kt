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
            Triple("Learn React Native", "Master React Native development for mobile apps", Triple(GoalCategory.SKILL, Priority.HIGH, GoalType.LONG_TERM)),
            Triple("Complete AWS Certification", "Pass AWS Cloud Practitioner exam", Triple(GoalCategory.CAREER, Priority.HIGH, GoalType.SHORT_TERM)),
            Triple("Improve Public Speaking", "Join Toastmasters and give 5 speeches", Triple(GoalCategory.PERSONAL, Priority.MEDIUM, GoalType.LONG_TERM)),
            Triple("Network with 5 professionals", "Connect with industry professionals on LinkedIn", Triple(GoalCategory.CAREER, Priority.MEDIUM, GoalType.SHORT_TERM)),
            Triple("Read 12 books this year", "Read one book per month to expand knowledge", Triple(GoalCategory.EDUCATION, Priority.MEDIUM, GoalType.YEARLY)),
            Triple("Complete a side project", "Build and deploy a personal web application", Triple(GoalCategory.SKILL, Priority.HIGH, GoalType.LONG_TERM)),
            Triple("Learn a new language", "Achieve conversational level in Spanish", Triple(GoalCategory.EDUCATION, Priority.LOW, GoalType.LONG_TERM)),
            Triple("Save $5000 for emergency fund", "Build financial security with emergency savings", Triple(GoalCategory.FINANCIAL, Priority.HIGH, GoalType.LONG_TERM))
        )

        goals.forEach { (title, description, categoryPriorityType) ->
            val (category, priority, type) = categoryPriorityType
            val targetDate = when (type) {
                GoalType.SHORT_TERM -> System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
                GoalType.LONG_TERM -> System.currentTimeMillis() + (180L * 24 * 60 * 60 * 1000)
                GoalType.YEARLY -> System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)
                else -> System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000)
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
            Triple("Morning Exercise", "30 minutes of physical activity", Triple(HabitCategory.HEALTH, HabitFrequency.DAILY, HabitDifficulty.MEDIUM)),
            Triple("Drink 8 glasses of water", "Stay hydrated throughout the day", Triple(HabitCategory.HEALTH, HabitFrequency.DAILY, HabitDifficulty.EASY)),
            Triple("Meditate", "10 minutes of mindfulness meditation", Triple(HabitCategory.WELLNESS, HabitFrequency.DAILY, HabitDifficulty.MEDIUM)),
            Triple("Daily Planning", "Plan the day every morning", Triple(HabitCategory.PRODUCTIVITY, HabitFrequency.DAILY, HabitDifficulty.EASY)),
            Triple("Read for 30 minutes", "Read technical or personal development books", Triple(HabitCategory.LEARNING, HabitFrequency.DAILY, HabitDifficulty.EASY)),
            Triple("Code Practice", "Practice coding for at least 1 hour", Triple(HabitCategory.CAREER, HabitFrequency.DAILY, HabitDifficulty.HARD)),
            Triple("Gratitude Journal", "Write down 3 things you're grateful for", Triple(HabitCategory.WELLNESS, HabitFrequency.DAILY, HabitDifficulty.EASY)),
            Triple("Language Practice", "Practice Spanish for 20 minutes", Triple(HabitCategory.LEARNING, HabitFrequency.DAILY, HabitDifficulty.MEDIUM)),
            Triple("Weekly Review", "Review goals and progress weekly", Triple(HabitCategory.PRODUCTIVITY, HabitFrequency.WEEKLY, HabitDifficulty.MEDIUM)),
            Triple("Networking", "Connect with one professional per week", Triple(HabitCategory.CAREER, HabitFrequency.WEEKLY, HabitDifficulty.MEDIUM))
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