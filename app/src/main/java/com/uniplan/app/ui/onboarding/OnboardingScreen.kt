package com.uniplan.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniplan.app.util.VietnameseText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinished: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) {
            onFinished()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chào mừng đến UniPlan") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Step content
            when (state.step) {
                1 -> StepSemester(state, viewModel)
                2 -> StepPeriodPreview()
                3 -> StepFirstSubject(state, viewModel)
            }

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.step > 1) {
                    OutlinedButton(onClick = { viewModel.prevStep() }) {
                        Text("Quay lại")
                    }
                } else {
                    TextButton(onClick = { viewModel.completeOnboarding() }) {
                        Text("Bỏ qua")
                    }
                }

                Button(onClick = { viewModel.nextStep() }) {
                    Text(if (state.step == 3) "Hoàn tất" else "Tiếp")
                }
            }
        }
    }
}

@Composable
fun StepSemester(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Bước 1: Thiết lập học kỳ đầu tiên", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.semesterName,
            onValueChange = { viewModel.updateSemesterName(it) },
            label = { Text("Tên học kỳ") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Ngày bắt đầu: ${VietnameseText.fullDate(state.startDate)}")
        Text("Ngày kết thúc: ${VietnameseText.fullDate(state.endDate)}")

        if (state.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(state.error, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun StepPeriodPreview() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Bước 2: Khung giờ tiết học mặc định", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "UniPlan cấu hình sẵn 12 tiết học (Tiết 1 từ 07:00). Bạn có thể chỉnh sửa khung giờ này bất kỳ lúc nào trong phần Cài đặt.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun StepFirstSubject(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Bước 3: Thêm môn học đầu tiên (tùy chọn)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.subjectName,
            onValueChange = { viewModel.updateSubject(it, state.subjectCode, state.subjectColor) },
            label = { Text("Tên môn học (ví dụ: Lập trình Android)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.subjectCode,
            onValueChange = { viewModel.updateSubject(state.subjectName, it, state.subjectColor) },
            label = { Text("Mã môn (ví dụ: INT1234)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
