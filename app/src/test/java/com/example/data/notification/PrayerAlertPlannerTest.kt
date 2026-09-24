package com.example.data.notification

import com.example.data.model.PrayerName
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class PrayerAlertPlannerTest {

  private val day = 24 * 60 * 60 * 1000L

  private fun fajr(dayOffset: Int, kind: PrayerAlertKind = PrayerAlertKind.AT_TIME) =
    ScheduledAlert.Prayer(
      prayer = PrayerName.FAJR,
      kind = kind,
      prayerTime = LocalTime.of(4, 30),
      triggerAtMillis = 1_000_000L + dayOffset * day,
      requestCode = AlertIds.prayer(PrayerName.FAJR, kind)
    )

  /** The regression: three days share one slot, and arming in day order let the last day win. */
  @Test
  fun keepsTheEarliestAlertPerSlot() {
    val armed = PrayerAlertPlanner.nextPerSlot(listOf(fajr(0), fajr(1), fajr(2)))

    assertEquals(listOf(fajr(0)), armed)
  }

  @Test
  fun distinctSlotsAreAllKept() {
    val armed = PrayerAlertPlanner.nextPerSlot(
      listOf(fajr(1), fajr(0, PrayerAlertKind.FOLLOW_UP), fajr(1, PrayerAlertKind.FOLLOW_UP))
    )

    assertEquals(listOf(fajr(0, PrayerAlertKind.FOLLOW_UP), fajr(1)), armed)
  }
}
