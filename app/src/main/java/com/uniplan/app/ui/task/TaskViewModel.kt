package com.uniplan.app.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.UniPlanApp
import com.uniplan.app.data.local.db.TaskEntity
import com.uniplan.app.domain.model.Priority
import com.uniplan.app.domain.model.TaskStatus
import com.uniplan.app.domain.model.TaskType
import com.uniplan.app.domain.usecase.PriorityGroups
import com.uniplan.app.util.VietnameseText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class TaskUiState(
    val openTasks: List<TaskEntity> = emptyList(),
    val doneTasks: List<TaskEntity> = emptyList(),
    val priorityGroups: PriorityGroups? = null,
    val searchQuery: String = ""
)

class TaskViewModel : ViewModel() {

    private val taskRepository = UniPlanApp.instance.taskRepository
    private val dateTimeProvider = UniPlanApp.instance.dateTimeProvider
    private val groupUseCase = UniPlanApp.instance.groupTasksByPriorityUseCase

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            val open = taskRepository.observeOpen().firstOrNull() ?: emptyList()
            val done = taskRepository.observeDone().firstOrNull() ?: emptyList()

            val filteredOpen = if (_uiState.value.searchQuery.isNotBlank()) {
                val queryNorm = VietnameseText.removeAccents(_uiState.value.searchQuery.lowercase())
                open.filter {
                    val titleNorm = VietnameseText.removeAccents(it.title.lowercase())
                    titleNorm.contains(queryNorm)
                }
            } else open

            val groups = groupUseCase.execute(open, dateTimeProvider.now())

            _uiState.value = _uiState.value.copy(
                openTasks = filteredOpen,
                doneTasks = done,
                priorityGroups = groups
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadTasks()
    }

    fun saveTask(title: String, dueAt: String?, priority: Priority, onComplete: () -> Unit) {
        viewModelScope.launch {
            val task = TaskEntity(
                subjectId = null,
                title = title,
                description = null,
                dueAt = dueAt,
                priority = priority,
                status = TaskStatus.TODO,
                taskType = TaskType.ASSIGNMENT,
                estimateMin = 30,
                createdAt = dateTimeProvider.nowMillis(),
                updatedAt = dateTimeProvider.nowMillis(),
                completedAt = null
            )
            taskRepository.saveTask(task, emptyList(), listOf(1440, 60))
            loadTasks()
            onComplete()
        }
    }

    fun markDone(taskId: Long) {
        viewModelScope.launch {
            taskRepository.markDone(taskId)
            loadTasks()
        }
    }

    fun reopen(taskId: Long) {
        viewModelScope.launch {
            taskRepository.reopen(taskId)
            loadTasks()
        }
    }
}
