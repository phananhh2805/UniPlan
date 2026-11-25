package com.uniplan.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.data.local.db.SubjectEntity
import com.uniplan.app.data.repository.SemesterRepository
import com.uniplan.app.data.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectListUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val isDialogShown: Boolean = false,
    val editingSubject: SubjectEntity? = null,
    val nameInput: String = "",
    val codeInput: String = "",
    val creditsInput: String = "",
    val teacherInput: String = "",
    val colorInput: Int = 0xFF1E88E5.toInt(),
    val error: String? = null
)

@HiltViewModel
class SubjectListViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val semesterRepository: SemesterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubjectListUiState())
    val uiState: StateFlow<SubjectListUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            val sem = semesterRepository.getCurrentSemester() ?: return@launch
            subjectRepository.observeSubjects(sem.id).collect { list ->
                _uiState.value = _uiState.value.copy(subjects = list)
            }
        }
    }

    fun openAddDialog() {
        _uiState.value = _uiState.value.copy(
            isDialogShown = true,
            editingSubject = null,
            nameInput = "",
            codeInput = "",
            creditsInput = "",
            teacherInput = "",
            colorInput = 0xFF1E88E5.toInt(),
            error = null
        )
    }

    fun openEditDialog(s: SubjectEntity) {
        _uiState.value = _uiState.value.copy(
            isDialogShown = true,
            editingSubject = s,
            nameInput = s.name,
            codeInput = s.code ?: "",
            creditsInput = s.credits?.toString() ?: "",
            teacherInput = s.teacher ?: "",
            colorInput = s.color,
            error = null
        )
    }

    fun dismissDialog() { _uiState.value = _uiState.value.copy(isDialogShown = false) }

    fun updateInputs(name: String, code: String, credits: String, teacher: String, color: Int) {
        _uiState.value = _uiState.value.copy(
            nameInput = name,
            codeInput = code,
            creditsInput = credits,
            teacherInput = teacher,
            colorInput = color
        )
    }

    fun saveSubject() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.nameInput.isBlank()) {
                _uiState.value = state.copy(error = "Vui lòng nhập tên môn học")
                return@launch
            }

            val sem = semesterRepository.getCurrentSemester() ?: return@launch
            val credits = state.creditsInput.toIntOrNull()

            val editing = state.editingSubject
            if (editing == null) {
                subjectRepository.addSubject(
                    SubjectEntity(
                        semesterId = sem.id,
                        name = state.nameInput.trim(),
                        code = state.codeInput.ifBlank { null },
                        credits = credits,
                        teacher = state.teacherInput.ifBlank { null },
                        color = state.colorInput
                    )
                )
            } else {
                subjectRepository.updateSubject(
                    editing.copy(
                        name = state.nameInput.trim(),
                        code = state.codeInput.ifBlank { null },
                        credits = credits,
                        teacher = state.teacherInput.ifBlank { null },
                        color = state.colorInput
                    )
                )
            }

            _uiState.value = _uiState.value.copy(isDialogShown = false)
        }
    }

    fun deleteSubject(s: SubjectEntity) {
        viewModelScope.launch {
            subjectRepository.deleteSubject(s)
        }
    }
}
