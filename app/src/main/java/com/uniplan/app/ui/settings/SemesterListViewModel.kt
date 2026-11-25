package com.uniplan.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.data.local.db.SemesterEntity
import com.uniplan.app.data.repository.SemesterRepository
import com.uniplan.app.util.DateTimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class SemesterListUiState(
    val semesters: List<SemesterEntity> = emptyList(),
    val isDialogShown: Boolean = false,
    val editingSemester: SemesterEntity? = null,
    val nameInput: String = "",
    val startDateInput: LocalDate = LocalDate.now(),
    val endDateInput: LocalDate = LocalDate.now().plusWeeks(18),
    val isCurrentInput: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SemesterListViewModel @Inject constructor(
    private val semesterRepository: SemesterRepository,
    private val dateTimeProvider: DateTimeProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(SemesterListUiState())
    val uiState: StateFlow<SemesterListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            semesterRepository.semestersFlow.collect { list ->
                _uiState.value = _uiState.value.copy(semesters = list)
            }
        }
    }

    fun openAddDialog() {
        _uiState.value = _uiState.value.copy(
            isDialogShown = true,
            editingSemester = null,
            nameInput = "",
            startDateInput = dateTimeProvider.today(),
            endDateInput = dateTimeProvider.today().plusWeeks(18),
            isCurrentInput = false,
            error = null
        )
    }

    fun openEditDialog(s: SemesterEntity) {
        _uiState.value = _uiState.value.copy(
            isDialogShown = true,
            editingSemester = s,
            nameInput = s.name,
            startDateInput = LocalDate.parse(s.startDate),
            endDateInput = LocalDate.parse(s.endDate),
            isCurrentInput = s.isCurrent,
            error = null
        )
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(isDialogShown = false)
    }

    fun updateInputs(name: String, start: LocalDate, end: LocalDate, isCurrent: Boolean) {
        _uiState.value = _uiState.value.copy(
            nameInput = name,
            startDateInput = start,
            endDateInput = end,
            isCurrentInput = isCurrent
        )
    }

    fun saveSemester() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.nameInput.isBlank()) {
                _uiState.value = state.copy(error = "Vui lòng nhập tên học kỳ")
                return@launch
            }
            if (!state.endDateInput.isAfter(state.startDateInput)) {
                _uiState.value = state.copy(error = "Ngày kết thúc phải sau ngày bắt đầu")
                return@launch
            }

            val editing = state.editingSemester
            if (editing == null) {
                semesterRepository.addSemester(
                    name = state.nameInput,
                    startDate = state.startDateInput.toString(),
                    endDate = state.endDateInput.toString(),
                    isCurrent = state.isCurrentInput
                )
            } else {
                semesterRepository.updateSemester(
                    editing.copy(
                        name = state.nameInput.trim(),
                        startDate = state.startDateInput.toString(),
                        endDate = state.endDateInput.toString(),
                        isCurrent = state.isCurrentInput
                    )
                )
            }

            _uiState.value = _uiState.value.copy(isDialogShown = false)
        }
    }

    fun setCurrent(id: Long) {
        viewModelScope.launch {
            semesterRepository.setCurrent(id)
        }
    }

    fun deleteSemester(s: SemesterEntity) {
        viewModelScope.launch {
            semesterRepository.deleteSemester(s)
        }
    }
}
