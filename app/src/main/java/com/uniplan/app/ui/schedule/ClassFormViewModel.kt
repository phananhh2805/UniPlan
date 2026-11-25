package com.uniplan.app.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.data.local.db.ClassSessionEntity
import com.uniplan.app.data.local.db.SubjectEntity
import com.uniplan.app.data.repository.ScheduleRepository
import com.uniplan.app.data.repository.SemesterRepository
import com.uniplan.app.data.repository.SubjectRepository
import com.uniplan.app.domain.model.ClassType
import com.uniplan.app.domain.model.ReminderOwner
import com.uniplan.app.domain.model.WeekPattern
import com.uniplan.app.domain.usecase.CheckConflictUseCase
import com.uniplan.app.notification.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClassFormUiState(
    val sessionId: Long = 0L,
    val selectedSubjectId: Long = 0L,
    val dayOfWeek: Int = 1,
    val isByPeriod: Boolean = true,
    val startPeriod: Int = 1,
    val endPeriod: Int = 3,
    val startTime: String = "07:00",
    val endTime: String = "09:35",
    val room: String = "",
    val campus: String = "",
    val type: ClassType = ClassType.LECTURE,
    val weekFrom: Int = 1,
    val weekTo: Int = 18,
    val weekPattern: WeekPattern = WeekPattern.ALL,
    val subjects: List<SubjectEntity> = emptyList(),
    val conflictCount: Int = 0,
    val showConflictDialog: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ClassFormViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val subjectRepository: SubjectRepository,
    private val semesterRepository: SemesterRepository,
    private val checkConflictUseCase: CheckConflictUseCase,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassFormUiState())
    val uiState: StateFlow<ClassFormUiState> = _uiState.asStateFlow()

    fun load(sessionId: Long) {
        viewModelScope.launch {
            val sem = semesterRepository.getCurrentSemester() ?: return@launch
            val subjects = subjectRepository.getSubjects(sem.id)

            if (sessionId != 0L) {
                val s = scheduleRepository.getSessionById(sessionId)
                if (s != null) {
                    _uiState.value = _uiState.value.copy(
                        sessionId = s.id,
                        selectedSubjectId = s.subjectId,
                        dayOfWeek = s.dayOfWeek,
                        startPeriod = s.startPeriod ?: 1,
                        endPeriod = s.endPeriod ?: 3,
                        startTime = s.startTime,
                        endTime = s.endTime,
                        room = s.room ?: "",
                        campus = s.campus ?: "",
                        type = s.type,
                        weekFrom = s.weekFrom,
                        weekTo = s.weekTo,
                        weekPattern = s.weekPattern,
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
    fun updateDayOfWeek(day: Int) { _uiState.value = _uiState.value.copy(dayOfWeek = day) }
    fun updatePeriods(start: Int, end: Int) {
        val stMin = (7 * 60) + (start - 1) * 55
        val etMin = stMin + (end - start + 1) * 50 + (end - start) * 5
        val st = String.format("%02d:%02d", stMin / 60, stMin % 60)
        val et = String.format("%02d:%02d", etMin / 60, etMin % 60)
        _uiState.value = _uiState.value.copy(startPeriod = start, endPeriod = end, startTime = st, endTime = et)
    }
    fun updateRoom(r: String) { _uiState.value = _uiState.value.copy(room = r) }
    fun updateWeeks(from: Int, to: Int, pattern: WeekPattern) {
        _uiState.value = _uiState.value.copy(weekFrom = from, weekTo = to, weekPattern = pattern)
    }

    fun save(force: Boolean = false) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.selectedSubjectId == 0L) {
                _uiState.value = state.copy(error = "Vui lòng chọn môn học")
                return@launch
            }
            if (state.endPeriod < state.startPeriod) {
                _uiState.value = state.copy(error = "Tiết kết thúc phải từ tiết bắt đầu trở đi")
                return@launch
            }

            val sem = semesterRepository.getCurrentSemester() ?: return@launch
            val draft = ClassSessionEntity(
                id = state.sessionId,
                subjectId = state.selectedSubjectId,
                dayOfWeek = state.dayOfWeek,
                startPeriod = state.startPeriod,
                endPeriod = state.endPeriod,
                startTime = state.startTime,
                endTime = state.endTime,
                room = state.room.ifBlank { null },
                campus = state.campus.ifBlank { null },
                type = state.type,
                weekFrom = state.weekFrom,
                weekTo = state.weekTo,
                weekPattern = state.weekPattern
            )

            if (!force) {
                val allSessions = scheduleRepository.getSessionsBySemester(sem.id)
                val conflicts = checkConflictUseCase.findConflicts(sem, draft, allSessions, emptyList())
                if (conflicts.isNotEmpty()) {
                    _uiState.value = state.copy(conflictCount = conflicts.size, showConflictDialog = true)
                    return@launch
                }
            }

            scheduleRepository.saveSession(draft)
            reminderScheduler.rescheduleOwner(ReminderOwner.CLASS, draft.id)
            _uiState.value = _uiState.value.copy(isSaved = true, showConflictDialog = false)
        }
    }
}
