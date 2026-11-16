package com.uniplan.app.domain.usecase

import com.uniplan.app.data.local.db.ClassExceptionEntity
import com.uniplan.app.data.local.db.ClassSessionEntity
import com.uniplan.app.data.local.db.SemesterEntity
import com.uniplan.app.data.local.db.SubjectEntity
import com.uniplan.app.domain.model.ExceptionStatus
import com.uniplan.app.domain.model.Occurrence
import com.uniplan.app.util.WeekCalculator
import java.time.LocalDate
import java.time.LocalTime

class OccurrenceGenerator {

    fun forDate(
        semester: SemesterEntity,
        subjects: Map<Long, SubjectEntity>,
        sessions: List<ClassSessionEntity>,
        exceptionsOfDate: List<ClassExceptionEntity>,
        date: LocalDate
    ): List<Occurrence> {
        val semStart = LocalDate.parse(semester.startDate)
        val semEnd = LocalDate.parse(semester.endDate)
        val weekIdx = WeekCalculator.weekIndex(semStart, semEnd, date) ?: return emptyList()

        val dayOfWeek = date.dayOfWeek.value
        val result = mutableListOf<Occurrence>()

        val exceptionsMap = exceptionsOfDate.associateBy { it.sessionId }

        val activeSessionsForDay = sessions.filter {
            it.isActive &&
                    it.dayOfWeek == dayOfWeek &&
                    weekIdx >= it.weekFrom &&
                    weekIdx <= it.weekTo &&
                    WeekCalculator.matchesPattern(it.weekPattern, weekIdx)
        }

        for (session in activeSessionsForDay) {
            val subject = subjects[session.subjectId] ?: continue
            val exc = exceptionsMap[session.id]

            val status: ExceptionStatus? = exc?.status
            val startTime = if (exc?.newStartTime != null) LocalTime.parse(exc.newStartTime) else LocalTime.parse(session.startTime)
            val endTime = if (exc?.newEndTime != null) LocalTime.parse(exc.newEndTime) else LocalTime.parse(session.endTime)
            val room = exc?.newRoom ?: session.room

            result.add(
                Occurrence(
                    sessionId = session.id,
                    subjectId = subject.id,
                    subjectName = subject.name,
                    subjectCode = subject.code,
                    subjectColor = subject.color,
                    subjectIcon = subject.icon,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    startPeriod = session.startPeriod,
                    endPeriod = session.endPeriod,
                    room = room,
                    campus = session.campus,
                    type = session.type,
                    status = status,
                    note = exc?.note,
                    weekIndex = weekIdx
                )
            )
        }

        val makeupExceptions = exceptionsOfDate.filter { it.status == ExceptionStatus.MAKEUP }
        for (exc in makeupExceptions) {
            if (result.any { it.sessionId == exc.sessionId && it.status == ExceptionStatus.MAKEUP }) continue

            val session = sessions.find { it.id == exc.sessionId } ?: continue
            val subject = subjects[session.subjectId] ?: continue

            val startTime = if (exc.newStartTime != null) LocalTime.parse(exc.newStartTime) else LocalTime.parse(session.startTime)
            val endTime = if (exc.newEndTime != null) LocalTime.parse(exc.newEndTime) else LocalTime.parse(session.endTime)
            val room = exc.newRoom ?: session.room

            result.add(
                Occurrence(
                    sessionId = session.id,
                    subjectId = subject.id,
                    subjectName = subject.name,
                    subjectCode = subject.code,
                    subjectColor = subject.color,
                    subjectIcon = subject.icon,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    startPeriod = session.startPeriod,
                    endPeriod = session.endPeriod,
                    room = room,
                    campus = session.campus,
                    type = session.type,
                    status = ExceptionStatus.MAKEUP,
                    note = exc.note,
                    weekIndex = weekIdx
                )
            )
        }

        return result.sortedWith(compareBy({ it.startTime }, { it.subjectName }))
    }
}
