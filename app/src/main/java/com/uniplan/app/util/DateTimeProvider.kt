package com.uniplan.app.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

interface DateTimeProvider {
    fun today(): LocalDate
    fun nowTime(): LocalTime
    fun now(): LocalDateTime
    fun nowMillis(): Long
    fun zone(): ZoneId
}

class DefaultDateTimeProvider : DateTimeProvider {
    override fun today(): LocalDate = LocalDate.now()
    override fun nowTime(): LocalTime = LocalTime.now()
    override fun now(): LocalDateTime = LocalDateTime.now()
    override fun nowMillis(): Long = System.currentTimeMillis()
    override fun zone(): ZoneId = ZoneId.systemDefault()
}
