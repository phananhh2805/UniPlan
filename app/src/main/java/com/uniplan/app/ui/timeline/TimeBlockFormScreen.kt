package com.uniplan.app.ui.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniplan.app.domain.model.BlockCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeBlockFormScreen(
    viewModel: TimeBlockFormViewModel,
    blockId: Long,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(blockId) {
        viewModel.load(blockId)
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (blockId == 0L) "Thêm khối thời gian" else "Sửa khối thời gian") },
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
            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Tên khối thời gian (ví dụ: Tự học Android)") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Danh mục", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.category == BlockCategory.STUDY,
                    onClick = { viewModel.updateCategory(BlockCategory.STUDY) },
                    label = { Text("Tự học") }
                )
                FilterChip(
                    selected = state.category == BlockCategory.WORK,
                    onClick = { viewModel.updateCategory(BlockCategory.WORK) },
                    label = { Text("Làm thêm") }
                )
                FilterChip(
                    selected = state.category == BlockCategory.REST,
                    onClick = { viewModel.updateCategory(BlockCategory.REST) },
                    label = { Text("Nghỉ ngơi") }
                )
                FilterChip(
                    selected = state.category == BlockCategory.EXERCISE,
                    onClick = { viewModel.updateCategory(BlockCategory.EXERCISE) },
                    label = { Text("Thể dục") }
                )
            }

            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lưu khối thời gian")
            }
        }
    }
}
