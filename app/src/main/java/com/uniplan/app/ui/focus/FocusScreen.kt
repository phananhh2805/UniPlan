package com.uniplan.app.ui.focus

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    viewModel: FocusViewModel,
    taskId: Long = 0L,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(taskId) {
        if (taskId != 0L) {
            viewModel.loadTask(taskId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đồng hồ tập trung") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (!state.taskTitle.isNullOrBlank()) {
                    Text(
                        text = state.taskTitle!!,
                        style = MaterialTheme.typography.titleLarge
                    )
                } else {
                    Text(
                        text = "Phiên tập trung",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            // Duration selector chips
            if (state.timerState == TimerState.IDLE) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.plannedMin == 25,
                        onClick = { viewModel.setPlannedMin(25) },
                        label = { Text("25 phút") }
                    )
                    FilterChip(
                        selected = state.plannedMin == 45,
                        onClick = { viewModel.setPlannedMin(45) },
                        label = { Text("45 phút") }
                    )
                    FilterChip(
                        selected = state.plannedMin == 60,
                        onClick = { viewModel.setPlannedMin(60) },
                        label = { Text("60 phút") }
                    )
                }
            }

            // Timer display
            val min = state.remainingSec / 60
            val sec = state.remainingSec % 60
            val timeStr = String.format("%02d:%02d", min, sec)

            Box(contentAlignment = Alignment.Center) {
                val totalSec = state.plannedMin * 60
                val progress = if (totalSec > 0) state.remainingSec.toFloat() / totalSec.toFloat() else 0f

                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(240.dp),
                    strokeWidth = 12.dp
                )

                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.displayLarge
                )
            }

            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.timerState == TimerState.RUNNING) {
                    IconButton(
                        onClick = { viewModel.pauseTimer() },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = "Tạm dừng", modifier = Modifier.size(48.dp))
                    }
                } else {
                    IconButton(
                        onClick = { viewModel.startTimer() },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Bắt đầu", modifier = Modifier.size(48.dp))
                    }
                }

                if (state.timerState != TimerState.IDLE) {
                    IconButton(
                        onClick = { viewModel.stopTimer() },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Dừng", modifier = Modifier.size(48.dp))
                    }
                }
            }
        }
    }
}
