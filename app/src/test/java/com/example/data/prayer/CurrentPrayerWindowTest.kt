package com.example.data.prayer

import com.example.data.model.PrayerName
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrentPrayerWindowTest {

  private val city = PrayerCalculator.PRESET_CITIES[0]
  private val date = LocalDate.of(2026, 9, 25)
  private val times = PrayerCalculator.calculateSchedule(date, city, LocalDateTime.of(date, LocalTime.NOON))

  private fun at(prayer: PrayerName) = times.items.first { it.prayer == prayer }.time
  private fun scheduleAt(time: LocalTime) =
    PrayerCalculator.calculateSchedule(date, city, LocalDateTime.of(date, time))

  @Test
  fun fajrEndsAtSunriseNotAtDhuhr() {
    val during = scheduleAt(at(PrayerName.FAJR).plusMinutes(5))
    assertEquals(PrayerName.FAJR, during.currentPrayer?.prayer)
    assertEquals(at(PrayerName.SUNRISE), during.currentPrayerEndsAt)

    val morning = scheduleAt(at(PrayerName.SUNRISE).plusMinutes(30))
    assertNull(morning.currentPrayer)
    assertTrue(morning.items.none { it.isCurrent })
  }

  /** Sahih Muslim 612: "the time of Isha is until half of the night has passed." */
  @Test
  fun ishaEndsAtMidnightNotFajr() {
    val evening = scheduleAt(at(PrayerName.ISHA).plusMinutes(10))
    assertEquals(PrayerName.ISHA, evening.currentPrayer?.prayer)
    val midnight = evening.islamicMidnight!!
    assertEquals(midnight, evening.currentPrayerEndsAt)
    // The midpoint of sunset and the next Fajr: Makkah's is shortly before or after 00:00.
    val sunset = at(PrayerName.MAGHRIB).minusMinutes(PrayerCalculator.MAGHRIB_MARGIN_MINUTES)
    val nightSeconds = java.time.Duration.between(
      date.atTime(sunset), date.plusDays(1).atTime(at(PrayerName.FAJR))
    ).seconds
    assertEquals(date.atTime(sunset).plusSeconds(nightSeconds / 2).toLocalTime(), midnight)
    // Ends at midnight, and Fajr is next, so the two countdowns differ.
    assertTrue(!evening.currentEndsAtNext)
  }

  @Test
  fun afterMidnightIshaHasEndedAndNothingIsCurrent() {
    val lateNight = scheduleAt(at(PrayerName.FAJR).minusMinutes(60))
    assertNull(lateNight.currentPrayer)
    assertTrue(lateNight.ishaPastMidnight)
    assertTrue(lateNight.items.none { it.isCurrent })
    assertEquals(PrayerName.FAJR, lateNight.nextPrayer?.prayer)
  }

  @Test
  fun asrRunsToMaghribAndSharesTheNextCountdown() {
    val s = scheduleAt(at(PrayerName.ASR).plusMinutes(10))
    assertEquals(PrayerName.ASR, s.currentPrayer?.prayer)
    assertEquals(at(PrayerName.MAGHRIB), s.currentPrayerEndsAt)
    assertTrue(s.currentEndsAtNext)
    assertEquals(s.timeUntilNextFormatted, s.timeLeftInCurrentFormatted)
  }
}
