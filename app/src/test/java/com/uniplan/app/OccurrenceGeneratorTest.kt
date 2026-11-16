package com.uniplan.app

import com.uniplan.app.data.local.db.ClassExceptionEntity
import com.uniplan.app.data.local.db.ClassSessionEntity
import com.uniplan.app.data.local.db.SemesterEntity
import com.uniplan.app.data.local.db.SubjectEntity
import com.uniplan.app.domain.model.ClassType
import com.uniplan.app.domain.model.ExceptionStatus
import com.uniplan.app.domain.model.WeekPattern
import com.uniplan.app.domain.usecase.OccurrenceGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class OccurrenceGeneratorTest {

    @Test
    fun testOccurrenceGeneration() {
        val sem = SemesterEntity(1, "HK1", "2026-09-08", "2027-01-03", true)
        val subject = SubjectEntity(1, 1, "CS101", "Lập trình Kotlin", 3, "Thầy Nam", 0xFF1E88E5.toInt(), "code", null)
        val session = ClassSessionEntity(1, 1, 1, 1, 3, "07:00", "09:35", "A101", "CS1", ClassType.LECTURE, 1, 15, WeekPattern.ALL, true)

        val generator = OccurrenceGenerator()
        val targetDate = LocalDate.of(2026, 9, 14) // Mon week 2

        val occurrences = generator.forDate(sem, mapOf(1L to subject), listOf(session), emptyList(), targetDate)

        assertEquals(1, occurrences.size)
        val occ = occurrences.first()
        assertEquals("Lập trình Kotlin", occ.subjectName)
        assertEquals("A101", occ.room)
        assertEquals(2, occ.weekIndex)
    }

    @Test
    fun testCanceledException() {
        val sem = SemesterEntity(1, "HK1", "2026-09-08", "2027-01-03", true)
        val subject = SubjectEntity(1, 1, "CS101", "Lập trình Kotlin", 3, "Thầy Nam", 0xFF1E88E5.toInt(), "code", null)
        val session = ClassSessionEntity(1, 1, 1, 1, 3, "07:00", "09:35", "A101", "CS1", ClassType.LECTURE, 1, 15, WeekPattern.ALL, true)
        val exception = ClassExceptionEntity(1, 1, "2026-09-14", ExceptionStatus.CANCELED, null, null, null, "Nghỉ lễ")

        val generator = OccurrenceGenerator()
        val targetDate = LocalDate.of(2026, 9, 14)

        val occurrences = generator.forDate(sem, mapOf(1L to subject), listOf(session), listOf(exception), targetDate)

        assertEquals(1, occurrences.size)
        assertEquals(ExceptionStatus.CANCELED, occurrences.first().status)
    }
}
