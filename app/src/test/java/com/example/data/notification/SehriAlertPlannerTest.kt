package com.example.data.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class SehriAlertPlannerTest {

  private val zone = ZoneId.of("Asia/Dhaka")
  private val date = LocalDate.of(2027, 2, 10)
  private val sehriEnd = LocalTime.of(5, 12)
  private val evening = ZonedDateTime.of(date.minusDays(1), LocalTime.of(21, 0), zone).toInstant().toEpochMilli()

  private fun plan(
    enabled: Boolean = true,
    lead: Int = 60,
    fasting: Boolean = true,
    forbidden: Boolean = false,
    now: Long = evening,
    end: LocalTime = sehriEnd
  ) = PrayerAlertPlanner.planSehri(date, end, zone, now, enabled, lead, fasting, forbidden)

  @Test
  fun `rings the lead time before Sehri ends`() {
    PrayerNotificationManager.SEHRI_LEAD_CHOICES.forEach { lead ->
      val alert = plan(lead = lead)
      assertNotNull(alert)
      val expected = ZonedDateTime.of(date, sehriEnd, zone).minusMinutes(lead.toLong()).toInstant().toEpochMilli()
      assertEquals("lead $lead", expected, alert!!.triggerAtMillis)
      assertEquals(AlertIds.SEHRI, alert.requestCode)
      assertEquals(date, alert.fastDate)
    }
  }

  @Test
  fun `off, not fasting, or forbidden - no alarm`() {
    assertNull(plan(enabled = false))
    assertNull(plan(fasting = false))
    assertNull("never on Eid or Tashreeq, even if marked", plan(forbidden = true))
  }

  @Test
  fun `a time already past is not armed`() {
    val past = ZonedDateTime.of(date, LocalTime.of(4, 30), zone).toInstant().toEpochMilli()
    assertNull(plan(now = past)) // 04:12 has gone
  }

  @Test
  fun `an early dawn rings the evening before`() {
    val alert = plan(lead = 90, end = LocalTime.of(1, 0))!!
    val trigger = java.time.Instant.ofEpochMilli(alert.triggerAtMillis).atZone(zone)
    assertEquals(date.minusDays(1), trigger.toLocalDate())
    assertEquals(LocalTime.of(23, 30), trigger.toLocalTime())
  }

  @Test
  fun `the Sehri slot is its own, and cancelled with the rest`() {
    assertEquals(35_000, AlertIds.SEHRI)
    assert(AlertIds.SEHRI in AlertIds.all())
    assertEquals(AlertIds.all().size, AlertIds.all().toSet().size)
  }
}
