package com.uniplan.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsScreen(
    viewModel: ReminderSettingsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt nhắc nhở") },
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Nhắc buổi học", style = MaterialTheme.typography.titleMedium)
                    Text("Thông báo trước giờ vào lớp (30 phút)", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = state.remClassEnabled,
                    onCheckedChange = { viewModel.setRemClass(it) }
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Nhắc deadline nhiệm vụ", style = MaterialTheme.typography.titleMedium)
                    Text("Thông báo trước 1 ngày và 1 giờ", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = state.remTaskEnabled,
                    onCheckedChange = { viewModel.setRemTask(it) }
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Nhắc lịch thi", style = MaterialTheme.typography.titleMedium)
                    Text("Thông báo trước 3 ngày, 1 ngày, 1 giờ", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = state.remExamEnabled,
                    onCheckedChange = { viewModel.setRemExam(it) }
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Giờ không làm phiền (DND)", style = MaterialTheme.typography.titleMedium)
                    Text("Tắt âm thanh thông báo từ 23:00 đến 06:00", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = state.dndEnabled,
                    onCheckedChange = { viewModel.setDnd(it) }
                )
            }
        }
    }
}
