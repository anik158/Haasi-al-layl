package com.example.data.astronomy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * New moons are the published times of four solar eclipses and one ordinary new moon (NASA /
 * timeanddate.com). Dhaka's sunset on 18 Feb 2026 is 17:55 local (timeanddate.com).
 */
class CrescentCalculatorTest {

  private val dhakaLat = 23.8103
  private val dhakaLon = 90.4125
  private val dhaka = ZoneId.of("Asia/Dhaka")
  private val makkahLat = 21.4225
  private val makkahLon = 39.8262
  private val makkah = ZoneId.of("Asia/Riyadh")

  private fun utc(y: Int, m: Int, d: Int, h: Int, min: Int): Instant =
    ZonedDateTime.of(y, m, d, h, min, 0, 0, ZoneOffset.UTC).toInstant()

  private fun assertNewMoon(expected: Instant) {
    // Start the search a few days away, so it has to find the moment, not stumble on it.
    listOf(-3L, 4L).forEach { days ->
      val found = CrescentCalculator.nearestNewMoon(expected.plus(Duration.ofDays(days)))
      val error = Duration.between(expected, found).abs().toMinutes()
      assertTrue("new moon $expected found $found, off by $error min", error <= 5)
    }
  }

  @Test
  fun `new moons match published times within five minutes`() {
    assertNewMoon(utc(2024, 4, 8, 18, 21))   // total solar eclipse
    assertNewMoon(utc(2025, 3, 29, 10, 58))  // partial solar eclipse
    assertNewMoon(utc(2026, 2, 17, 12, 1))   // annular solar eclipse
    assertNewMoon(utc(2026, 8, 12, 17, 37))  // total solar eclipse
    assertNewMoon(utc(2026, 9, 11, 3, 27))
  }

  @Test
  fun `Dhaka sunset matches a published table`() {
    val sunset = CrescentCalculator.sunset(LocalDate.of(2026, 2, 18), dhakaLat, dhakaLon, dhaka)
    assertNotNull(sunset)
    val published = ZonedDateTime.of(2026, 2, 18, 17, 55, 0, 0, dhaka).toInstant()
    val error = Duration.between(published, sunset).abs().toMinutes()
    assertTrue("off by $error min", error <= 3)
  }

  @Test
  fun `the evening before the new moon is impossible`() {
    // 16 Feb 2026: the new moon is still a day away (17 Feb, 12:01 UTC).
    val evening = CrescentCalculator.evaluate(LocalDate.of(2026, 2, 16), dhakaLat, dhakaLon, dhaka)
    assertEquals(CrescentVisibility.IMPOSSIBLE, evening.visibility)
    assertTrue(evening.ageHours < 0)
  }

  @Test
  fun `a day-old moon is easy to see the next evening`() {
    // 18 Feb 2026 in Dhaka: about 24 h old at sunset. Bangladesh sighted it that evening and began
    // Ramadan on the 19th.
    val evening = CrescentCalculator.evaluate(LocalDate.of(2026, 2, 18), dhakaLat, dhakaLon, dhaka)
    assertTrue("age ${evening.ageHours}", evening.ageHours in 23.0..25.0)
    assertTrue("lag ${evening.lagMinutes}", (evening.lagMinutes ?: 0) > 40)
    assertEquals(CrescentVisibility.NAKED_EYE, evening.visibility)
  }

  @Test
  fun `a few hours after the new moon is not visible`() {
    // 17 Feb 2026 in Makkah: sunset ~15:25 UTC, about 3 h after the new moon.
    val evening = CrescentCalculator.evaluate(LocalDate.of(2026, 2, 17), makkahLat, makkahLon, makkah)
    assertTrue("age ${evening.ageHours}", evening.ageHours in 2.0..5.0)
    assertTrue(
      "was ${evening.visibility}, V=${evening.odehV}",
      evening.visibility <= CrescentVisibility.NOT_VISIBLE
    )
  }

  @Test
  fun `visibility rises evening by evening after the new moon`() {
    val days = (17..20).map {
      CrescentCalculator.evaluate(LocalDate.of(2026, 2, it), dhakaLat, dhakaLon, dhaka)
    }
    days.zipWithNext().forEach { (a, b) ->
      assertTrue("${a.date} V=${a.odehV} then ${b.date} V=${b.odehV}", b.odehV > a.odehV)
      println("${a.date} age=${a.ageHours} lag=${a.lagMinutes} V=${a.odehV} ${a.visibility}")
    }
  }
}
