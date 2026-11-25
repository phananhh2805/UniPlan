package com.uniplan.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val SubjectColors = listOf(
    0xFFE53935.toInt(), 0xFFD81B60.toInt(), 0xFF8E24AA.toInt(), 0xFF5E35B1.toInt(),
    0xFF1E88E5.toInt(), 0xFF039BE5.toInt(), 0xFF00ACC1.toInt(), 0xFF43A047.toInt(),
    0xFF7CB342.toInt(), 0xFFFDD835.toInt(), 0xFFFB8C00.toInt(), 0xFF6D4C41.toInt()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectListScreen(
    viewModel: SubjectListViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh sách môn học") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm môn học")
            }
        }
    ) { padding ->
        if (state.subjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Chưa có môn học nào. Thêm môn học đầu tiên")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.subjects) { sub ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.openEditDialog(sub) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(16.dp),
                                shape = MaterialTheme.shapes.small,
                                color = Color(sub.color)
                            ) {}
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sub.name, style = MaterialTheme.typography.titleMedium)
                                val subInfo = listOfNotNull(sub.code, sub.credits?.let { "$it tín chỉ" }, sub.teacher).joinToString(" · ")
                                if (subInfo.isNotBlank()) {
                                    Text(subInfo, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            IconButton(onClick = { viewModel.deleteSubject(sub) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        if (state.isDialogShown) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text(if (state.editingSubject == null) "Thêm môn học" else "Sửa môn học") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = state.nameInput,
                            onValueChange = { viewModel.updateInputs(it, state.codeInput, state.creditsInput, state.teacherInput, state.colorInput) },
                            label = { Text("Tên môn học") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.codeInput,
                            onValueChange = { viewModel.updateInputs(state.nameInput, it, state.creditsInput, state.teacherInput, state.colorInput) },
                            label = { Text("Mã môn (tùy chọn)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.creditsInput,
                            onValueChange = { viewModel.updateInputs(state.nameInput, state.codeInput, it, state.teacherInput, state.colorInput) },
                            label = { Text("Số tín chỉ") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.teacherInput,
                            onValueChange = { viewModel.updateInputs(state.nameInput, state.codeInput, state.creditsInput, it, state.colorInput) },
                            label = { Text("Giảng viên") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Color grid
                        Text("Màu đại diện", style = MaterialTheme.typography.titleSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SubjectColors.take(6).forEach { colorInt ->
                                Surface(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { viewModel.updateInputs(state.nameInput, state.codeInput, state.creditsInput, state.teacherInput, colorInt) },
                                    shape = MaterialTheme.shapes.small,
                                    color = Color(colorInt)
                                ) {}
                            }
                        }

                        if (state.error != null) {
                            Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.saveSubject() }) {
                        Text("Lưu")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDialog() }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}
