package com.example.data.astronomy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * Reference values: September 2026 new moon 11 Sep 03:27 UTC and full moon 26 Sep 16:49 UTC
 * (timeanddate.com); Dhaka moonrise on 23 Sep 2026 at 15:52 local (timehubzone.com).
 */
class MoonCalculatorTest {

  private val dhakaLat = 23.8103
  private val dhakaLon = 90.4125
  private val dhaka = ZoneOffset.ofHours(6)

  private fun utc(month: Int, day: Int, hour: Int, minute: Int): Instant =
    ZonedDateTime.of(2026, month, day, hour, minute, 0, 0, ZoneOffset.UTC).toInstant()

  @Test
  fun `new moon is dark`() {
    val (fraction, _) = MoonCalculator.illumination(utc(9, 11, 3, 27))
    assertTrue("was ${fraction * 100}%", fraction < 0.01)
  }

  @Test
  fun `full moon is fully lit`() {
    val (fraction, _) = MoonCalculator.illumination(utc(9, 26, 16, 49))
    assertTrue("was ${fraction * 100}%", fraction > 0.99)
    assertEquals("FULL_MOON", MoonCalculator.phaseKeyFor(MoonCalculator.illumination(utc(9, 26, 16, 49)).second))
  }

  @Test
  fun `the screenshot moment - waxing gibbous, about 88 percent, below the horizon`() {
    // 23 Sep 2026, 12:41 in Dhaka. The old Hijri-day table said 80%.
    val state = MoonCalculator.stateAt(utc(9, 23, 6, 41), dhakaLat, dhakaLon)
    assertEquals("WAXING_GIBBOUS", state.phaseKey)
    assertTrue(state.isWaxing)
    assertTrue("was ${state.percentLit}%", state.percentLit in 86..90)
    assertFalse(state.isAboveHorizon)
  }

  @Test
  fun `moonrise in Dhaka matches a published table within a few minutes`() {
    val from = utc(9, 23, 6, 41)
    val rise = MoonCalculator.nextCrossing(from, dhakaLat, dhakaLon, rising = true)
    assertNotNull(rise)
    val published = ZonedDateTime.of(2026, 9, 23, 15, 52, 0, 0, dhaka).toInstant()
    val error = Duration.between(published, rise).abs()
    assertTrue("off by ${error.toMinutes()} min", error.toMinutes() <= 10)
  }

  @Test
  fun `phase names follow the elongation`() {
    assertEquals("NEW_MOON", MoonCalculator.phaseKeyFor(3.0))
    assertEquals("NEW_MOON", MoonCalculator.phaseKeyFor(355.0))
    assertEquals("WAXING_CRESCENT", MoonCalculator.phaseKeyFor(45.0))
    assertEquals("FIRST_QUARTER", MoonCalculator.phaseKeyFor(90.0))
    assertEquals("WANING_GIBBOUS", MoonCalculator.phaseKeyFor(220.0))
    assertEquals("LAST_QUARTER", MoonCalculator.phaseKeyFor(270.0))
    assertEquals("WANING_CRESCENT", MoonCalculator.phaseKeyFor(300.0))
  }
}
