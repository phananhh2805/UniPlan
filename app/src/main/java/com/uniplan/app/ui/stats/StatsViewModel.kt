package com.uniplan.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.data.repository.FocusRepository
import com.uniplan.app.data.repository.TaskRepository
import com.uniplan.app.util.DateTimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatsUiState(
    val completedCount: Int = 0,
    val onTimeRatePercent: Int = 100,
    val totalFocusMinutes: Int = 0
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val focusRepository: FocusRepository,
    private val dateTimeProvider: DateTimeProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val nowMs = dateTimeProvider.nowMillis()
            val weekStartMs = nowMs - 7 * 24 * 60 * 60 * 1000L

            taskRepository.countCompleted(weekStartMs, nowMs + 86400000L).collect { count ->
                _uiState.value = _uiState.value.copy(completedCount = count)
            }
        }
    }
}
