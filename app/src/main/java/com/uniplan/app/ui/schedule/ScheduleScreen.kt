package com.uniplan.app.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.uniplan.app.domain.model.TimelineItem
import com.uniplan.app.util.VietnameseText
import com.uniplan.app.util.WeekCalculator
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    onNavigateToClassDetail: (Long, String) -> Unit,
    onNavigateToClassForm: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.weekLabel.ifBlank { "Lịch" }) },
                actions = {
                    IconButton(onClick = { viewModel.goToToday() }) {
                        Icon(Icons.Default.Today, contentDescription = "Hôm nay")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tiimo Mode Switcher Chips (Tuần, Ngày, Tháng)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                FilterChip(
                    selected = state.mode == ScheduleMode.WEEK,
                    onClick = { viewModel.setMode(ScheduleMode.WEEK) },
                    label = { Text("Tuần") },
                    shape = RoundedCornerShape(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = state.mode == ScheduleMode.DAY,
                    onClick = { viewModel.setMode(ScheduleMode.DAY) },
                    label = { Text("Ngày") },
                    shape = RoundedCornerShape(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = state.mode == ScheduleMode.MONTH,
                    onClick = { viewModel.setMode(ScheduleMode.MONTH) },
                    label = { Text("Tháng") },
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // Week navigation header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.setSelectedDate(state.selectedDate.minusWeeks(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tuần trước")
                }
                Text(
                    text = VietnameseText.fullDate(state.selectedDate),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { viewModel.setSelectedDate(state.selectedDate.plusWeeks(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Tuần sau")
                }
            }

            Spacer(Modifier.height(8.dp))

            when (state.mode) {
                ScheduleMode.WEEK -> WeekView(state, onNavigateToClassDetail, viewModel)
                ScheduleMode.DAY -> DayTimelineView(state, onNavigateToClassDetail)
                ScheduleMode.MONTH -> MonthView(state, viewModel)
            }
        }
    }
}

@Composable
fun WeekView(
    state: ScheduleUiState,
    onNavigateToClassDetail: (Long, String) -> Unit,
    viewModel: ScheduleViewModel
) {
    val weekStart = WeekCalculator.weekStart(state.selectedDate)
    val days = (0..6).map { weekStart.plusDays(it.toLong()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(days) { date ->
            val isToday = date == LocalDate.now()
            val occs = state.weekOccurrences[date] ?: emptyList()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setSelectedDate(date) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${VietnameseText.dayLabel(date.dayOfWeek.value)} - ${VietnameseText.shortDate(date)}",
                        style = MaterialTheme.typography.titleSmall
                    )

                    if (occs.isEmpty()) {
                        Text("Không có tiết học", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Spacer(Modifier.height(8.dp))
                        occs.forEach { occ ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onNavigateToClassDetail(occ.sessionId, date.toString()) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(occ.subjectColor))
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "${occ.subjectName} (${occ.startTime} - ${occ.endTime}) - Phòng ${occ.room ?: "Chưa xếp"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayTimelineView(
    state: ScheduleUiState,
    onNavigateToClassDetail: (Long, String) -> Unit
) {
    if (state.dayTimelineItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không có tiết học hoặc sự kiện nào trong ngày này")
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.dayTimelineItems) { placed ->
                when (val item = placed.item) {
                    is TimelineItem.ClassItem -> {
                        val occ = item.occurrence
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToClassDetail(occ.sessionId, state.selectedDate.toString()) },
                            shape = RoundedCornerShape(20.dp)
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
                                    Text(occ.subjectName, style = MaterialTheme.typography.titleMedium)
                                    Text("${occ.startTime} - ${occ.endTime} · Phòng ${occ.room ?: "Chưa xếp"}")
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${occ.startPeriod ?: 1}-${occ.endPeriod ?: 3}",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun MonthView(
    state: ScheduleUiState,
    viewModel: ScheduleViewModel
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Chế độ xem Tháng cho ngày ${VietnameseText.shortDate(state.selectedDate)}")
    }
}
