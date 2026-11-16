package com.uniplan.app.domain.usecase

import com.uniplan.app.data.local.db.ClassExceptionEntity
import com.uniplan.app.data.local.db.ClassSessionEntity
import com.uniplan.app.data.local.db.SemesterEntity
import com.uniplan.app.domain.model.ExceptionStatus
import com.uniplan.app.util.WeekCalculator
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.max
import kotlin.math.min

class CheckConflictUseCase {

    fun findConflicts(
        semester: SemesterEntity,
        draft: ClassSessionEntity,
        allSessions: List<ClassSessionEntity>,
        exceptions: List<ClassExceptionEntity>
    ): List<ClassSessionEntity> {
        val semStart = LocalDate.parse(semester.startDate)
        val semEnd = LocalDate.parse(semester.endDate)

        val draftStart = LocalTime.parse(draft.startTime)
        val draftEnd = LocalTime.parse(draft.endTime)

        val canceledMap = exceptions
            .filter { it.status == ExceptionStatus.CANCELED }
            .groupBy { Triple(it.sessionId, it.date, true) }

        return allSessions.filter { existing ->
            existing.id != draft.id &&
                    existing.isActive &&
                    existing.dayOfWeek == draft.dayOfWeek &&
                    overlap(draftStart, draftEnd, LocalTime.parse(existing.startTime), LocalTime.parse(existing.endTime)) &&
                    hasIntersectingActiveWeeks(semStart, semEnd, draft, existing, canceledMap)
        }
    }

    private fun overlap(s1: LocalTime, e1: LocalTime, s2: LocalTime, e2: LocalTime): Boolean {
        return s1.isBefore(e2) && s2.isBefore(e1)
    }

    private fun hasIntersectingActiveWeeks(
        semStart: LocalDate,
        semEnd: LocalDate,
        draft: ClassSessionEntity,
        existing: ClassSessionEntity,
        canceledMap: Map<Triple<Long, String, Boolean>, List<ClassExceptionEntity>>
    ): Boolean {
        val lo = max(draft.weekFrom, existing.weekFrom)
        val hi = min(draft.weekTo, existing.weekTo)

        for (w in lo..hi) {
            if (WeekCalculator.matchesPattern(draft.weekPattern, w) &&
                WeekCalculator.matchesPattern(existing.weekPattern, w)
            ) {
                val date = WeekCalculator.dateOf(semStart, w, existing.dayOfWeek)
                val isCanceled = canceledMap.containsKey(Triple(existing.id, date.toString(), true))
                if (!isCanceled) {
                    return true
                }
            }
        }
        return false
    }
}
