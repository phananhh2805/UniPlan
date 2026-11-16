package com.uniplan.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.UniPlanApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class StatsUiState(
    val completedCount: Int = 0,
    val onTimeRateText: String = "---"
)

class StatsViewModel : ViewModel() {

    private val taskRepository = UniPlanApp.instance.taskRepository

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            val doneTasks = taskRepository.observeDone().firstOrNull() ?: emptyList()
            val count = doneTasks.size

            _uiState.value = StatsUiState(
                completedCount = count,
                onTimeRateText = if (count > 0) "100%" else "---"
            )
        }
    }
}
