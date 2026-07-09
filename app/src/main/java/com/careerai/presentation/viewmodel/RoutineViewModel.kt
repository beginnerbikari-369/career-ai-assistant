package com.careerai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.careerai.data.repository.HabitRepository
import com.careerai.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RoutineUiState())
    val uiState: StateFlow<RoutineUiState> = _uiState.asStateFlow()
    
    private val _currentUserId = MutableStateFlow("dummy_user_id")
    
    init {
        loadRoutineData()
    }
    
    private fun loadRoutineData() {
        val userId = _currentUserId.value
        
        viewModelScope.launch {
            habitRepository.getActiveHabitsFlow(userId)
                .catch { error -> _uiState.update { it.copy(error = error.message) } }
                .collect { habits ->
                    _uiState.update { it.copy(habits = habits) }
                }
        }
        
        viewModelScope.launch {
            loadTodayProgress()
        }
    }
    
    private suspend fun loadTodayProgress() {
        habitRepository.getTodayCompletionsCount(_currentUserId.value)
            .fold(
                onSuccess = { completedCount ->
                    val totalHabits = _uiState.value.habits.size
                    val progress = TodayProgress(
                        completedHabits = completedCount,
                        totalHabits = totalHabits,
                        focusTimeCompleted = 4, // Mock data
                        totalFocusTime = 6,
                        wellnessActivities = 3,
                        totalWellnessActivities = 4
                    )
                    _uiState.update { it.copy(todayProgress = progress) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
    }
    
    fun createHabit(
        name: String,
        description: String?,
        category: HabitCategory,
        frequency: HabitFrequency,
        targetCount: Int = 1,
        duration: Int? = null,
        reminderTime: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            habitRepository.createHabit(
                userId = _currentUserId.value,
                name = name,
                description = description,
                category = category,
                frequency = frequency,
                targetCount = targetCount,
                duration = duration,
                reminderTime = reminderTime
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
    
    fun completeHabit(habitId: String) {
        viewModelScope.launch {
            habitRepository.completeHabit(habitId)
                .fold(
                    onSuccess = { 
                        loadTodayProgress()
                    },
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

data class RoutineUiState(
    val habits: List<Habit> = emptyList(),
    val todayProgress: TodayProgress = TodayProgress(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class TodayProgress(
    val completedHabits: Int = 0,
    val totalHabits: Int = 0,
    val focusTimeCompleted: Int = 0,
    val totalFocusTime: Int = 0,
    val wellnessActivities: Int = 0,
    val totalWellnessActivities: Int = 0
)