package com.uniplan.app.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.UniPlanApp
import com.uniplan.app.data.local.db.ClassSessionEntity
import com.uniplan.app.data.local.db.SubjectEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

sealed class SessionFormEvent {
    object SaveSuccess : SessionFormEvent()
    data class ConflictFound(val conflicts: List<ClassSessionEntity>) : SessionFormEvent()
    data class ValidationError(val message: String) : SessionFormEvent()
}

class SessionViewModel : ViewModel() {

    private val scheduleRepository = UniPlanApp.instance.scheduleRepository
    private val subjectRepository = UniPlanApp.instance.subjectRepository
    private val semesterRepository = UniPlanApp.instance.semesterRepository
    private val checkConflictUseCase = UniPlanApp.instance.checkConflictUseCase

    private val _subjects = MutableStateFlow<List<SubjectEntity>>(emptyList())
    val subjects: StateFlow<List<SubjectEntity>> = _subjects.asStateFlow()

    private val _eventFlow = MutableSharedFlow<SessionFormEvent>()
    val eventFlow: SharedFlow<SessionFormEvent> = _eventFlow.asSharedFlow()

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            val sem = semesterRepository.observeCurrent().firstOrNull()
            if (sem != null) {
                val list = subjectRepository.observeBySemester(sem.id).firstOrNull() ?: emptyList()
                _subjects.value = list
            }
        }
    }

    fun saveSession(session: ClassSessionEntity, remOffsetMin: Int?) {
        viewModelScope.launch {
            if (session.subjectId == 0L) {
                _eventFlow.emit(SessionFormEvent.ValidationError("Vui lòng chọn môn học"))
                return@launch
            }

            val sem = semesterRepository.observeCurrent().firstOrNull()
            if (sem != null) {
                val conflicts = checkConflictUseCase.findConflicts(sem, session, emptyList(), emptyList())
                if (conflicts.isNotEmpty()) {
                    _eventFlow.emit(SessionFormEvent.ConflictFound(conflicts))
                    return@launch
                }
            }

            scheduleRepository.saveClassSession(session, remOffsetMin)
            _eventFlow.emit(SessionFormEvent.SaveSuccess)
        }
    }

    fun forceSaveSession(session: ClassSessionEntity, remOffsetMin: Int?) {
        viewModelScope.launch {
            scheduleRepository.saveClassSession(session, remOffsetMin)
            _eventFlow.emit(SessionFormEvent.SaveSuccess)
        }
    }
}
