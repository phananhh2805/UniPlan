package com.uniplan.app.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.UniPlanApp
import com.uniplan.app.data.local.db.SemesterEntity
import com.uniplan.app.domain.model.Occurrence
import com.uniplan.app.util.WeekCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ScheduleUiState(
    val mode: String = "WEEK",
    val selectedDate: LocalDate = LocalDate.now(),
    val currentSemester: SemesterEntity? = null,
    val titleText: String = "",
    val occurrences: List<Occurrence> = emptyList()
)

class ScheduleViewModel : ViewModel() {

    private val scheduleRepository = UniPlanApp.instance.scheduleRepository
    private val semesterRepository = UniPlanApp.instance.semesterRepository
    private val settingsDataStore = UniPlanApp.instance.settingsDataStore
    private val dateTimeProvider = UniPlanApp.instance.dateTimeProvider

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun setMode(mode: String) {
        _uiState.value = _uiState.value.copy(mode = mode)
        viewModelScope.launch {
            settingsDataStore.setScheduleMode(mode)
        }
    }

    fun setSelectedDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        loadOccurrencesForDate(date)
    }

    fun loadData() {
        viewModelScope.launch {
            val savedMode = settingsDataStore.scheduleMode.firstOrNull() ?: "WEEK"
            val currentSem = semesterRepository.observeCurrent().firstOrNull()
            val today = dateTimeProvider.today()

            _uiState.value = _uiState.value.copy(
                mode = savedMode,
                selectedDate = today,
                currentSemester = currentSem
            )

            loadOccurrencesForDate(today)
        }
    }

    private fun loadOccurrencesForDate(date: LocalDate) {
        viewModelScope.launch {
            val sem = _uiState.value.currentSemester ?: return@launch
            val occurrences = scheduleRepository.observeOccurrencesForDate(sem.id, date).firstOrNull() ?: emptyList()

            val start = LocalDate.parse(sem.startDate)
            val end = LocalDate.parse(sem.endDate)
            val weekIdx = WeekCalculator.weekIndex(start, end, date)

            val titleText = if (weekIdx != null) "Tuần $weekIdx · ${date}" else "Ngoài học kỳ"

            _uiState.value = _uiState.value.copy(
                titleText = titleText,
                occurrences = occurrences
            )
        }
    }
}
