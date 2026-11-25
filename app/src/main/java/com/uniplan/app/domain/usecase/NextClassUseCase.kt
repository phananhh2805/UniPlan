package com.uniplan.app.domain.usecase

import com.uniplan.app.domain.model.ExceptionStatus
import com.uniplan.app.domain.model.Occurrence
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

sealed class NextClassState {
    data class Ongoing(val occurrence: Occurrence, val remainingMinutes: Long) : NextClassState()
    data class Next(val occurrence: Occurrence, val minutesUntil: Long, val hoursUntil: Long) : NextClassState()
    object EndedToday : NextClassState()
    object NoUpcoming : NextClassState()
}

class NextClassUseCase @Inject constructor() {
    fun calculate(
        todayOccurrences: List<Occurrence>,
        now: LocalDateTime
    ): NextClassState {
        val currentTime = now.toLocalTime()
        val validOccurrences = todayOccurrences.filter { it.status != ExceptionStatus.CANCELED }

        if (validOccurrences.isEmpty()) return NextClassState.NoUpcoming

        // 1. Check if currently in class
        val ongoing = validOccurrences.find {
            !currentTime.isBefore(it.startTime) && currentTime.isBefore(it.endTime)
        }
        if (ongoing != null) {
            val remainingMin = ChronoUnit.MINUTES.between(currentTime, ongoing.endTime)
            return NextClassState.Ongoing(ongoing, remainingMin)
        }

        // 2. Check for next class today
        val nextClass = validOccurrences.filter { it.startTime.isAfter(currentTime) }
            .minByOrNull { it.startTime }

        if (nextClass != null) {
            val minutesUntil = ChronoUnit.MINUTES.between(currentTime, nextClass.startTime)
            val hoursUntil = ChronoUnit.HOURS.between(currentTime, nextClass.startTime)
            return NextClassState.Next(nextClass, minutesUntil, hoursUntil)
        }

        return NextClassState.EndedToday
    }
}
