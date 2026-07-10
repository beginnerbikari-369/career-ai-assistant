package com.careerai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.careerai.data.analysis.PersonalizedAnalysisService
import com.careerai.data.analysis.PersonalizedInsights
import com.careerai.data.repository.GoalRepository
import com.careerai.data.repository.GoalStatistics
import com.careerai.data.repository.HabitRepository
import com.careerai.data.repository.SkillRepository
import com.careerai.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val habitRepository: HabitRepository,
    private val skillRepository: SkillRepository,
    private val personalizedAnalysisService: PersonalizedAnalysisService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    private val _currentUserId = MutableStateFlow("dummy_user_id")
    
    init {
        loadDashboardData()
        generatePersonalizedInsights()
    }
    
    private fun loadDashboardData() {
        val userId = _currentUserId.value
        
        // Load goal statistics
        viewModelScope.launch {
            goalRepository.getGoalStatistics(userId)
                .fold(
                    onSuccess = { stats ->
                        _uiState.update { it.copy(goalStatistics = stats) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
                )
        }
        
        // Load active goals
        viewModelScope.launch {
            goalRepository.getIncompleteGoalsFlow(userId)
                .catch { error -> _uiState.update { it.copy(error = error.message) } }
                .collect { goals ->
                    _uiState.update { 
                        it.copy(
                            currentGoals = goals.take(5), // Show top 5 goals
                            totalActiveGoals = goals.size
                        ) 
                    }
                }
        }
        
        // Load recent habits
        viewModelScope.launch {
            habitRepository.getActiveHabitsFlow(userId)
                .catch { error -> _uiState.update { it.copy(error = error.message) } }
                .collect { habits ->
                    _uiState.update { 
                        it.copy(
                            recentHabits = habits.take(4), // Show top 4 habits
                            totalActiveHabits = habits.size
                        ) 
                    }
                }
        }
        
        // Load skills in development
        viewModelScope.launch {
            skillRepository.getSkillsInDevelopmentFlow(userId)
                .catch { error -> _uiState.update { it.copy(error = error.message) } }
                .collect { skills ->
                    _uiState.update { 
                        it.copy(
                            skillsInDevelopment = skills.take(3), // Show top 3 skills
                            totalSkillsInDevelopment = skills.size
                        ) 
                    }
                }
        }
        
        // Load today's habit completions
        viewModelScope.launch {
            loadTodayHabitProgress()
        }
    }
    
    private suspend fun loadTodayHabitProgress() {
        val userId = _currentUserId.value
        val today = System.currentTimeMillis()
        
        habitRepository.getTodayCompletionsFlow(userId, today)
            .catch { error -> _uiState.update { it.copy(error = error.message) } }
            .collect { completions ->
                val totalHabits = _uiState.value.totalActiveHabits
                val completedToday = completions.size
                val completionPercentage = if (totalHabits > 0) {
                    (completedToday * 100 / totalHabits)
                } else 0
                
                _uiState.update { 
                    it.copy(
                        todayHabitCompletions = completions,
                        todayCompletionPercentage = completionPercentage
                    ) 
                }
            }
    }
    
    fun generatePersonalizedInsights() {
        val userId = _currentUserId.value
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingInsights = true) }
            
            personalizedAnalysisService.generatePersonalizedInsights(userId)
                .fold(
                    onSuccess = { insights ->
                        _uiState.update { 
                            it.copy(
                                personalizedInsights = insights,
                                isLoadingInsights = false
                            ) 
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                error = "Failed to generate insights: ${error.message}",
                                isLoadingInsights = false
                            ) 
                        }
                    }
                )
        }
    }
    
    fun completeHabitToday(habitId: String) {
        viewModelScope.launch {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
            
            habitRepository.recordHabitCompletion(
                habitId = habitId,
                date = today,
                completedAt = System.currentTimeMillis(),
                completionCount = 1
            ).fold(
                onSuccess = { 
                    // UI will auto-update via Flow
                    generatePersonalizedInsights() // Refresh insights after habit completion
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
        }
    }
    
    fun updateGoalProgress(goalId: String, progress: Int) {
        viewModelScope.launch {
            goalRepository.updateGoalProgress(goalId, progress)
                .fold(
                    onSuccess = { 
                        // UI will auto-update via Flow
                        generatePersonalizedInsights() // Refresh insights after goal update
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
                )
        }
    }
    
    fun markGoalAsCompleted(goalId: String) {
        viewModelScope.launch {
            goalRepository.completeGoal(goalId)
                .fold(
                    onSuccess = { 
                        generatePersonalizedInsights() // Refresh insights after goal completion
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
                )
        }
    }
    
    fun getQuickActions(): List<QuickAction> {
        val insights = _uiState.value.personalizedInsights
        val actions = mutableListOf<QuickAction>()
        
        // Add AI-powered quick actions based on insights
        insights?.recommendations?.take(3)?.forEach { recommendation ->
            actions.add(
                QuickAction(
                    id = "rec_${recommendation.title.hashCode()}",
                    title = recommendation.title,
                    description = recommendation.description,
                    icon = getIconForCategory(recommendation.category),
                    priority = recommendation.priority
                )
            )
        }
        
        // Add default quick actions if no AI recommendations
        if (actions.isEmpty()) {
            actions.addAll(getDefaultQuickActions())
        }
        
        return actions.take(4) // Limit to 4 quick actions
    }
    
    private fun getDefaultQuickActions(): List<QuickAction> {
        return listOf(
            QuickAction(
                id = "add_goal",
                title = "Add New Goal",
                description = "Set a new career or personal goal",
                icon = "goal",
                priority = Priority.MEDIUM
            ),
            QuickAction(
                id = "track_habit",
                title = "Track Habit",
                description = "Record today's habit completions",
                icon = "habit",
                priority = Priority.HIGH
            ),
            QuickAction(
                id = "skill_practice",
                title = "Practice Skills",
                description = "Work on your skill development",
                icon = "skill",
                priority = Priority.MEDIUM
            ),
            QuickAction(
                id = "review_progress",
                title = "Review Progress",
                description = "Check your weekly achievements",
                icon = "progress",
                priority = Priority.LOW
            )
        )
    }
    
    private fun getIconForCategory(category: com.careerai.data.analysis.RecommendationCategory): String {
        return when (category) {
            com.careerai.data.analysis.RecommendationCategory.CAREER -> "career"
            com.careerai.data.analysis.RecommendationCategory.SKILL -> "skill"
            com.careerai.data.analysis.RecommendationCategory.HABIT -> "habit"
            com.careerai.data.analysis.RecommendationCategory.GOAL -> "goal"
            com.careerai.data.analysis.RecommendationCategory.PRODUCTIVITY -> "productivity"
        }
    }
    
    fun refreshAll() {
        loadDashboardData()
        generatePersonalizedInsights()
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class DashboardUiState(
    val goalStatistics: GoalStatistics? = null,
    val currentGoals: List<Goal> = emptyList(),
    val totalActiveGoals: Int = 0,
    val recentHabits: List<Habit> = emptyList(),
    val totalActiveHabits: Int = 0,
    val skillsInDevelopment: List<Skill> = emptyList(),
    val totalSkillsInDevelopment: Int = 0,
    val todayHabitCompletions: List<HabitCompletion> = emptyList(),
    val todayCompletionPercentage: Int = 0,
    val personalizedInsights: PersonalizedInsights? = null,
    val isLoadingInsights: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class QuickAction(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val priority: Priority
)