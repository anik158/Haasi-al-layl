package com.example.data.hijri

import com.example.data.prayer.FastingRules
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/** Every day of the year: the note is on exactly where a day's error changes whether to fast. */
class MonthStartWarningTest {

  private val start = MonthStart(HijriMonthId(1448, 1), LocalDate.of(2026, 6, 16), MonthStartSource.ANNOUNCED, Authority.SAUDI, null)

  private fun day(month: Int, d: Int) = HijriDay(d, month, 1448, start.copy(id = HijriMonthId(1448, month)), 30)

  @Test
  fun `the note is on the days that matter and off on the others`() {
    (1..12).forEach { month ->
      (1..30).forEach { d ->
        // Wednesday: no weekday rule muddies it.
        val rules = FastingRules.rulesFor(month, d, DayOfWeek.WEDNESDAY)
        val expected = d >= 29 ||
          (month == 9 && (d <= 2 || d >= 28)) ||
          (month == 10 && d == 1) ||
          (month == 12 && d in 9..13)
        assertEquals("$month/$d", expected, MonthStartWarning.applies(day(month, d), rules))
      }
    }
  }

  @Test
  fun `every forbidden day carries it`() {
    listOf(10 to 1, 12 to 10, 12 to 11, 12 to 12, 12 to 13).forEach { (m, d) ->
      assertEquals(true, MonthStartWarning.applies(day(m, d), FastingRules.rulesFor(m, d, DayOfWeek.MONDAY)))
    }
  }

  @Test
  fun `outside the table only Hijri-dated rules carry it`() {
    assertEquals(false, MonthStartWarning.applies(null, FastingRules.rulesFor(5, 10, DayOfWeek.MONDAY)))
    assertEquals(true, MonthStartWarning.applies(null, FastingRules.rulesFor(5, 14, DayOfWeek.WEDNESDAY)))
  }
}
