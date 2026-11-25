package com.uniplan.app.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamFormScreen(
    viewModel: ExamFormViewModel,
    examId: Long,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(examId) {
        viewModel.load(examId)
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (examId == 0L) "Thêm lịch thi" else "Sửa lịch thi") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Subject selector
            var subjectExpanded by remember { mutableStateOf(false) }
            val selectedSubName = state.subjects.find { it.id == state.selectedSubjectId }?.name ?: "Chọn môn thi"

            ExposedDropdownMenuBox(
                expanded = subjectExpanded,
                onExpandedChange = { subjectExpanded = !subjectExpanded }
            ) {
                OutlinedTextField(
                    value = selectedSubName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Môn thi") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )

                ExposedDropdownMenu(
                    expanded = subjectExpanded,
                    onDismissRequest = { subjectExpanded = false }
                ) {
                    state.subjects.forEach { sub ->
                        DropdownMenuItem(
                            text = { Text(sub.name) },
                            onClick = {
                                viewModel.updateSubjectId(sub.id)
                                subjectExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.examType,
                onValueChange = { viewModel.updateExamType(it) },
                label = { Text("Loại kỳ thi (ví dụ: Giữa kỳ, Cuối kỳ)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.room,
                onValueChange = { viewModel.updateRoom(it) },
                label = { Text("Phòng thi") },
                modifier = Modifier.fillMaxWidth()
            )

            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lưu lịch thi")
            }
        }
    }
}
