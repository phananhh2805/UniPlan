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
import javax.inject.Inject

class OccurrenceGenerator @Inject constructor() {
    fun forDate(
        sem: SemesterEntity,
        sessions: List<ClassSessionEntity>,
        exceptionsOfDate: List<ClassExceptionEntity>,
        subjectsMap: Map<Long, SubjectEntity>,
        date: LocalDate
    ): List<Occurrence> {
        val semStart = LocalDate.parse(sem.startDate)
        val semEnd = LocalDate.parse(sem.endDate)
        val idx = WeekCalculator.weekIndex(semStart, semEnd, date) ?: return emptyList()

        val occurrences = mutableListOf<Occurrence>()

        val dayValue = date.dayOfWeek.value
        val regularSessions = sessions.filter { s ->
            s.isActive &&
                    s.dayOfWeek == dayValue &&
                    idx in s.weekFrom..s.weekTo &&
                    WeekCalculator.matchesPattern(s.weekPattern, idx)
        }

        for (s in regularSessions) {
            val ex = exceptionsOfDate.find { it.sessionId == s.id }
            val subject = subjectsMap[s.subjectId]
            val subName = subject?.name ?: ""
            val subColor = subject?.color ?: 0xFF1E88E5.toInt()

            if (ex == null) {
                occurrences.add(
                    Occurrence(
                        sessionId = s.id,
                        subjectId = s.subjectId,
                        date = date,
                        startTime = LocalTime.parse(s.startTime),
                        endTime = LocalTime.parse(s.endTime),
                        room = s.room,
                        campus = s.campus,
                        type = s.type,
                        status = null,
                        note = null,
                        weekIndex = idx,
                        subjectName = subName,
                        subjectColor = subColor,
                        startPeriod = s.startPeriod,
                        endPeriod = s.endPeriod
                    )
                )
            } else {
                when (ex.status) {
                    ExceptionStatus.CANCELED -> {
                        occurrences.add(
                            Occurrence(
                                sessionId = s.id,
                                subjectId = s.subjectId,
                                date = date,
                                startTime = LocalTime.parse(s.startTime),
                                endTime = LocalTime.parse(s.endTime),
                                room = s.room,
                                campus = s.campus,
                                type = s.type,
                                status = ExceptionStatus.CANCELED,
                                note = ex.note,
                                weekIndex = idx,
                                subjectName = subName,
                                subjectColor = subColor,
                                startPeriod = s.startPeriod,
                                endPeriod = s.endPeriod
                            )
                        )
                    }
                    ExceptionStatus.ROOM_CHANGED -> {
                        occurrences.add(
                            Occurrence(
                                sessionId = s.id,
                                subjectId = s.subjectId,
                                date = date,
                                startTime = LocalTime.parse(s.startTime),
                                endTime = LocalTime.parse(s.endTime),
                                room = ex.newRoom ?: s.room,
                                campus = s.campus,
                                type = s.type,
                                status = ExceptionStatus.ROOM_CHANGED,
                                note = ex.note,
                                weekIndex = idx,
                                subjectName = subName,
                                subjectColor = subColor,
                                startPeriod = s.startPeriod,
                                endPeriod = s.endPeriod
                            )
                        )
                    }
                    ExceptionStatus.TIME_CHANGED -> {
                        val st = ex.newStartTime?.let { LocalTime.parse(it) } ?: LocalTime.parse(s.startTime)
                        val et = ex.newEndTime?.let { LocalTime.parse(it) } ?: LocalTime.parse(s.endTime)
                        occurrences.add(
                            Occurrence(
                                sessionId = s.id,
                                subjectId = s.subjectId,
                                date = date,
                                startTime = st,
                                endTime = et,
                                room = s.room,
                                campus = s.campus,
                                type = s.type,
                                status = ExceptionStatus.TIME_CHANGED,
                                note = ex.note,
                                weekIndex = idx,
                                subjectName = subName,
                                subjectColor = subColor,
                                startPeriod = s.startPeriod,
                                endPeriod = s.endPeriod
                            )
                        )
                    }
                    ExceptionStatus.MAKEUP -> {
                    }
                }
            }
        }

        val makeupExceptions = exceptionsOfDate.filter { it.status == ExceptionStatus.MAKEUP }
        for (ex in makeupExceptions) {
            val s = sessions.find { it.id == ex.sessionId } ?: continue
            val subject = subjectsMap[s.subjectId]
            val subName = subject?.name ?: ""
            val subColor = subject?.color ?: 0xFF1E88E5.toInt()

            val st = ex.newStartTime?.let { LocalTime.parse(it) } ?: LocalTime.parse(s.startTime)
            val et = ex.newEndTime?.let { LocalTime.parse(it) } ?: LocalTime.parse(s.endTime)
            val rm = ex.newRoom ?: s.room

            occurrences.add(
                Occurrence(
                    sessionId = s.id,
                    subjectId = s.subjectId,
                    date = date,
                    startTime = st,
                    endTime = et,
                    room = rm,
                    campus = s.campus,
                    type = s.type,
                    status = ExceptionStatus.MAKEUP,
                    note = ex.note,
                    weekIndex = idx,
                    subjectName = subName,
                    subjectColor = subColor,
                    startPeriod = s.startPeriod,
                    endPeriod = s.endPeriod
                )
            )
        }

        return occurrences.sortedWith(compareBy({ it.startTime }, { it.subjectName }))
    }
}
