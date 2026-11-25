package com.uniplan.app.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    onNavigateToCoinJar: () -> Unit,
    onNavigateToFocus: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thống kê") }
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
                    Text("Tổng quan tuần này", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Nhiệm vụ hoàn thành: ${state.completedCount}", style = MaterialTheme.typography.bodyLarge)
                    Text("Tỷ lệ đúng hạn: ${state.onTimeRatePercent}%", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Card(
                onClick = onNavigateToCoinJar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Lọ thành tựu", style = MaterialTheme.typography.titleMedium)
                        Text("Xem bộ sưu tập xu thành tựu của bạn", style = MaterialTheme.typography.bodyMedium)
                    }
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            Button(
                onClick = onNavigateToFocus,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Bắt đầu phiên tập trung")
            }
        }
    }
}
