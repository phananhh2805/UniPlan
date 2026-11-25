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
fun BackupScreen(
    viewModel: BackupViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sao lưu & Khôi phục") },
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Sao lưu dữ liệu", style = MaterialTheme.typography.titleMedium)
                    Text("Xuất toàn bộ dữ liệu ra tệp JSON để lưu trữ an toàn.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.exportData() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tạo tệp sao lưu")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Khôi phục dữ liệu", style = MaterialTheme.typography.titleMedium)
                    Text("Cảnh báo: Khôi phục tệp JSON sẽ thay thế toàn bộ dữ liệu hiện tại trong ứng dụng.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            // Demo restore JSON payload
                            viewModel.restoreData("{}")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Chọn tệp JSON khôi phục")
                    }
                }
            }

            if (state.restoreMessage != null) {
                Text(
                    text = state.restoreMessage!!,
                    color = if (state.isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
