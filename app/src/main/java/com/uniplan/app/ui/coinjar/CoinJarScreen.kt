package com.uniplan.app.ui.coinjar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinJarScreen(
    viewModel: CoinJarViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showGoalDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lọ thành tựu (${state.currentMonthKey})") },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Count and Goal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.coins.size} / ${state.goal} xu",
                    style = MaterialTheme.typography.titleLarge
                )
                TextButton(onClick = { showGoalDialog = true }) {
                    Text("Đổi mục tiêu")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Canvas Glass Jar
            CoinJarCanvas(
                coins = state.coins,
                goal = state.goal,
                onCoinClicked = { viewModel.selectCoin(it) },
                modifier = Modifier.weight(1f)
            )

            // Encouragement message
            val percent = if (state.goal > 0) (state.coins.size * 100) / state.goal else 0
            val msg = when {
                state.coins.isEmpty() -> "Lọ còn trống, việc đầu tiên đang chờ bạn"
                percent < 50 -> "Đã có ${state.coins.size} xu, cứ thế tiến lên!"
                percent < 100 -> "Hơn nửa lọ rồi, tuyệt vời!"
                else -> "Đầy lọ! Bạn đã hoàn thành mục tiêu tháng 🎉"
            }
            Text(
                text = msg,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        // Coin details dialog
        state.selectedCoin?.let { coin ->
            AlertDialog(
                onDismissRequest = { viewModel.selectCoin(null) },
                title = { Text(coin.label) },
                text = {
                    Column {
                        coin.subjectName?.let { Text("Môn: $it") }
                        Text("Thời điểm: ${coin.earnedAt}")
                        Text("Loại xu: ${coin.tier}")
                        Text(if (coin.isOnTime) "Đúng hạn" else "Hoàn thành sau hạn")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.selectCoin(null) }) {
                        Text("Đóng")
                    }
                }
            )
        }

        // Goal dialog
        if (showGoalDialog) {
            var inputGoal by remember { mutableStateOf(state.goal.toString()) }
            AlertDialog(
                onDismissRequest = { showGoalDialog = false },
                title = { Text("Đặt mục tiêu xu cho tháng") },
                text = {
                    OutlinedTextField(
                        value = inputGoal,
                        onValueChange = { inputGoal = it },
                        label = { Text("Số xu (1 - 200)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        val g = inputGoal.toIntOrNull() ?: 30
                        viewModel.updateGoal(g)
                        showGoalDialog = false
                    }) {
                        Text("Lưu")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGoalDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}
