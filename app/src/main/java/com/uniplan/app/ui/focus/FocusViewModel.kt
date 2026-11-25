package com.uniplan.app.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.data.repository.FocusRepository
import com.uniplan.app.data.repository.TaskRepository
import com.uniplan.app.util.DateTimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TimerState { IDLE, RUNNING, PAUSED, FINISHED }

data class FocusUiState(
    val timerState: TimerState = TimerState.IDLE,
    val plannedMin: Int = 25,
    val remainingSec: Int = 25 * 60,
    val taskId: Long? = null,
    val taskTitle: String? = null,
    val subjectId: Long? = null,
    val startedAtMs: Long = 0L,
    val actualSec: Int = 0
)

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val focusRepository: FocusRepository,
    private val taskRepository: TaskRepository,
    private val dateTimeProvider: DateTimeProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun loadTask(taskId: Long) {
        if (taskId == 0L) return
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId)
            if (task != null) {
                _uiState.value = _uiState.value.copy(
                    taskId = task.id,
                    taskTitle = task.title,
                    subjectId = task.subjectId
                )
            }
        }
    }

    fun setPlannedMin(min: Int) {
        if (_uiState.value.timerState == TimerState.IDLE) {
            _uiState.value = _uiState.value.copy(
                plannedMin = min,
                remainingSec = min * 60
            )
        }
    }

    fun startTimer() {
        if (_uiState.value.timerState == TimerState.RUNNING) return

        val nowMs = dateTimeProvider.nowMillis()
        _uiState.value = _uiState.value.copy(
            timerState = TimerState.RUNNING,
            startedAtMs = if (_uiState.value.startedAtMs == 0L) nowMs else _uiState.value.startedAtMs
        )

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSec > 0 && _uiState.value.timerState == TimerState.RUNNING) {
                delay(1000L)
                val newRem = _uiState.value.remainingSec - 1
                val newActual = _uiState.value.actualSec + 1
                _uiState.value = _uiState.value.copy(
                    remainingSec = newRem,
                    actualSec = newActual
                )
            }

            if (_uiState.value.remainingSec <= 0) {
                finishTimer()
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(timerState = TimerState.PAUSED)
    }

    fun stopTimer() {
        timerJob?.cancel()
        val state = _uiState.value
        saveSession(isCompleted = false)
        _uiState.value = FocusUiState(plannedMin = state.plannedMin, remainingSec = state.plannedMin * 60)
    }

    private fun finishTimer() {
        _uiState.value = _uiState.value.copy(timerState = TimerState.FINISHED)
        saveSession(isCompleted = true)
    }

    private fun saveSession(isCompleted: Boolean) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.actualSec >= 60) {
                focusRepository.saveFocusSession(
                    taskId = state.taskId,
                    subjectId = state.subjectId,
                    startedAt = state.startedAtMs,
                    plannedMin = state.plannedMin,
                    actualSec = state.actualSec,
                    isCompleted = isCompleted
                )
            }
        }
    }
}
