package com.example.data.prayer

import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FastingRulesTest {

  private fun kinds(month: Int, day: Int, weekday: DayOfWeek = DayOfWeek.WEDNESDAY) =
    FastingRules.rulesFor(month, day, weekday).map { it.kind to it.key }

  @Test
  fun eidsAndTashreeqAreForbiddenAndNothingElseApplies() {
    assertEquals(listOf(FastDayKind.PROHIBITED to "eid_fitr"), kinds(10, 1, DayOfWeek.MONDAY))
    assertEquals(listOf(FastDayKind.PROHIBITED to "eid_adha"), kinds(12, 10))
    // 13 Dhul-Hijjah would also be a white day - Tashreeq overrides it.
    assertEquals(listOf(FastDayKind.PROHIBITED to "tashreeq"), kinds(12, 13, DayOfWeek.THURSDAY))
  }

  @Test
  fun ramadanIsObligatory() {
    assertEquals(listOf(FastDayKind.OBLIGATORY to "ramadan"), kinds(9, 17, DayOfWeek.FRIDAY))
  }

  @Test
  fun recommendedDays() {
    assertTrue(FastDayKind.RECOMMENDED to "arafah" in kinds(12, 9))
    assertTrue(FastDayKind.RECOMMENDED to "ashura" in kinds(1, 10))
    assertTrue(FastDayKind.RECOMMENDED to "tasua" in kinds(1, 9))
    assertTrue(FastDayKind.RECOMMENDED to "six_shawwal" in kinds(10, 2))
    assertTrue(FastDayKind.RECOMMENDED to "white_days" in kinds(4, 14))
    assertTrue(FastDayKind.RECOMMENDED to "monday" in kinds(4, 3, DayOfWeek.MONDAY))
    assertTrue(FastDayKind.RECOMMENDED to "thursday" in kinds(4, 3, DayOfWeek.THURSDAY))
  }

  @Test
  fun discouragedDays() {
    assertTrue(FastDayKind.DISCOURAGED to "before_ramadan" in kinds(8, 29))
    assertTrue(FastDayKind.DISCOURAGED to "friday_alone" in kinds(4, 3, DayOfWeek.FRIDAY))
    // Most binding first.
    assertEquals(FastDayKind.DISCOURAGED, FastingRules.rulesFor(10, 14, DayOfWeek.FRIDAY).first().kind)
  }

  @Test
  fun anOrdinaryDayHasNoRules() {
    assertTrue(kinds(4, 3, DayOfWeek.WEDNESDAY).isEmpty())
  }

  @Test
  fun onlyHijriRulesCarryTheDateWarning() {
    val monday = FastingRules.rulesFor(4, 3, DayOfWeek.MONDAY).single()
    assertFalse(monday.dependsOnHijriDate)
    assertTrue(FastingRules.rulesFor(12, 9, DayOfWeek.WEDNESDAY).single().dependsOnHijriDate)
  }
}
