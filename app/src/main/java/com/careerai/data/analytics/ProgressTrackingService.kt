package com.careerai.data.analytics

import com.careerai.data.repository.GoalRepository
import com.careerai.data.repository.HabitRepository
import com.careerai.data.repository.SkillRepository
import com.careerai.domain.model.*
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressTrackingService @Inject constructor(
    private val goalRepository: GoalRepository,
    private val habitRepository: HabitRepository,
    private val skillRepository: SkillRepository
) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    suspend fun trackUserActivity(
        userId: String,
        activityType: ActivityType,
        activityData: ActivityData
    ): Result<ActivityInsight> {
        return try {
            when (activityType) {
                ActivityType.HABIT_COMPLETION -> trackHabitCompletion(userId, activityData)
                ActivityType.GOAL_PROGRESS -> trackGoalProgress(userId, activityData)
                ActivityType.SKILL_PRACTICE -> trackSkillPractice(userId, activityData)
                ActivityType.SESSION_START -> trackSessionStart(userId)
                ActivityType.SESSION_END -> trackSessionEnd(userId, activityData)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun trackHabitCompletion(userId: String, data: ActivityData): Result<ActivityInsight> {
        val habitId = data.entityId ?: return Result.failure(Exception("Missing habit ID"))
        
        // Record the completion
        val today = dateFormat.format(Date())
        habitRepository.recordHabitCompletion(
            habitId = habitId,
            date = today,
            completedAt = System.currentTimeMillis()
        )
        
        // Analyze the impact
        val habit = habitRepository.getActiveHabitsFlow(userId).first()
            .find { it.id == habitId }
            ?: return Result.failure(Exception("Habit not found"))
        
        val insight = generateHabitCompletionInsight(habit, userId)
        return Result.success(insight)
    }
    
    private suspend fun trackGoalProgress(userId: String, data: ActivityData): Result<ActivityInsight> {
        val goalId = data.entityId ?: return Result.failure(Exception("Missing goal ID"))
        val progress = data.progress ?: return Result.failure(Exception("Missing progress value"))
        
        // Update goal progress
        goalRepository.updateGoalProgress(goalId, progress)
        
        // Analyze the impact
        val goal = goalRepository.getGoalByIdFlow(goalId).first()
            ?: return Result.failure(Exception("Goal not found"))
        
        val insight = generateGoalProgressInsight(goal, progress, userId)
        return Result.success(insight)
    }
    
    private suspend fun trackSkillPractice(userId: String, data: ActivityData): Result<ActivityInsight> {
        val skillId = data.entityId ?: return Result.failure(Exception("Missing skill ID"))
        val practiceTime = data.duration ?: return Result.failure(Exception("Missing practice duration"))
        
        // Update skill level based on practice time (simplified algorithm)
        val skill = skillRepository.getSkillsFlow(userId).first()
            .find { it.id == skillId }
            ?: return Result.failure(Exception("Skill not found"))
        
        val progressGain = calculateSkillProgress(skill, practiceTime)
        if (progressGain > 0) {
            val newLevel = minOf(skill.currentLevel + progressGain, skill.targetLevel)
            skillRepository.updateSkillLevel(skillId, newLevel)
        }
        
        val insight = generateSkillPracticeInsight(skill, practiceTime, progressGain)
        return Result.success(insight)
    }
    
    private suspend fun trackSessionStart(userId: String): Result<ActivityInsight> {
        // Track app session start for engagement analytics
        val todayHabits = habitRepository.getTodayCompletionsFlow(userId, System.currentTimeMillis()).first()
        val activeGoals = goalRepository.getIncompleteGoalsFlow(userId).first()
        
        val insight = ActivityInsight(
            type = InsightType.SESSION_START,
            title = "Welcome Back!",
            message = "You have ${activeGoals.size} active goals and completed ${todayHabits.size} habits today.",
            actionSuggestion = if (todayHabits.isEmpty()) "Start with a quick habit!" else "Keep the momentum going!",
            priority = InsightPriority.LOW,
            category = InsightCategory.ENGAGEMENT
        )
        
        return Result.success(insight)
    }
    
    private suspend fun trackSessionEnd(userId: String, data: ActivityData): Result<ActivityInsight> {
        val sessionDuration = data.duration ?: 0
        
        // Analyze session productivity
        val insight = when {
            sessionDuration > 600 * 1000 -> ActivityInsight( // 10 minutes
                type = InsightType.SESSION_PRODUCTIVE,
                title = "Productive Session!",
                message = "You spent ${sessionDuration / 60000} minutes working on your goals.",
                actionSuggestion = "Great focus! Remember to take breaks.",
                priority = InsightPriority.MEDIUM,
                category = InsightCategory.PRODUCTIVITY
            )
            sessionDuration > 60 * 1000 -> ActivityInsight( // 1 minute
                type = InsightType.SESSION_BRIEF,
                title = "Quick Check-in",
                message = "Every moment counts! Consider setting a daily habit.",
                actionSuggestion = "Set a 5-minute daily goal to build momentum.",
                priority = InsightPriority.LOW,
                category = InsightCategory.ENGAGEMENT
            )
            else -> ActivityInsight(
                type = InsightType.SESSION_QUICK,
                title = "Thanks for Checking In!",
                message = "Small consistent actions lead to big results.",
                actionSuggestion = "Try a quick habit tomorrow.",
                priority = InsightPriority.LOW,
                category = InsightCategory.MOTIVATION
            )
        }
        
        return Result.success(insight)
    }
    
    private suspend fun generateHabitCompletionInsight(
        habit: Habit,
        userId: String
    ): ActivityInsight {
        val streak = habit.streakCount
        val category = habit.category
        
        return when {
            streak == 1 -> ActivityInsight(
                type = InsightType.HABIT_STARTED,
                title = "Great Start! 🎯",
                message = "You've started your ${habit.name} journey!",
                actionSuggestion = "Come back tomorrow to build your streak!",
                priority = InsightPriority.HIGH,
                category = InsightCategory.HABIT_BUILDING
            )
            
            streak == 7 -> ActivityInsight(
                type = InsightType.STREAK_MILESTONE,
                title = "Week Streak! 🔥",
                message = "7 days of ${habit.name}! You're building momentum.",
                actionSuggestion = "Keep going - the next milestone is 21 days!",
                priority = InsightPriority.HIGH,
                category = InsightCategory.CELEBRATION
            )
            
            streak == 21 -> ActivityInsight(
                type = InsightType.HABIT_FORMING,
                title = "Habit Forming! ⭐",
                message = "21 days of ${habit.name}! This is becoming automatic.",
                actionSuggestion = "Consider adding a complementary habit.",
                priority = InsightPriority.HIGH,
                category = InsightCategory.CELEBRATION
            )
            
            streak == 66 -> ActivityInsight(
                type = InsightType.HABIT_MASTERY,
                title = "Habit Master! 🏆",
                message = "66 days! ${habit.name} is now part of who you are.",
                actionSuggestion = "Share your success story and inspire others!",
                priority = InsightPriority.HIGH,
                category = InsightCategory.CELEBRATION
            )
            
            streak % 30 == 0 -> ActivityInsight(
                type = InsightType.MONTHLY_MILESTONE,
                title = "Monthly Milestone! 📅",
                message = "${streak} days of ${habit.name}! Incredible consistency.",
                actionSuggestion = "Reflect on how this habit has changed you.",
                priority = InsightPriority.MEDIUM,
                category = InsightCategory.CELEBRATION
            )
            
            else -> ActivityInsight(
                type = InsightType.HABIT_PROGRESS,
                title = "Consistency Wins! 💪",
                message = "${streak} days of ${habit.name}! Every day matters.",
                actionSuggestion = "Keep this momentum going strong!",
                priority = InsightPriority.LOW,
                category = InsightCategory.MOTIVATION
            )
        }
    }
    
    private suspend fun generateGoalProgressInsight(
        goal: Goal,
        newProgress: Int,
        userId: String
    ): ActivityInsight {
        val oldProgress = goal.progress
        val progressGain = newProgress - oldProgress
        val isCompleted = newProgress >= 100
        
        return when {
            isCompleted -> ActivityInsight(
                type = InsightType.GOAL_COMPLETED,
                title = "Goal Achieved! 🎉",
                message = "Congratulations on completing '${goal.title}'!",
                actionSuggestion = "Celebrate this win and set your next goal!",
                priority = InsightPriority.HIGH,
                category = InsightCategory.CELEBRATION
            )
            
            newProgress >= 75 && oldProgress < 75 -> ActivityInsight(
                type = InsightType.GOAL_NEAR_COMPLETION,
                title = "Almost There! 🎯",
                message = "'${goal.title}' is 75% complete! The finish line is in sight.",
                actionSuggestion = "Push through - you're so close to achieving this goal!",
                priority = InsightPriority.HIGH,
                category = InsightCategory.MOTIVATION
            )
            
            newProgress >= 50 && oldProgress < 50 -> ActivityInsight(
                type = InsightType.GOAL_HALFWAY,
                title = "Halfway Point! ⚡",
                message = "'${goal.title}' is 50% complete! Great progress so far.",
                actionSuggestion = "Keep the momentum going - you've got this!",
                priority = InsightPriority.MEDIUM,
                category = InsightCategory.PROGRESS
            )
            
            progressGain >= 10 -> ActivityInsight(
                type = InsightType.SIGNIFICANT_PROGRESS,
                title = "Great Progress! 📈",
                message = "You made significant progress on '${goal.title}' (+${progressGain}%).",
                actionSuggestion = "This momentum is powerful - keep it up!",
                priority = InsightPriority.MEDIUM,
                category = InsightCategory.PROGRESS
            )
            
            else -> ActivityInsight(
                type = InsightType.STEADY_PROGRESS,
                title = "Progress Made! 👍",
                message = "Every step counts towards '${goal.title}'.",
                actionSuggestion = "Small consistent progress leads to big achievements.",
                priority = InsightPriority.LOW,
                category = InsightCategory.MOTIVATION
            )
        }
    }
    
    private fun generateSkillPracticeInsight(
        skill: Skill,
        practiceTime: Int,
        progressGain: Int
    ): ActivityInsight {
        val practiceMinutes = practiceTime / 60000
        
        return when {
            progressGain > 0 -> ActivityInsight(
                type = InsightType.SKILL_IMPROVEMENT,
                title = "Skill Level Up! 📚",
                message = "${practiceMinutes}min of ${skill.name} practice paid off! Level increased by $progressGain.",
                actionSuggestion = "Keep practicing regularly to reach your target!",
                priority = InsightPriority.MEDIUM,
                category = InsightCategory.SKILL_DEVELOPMENT
            )
            
            practiceTime > 30 * 60 * 1000 -> ActivityInsight( // 30 minutes
                type = InsightType.DEDICATED_PRACTICE,
                title = "Dedicated Practice! 🎓",
                message = "${practiceMinutes} minutes practicing ${skill.name}! Dedication builds expertise.",
                actionSuggestion = "Consistent practice like this will accelerate your progress!",
                priority = InsightPriority.MEDIUM,
                category = InsightCategory.SKILL_DEVELOPMENT
            )
            
            else -> ActivityInsight(
                type = InsightType.SKILL_PRACTICE,
                title = "Practice Session! 💡",
                message = "Every minute of ${skill.name} practice builds your expertise.",
                actionSuggestion = "Try to practice a little every day for best results.",
                priority = InsightPriority.LOW,
                category = InsightCategory.SKILL_DEVELOPMENT
            )
        }
    }
    
    private fun calculateSkillProgress(skill: Skill, practiceTimeMs: Int): Int {
        // Simplified skill progress calculation
        // In a real app, this would be more sophisticated
        val practiceMinutes = practiceTimeMs / 60000
        val gap = skill.targetLevel - skill.currentLevel
        
        return when {
            practiceMinutes >= 60 && gap > 0 -> 1 // 1 hour practice = 1 level gain
            practiceMinutes >= 30 && gap > 1 -> if (kotlin.random.Random.nextBoolean()) 1 else 0
            else -> 0
        }
    }
    
    suspend fun getWeeklyProgressSummary(userId: String): ProgressSummary {
        val now = System.currentTimeMillis()
        val weekStart = now - (7 * 24 * 60 * 60 * 1000)
        
        val habits = habitRepository.getActiveHabitsFlow(userId).first()
        val goals = goalRepository.getIncompleteGoalsFlow(userId).first()
        val completedGoals = goalRepository.getCompletedGoalsFlow(userId, 10).first()
        
        val habitCompletionRate = calculateHabitCompletionRate(habits, weekStart, now)
        val goalProgressRate = calculateGoalProgressRate(goals, weekStart)
        
        return ProgressSummary(
            weekStart = weekStart,
            weekEnd = now,
            habitCompletionRate = habitCompletionRate,
            goalProgressRate = goalProgressRate,
            completedGoalsThisWeek = completedGoals.count { it.completedAt != null && it.completedAt!! > weekStart },
            totalActiveGoals = goals.size,
            totalActiveHabits = habits.size,
            insights = generateWeeklyInsights(habitCompletionRate, goalProgressRate, goals.size, habits.size)
        )
    }
    
    private fun calculateHabitCompletionRate(habits: List<Habit>, weekStart: Long, weekEnd: Long): Int {
        if (habits.isEmpty()) return 100
        
        val totalPossible = habits.size * 7 // 7 days in a week
        val totalCompleted = habits.sumOf { it.streakCount.coerceAtMost(7) }
        
        return if (totalPossible > 0) {
            (totalCompleted * 100 / totalPossible)
        } else 100
    }
    
    private fun calculateGoalProgressRate(goals: List<Goal>, weekStart: Long): Int {
        if (goals.isEmpty()) return 100
        
        val goalsWithProgress = goals.filter { it.updatedAt > weekStart }
        return if (goals.isNotEmpty()) {
            (goalsWithProgress.size * 100 / goals.size)
        } else 100
    }
    
    private fun generateWeeklyInsights(
        habitRate: Int,
        goalRate: Int,
        totalGoals: Int,
        totalHabits: Int
    ): List<String> {
        val insights = mutableListOf<String>()
        
        when {
            habitRate >= 80 -> insights.add("Excellent habit consistency this week! ($habitRate%)")
            habitRate >= 60 -> insights.add("Good habit progress this week! ($habitRate%)")
            habitRate < 40 -> insights.add("Habits need attention - focus on consistency")
        }
        
        when {
            goalRate >= 70 -> insights.add("Great goal momentum this week!")
            goalRate >= 40 -> insights.add("Decent goal progress - keep pushing!")
            goalRate < 40 -> insights.add("Goals need more attention this week")
        }
        
        if (totalGoals > 10) {
            insights.add("Consider focusing on fewer goals for better results")
        }
        
        if (totalHabits > 8) {
            insights.add("Many habits! Focus on the most impactful ones")
        }
        
        return insights
    }
}

enum class ActivityType {
    HABIT_COMPLETION,
    GOAL_PROGRESS,
    SKILL_PRACTICE,
    SESSION_START,
    SESSION_END
}

data class ActivityData(
    val entityId: String? = null,
    val progress: Int? = null,
    val duration: Int? = null,
    val metadata: Map<String, Any> = emptyMap()
)

data class ActivityInsight(
    val type: InsightType,
    val title: String,
    val message: String,
    val actionSuggestion: String,
    val priority: InsightPriority,
    val category: InsightCategory,
    val timestamp: Long = System.currentTimeMillis()
)

enum class InsightType {
    HABIT_STARTED,
    STREAK_MILESTONE,
    HABIT_FORMING,
    HABIT_MASTERY,
    MONTHLY_MILESTONE,
    HABIT_PROGRESS,
    GOAL_COMPLETED,
    GOAL_NEAR_COMPLETION,
    GOAL_HALFWAY,
    SIGNIFICANT_PROGRESS,
    STEADY_PROGRESS,
    SKILL_IMPROVEMENT,
    DEDICATED_PRACTICE,
    SKILL_PRACTICE,
    SESSION_START,
    SESSION_PRODUCTIVE,
    SESSION_BRIEF,
    SESSION_QUICK
}

enum class InsightPriority {
    HIGH,
    MEDIUM,
    LOW
}

enum class InsightCategory {
    HABIT_BUILDING,
    SKILL_DEVELOPMENT,
    PROGRESS,
    CELEBRATION,
    MOTIVATION,
    PRODUCTIVITY,
    ENGAGEMENT
}

data class ProgressSummary(
    val weekStart: Long,
    val weekEnd: Long,
    val habitCompletionRate: Int,
    val goalProgressRate: Int,
    val completedGoalsThisWeek: Int,
    val totalActiveGoals: Int,
    val totalActiveHabits: Int,
    val insights: List<String>
)