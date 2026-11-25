package com.uniplan.app.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.uniplan.app.domain.usecase.NextClassState
import com.uniplan.app.util.VietnameseText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onNavigateToSchedule: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToCoinJar: () -> Unit,
    onNavigateToClassForm: (Long) -> Unit,
    onNavigateToTaskForm: (Long) -> Unit,
    onNavigateToExamForm: (Long) -> Unit,
    onNavigateToTimeBlockForm: (Long) -> Unit,
    onNavigateToCreateSemester: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showFabMenu by remember { mutableStateOf(false) }
    var selectedMood by remember { mutableStateOf("🎯 Tập trung") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = VietnameseText.fullDate(state.todayDate),
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (state.weekLabel.isNotBlank()) {
                            Text(
                                text = state.weekLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (showFabMenu) {
                    SmallFloatingActionButton(
                        onClick = { showFabMenu = false; onNavigateToClassForm(0L) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Class, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Buổi học")
                        }
                    }

                    SmallFloatingActionButton(
                        onClick = { showFabMenu = false; onNavigateToTimeBlockForm(0L) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Event, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Khối thời gian")
                        }
                    }

                    SmallFloatingActionButton(
                        onClick = { showFabMenu = false; onNavigateToTaskForm(0L) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Task, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Nhiệm vụ")
                        }
                    }

                    SmallFloatingActionButton(
                        onClick = { showFabMenu = false; onNavigateToExamForm(0L) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Event, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Lịch thi")
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm nhanh")
                }
            }
        }
    ) { padding ->
        if (state.currentSemester == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tạo học kỳ để bắt đầu", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onNavigateToCreateSemester, shape = RoundedCornerShape(20.dp)) {
                        Text("Tạo học kỳ")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tiimo Mood & Energy Check-in Bar
                item {
                    Column {
                        Text(
                            text = "⚡ Năng lượng & Tâm trạng",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val moods = listOf("🔥 Sung sức", "🎯 Tập trung", "😐 Bình thường", "☕ Cần nghỉ ngơi")
                            items(moods) { mood ->
                                FilterChip(
                                    selected = selectedMood == mood,
                                    onClick = { selectedMood = mood },
                                    label = { Text(mood) },
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                        }
                    }
                }

                // Tiimo Visual Elapsing Time Card (Rendez le temps visible)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                when (val nc = state.nextClassState) {
                                    is NextClassState.Ongoing -> {
                                        Text("ĐANG DIỄN RA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.height(4.dp))
                                        Text(nc.occurrence.subjectName, style = MaterialTheme.typography.titleLarge)
                                        Text("Còn ${nc.remainingMinutes} phút · Phòng ${nc.occurrence.room ?: "Online"}")
                                    }
                                    is NextClassState.Next -> {
                                        Text("SẮP DIỄN RA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                        Spacer(Modifier.height(4.dp))
                                        Text(nc.occurrence.subjectName, style = MaterialTheme.typography.titleLarge)
                                        val timeStr = if (nc.minutesUntil < 60) "${nc.minutesUntil} phút nữa" else "${nc.hoursUntil} giờ nữa"
                                        Text("$timeStr (${nc.occurrence.startTime}) · Phòng ${nc.occurrence.room ?: "Chưa xếp"}")
                                    }
                                    is NextClassState.EndedToday -> {
                                        Text("HOÀN THÀNH LỊCH HỌC", style = MaterialTheme.typography.labelSmall)
                                        Spacer(Modifier.height(4.dp))
                                        Text("Hôm nay đã hết tiết học! ✨", style = MaterialTheme.typography.titleMedium)
                                    }
                                    is NextClassState.NoUpcoming -> {
                                        Text("THỜI GIAN THƯ GIÃN", style = MaterialTheme.typography.labelSmall)
                                        Spacer(Modifier.height(4.dp))
                                        Text("Không có lịch học sắp tới", style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }

                            // Visual Progress Circle
                            if (state.nextClassState is NextClassState.Ongoing) {
                                val nc = state.nextClassState as NextClassState.Ongoing
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        progress = { 0.65f },
                                        modifier = Modifier.size(64.dp),
                                        strokeWidth = 6.dp
                                    )
                                    Text("${nc.remainingMinutes}m", style = MaterialTheme.typography.labelMedium)
                                }
                            } else {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Tiimo Streak Counter Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFFF6D00),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Chuỗi 5 ngày liên tiếp! 🔥", style = MaterialTheme.typography.titleSmall)
                                Text("Bạn đang duy trì thói quen học tập tuyệt vời", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Mini Coin Jar Banner
                item {
                    Card(
                        onClick = onNavigateToCoinJar,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Lọ thành tựu (Coin Jar) 🏺", style = MaterialTheme.typography.titleSmall)
                                Text("Đã tích lũy ${state.coinCount} xu trong tháng này", style = MaterialTheme.typography.bodyMedium)
                            }
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Today Schedule Summary
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Lịch học hôm nay", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = onNavigateToSchedule) { Text("Xem timeline") }
                    }
                }

                if (state.todayOccurrences.isEmpty()) {
                    item { Text("Không có tiết học nào trong hôm nay", style = MaterialTheme.typography.bodyMedium) }
                } else {
                    items(state.todayOccurrences) { occ ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToClassForm(occ.sessionId) },
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color(occ.subjectColor))
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(occ.subjectName, style = MaterialTheme.typography.titleSmall)
                                    Text("${occ.startTime} - ${occ.endTime} · Phòng ${occ.room ?: "Chưa xếp"}", style = MaterialTheme.typography.bodySmall)
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "${occ.startPeriod ?: 1}-${occ.endPeriod ?: 3}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }

                // Upcoming Tasks
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Nhiệm vụ & Deadline sắp tới", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = onNavigateToTasks) { Text("Xem tất cả") }
                    }
                }

                if (state.upcomingTasks.isEmpty()) {
                    item { Text("Chưa có nhiệm vụ nào", style = MaterialTheme.typography.bodyMedium) }
                } else {
                    items(state.upcomingTasks) { task ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToTaskForm(task.id) },
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = false,
                                    onCheckedChange = { viewModel.markTaskDone(task.id) }
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(task.title, style = MaterialTheme.typography.titleSmall)
                                    if (task.dueAt != null) {
                                        Text("Hạn: ${task.dueAt.replace('T', ' ')}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(task.priority.name) },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}
