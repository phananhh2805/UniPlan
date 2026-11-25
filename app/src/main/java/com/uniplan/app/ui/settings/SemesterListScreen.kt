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
import androidx.compose.ui.unit.dp
import com.uniplan.app.data.local.db.SemesterEntity
import com.uniplan.app.util.VietnameseText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterListScreen(
    viewModel: SemesterListViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý học kỳ") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm học kỳ")
            }
        }
    ) { padding ->
        if (state.semesters.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Chưa có học kỳ nào. Thêm học kỳ đầu tiên")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.semesters) { sem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.openEditDialog(sem) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(sem.name, style = MaterialTheme.typography.titleMedium)
                                    if (sem.isCurrent) {
                                        Spacer(Modifier.width(8.dp))
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("Hiện tại") }
                                        )
                                    }
                                }
                                Text(
                                    "${sem.startDate} - ${sem.endDate}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            if (!sem.isCurrent) {
                                TextButton(onClick = { viewModel.setCurrent(sem.id) }) {
                                    Text("Đặt hiện tại")
                                }
                            }

                            IconButton(onClick = { viewModel.deleteSemester(sem) }) {
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
                title = { Text(if (state.editingSemester == null) "Thêm học kỳ" else "Sửa học kỳ") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = state.nameInput,
                            onValueChange = { viewModel.updateInputs(it, state.startDateInput, state.endDateInput, state.isCurrentInput) },
                            label = { Text("Tên học kỳ (ví dụ: HK1 2026-2027)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Đặt làm học kỳ hiện tại")
                            Switch(
                                checked = state.isCurrentInput,
                                onCheckedChange = { viewModel.updateInputs(state.nameInput, state.startDateInput, state.endDateInput, it) }
                            )
                        }

                        if (state.error != null) {
                            Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.saveSemester() }) {
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
