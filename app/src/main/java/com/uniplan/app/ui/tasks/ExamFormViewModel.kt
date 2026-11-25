package com.uniplan.app.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.data.local.db.ExamEntity
import com.uniplan.app.data.local.db.SubjectEntity
import com.uniplan.app.data.repository.ExamRepository
import com.uniplan.app.data.repository.SemesterRepository
import com.uniplan.app.data.repository.SubjectRepository
import com.uniplan.app.domain.model.ReminderOwner
import com.uniplan.app.notification.ReminderScheduler
import com.uniplan.app.util.DateTimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ExamFormUiState(
    val examId: Long = 0L,
    val selectedSubjectId: Long = 0L,
    val examType: String = "Cuối kỳ",
    val startAt: LocalDateTime = LocalDateTime.now().plusDays(7),
    val durationMin: Int = 90,
    val room: String = "",
    val note: String = "",
    val subjects: List<SubjectEntity> = emptyList(),
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ExamFormViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val subjectRepository: SubjectRepository,
    private val semesterRepository: SemesterRepository,
    private val reminderScheduler: ReminderScheduler,
    private val dateTimeProvider: DateTimeProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamFormUiState())
    val uiState: StateFlow<ExamFormUiState> = _uiState.asStateFlow()

    fun load(examId: Long) {
        viewModelScope.launch {
            val sem = semesterRepository.getCurrentSemester()
            val subjects = if (sem != null) subjectRepository.getSubjects(sem.id) else emptyList()

            if (examId != 0L) {
                val e = examRepository.getExamById(examId)
                if (e != null) {
                    _uiState.value = _uiState.value.copy(
                        examId = e.id,
                        selectedSubjectId = e.subjectId,
                        examType = e.examType,
                        startAt = LocalDateTime.parse(e.startAt),
                        durationMin = e.durationMin,
                        room = e.room ?: "",
                        note = e.note ?: "",
                        subjects = subjects
                    )
                    return@launch
                }
            }

            _uiState.value = _uiState.value.copy(
                subjects = subjects,
                selectedSubjectId = subjects.firstOrNull()?.id ?: 0L
            )
        }
    }

    fun updateSubjectId(id: Long) { _uiState.value = _uiState.value.copy(selectedSubjectId = id) }
    fun updateExamType(type: String) { _uiState.value = _uiState.value.copy(examType = type) }
    fun updateStartAt(dt: LocalDateTime) { _uiState.value = _uiState.value.copy(startAt = dt) }
    fun updateRoom(r: String) { _uiState.value = _uiState.value.copy(room = r) }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.selectedSubjectId == 0L) {
                _uiState.value = state.copy(error = "Vui lòng chọn môn thi")
                return@launch
            }
            if (state.examType.isBlank()) {
                _uiState.value = state.copy(error = "Vui lòng nhập loại kỳ thi")
                return@launch
            }

            val exam = ExamEntity(
                id = state.examId,
                subjectId = state.selectedSubjectId,
                examType = state.examType.trim(),
                startAt = state.startAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                durationMin = state.durationMin,
                room = state.room.ifBlank { null },
                note = state.note.ifBlank { null }
            )

            val newId = examRepository.saveExam(exam)
            reminderScheduler.rescheduleOwner(ReminderOwner.EXAM, newId)
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
