package com.uniplan.app.util

import com.uniplan.app.domain.model.WeekPattern
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

object WeekCalculator {
    fun weekStart(d: LocalDate): LocalDate =
        d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    fun weekIndex(startDate: LocalDate, endDate: LocalDate, d: LocalDate): Int? {
        if (d.isBefore(startDate) || d.isAfter(endDate)) return null
        val days = ChronoUnit.DAYS.between(weekStart(startDate), d)
        return Math.floorDiv(days, 7L).toInt() + 1
    }

    fun matchesPattern(p: WeekPattern, w: Int): Boolean = when (p) {
        WeekPattern.ALL -> true
        WeekPattern.ODD -> w % 2 == 1
        WeekPattern.EVEN -> w % 2 == 0
    }

    fun dateOf(startDate: LocalDate, week: Int, dayOfWeek: Int): LocalDate =
        weekStart(startDate).plusDays(((week - 1) * 7 + (dayOfWeek - 1)).toLong())
}
