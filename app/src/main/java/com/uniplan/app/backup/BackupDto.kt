package com.uniplan.app.backup

import com.uniplan.app.data.local.db.*

data class BackupDto(
    val schemaVersion: Int = 1,
    val appVersion: String = "1.0.0",
    val exportedAt: String,
    val semesters: List<SemesterEntity> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
    val classSessions: List<ClassSessionEntity> = emptyList(),
    val classExceptions: List<ClassExceptionEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val subtasks: List<SubtaskEntity> = emptyList(),
    val exams: List<ExamEntity> = emptyList(),
    val timeBlocks: List<TimeBlockEntity> = emptyList(),
    val reminders: List<ReminderEntity> = emptyList(),
    val focusSessions: List<FocusSessionEntity> = emptyList(),
    val coins: List<CoinEntity> = emptyList(),
    val jarMonths: List<JarMonthEntity> = emptyList()
)
