package com.uniplan.app.util

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
        val formatted = d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        return "$label, $formatted"
    }

    fun shortDate(d: LocalDate): String = d.format(DateTimeFormatter.ofPattern("dd/MM"))

    fun time(t: LocalTime): String = t.format(DateTimeFormatter.ofPattern("HH:mm"))

    fun relativeDue(due: LocalDateTime, now: LocalDateTime): String {
        val nowDate = now.toLocalDate()
        val dueDate = due.toLocalDate()
        val daysDiff = ChronoUnit.DAYS.between(nowDate, dueDate)

        if (due.isBefore(now)) {
            val hours = ChronoUnit.HOURS.between(due, now)
            return if (hours < 24) "Trễ $hours giờ" else "Trễ ${Math.abs(daysDiff)} ngày"
        }

        val timeStr = time(due.toLocalTime())
        return when (daysDiff) {
            0L -> "Hôm nay $timeStr"
            1L -> "Ngày mai $timeStr"
            in 2L..6L -> "${dayLabel(dueDate.dayOfWeek.value)} $timeStr"
            else -> "${shortDate(dueDate)} $timeStr"
        }
    }

    fun duration(min: Int): String {
        val h = min / 60
        val m = min % 60
        return when {
            h > 0 && m > 0 -> "$h giờ $m phút"
            h > 0 -> "$h giờ"
            else -> "$m phút"
        }
    }

    fun countdown(days: Int): String = when {
        days > 0 -> "Còn $days ngày"
        days == 0 -> "Hôm nay"
        else -> "Đã qua"
    }
}
