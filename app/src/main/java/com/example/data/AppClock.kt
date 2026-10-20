package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * The time Today and the Hijri calendar run on: the real clock, or - in a debug build, from
 * Settings → "Test a date" - a moment chosen to try Ramadan, Eid or a sighting evening out of
 * season.
 *
 * Kept in memory only, so a restart always comes back to the real time. Alarms never read it:
 * they stay on the real clock, so a test date cannot make the phone ring at the wrong hour.
 */
object AppClock {

  private val _offsetMillis = MutableStateFlow(0L)

  /** How far the test time is from the real time; 0 when not testing. */
  val offsetMillis: StateFlow<Long> = _offsetMillis

  val isTesting: Boolean get() = _offsetMillis.value != 0L

  fun nowMillis(): Long = System.currentTimeMillis() + _offsetMillis.value

  fun now(zone: ZoneId): ZonedDateTime = Instant.ofEpochMilli(nowMillis()).atZone(zone)

  fun today(zone: ZoneId): LocalDate = now(zone).toLocalDate()

  /** Runs the app's clock from [moment] on. */
  fun testAt(moment: ZonedDateTime) {
    _offsetMillis.value = moment.toInstant().toEpochMilli() - System.currentTimeMillis()
  }

  fun backToRealTime() {
    _offsetMillis.value = 0L
  }
}
