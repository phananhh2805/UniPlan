package com.uniplan.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class Occurrence(
    val sessionId: Long,
    val subjectId: Long,
    val subjectName: String,
    val subjectCode: String?,
    val subjectColor: Int,
    val subjectIcon: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val startPeriod: Int?,
    val endPeriod: Int?,
    val room: String?,
    val campus: String?,
    val type: ClassType,
    val status: ExceptionStatus?,
    val note: String?,
    val weekIndex: Int
)

sealed class TimelineItem {
    abstract val start: LocalTime
    abstract val end: LocalTime

    data class ClassItem(val occurrence: Occurrence) : TimelineItem() {
        override val start: LocalTime get() = occurrence.startTime
        override val end: LocalTime get() = occurrence.endTime
    }

    data class BlockItem(
        val id: Long,
        val taskId: Long?,
        val title: String,
        val category: BlockCategory,
        val date: LocalDate,
        val startTime: LocalTime,
        val endTime: LocalTime,
        val color: Int,
        val icon: String
    ) : TimelineItem() {
        override val start: LocalTime get() = startTime
        override val end: LocalTime get() = endTime
    }

    data class ExamItem(
        val id: Long,
        val subjectId: Long,
        val subjectName: String,
        val examType: String,
        val date: LocalDate,
        val startTime: LocalTime,
        val durationMin: Int,
        val room: String?,
        val color: Int
    ) : TimelineItem() {
        override val start: LocalTime get() = startTime
        override val end: LocalTime get() = startTime.plusMinutes(durationMin.toLong())
    }

    data class DeadlineItem(
        val id: Long,
        val title: String,
        val dueAt: LocalDateTime,
        val priority: Priority,
        val subjectName: String?,
        val subjectColor: Int?
    ) : TimelineItem() {
        override val start: LocalTime get() = dueAt.toLocalTime()
        override val end: LocalTime get() = dueAt.toLocalTime().plusMinutes(1)
    }

    data class AllDayOverdue(
        val overdueTasks: List<DeadlineItem>
    ) : TimelineItem() {
        override val start: LocalTime get() = LocalTime.MIN
        override val end: LocalTime get() = LocalTime.MIN
    }
}

data class PeriodInfo(
    val period: Int,
    val startTime: LocalTime,
    val endTime: LocalTime
)

data class PlacedTimelineItem(
    val item: TimelineItem,
    val col: Int,
    val colCount: Int
)
