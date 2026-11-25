package com.uniplan.app.domain.usecase

import com.uniplan.app.data.local.db.ClassExceptionEntity
import com.uniplan.app.data.local.db.ClassSessionEntity
import com.uniplan.app.data.local.db.SemesterEntity
import com.uniplan.app.domain.model.ExceptionStatus
import com.uniplan.app.util.WeekCalculator
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class CheckConflictUseCase @Inject constructor() {
    private fun overlap(aStart: LocalTime, aEnd: LocalTime, bStart: LocalTime, bEnd: LocalTime): Boolean {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd)
    }

    private fun isCanceled(s: ClassSessionEntity, date: LocalDate, exceptions: List<ClassExceptionEntity>): Boolean {
        val ex = exceptions.find { it.sessionId == s.id && it.date == date.toString() }
        return ex?.status == ExceptionStatus.CANCELED
    }

    fun findConflicts(
        sem: SemesterEntity,
        sessionDraft: ClassSessionEntity,
        allSessions: List<ClassSessionEntity>,
        exceptions: List<ClassExceptionEntity>
    ): List<ClassSessionEntity> {
        val semStart = LocalDate.parse(sem.startDate)
        val draftStart = LocalTime.parse(sessionDraft.startTime)
        val draftEnd = LocalTime.parse(sessionDraft.endTime)

        return allSessions.filter { t ->
            t.id != sessionDraft.id &&
                    t.isActive &&
                    t.dayOfWeek == sessionDraft.dayOfWeek &&
                    overlap(draftStart, draftEnd, LocalTime.parse(t.startTime), LocalTime.parse(t.endTime))
        }.filter { t ->
            val lo = max(sessionDraft.weekFrom, t.weekFrom)
            val hi = min(sessionDraft.weekTo, t.weekTo)
            (lo..hi).any { w ->
                WeekCalculator.matchesPattern(sessionDraft.weekPattern, w) &&
                        WeekCalculator.matchesPattern(t.weekPattern, w) &&
                        !isCanceled(t, WeekCalculator.dateOf(semStart, w, t.dayOfWeek), exceptions)
            }
        }
    }
}
