package com.uniplan.app.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.data.local.db.SemesterEntity
import com.uniplan.app.data.local.prefs.SettingsDataStore
import com.uniplan.app.data.repository.ScheduleRepository
import com.uniplan.app.data.repository.SemesterRepository
import com.uniplan.app.data.repository.TimeBlockRepository
import com.uniplan.app.domain.model.Occurrence
import com.uniplan.app.domain.model.PlacedTimelineItem
import com.uniplan.app.domain.model.TimelineColumnLayout
import com.uniplan.app.domain.model.TimelineItem
import com.uniplan.app.util.DateTimeProvider
import com.uniplan.app.util.WeekCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class ScheduleMode { WEEK, DAY, MONTH }

data class ScheduleUiState(
    val currentSemester: SemesterEntity? = null,
    val mode: ScheduleMode = ScheduleMode.WEEK,
    val selectedDate: LocalDate = LocalDate.now(),
    val weekOccurrences: Map<LocalDate, List<Occurrence>> = emptyMap(),
    val dayTimelineItems: List<PlacedTimelineItem> = emptyList(),
    val weekLabel: String = ""
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val semesterRepository: SemesterRepository,
    private val scheduleRepository: ScheduleRepository,
    private val timeBlockRepository: TimeBlockRepository,
    private val settingsDataStore: SettingsDataStore,
    private val dateTimeProvider: DateTimeProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState(selectedDate = dateTimeProvider.today()))
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                semesterRepository.currentSemesterFlow,
                settingsDataStore.scheduleViewMode
            ) { sem, modeStr ->
                val mode = try { ScheduleMode.valueOf(modeStr) } catch (_: Exception) { ScheduleMode.WEEK }
                _uiState.value = _uiState.value.copy(currentSemester = sem, mode = mode)
                sem
            }.collect { sem ->
                if (sem != null) {
                    loadForDate(sem, _uiState.value.selectedDate)
                }
            }
        }
    }

    fun setMode(mode: ScheduleMode) {
        viewModelScope.launch {
            settingsDataStore.setScheduleViewMode(mode.name)
            _uiState.value = _uiState.value.copy(mode = mode)
        }
    }

    fun setSelectedDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        val sem = _uiState.value.currentSemester
        if (sem != null) {
            loadForDate(sem, date)
        }
    }

    fun goToToday() {
        setSelectedDate(dateTimeProvider.today())
    }

    private fun loadForDate(sem: SemesterEntity, date: LocalDate) {
        viewModelScope.launch {
            val weekStart = WeekCalculator.weekStart(date)
            val weekEnd = weekStart.plusDays(6)

            val weekIndex = WeekCalculator.weekIndex(LocalDate.parse(sem.startDate), LocalDate.parse(sem.endDate), date)
            val weekLabel = if (weekIndex != null) "Tuần $weekIndex · ${weekStart.dayOfMonth}/${weekStart.monthValue} - ${weekEnd.dayOfMonth}/${weekEnd.monthValue}" else "Ngoài học kỳ"

            _uiState.value = _uiState.value.copy(weekLabel = weekLabel)

            scheduleRepository.observeOccurrencesBetween(sem, weekStart, weekEnd)
                .collect { occMap ->
                    _uiState.value = _uiState.value.copy(weekOccurrences = occMap)

                    // Also build day timeline
                    val dayOccs = occMap[date] ?: emptyList()
                    val dayItems = dayOccs.map { TimelineItem.ClassItem(it) }
                    val placed = TimelineColumnLayout.layoutColumns(dayItems)
                    _uiState.value = _uiState.value.copy(dayTimelineItems = placed)
                }
        }
    }
}
