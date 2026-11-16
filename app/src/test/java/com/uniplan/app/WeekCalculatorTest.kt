package com.uniplan.app

import com.uniplan.app.util.WeekCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class WeekCalculatorTest {

    @Test
    fun testWeekIndex_TC08() {
        val semStart = LocalDate.of(2026, 9, 8)
        val semEnd = LocalDate.of(2027, 1, 3)

        assertEquals(1, WeekCalculator.weekIndex(semStart, semEnd, LocalDate.of(2026, 9, 8)))
        assertEquals(1, WeekCalculator.weekIndex(semStart, semEnd, LocalDate.of(2026, 9, 13)))
        assertEquals(2, WeekCalculator.weekIndex(semStart, semEnd, LocalDate.of(2026, 9, 14)))
        assertEquals(17, WeekCalculator.weekIndex(semStart, semEnd, LocalDate.of(2027, 1, 3)))

        assertNull(WeekCalculator.weekIndex(semStart, semEnd, LocalDate.of(2027, 1, 4)))
        assertNull(WeekCalculator.weekIndex(semStart, semEnd, LocalDate.of(2026, 9, 6)))
    }
}
