package com.uniplan.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.data.local.prefs.SettingsDataStore
import com.uniplan.app.data.repository.SemesterRepository
import com.uniplan.app.data.repository.SubjectRepository
import com.uniplan.app.data.local.db.SubjectEntity
import com.uniplan.app.util.DateTimeProvider
import com.uniplan.app.util.WeekCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class OnboardingUiState(
    val step: Int = 1, // 1: Semester, 2: Period Config, 3: First Subject
    val semesterName: String = "Học kỳ này",
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now().plusWeeks(18),
    val subjectName: String = "",
    val subjectCode: String = "",
    val subjectColor: Int = 0xFF1E88E5.toInt(),
    val isCompleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val semesterRepository: SemesterRepository,
    private val subjectRepository: SubjectRepository,
    private val settingsDataStore: SettingsDataStore,
    private val dateTimeProvider: DateTimeProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OnboardingUiState(
            startDate = WeekCalculator.weekStart(dateTimeProvider.today()),
            endDate = WeekCalculator.weekStart(dateTimeProvider.today()).plusWeeks(18)
        )
    )
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updateSemesterName(name: String) {
        _uiState.value = _uiState.value.copy(semesterName = name)
    }

    fun updateDates(start: LocalDate, end: LocalDate) {
        _uiState.value = _uiState.value.copy(startDate = start, endDate = end)
    }

    fun updateSubject(name: String, code: String, color: Int) {
        _uiState.value = _uiState.value.copy(subjectName = name, subjectCode = code, subjectColor = color)
    }

    fun nextStep() {
        val curr = _uiState.value.step
        if (curr < 3) {
            _uiState.value = _uiState.value.copy(step = curr + 1)
        } else {
            completeOnboarding()
        }
    }

    fun prevStep() {
        val curr = _uiState.value.step
        if (curr > 1) {
            _uiState.value = _uiState.value.copy(step = curr - 1)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.semesterName.isBlank()) {
                _uiState.value = state.copy(error = "Vui lòng nhập tên học kỳ")
                return@launch
            }
            if (!state.endDate.isAfter(state.startDate)) {
                _uiState.value = state.copy(error = "Ngày kết thúc phải sau ngày bắt đầu")
                return@launch
            }

            val semId = semesterRepository.addSemester(
                name = state.semesterName,
                startDate = state.startDate.toString(),
                endDate = state.endDate.toString(),
                isCurrent = true
            )

            if (state.subjectName.isNotBlank()) {
                subjectRepository.addSubject(
                    SubjectEntity(
                        semesterId = semId,
                        code = state.subjectCode.ifBlank { null },
                        name = state.subjectName.trim(),
                        color = state.subjectColor
                    )
                )
            }

            settingsDataStore.setOnboardingDone(true)
            _uiState.value = _uiState.value.copy(isCompleted = true)
        }
    }
}
