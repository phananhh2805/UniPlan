package com.uniplan.app.util

import java.text.Normalizer
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object VietnameseText {

    fun dayLabel(dayOfWeek: Int): String = when (dayOfWeek) {
        1 -> "Thứ Hai"
        2 -> "Thứ Ba"
        3 -> "Thứ Tư"
        4 -> "Thứ Năm"
        5 -> "Thứ Sáu"
        6 -> "Thứ Bảy"
        7 -> "Chủ Nhật"
        else -> ""
    }

    fun dayShort(dayOfWeek: Int): String = when (dayOfWeek) {
        1 -> "T2"
        2 -> "T3"
        3 -> "T4"
        4 -> "T5"
        5 -> "T6"
        6 -> "T7"
        7 -> "CN"
        else -> ""
    }

    fun fullDate(d: LocalDate): String {
        val label = dayLabel(d.dayOfWeek.value)
        val formattedDate = d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        return "$label, $formattedDate"
    }

    fun shortDate(d: LocalDate): String = d.format(DateTimeFormatter.ofPattern("dd/MM"))

    fun time(t: LocalTime): String = t.format(DateTimeFormatter.ofPattern("HH:mm"))

    fun relativeDue(dueAt: LocalDateTime, now: LocalDateTime): String {
        val dueLocalDate = dueAt.toLocalDate()
        val nowLocalDate = now.toLocalDate()
        val dueLocalTimeStr = time(dueAt.toLocalTime())

        if (dueAt.isBefore(now)) {
            val daysLate = ChronoUnitDaysBetween(nowLocalDate, dueLocalDate)
            return if (Math.abs(daysLate) >= 1) {
                "Trễ ${Math.abs(daysLate)} ngày"
            } else {
                val hoursLate = Duration.between(dueAt, now).toHours()
                "Trễ $hoursLate giờ"
            }
        }

        if (dueLocalDate == nowLocalDate) {
            return "Hôm nay $dueLocalTimeStr"
        }

        if (dueLocalDate == nowLocalDate.plusDays(1)) {
            return "Ngày mai $dueLocalTimeStr"
        }

        val daysBetween = ChronoUnitDaysBetween(nowLocalDate, dueLocalDate)
        if (daysBetween in 2..6) {
            return "${dayLabel(dueLocalDate.dayOfWeek.value)} $dueLocalTimeStr"
        }

        return "${shortDate(dueLocalDate)} $dueLocalTimeStr"
    }

    fun duration(minutes: Int): String {
        val hrs = minutes / 60
        val mins = minutes % 60
        return when {
            hrs > 0 && mins > 0 -> "$hrs giờ $mins phút"
            hrs > 0 -> "$hrs giờ"
            else -> "$mins phút"
        }
    }

    fun countdown(target: LocalDateTime, now: LocalDateTime): String {
        val targetDate = target.toLocalDate()
        val nowDate = now.toLocalDate()

        if (targetDate == nowDate) {
            return "Hôm nay ${time(target.toLocalTime())}"
        }

        val days = ChronoUnitDaysBetween(nowDate, targetDate)
        if (days > 0) {
            return "Còn $days ngày"
        }

        val hours = Duration.between(now, target).toHours()
        if (hours > 0) {
            return "Còn $hours giờ"
        }

        val mins = Duration.between(now, target).toMinutes()
        return "Còn ${Math.max(1L, mins)} phút"
    }

    fun removeAccents(str: String): String {
        val nfd = Normalizer.normalize(str, Normalizer.Form.NFD)
        val regex = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        return regex.replace(nfd, "")
            .replace('đ', 'd')
            .replace('Đ', 'D')
    }

    private fun ChronoUnitDaysBetween(start: LocalDate, end: LocalDate): Long {
        return ChronoUnit.DAYS.between(start, end)
    }
}
