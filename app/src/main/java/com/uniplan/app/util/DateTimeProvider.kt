package com.uniplan.app.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

interface DateTimeProvider {
    fun today(): LocalDate
    fun now(): LocalDateTime
    fun nowMillis(): Long
    fun zone(): ZoneId
}

@Singleton
class DefaultDateTimeProvider @Inject constructor() : DateTimeProvider {
    override fun today(): LocalDate = LocalDate.now()
    override fun now(): LocalDateTime = LocalDateTime.now()
    override fun nowMillis(): Long = System.currentTimeMillis()
    override fun zone(): ZoneId = ZoneId.systemDefault()
}
