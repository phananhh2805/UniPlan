package com.uniplan.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToSemesters: () -> Unit,
    onNavigateToSubjects: () -> Unit,
    onNavigateToPeriods: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToBackup: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsCategoryHeader("Dữ liệu & Cấu hình")

            SettingsItem(
                icon = Icons.Default.DateRange,
                title = "Quản lý học kỳ",
                onClick = onNavigateToSemesters
            )

            SettingsItem(
                icon = Icons.Default.Book,
                title = "Danh sách môn học",
                onClick = onNavigateToSubjects
            )

            SettingsItem(
                icon = Icons.Default.Schedule,
                title = "Khung giờ tiết học",
                onClick = onNavigateToPeriods
            )

            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Cài đặt nhắc nhở",
                onClick = onNavigateToReminders
            )

            HorizontalDivider()
            SettingsCategoryHeader("Hệ thống")

            SettingsItem(
                icon = Icons.Default.Backup,
                title = "Sao lưu & Khôi phục",
                onClick = onNavigateToBackup
            )

            // Theme selector
            var themeExpanded by remember { mutableStateOf(false) }
            ListItem(
                leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) },
                headlineContent = { Text("Giao diện") },
                supportingContent = {
                    Text(
                        when (state.themeMode) {
                            "LIGHT" -> "Sáng"
                            "DARK" -> "Tối"
                            else -> "Theo hệ thống"
                        }
                    )
                },
                modifier = Modifier.clickable { themeExpanded = true }
            )

            if (themeExpanded) {
                AlertDialog(
                    onDismissRequest = { themeExpanded = false },
                    title = { Text("Chọn giao diện") },
                    text = {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setThemeMode("SYSTEM"); themeExpanded = false }
                                    .padding(12.dp)
                            ) { Text("Theo hệ thống") }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setThemeMode("LIGHT"); themeExpanded = false }
                                    .padding(12.dp)
                            ) { Text("Sáng") }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setThemeMode("DARK"); themeExpanded = false }
                                    .padding(12.dp)
                            ) { Text("Tối") }
                        }
                    },
                    confirmButton = {}
                )
            }

            HorizontalDivider()
            SettingsCategoryHeader("Giới thiệu")

            ListItem(
                leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                headlineContent = { Text("UniPlan") },
                supportingContent = { Text("Phiên bản 1.0 (MVP)") }
            )
        }
    }
}

@Composable
fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    ListItem(
        leadingContent = { Icon(icon, contentDescription = null) },
        headlineContent = { Text(title) },
        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
        modifier = Modifier.clickable { onClick() }
    )
}
