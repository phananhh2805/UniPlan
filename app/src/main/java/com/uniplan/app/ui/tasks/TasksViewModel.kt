package com.uniplan.app.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.data.local.db.ExamEntity
import com.uniplan.app.data.local.db.TaskEntity
import com.uniplan.app.data.repository.ExamRepository
import com.uniplan.app.data.repository.TaskRepository
import com.uniplan.app.domain.model.Priority
import com.uniplan.app.domain.model.ReminderOwner
import com.uniplan.app.domain.model.TaskStatus
import com.uniplan.app.domain.usecase.GroupTasksByPriorityUseCase
import com.uniplan.app.domain.usecase.PriorityMatrix
import com.uniplan.app.notification.ReminderScheduler
import com.uniplan.app.util.DateTimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TaskTab { TODO, DONE, EXAMS }

data class TasksUiState(
    val selectedTab: TaskTab = TaskTab.TODO,
    val isPriorityMatrixMode: Boolean = false,
    val openTasks: List<TaskEntity> = emptyList(),
    val doneTasks: List<TaskEntity> = emptyList(),
    val exams: List<ExamEntity> = emptyList(),
    val priorityMatrix: PriorityMatrix? = null,
    val searchQuery: String = "",
    val filterPriority: Priority? = null
)

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val examRepository: ExamRepository,
    private val groupTasksByPriorityUseCase: GroupTasksByPriorityUseCase,
    private val reminderScheduler: ReminderScheduler,
    private val dateTimeProvider: DateTimeProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            combine(
                taskRepository.openTasksFlow,
                taskRepository.doneTasksFlow,
                examRepository.allExamsFlow
            ) { open, done, exams ->
                val matrix = groupTasksByPriorityUseCase.execute(open, dateTimeProvider.now())
                TasksUiState(
                    openTasks = open,
                    doneTasks = done,
                    exams = exams,
                    priorityMatrix = matrix
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setTab(tab: TaskTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun toggleMatrixMode() {
        _uiState.value = _uiState.value.copy(isPriorityMatrixMode = !_uiState.value.isPriorityMatrixMode)
    }

    fun markTaskDone(taskId: Long) {
        viewModelScope.launch {
            taskRepository.markTaskDone(taskId)
        }
    }

    fun reopenTask(taskId: Long) {
        viewModelScope.launch {
            taskRepository.reopenTask(taskId)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            reminderScheduler.cancelAlarmsOf(ReminderOwner.TASK, taskId)
            taskRepository.deleteTask(taskId)
        }
    }
}
