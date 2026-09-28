package com.example.data.prayer

import com.example.data.model.PrayerName
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FastingTimesTest {

  private val city = PrayerCalculator.PRESET_CITIES[0]
  private val method = CalculationMethod.UMM_AL_QURA
  private val madhab = IslamicMadhab.DEFAULT

  @After
  fun resetAdjustment() {
    HijriDateHelper.adjustmentDays = 0
  }

  /** The first day of Ramadan in 2026 as the offline calendar has it, found rather than assumed. */
  private fun firstOfRamadan(): LocalDate =
    generateSequence(LocalDate.of(2026, 1, 1)) { it.plusDays(1) }
      .first { HijriDateHelper.isRamadan(it) }

  private fun times(date: LocalDate) =
    PrayerCalculator.calculateSchedule(date, city, LocalDateTime.of(date, LocalTime.NOON), method = method, madhab = madhab)

  private fun at(date: LocalDate, prayer: PrayerName) = times(date).items.first { it.prayer == prayer }.time

  private fun status(at: LocalDateTime) = FastingTimes.statusAt(at, city, method, madhab)

  @Test
  fun sehriEndsAtFajrAndIftarIsMaghrib() {
    val date = LocalDate.of(2026, 9, 25)
    val day = FastingTimes.dayFor(date, city, method, madhab)
    assertEquals(at(date, PrayerName.FAJR), day.sehriEnd)
    assertEquals(at(date, PrayerName.MAGHRIB), day.iftar)
  }

  @Test
  fun beforeIftarItIsTodaysFastAfterItTomorrows() {
    val date = LocalDate.of(2026, 9, 25)
    val iftar = at(date, PrayerName.MAGHRIB)

    val during = status(LocalDateTime.of(date, iftar.minusMinutes(30)))
    assertEquals(date, during.day.date)
    assertTrue(during.isFasting)
    assertFalse(during.isTomorrow)

    val evening = status(LocalDateTime.of(date, iftar.plusMinutes(1)))
    assertEquals(date.plusDays(1), evening.day.date)
    assertFalse(evening.isFasting)
    assertTrue(evening.isTomorrow)
  }

  @Test
  fun beforeDawnTheCountdownIsToSehriEnd() {
    val date = LocalDate.of(2026, 9, 25)
    val fajr = at(date, PrayerName.FAJR)
    val s = status(LocalDateTime.of(date, fajr.minusMinutes(90)))
    assertFalse(s.isFasting)
    assertEquals("01:30:00", s.countdownFormatted)
  }

  /** The first Sehri has to be on screen the evening before 1 Ramadan. */
  @Test
  fun eveningBeforeRamadanShowsTheFirstSehri() {
    val first = firstOfRamadan()
    val eve = first.minusDays(1)
    val s = status(LocalDateTime.of(eve, at(eve, PrayerName.MAGHRIB).plusMinutes(5)))
    assertEquals(first, s.day.date)
    assertTrue(s.day.isRamadan)
    assertEquals(1, s.day.ramadanDay)
  }

  @Test
  fun automaticAdjustmentFollowsTheCountry() {
    assertEquals(-1, HijriDateHelper.automaticAdjustmentFor("Bangladesh"))
    assertEquals(-1, HijriDateHelper.automaticAdjustmentFor("BD")) // IP lookup gives codes
    assertEquals(-1, HijriDateHelper.automaticAdjustmentFor(" pakistan "))
    assertEquals(0, HijriDateHelper.automaticAdjustmentFor("Saudi Arabia"))
    assertEquals(0, HijriDateHelper.automaticAdjustmentFor(null))
  }

  @Test
  fun adjustmentMovesTheStartOfRamadan() {
    val first = firstOfRamadan()
    HijriDateHelper.adjustmentDays = -1
    assertFalse(HijriDateHelper.isRamadan(first))
    assertTrue(HijriDateHelper.isRamadan(first.plusDays(1)))
  }
}
