package com.careerai.data.analysis

import com.careerai.data.api.AIService
import com.careerai.data.api.ChatMessage
import com.careerai.data.repository.GoalRepository
import com.careerai.data.repository.HabitRepository
import com.careerai.data.repository.SkillRepository
import com.careerai.domain.model.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonalizedAnalysisService @Inject constructor(
    private val aiService: AIService,
    private val goalRepository: GoalRepository,
    private val habitRepository: HabitRepository,
    private val skillRepository: SkillRepository
) {
    
    suspend fun generatePersonalizedInsights(userId: String): Result<PersonalizedInsights> {
        return try {
            // Gather user data
            val goals = goalRepository.getIncompleteGoalsFlow(userId).first()
            val completedGoals = goalRepository.getCompletedGoalsFlow(userId, limit = 10).first()
            val habits = habitRepository.getHabitsWithStreaksFlow(userId).first()
            val skills = skillRepository.getCareerRelevantSkillsFlow(userId).first()
            val goalStats = goalRepository.getGoalStatistics(userId).getOrNull()
            
            // Create analysis prompt
            val analysisPrompt = buildAnalysisPrompt(
                goals = goals,
                completedGoals = completedGoals,
                habits = habits,
                skills = skills,
                goalStats = goalStats
            )
            
            // Get AI analysis
            val aiResponse = aiService.sendMessage(
                messages = listOf(ChatMessage(role = "user", content = analysisPrompt)),
                context = ConversationContext.CAREER,
                model = "gpt-4"
            )
            
            aiResponse.fold(
                onSuccess = { response ->
                    val analysis = response.choices.firstOrNull()?.message?.content
                        ?: "Unable to generate analysis"
                    
                    // Parse AI response and create structured insights
                    val insights = parseAIAnalysis(analysis, goals, habits, skills)
                    Result.success(insights)
                },
                onFailure = { error ->
                    // Fallback to rule-based analysis if AI fails
                    val fallbackInsights = generateRuleBasedInsights(goals, habits, skills, goalStats)
                    Result.success(fallbackInsights)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun buildAnalysisPrompt(
        goals: List<Goal>,
        completedGoals: List<Goal>,
        habits: List<Habit>,
        skills: List<Skill>,
        goalStats: com.careerai.data.repository.GoalStatistics?
    ): String {
        return """
            As a Career AI Assistant, analyze the following user data and provide personalized insights and recommendations:

            **CURRENT GOALS** (${goals.size} active):
            ${goals.take(10).joinToString("\n") { 
                "- ${it.title} (${it.category.name}, Priority: ${it.priority.name}, Progress: ${it.progress}%)" 
            }}

            **RECENTLY COMPLETED GOALS** (${completedGoals.size}):
            ${completedGoals.take(5).joinToString("\n") { 
                "- ${it.title} (${it.category.name})" 
            }}

            **ACTIVE HABITS** (${habits.size}):
            ${habits.take(10).joinToString("\n") { 
                "- ${it.name} (${it.category.name}, Streak: ${it.streakCount} days)" 
            }}

            **SKILLS IN DEVELOPMENT** (${skills.size}):
            ${skills.take(10).joinToString("\n") { 
                "- ${it.name}: ${it.currentLevel}/${it.targetLevel} (${it.category})" 
            }}

            **PERFORMANCE STATS**:
            ${goalStats?.let {
            "Total Goals: ${it.totalGoals}, Completed: ${it.completedGoals}, In Progress: ${it.activeGoals}, " +                "Completion Rate: ${if (it.totalGoals > 0) "${(it.completedGoals * 100 / it.totalGoals)}%" else "N/A"}"
            } ?: "No statistics available"}

            Please provide:
            1. **PROGRESS ANALYSIS**: How is the user performing? What patterns do you see?
            2. **KEY INSIGHTS**: What are 3-5 specific insights about their career/personal development?
            3. **RECOMMENDATIONS**: What should they focus on next? (Be specific and actionable)
            4. **POTENTIAL CHALLENGES**: What obstacles might they face and how to overcome them?
            5. **MOTIVATION**: Encouraging words based on their progress

            Format your response as JSON with the following structure:
            {
                "progressAnalysis": "text",
                "keyInsights": ["insight1", "insight2", "insight3"],
                "recommendations": [
                    {"title": "Recommendation Title", "description": "Detailed description", "priority": "HIGH|MEDIUM|LOW", "category": "CAREER|SKILL|HABIT|GOAL"},
                ],
                "challenges": [
                    {"challenge": "Challenge description", "solution": "How to overcome it"}
                ],
                "motivationalMessage": "Encouraging message"
            }
        """.trimIndent()
    }
    
    private fun parseAIAnalysis(
        analysis: String,
        goals: List<Goal>,
        habits: List<Habit>,
        skills: List<Skill>
    ): PersonalizedInsights {
        // Try to parse JSON response from AI
        return try {
            // TODO: Implement proper JSON parsing
            // For now, create structured response based on content analysis
            createStructuredInsights(analysis, goals, habits, skills)
        } catch (e: Exception) {
            // Fallback if parsing fails
            generateRuleBasedInsights(goals, habits, skills, null)
        }
    }
    
    private fun createStructuredInsights(
        analysis: String,
        goals: List<Goal>,
        habits: List<Habit>,
        skills: List<Skill>
    ): PersonalizedInsights {
        return PersonalizedInsights(
            progressAnalysis = extractProgressAnalysis(analysis),
            keyInsights = extractInsights(analysis),
            recommendations = generateSmartRecommendations(goals, habits, skills),
            challenges = identifyPotentialChallenges(goals, habits),
            motivationalMessage = extractMotivationalMessage(analysis),
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    private fun generateRuleBasedInsights(
        goals: List<Goal>,
        habits: List<Habit>,
        skills: List<Skill>,
        goalStats: com.careerai.data.repository.GoalStatistics?
    ): PersonalizedInsights {
        val recommendations = mutableListOf<Recommendation>()
        val insights = mutableListOf<String>()
        val challenges = mutableListOf<Challenge>()
        
        // Analyze goal progress
        val stagnantGoals = goals.filter { it.progress < 10 && 
            (System.currentTimeMillis() - it.updatedAt) > 7 * 24 * 60 * 60 * 1000 } // 7 days
        
        if (stagnantGoals.isNotEmpty()) {
            insights.add("You have ${stagnantGoals.size} goals with minimal progress in the past week")
            recommendations.add(
                Recommendation(
                    title = "Review Stagnant Goals",
                    description = "Break down ${stagnantGoals.first().title} into smaller, actionable steps",
                    priority = Priority.HIGH,
                    category = RecommendationCategory.GOAL
                )
            )
        }
        
        // Analyze habit consistency
        val strugglingHabits = habits.filter { it.streakCount < 3 }
        if (strugglingHabits.isNotEmpty()) {
            recommendations.add(
                Recommendation(
                    title = "Focus on Habit Consistency",
                    description = "Start with just one habit: ${strugglingHabits.first().name}. Build consistency before adding more.",
                    priority = Priority.HIGH,
                    category = RecommendationCategory.HABIT
                )
            )
        }
        
        // Analyze skill development
        val skillsNeedingAttention = skills.filter { 
            it.currentLevel < it.targetLevel && (it.targetLevel - it.currentLevel) > 3 
        }
        
        if (skillsNeedingAttention.isNotEmpty()) {
            recommendations.add(
                Recommendation(
                    title = "Accelerate Skill Development",
                    description = "Create a learning plan for ${skillsNeedingAttention.first().name} with daily practice sessions",
                    priority = Priority.MEDIUM,
                    category = RecommendationCategory.SKILL
                )
            )
        }
        
        // Career-specific recommendations
        val careerGoals = goals.filter { it.category == GoalCategory.CAREER }
        if (careerGoals.isEmpty()) {
            recommendations.add(
                Recommendation(
                    title = "Set Career Goals",
                    description = "Define specific career objectives for the next 6-12 months",
                    priority = Priority.MEDIUM,
                    category = RecommendationCategory.CAREER
                )
            )
        }
        
        return PersonalizedInsights(
            progressAnalysis = generateProgressSummary(goalStats, habits.size, skills.size),
            keyInsights = insights.ifEmpty { 
                listOf("Keep building momentum with your current goals and habits") 
            },
            recommendations = recommendations,
            challenges = challenges,
            motivationalMessage = generateMotivationalMessage(goals.size, habits.size),
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    private fun generateSmartRecommendations(
        goals: List<Goal>,
        habits: List<Habit>,
        skills: List<Skill>
    ): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()
        
        // Priority-based recommendations
        val highPriorityGoals = goals.filter { it.priority == Priority.HIGH && it.progress < 50 }
        highPriorityGoals.forEach { goal ->
            recommendations.add(
                Recommendation(
                    title = "Accelerate High-Priority Goal",
                    description = "Focus extra time and energy on '${goal.title}' this week",
                    priority = Priority.HIGH,
                    category = RecommendationCategory.GOAL
                )
            )
        }
        
        return recommendations.take(5) // Limit to top 5 recommendations
    }
    
    private fun identifyPotentialChallenges(goals: List<Goal>, habits: List<Habit>): List<Challenge> {
        val challenges = mutableListOf<Challenge>()
        
        if (goals.size > 10) {
            challenges.add(
                Challenge(
                    challenge = "Goal Overload: You have ${goals.size} active goals",
                    solution = "Consider focusing on 3-5 most important goals and archive or postpone others"
                )
            )
        }
        
        if (habits.size > 8) {
            challenges.add(
                Challenge(
                    challenge = "Too Many Habits: Tracking ${habits.size} habits simultaneously",
                    solution = "Focus on 3-4 core habits and gradually add more as they become automatic"
                )
            )
        }
        
        return challenges
    }
    
    private fun extractProgressAnalysis(analysis: String): String {
        return "Making steady progress across multiple areas with room for focused improvement"
    }
    
    private fun extractInsights(analysis: String): List<String> {
        return listOf(
            "Your goal completion rate indicates strong commitment",
            "Habit consistency varies - focus on building steady routines",
            "Skill development is aligned with career objectives"
        )
    }
    
    private fun extractMotivationalMessage(analysis: String): String {
        return "You're making great progress! Keep up the momentum and focus on one step at a time."
    }
    
    private fun generateProgressSummary(
        goalStats: com.careerai.data.repository.GoalStatistics?,
        habitCount: Int,
        skillCount: Int
    ): String {
        return goalStats?.let { stats ->
            "You have ${stats.activeGoals} goals in progress with a ${
                if (stats.totalGoals > 0) "${(stats.completedGoals * 100 / stats.totalGoals)}%" else "0%"
            } completion rate, actively tracking $habitCount habits and developing $skillCount skills."
        } ?: "Building momentum with your goals, habits, and skill development."
    }
    
    private fun generateMotivationalMessage(goalCount: Int, habitCount: Int): String {
        return when {
            goalCount > 5 && habitCount > 3 -> 
                "Impressive dedication! You're actively working on $goalCount goals and $habitCount habits. Stay focused and keep pushing forward!"
            goalCount > 0 || habitCount > 0 -> 
                "Great start! Every small step counts. Keep building on your current momentum."
            else -> 
                "Ready to begin your journey? Start with one meaningful goal and build from there!"
        }
    }
}

data class PersonalizedInsights(
    val progressAnalysis: String,
    val keyInsights: List<String>,
    val recommendations: List<Recommendation>,
    val challenges: List<Challenge>,
    val motivationalMessage: String,
    val lastUpdated: Long
)

data class Recommendation(
    val title: String,
    val description: String,
    val priority: Priority,
    val category: RecommendationCategory
)

enum class RecommendationCategory {
    CAREER,
    SKILL,
    HABIT,
    GOAL,
    PRODUCTIVITY
}

data class Challenge(
    val challenge: String,
    val solution: String
)