package com.uniplan.app.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniplan.app.domain.model.Priority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
    viewModel: TaskFormViewModel,
    taskId: Long,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(taskId) {
        viewModel.load(taskId)
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == 0L) "Thêm nhiệm vụ" else "Sửa nhiệm vụ") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Tiêu đề nhiệm vụ") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Mô tả chi tiết") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Priority Chips
            Text("Mức ưu tiên", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.priority == Priority.LOW,
                    onClick = { viewModel.updatePriority(Priority.LOW) },
                    label = { Text("Thấp") }
                )
                FilterChip(
                    selected = state.priority == Priority.MEDIUM,
                    onClick = { viewModel.updatePriority(Priority.MEDIUM) },
                    label = { Text("Vừa") }
                )
                FilterChip(
                    selected = state.priority == Priority.HIGH,
                    onClick = { viewModel.updatePriority(Priority.HIGH) },
                    label = { Text("Cao") }
                )
            }

            // Has Due Date switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Có hạn nộp")
                Switch(
                    checked = state.hasDueDate,
                    onCheckedChange = { viewModel.updateHasDueDate(it) }
                )
            }

            // Subtasks section
            Text("Việc con (${state.subtasks.size})", style = MaterialTheme.typography.titleSmall)
            state.subtasks.forEachIndexed { idx, st ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("• ${st.title}", modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.removeSubtask(idx) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            var subtaskInput by remember { mutableStateOf("") }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = subtaskInput,
                    onValueChange = { subtaskInput = it },
                    label = { Text("Thêm việc con") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    viewModel.addSubtask(subtaskInput)
                    subtaskInput = ""
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm")
                }
            }

            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lưu nhiệm vụ")
            }
        }
    }
}
