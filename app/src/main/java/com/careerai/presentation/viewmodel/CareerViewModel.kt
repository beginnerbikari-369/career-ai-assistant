package com.careerai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.careerai.data.repository.GoalRepository
import com.careerai.data.repository.GoalStatistics
import com.careerai.data.repository.SkillRepository
import com.careerai.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CareerViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val skillRepository: SkillRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CareerUiState())
    val uiState: StateFlow<CareerUiState> = _uiState.asStateFlow()
    
    private val _currentUserId = MutableStateFlow("dummy_user_id")
    
    init {
        loadCareerData()
    }
    
    private fun loadCareerData() {
        val userId = _currentUserId.value
        
        viewModelScope.launch {
            // Load goals
            goalRepository.getIncompleteGoalsFlow(userId)
                .catch { error -> _uiState.update { it.copy(error = error.message) } }
                .collect { goals ->
                    _uiState.update { it.copy(goals = goals) }
                }
        }
        
        viewModelScope.launch {
            // Load skills
            skillRepository.getCareerRelevantSkillsFlow(userId)
                .catch { error -> _uiState.update { it.copy(error = error.message) } }
                .collect { skills ->
                    _uiState.update { it.copy(skills = skills) }
                }
        }
        
        viewModelScope.launch {
            // Load goal statistics
            loadGoalStatistics()
        }
    }
    
    private suspend fun loadGoalStatistics() {
        goalRepository.getGoalStatistics(_currentUserId.value)
            .fold(
                onSuccess = { stats ->
                    _uiState.update { it.copy(goalStatistics = stats) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
    }
    
    fun createGoal(
        title: String,
        description: String?,
        category: GoalCategory,
        priority: Priority,
        type: GoalType,
        targetDate: Long? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            goalRepository.createGoal(
                userId = _currentUserId.value,
                title = title,
                description = description,
                category = category,
                priority = priority,
                type = type,
                targetDate = targetDate
            ).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    loadGoalStatistics()
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }
    
    fun updateGoalProgress(goalId: String, progress: Int) {
        viewModelScope.launch {
            goalRepository.updateGoalProgress(goalId, progress)
                .fold(
                    onSuccess = { loadGoalStatistics() },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
                )
        }
    }
    
    fun completeGoal(goalId: String) {
        viewModelScope.launch {
            goalRepository.completeGoal(goalId)
                .fold(
                    onSuccess = { loadGoalStatistics() },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
                )
        }
    }
    
    fun addSkill(
        name: String,
        category: String,
        currentLevel: Int,
        targetLevel: Int,
        description: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            skillRepository.addSkill(
                userId = _currentUserId.value,
                name = name,
                category = category,
                currentLevel = currentLevel,
                targetLevel = targetLevel,
                description = description
            ).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }
    
    fun updateSkillLevel(skillId: String, newLevel: Int) {
        viewModelScope.launch {
            skillRepository.updateSkillLevel(skillId, newLevel)
                .fold(
                    onSuccess = { /* Skills will auto-update via Flow */ },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
                )
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class CareerUiState(
    val goals: List<Goal> = emptyList(),
    val skills: List<com.careerai.domain.model.Skill> = emptyList(),
    val goalStatistics: GoalStatistics? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)