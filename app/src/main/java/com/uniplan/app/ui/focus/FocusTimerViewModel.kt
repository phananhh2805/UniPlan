package com.uniplan.app.ui.focus

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.UniPlanApp
import com.uniplan.app.data.local.db.FocusSessionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FocusTimerUiState(
    val isRunning: Boolean = false,
    val secondsLeft: Long = 1500L,
    val formattedTime: String = "25:00",
    val plannedMin: Int = 25
)

class FocusTimerViewModel : ViewModel() {

    private val focusRepository = UniPlanApp.instance.focusRepository
    private val dateTimeProvider = UniPlanApp.instance.dateTimeProvider

    private val _uiState = MutableStateFlow(FocusTimerUiState())
    val uiState: StateFlow<FocusTimerUiState> = _uiState.asStateFlow()

    private var timer: CountDownTimer? = null
    private var startedAtMs: Long = 0L

    fun startTimer(minutes: Int = 25) {
        val totalSec = minutes * 60L
        startedAtMs = dateTimeProvider.nowMillis()

        timer?.cancel()
        _uiState.value = FocusTimerUiState(isRunning = true, secondsLeft = totalSec, plannedMin = minutes)

        timer = object : CountDownTimer(totalSec * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secLeft = millisUntilFinished / 1000L
                val mins = secLeft / 60
                val secs = secLeft % 60
                val formatted = String.format("%02d:%02d", mins, secs)

                _uiState.value = _uiState.value.copy(
                    secondsLeft = secLeft,
                    formattedTime = formatted
                )
            }

            override fun onFinish() {
                _uiState.value = _uiState.value.copy(isRunning = false, secondsLeft = 0, formattedTime = "00:00")
                saveSession(minutes, minutes * 60, true)
            }
        }.start()
    }

    fun stopTimer() {
        timer?.cancel()
        val planned = _uiState.value.plannedMin
        val actualSec = ((dateTimeProvider.nowMillis() - startedAtMs) / 1000L).toInt().coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(isRunning = false)
        saveSession(planned, actualSec, false)
    }

    private fun saveSession(plannedMin: Int, actualSec: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            val session = FocusSessionEntity(
                taskId = null,
                subjectId = null,
                startedAt = startedAtMs,
                plannedMin = plannedMin,
                actualSec = actualSec,
                isCompleted = isCompleted
            )
            focusRepository.saveFocusSession(session)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}
