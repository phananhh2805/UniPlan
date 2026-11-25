package com.uniplan.app.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class Occurrence(
    val sessionId: Long,
    val subjectId: Long,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val room: String?,
    val campus: String?,
    val type: ClassType,
    val status: ExceptionStatus?,
    val note: String?,
    val weekIndex: Int,
    val subjectName: String = "",
    val subjectColor: Int = 0,
    val startPeriod: Int? = null,
    val endPeriod: Int? = null
)
