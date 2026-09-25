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

  @Test
  fun beforeFajrItIsStillIsha() {
    val night = scheduleAt(LocalTime.of(0, 30))
    assertEquals(PrayerName.ISHA, night.currentPrayer?.prayer)
    assertEquals(at(PrayerName.FAJR), night.currentPrayerEndsAt)
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
