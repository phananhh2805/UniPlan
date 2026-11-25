package com.uniplan.app.ui.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniplan.app.domain.model.WeekPattern
import com.uniplan.app.util.VietnameseText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassFormScreen(
    viewModel: ClassFormViewModel,
    sessionId: Long,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.load(sessionId)
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (sessionId == 0L) "Thêm buổi học" else "Sửa buổi học") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Subject Dropdown
            var subjectExpanded by remember { mutableStateOf(false) }
            val selectedSubName = state.subjects.find { it.id == state.selectedSubjectId }?.name ?: "Chọn môn học"

            ExposedDropdownMenuBox(
                expanded = subjectExpanded,
                onExpandedChange = { subjectExpanded = !subjectExpanded }
            ) {
                OutlinedTextField(
                    value = selectedSubName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Môn học") },
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

            // Day of week chips
            Text("Thứ trong tuần", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                (1..7).forEach { day ->
                    FilterChip(
                        selected = state.dayOfWeek == day,
                        onClick = { viewModel.updateDayOfWeek(day) },
                        label = { Text(VietnameseText.dayShort(day)) }
                    )
                }
            }

            // Period selector
            Text("Tiết học: Tiết ${state.startPeriod} đến Tiết ${state.endPeriod} (${state.startTime} - ${state.endTime})", style = MaterialTheme.typography.titleSmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tiết bắt đầu: ")
                Slider(
                    value = state.startPeriod.toFloat(),
                    onValueChange = { viewModel.updatePeriods(it.toInt(), maxOf(it.toInt(), state.endPeriod)) },
                    valueRange = 1f..12f,
                    steps = 10,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tiết kết thúc: ")
                Slider(
                    value = state.endPeriod.toFloat(),
                    onValueChange = { viewModel.updatePeriods(minOf(it.toInt(), state.startPeriod), it.toInt()) },
                    valueRange = 1f..12f,
                    steps = 10,
                    modifier = Modifier.weight(1f)
                )
            }

            // Room
            OutlinedTextField(
                value = state.room,
                onValueChange = { viewModel.updateRoom(it) },
                label = { Text("Phòng học (ví dụ: A2-301)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Weeks
            Text("Tuần học: Tuần ${state.weekFrom} đến ${state.weekTo}", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.weekPattern == WeekPattern.ALL,
                    onClick = { viewModel.updateWeeks(state.weekFrom, state.weekTo, WeekPattern.ALL) },
                    label = { Text("Mọi tuần") }
                )
                FilterChip(
                    selected = state.weekPattern == WeekPattern.ODD,
                    onClick = { viewModel.updateWeeks(state.weekFrom, state.weekTo, WeekPattern.ODD) },
                    label = { Text("Tuần lẻ") }
                )
                FilterChip(
                    selected = state.weekPattern == WeekPattern.EVEN,
                    onClick = { viewModel.updateWeeks(state.weekFrom, state.weekTo, WeekPattern.EVEN) },
                    label = { Text("Tuần chẵn") }
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
                Text("Lưu buổi học")
            }
        }

        // Conflict dialog (TC-11)
        if (state.showConflictDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Trùng lịch học") },
                text = { Text("Buổi học này bị trùng giờ với ${state.conflictCount} buổi học khác trong học kỳ. Bạn vẫn muốn lưu?") },
                confirmButton = {
                    Button(onClick = { viewModel.save(force = true) }) {
                        Text("Vẫn lưu")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.save(force = false) }) {
                        Text("Sửa lại")
                    }
                }
            )
        }
    }
}
