package com.uniplan.app.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.data.local.db.SubtaskEntity
import com.uniplan.app.data.local.db.SubjectEntity
import com.uniplan.app.data.local.db.TaskEntity
import com.uniplan.app.data.repository.SemesterRepository
import com.uniplan.app.data.repository.SubjectRepository
import com.uniplan.app.data.repository.TaskRepository
import com.uniplan.app.domain.model.Priority
import com.uniplan.app.domain.model.ReminderOwner
import com.uniplan.app.domain.model.TaskType
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

data class TaskFormUiState(
    val taskId: Long = 0L,
    val title: String = "",
    val description: String = "",
    val selectedSubjectId: Long? = null,
    val hasDueDate: Boolean = false,
    val dueAt: LocalDateTime = LocalDateTime.now().plusDays(1),
    val priority: Priority = Priority.MEDIUM,
    val taskType: TaskType = TaskType.ASSIGNMENT,
    val subtasks: List<SubtaskEntity> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TaskFormViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val subjectRepository: SubjectRepository,
    private val semesterRepository: SemesterRepository,
    private val reminderScheduler: ReminderScheduler,
    private val dateTimeProvider: DateTimeProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskFormUiState())
    val uiState: StateFlow<TaskFormUiState> = _uiState.asStateFlow()

    fun load(taskId: Long) {
        viewModelScope.launch {
            val sem = semesterRepository.getCurrentSemester()
            val subjects = if (sem != null) subjectRepository.getSubjects(sem.id) else emptyList()

            if (taskId != 0L) {
                val t = taskRepository.getTaskById(taskId)
                if (t != null) {
                    val subtasks = taskRepository.getSubtasks(taskId)
                    val due = t.dueAt?.let { LocalDateTime.parse(it) } ?: dateTimeProvider.now().plusDays(1)
                    _uiState.value = _uiState.value.copy(
                        taskId = t.id,
                        title = t.title,
                        description = t.description ?: "",
                        selectedSubjectId = t.subjectId,
                        hasDueDate = t.dueAt != null,
                        dueAt = due,
                        priority = t.priority,
                        taskType = t.taskType,
                        subtasks = subtasks,
                        subjects = subjects
                    )
                    return@launch
                }
            }

            _uiState.value = _uiState.value.copy(subjects = subjects)
        }
    }

    fun updateTitle(t: String) { _uiState.value = _uiState.value.copy(title = t) }
    fun updateDescription(d: String) { _uiState.value = _uiState.value.copy(description = d) }
    fun updateSubject(id: Long?) { _uiState.value = _uiState.value.copy(selectedSubjectId = id) }
    fun updateHasDueDate(b: Boolean) { _uiState.value = _uiState.value.copy(hasDueDate = b) }
    fun updateDueAt(dt: LocalDateTime) { _uiState.value = _uiState.value.copy(dueAt = dt) }
    fun updatePriority(p: Priority) { _uiState.value = _uiState.value.copy(priority = p) }

    fun addSubtask(title: String) {
        if (title.isBlank()) return
        val current = _uiState.value.subtasks.toMutableList()
        current.add(SubtaskEntity(taskId = _uiState.value.taskId, title = title.trim(), position = current.size))
        _uiState.value = _uiState.value.copy(subtasks = current)
    }

    fun removeSubtask(index: Int) {
        val current = _uiState.value.subtasks.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _uiState.value = _uiState.value.copy(subtasks = current)
        }
    }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.title.isBlank()) {
                _uiState.value = state.copy(error = "Vui lòng nhập tiêu đề nhiệm vụ")
                return@launch
            }

            val dueStr = if (state.hasDueDate) state.dueAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) else null
            val nowMs = dateTimeProvider.nowMillis()

            val task = TaskEntity(
                id = state.taskId,
                subjectId = state.selectedSubjectId,
                title = state.title.trim(),
                description = state.description.ifBlank { null },
                dueAt = dueStr,
                priority = state.priority,
                taskType = state.taskType,
                createdAt = nowMs,
                updatedAt = nowMs
            )

            val newId = taskRepository.saveTask(task, state.subtasks)
            reminderScheduler.rescheduleOwner(ReminderOwner.TASK, newId)
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
