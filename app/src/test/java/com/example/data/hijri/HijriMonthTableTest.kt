package com.example.data.hijri

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class HijriMonthTableTest {

  private val ramadan1447 = HijriMonthId(1447, 9)

  private fun resolver(
    policy: SightingPolicy = SightingPolicy.FOLLOW_SAUDI,
    announced: Map<HijriMonthId, LocalDate> = emptyMap(),
    user: Map<HijriMonthId, LocalDate> = emptyMap(),
    v: (LocalDate) -> Double = { 20.0 }
  ) = MonthStartResolver(policy, announced, user, v)

  @Test
  fun `month ids wrap across years`() {
    assertEquals(HijriMonthId(1448, 1), HijriMonthId(1447, 12).next())
    assertEquals(HijriMonthId(1446, 12), HijriMonthId(1447, 1).previous())
    assertEquals(HijriMonthId(1449, 3), HijriMonthId(1447, 9) + 18)
    assertEquals(HijriMonthId(1447, 9), HijriMonthId.parse("1447-09"))
    assertNull(HijriMonthId.parse("1447-13"))
    assertNull(HijriMonthId.parse("junk"))
  }

  @Test
  fun `with nothing announced a Saudi follower gets Umm al-Qura`() {
    val table = resolver().resolve(ramadan1447, 1)
    val start = table.start(ramadan1447)!!
    assertEquals(LocalDate.of(2026, 2, 18), start.startsOn)
    assertEquals(MonthStartSource.CALCULATED, start.source)
  }

  @Test
  fun `an announcement wins over the calculation`() {
    val table = resolver(announced = mapOf(ramadan1447 to LocalDate.of(2026, 2, 19))).resolve(ramadan1447, 1)
    assertEquals(LocalDate.of(2026, 2, 19), table.start(ramadan1447)!!.startsOn)
    assertEquals(MonthStartSource.ANNOUNCED, table.start(ramadan1447)!!.source)
  }

  @Test
  fun `the user wins over everything`() {
    val table = resolver(
      policy = SightingPolicy.LOCAL_SIGHTING,
      announced = mapOf(ramadan1447 to LocalDate.of(2026, 2, 18)),
      user = mapOf(ramadan1447 to LocalDate.of(2026, 2, 18))
    ).resolve(ramadan1447, 1)
    assertEquals(LocalDate.of(2026, 2, 18), table.start(ramadan1447)!!.startsOn)
    assertEquals(MonthStartSource.USER, table.start(ramadan1447)!!.source)
  }

  @Test
  fun `a sighting country moves by the moon`() {
    val saudi = LocalDate.of(2026, 2, 18)
    fun estimate(eve: Double, first: Double) =
      resolver(SightingPolicy.LOCAL_SIGHTING, v = { if (it == saudi.minusDays(1)) eve else first })
        .localEstimate(saudi)
    assertEquals(saudi, estimate(eve = 9.0, first = 30.0))
    assertEquals(saudi.plusDays(1), estimate(eve = 6.0, first = 30.0))
    assertEquals(saudi.plusDays(2), estimate(eve = -3.0, first = 5.0))
  }

  @Test
  fun `a month is never held to 28 or 31 days`() {
    val shaban = HijriMonthId(1447, 8)
    // Announcements 28 days apart, then 32: both must be pulled back to 29..30.
    val table = resolver(
      announced = mapOf(
        shaban to LocalDate.of(2026, 1, 20),
        shaban.next() to LocalDate.of(2026, 2, 17),
        shaban + 2 to LocalDate.of(2026, 3, 21)
      )
    ).resolve(shaban, 3)
    assertEquals(LocalDate.of(2026, 2, 18), table.start(shaban.next())!!.startsOn)
    assertEquals(LocalDate.of(2026, 3, 20), table.start(shaban + 2)!!.startsOn)
  }

  @Test
  fun `a user date that breaks the length rule stops being the user's`() {
    val shaban = HijriMonthId(1447, 8)
    val table = resolver(
      announced = mapOf(shaban to LocalDate.of(2026, 1, 20)),
      user = mapOf(shaban.next() to LocalDate.of(2026, 2, 25))
    ).resolve(shaban, 2)
    assertEquals(LocalDate.of(2026, 2, 19), table.start(shaban.next())!!.startsOn)
    assertEquals(MonthStartSource.CALCULATED, table.start(shaban.next())!!.source)
  }

  @Test
  fun `dates resolve to day and month, and outside the table to null`() {
    val table = resolver(announced = mapOf(
      ramadan1447 to LocalDate.of(2026, 2, 18),
      ramadan1447.next() to LocalDate.of(2026, 3, 20)
    )).resolve(ramadan1447, 2)
    assertNull(table.resolve(LocalDate.of(2026, 2, 17)))
    assertEquals(1, table.resolve(LocalDate.of(2026, 2, 18))!!.day)
    val last = table.resolve(LocalDate.of(2026, 3, 19))!!
    assertEquals(30, last.day)
    assertEquals(9, last.month)
    assertEquals(30, last.monthLength)
    assertEquals(10, table.resolve(LocalDate.of(2026, 3, 20))!!.month)
    assertEquals(30, table.resolve(LocalDate.of(2026, 4, 18))!!.day)
    assertNull(table.resolve(LocalDate.of(2026, 4, 19)))
  }

  /**
   * Thousands of random calendars - policies, announcements a few days off, user dates, moon
   * values - and the invariants that make it a calendar: every month 29 or 30 days, consecutive,
   * and every day of the span in exactly one month.
   */
  @Test
  fun `random calendars always have 29 or 30 day months`() {
    val random = Random(1447)
    repeat(3000) { run ->
      val first = HijriMonthId(1440 + random.nextInt(10), 1 + random.nextInt(12))
      val count = 1 + random.nextInt(16)
      val announced = mutableMapOf<HijriMonthId, LocalDate>()
      val user = mutableMapOf<HijriMonthId, LocalDate>()
      (0 until count).forEach { i ->
        val id = first + i
        val uaq = MonthStartResolver.ummAlQuraStart(id)
        if (random.nextInt(3) > 0) announced[id] = uaq.plusDays(random.nextLong(-2, 3))
        if (random.nextInt(6) == 0) user[id] = uaq.plusDays(random.nextLong(-2, 3))
      }
      val policy = SightingPolicy.entries[random.nextInt(SightingPolicy.entries.size)]
      val table = MonthStartResolver(policy, announced, user, { random.nextDouble(-10.0, 30.0) })
        .resolve(first, count)

      assertEquals(count, table.starts.size)
      table.starts.zipWithNext().forEach { (a, b) ->
        val length = ChronoUnit.DAYS.between(a.startsOn, b.startsOn)
        assertTrue("run $run: ${a.id.key} has $length days", length in 29..30)
      }
      var date = table.starts.first().startsOn
      var expectedMonth = table.starts.first().id
      var expectedDay = 1
      val end = table.starts.last().startsOn.plusDays(29)
      while (!date.isAfter(end)) {
        val day = table.resolve(date)!!
        if (day.day == 1 && date != table.starts.first().startsOn) {
          expectedMonth = expectedMonth.next(); expectedDay = 1
        }
        assertEquals("run $run $date", expectedMonth, day.id)
        assertEquals("run $run $date", expectedDay, day.day)
        expectedDay++
        date = date.plusDays(1)
      }
    }
  }

  @Test
  fun `countries map to their policy`() {
    assertEquals(SightingPolicy.LOCAL_SIGHTING, HijriCountryRules.policyFor("Bangladesh"))
    assertEquals(SightingPolicy.LOCAL_SIGHTING, HijriCountryRules.policyFor("bd"))
    assertEquals(SightingPolicy.FOLLOW_TURKEY, HijriCountryRules.policyFor("Türkiye"))
    assertEquals(SightingPolicy.FOLLOW_SAUDI, HijriCountryRules.policyFor("Saudi Arabia"))
    assertEquals(SightingPolicy.FOLLOW_SAUDI, HijriCountryRules.policyFor(null))
  }
}
