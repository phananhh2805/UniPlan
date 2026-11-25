package com.uniplan.app.ui.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniplan.app.data.local.db.ExamEntity
import com.uniplan.app.data.local.db.TaskEntity
import com.uniplan.app.domain.usecase.PriorityGroup
import com.uniplan.app.domain.usecase.PriorityMatrix

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    onNavigateToTaskForm: (Long) -> Unit,
    onNavigateToExamForm: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nhiệm vụ") },
                actions = {
                    IconButton(onClick = { viewModel.toggleMatrixMode() }) {
                        Icon(
                            imageVector = if (state.isPriorityMatrixMode) Icons.Default.List else Icons.Default.GridView,
                            contentDescription = "Đổi chế độ xem"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (state.selectedTab == TaskTab.EXAMS) {
                    onNavigateToExamForm(0L)
                } else {
                    onNavigateToTaskForm(0L)
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab row
            TabRow(selectedTabIndex = state.selectedTab.ordinal) {
                Tab(
                    selected = state.selectedTab == TaskTab.TODO,
                    onClick = { viewModel.setTab(TaskTab.TODO) },
                    text = { Text("Cần làm (${state.openTasks.size})") }
                )
                Tab(
                    selected = state.selectedTab == TaskTab.DONE,
                    onClick = { viewModel.setTab(TaskTab.DONE) },
                    text = { Text("Hoàn thành") }
                )
                Tab(
                    selected = state.selectedTab == TaskTab.EXAMS,
                    onClick = { viewModel.setTab(TaskTab.EXAMS) },
                    text = { Text("Lịch thi (${state.exams.size})") }
                )
            }

            Spacer(Modifier.height(8.dp))

            when (state.selectedTab) {
                TaskTab.TODO -> {
                    if (state.isPriorityMatrixMode && state.priorityMatrix != null) {
                        PriorityMatrixView(state.priorityMatrix!!, onNavigateToTaskForm)
                    } else {
                        TaskList(state.openTasks, isDone = false, viewModel, onNavigateToTaskForm)
                    }
                }
                TaskTab.DONE -> TaskList(state.doneTasks, isDone = true, viewModel, onNavigateToTaskForm)
                TaskTab.EXAMS -> ExamList(state.exams, onNavigateToExamForm)
            }
        }
    }
}

@Composable
fun TaskList(
    tasks: List<TaskEntity>,
    isDone: Boolean,
    viewModel: TasksViewModel,
    onNavigateToTaskForm: (Long) -> Unit
) {
    if (tasks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(if (isDone) "Chưa có nhiệm vụ đã hoàn thành" else "Chưa có nhiệm vụ nào. Thêm nhiệm vụ đầu tiên")
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks) { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTaskForm(task.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isDone,
                            onCheckedChange = {
                                if (isDone) viewModel.reopenTask(task.id) else viewModel.markTaskDone(task.id)
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.title, style = MaterialTheme.typography.titleMedium)
                            if (task.dueAt != null) {
                                Text("Hạn: ${task.dueAt.replace('T', ' ')}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        IconButton(onClick = { viewModel.deleteTask(task.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PriorityMatrixView(
    matrix: PriorityMatrix,
    onNavigateToTaskForm: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { PriorityGroupCard(matrix.doFirst, onNavigateToTaskForm) }
        item { PriorityGroupCard(matrix.plan, onNavigateToTaskForm) }
        item { PriorityGroupCard(matrix.quickAction, onNavigateToTaskForm) }
        item { PriorityGroupCard(matrix.defer, onNavigateToTaskForm) }
    }
}

@Composable
fun PriorityGroupCard(
    group: PriorityGroup,
    onNavigateToTaskForm: (Long) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("${group.title} (${group.tasks.size})", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            group.tasks.forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onNavigateToTaskForm(task.id) }
                ) {
                    Text("• ${task.title}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun ExamList(
    exams: List<ExamEntity>,
    onNavigateToExamForm: (Long) -> Unit
) {
    if (exams.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Chưa có lịch thi nào")
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(exams) { exam ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToExamForm(exam.id) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(exam.examType, style = MaterialTheme.typography.titleMedium)
                        Text("Thời gian: ${exam.startAt.replace('T', ' ')} (${exam.durationMin} phút)")
                        if (!exam.room.isNullOrBlank()) {
                            Text("Phòng: ${exam.room}")
                        }
                    }
                }
            }
        }
    }
}
