package com.example.data.prayer

import com.example.data.model.CityLocation
import com.example.data.model.PrayerName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Dhuhr waits three minutes after the sun's highest point, and the noon forbidden window ends
 * exactly then. Dhaka, 22 Oct 2026: solar noon is about 11:42 (timeanddate.com: 11:42), so Dhuhr
 * is 11:45 and the window 11:35-11:45.
 */
class DhuhrMarginTest {

  private val dhaka = CityLocation("Dhaka", "Bangladesh", 23.8103, 90.4125, "Asia/Dhaka")
  private val date = LocalDate.of(2026, 10, 22)
  private val schedule = PrayerCalculator.calculateSchedule(
    date = date,
    location = dhaka,
    now = LocalDateTime.of(date, LocalTime.NOON),
    method = CalculationMethod.KARACHI,
    madhab = IslamicMadhab.DEFAULT
  )
  private fun at(p: PrayerName) = schedule.items.first { it.prayer == p }.time

  @Test
  fun `Dhuhr is three minutes after the sun's highest point`() {
    // The highest point, found independently: halfway between sunrise and the true sunset.
    val sunrise = at(PrayerName.SUNRISE).toSecondOfDay()
    val sunset = at(PrayerName.MAGHRIB).minusMinutes(PrayerCalculator.MAGHRIB_MARGIN_MINUTES).toSecondOfDay()
    val noon = LocalTime.ofSecondOfDay(((sunrise + sunset) / 2).toLong())
    val gap = Duration.between(noon, at(PrayerName.DHUHR)).toMinutes()
    assertTrue("noon $noon, Dhuhr ${at(PrayerName.DHUHR)}", gap in 2..4)
    // 11:45 and some seconds: shown as 11:45.
    assertEquals(LocalTime.of(11, 45), at(PrayerName.DHUHR).withSecond(0).withNano(0))
  }

  @Test
  fun `the noon window is the ten minutes before Dhuhr`() {
    val zenith = schedule.forbiddenTimes.single { it.type == ForbiddenWindowType.ZENITH }
    assertEquals(at(PrayerName.DHUHR), zenith.endTime)
    assertEquals(at(PrayerName.DHUHR).minusMinutes(10), zenith.startTime)
  }

  @Test
  fun `Asr and Maghrib are unchanged by it`() {
    // Asr is measured from the true noon; Maghrib from sunset.
    assertEquals(LocalTime.of(15, 0), at(PrayerName.ASR).withSecond(0).withNano(0))
    assertEquals(LocalTime.of(17, 30), at(PrayerName.MAGHRIB).withSecond(0).withNano(0))
  }
}
