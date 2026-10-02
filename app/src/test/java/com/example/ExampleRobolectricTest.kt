package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Haasi al-Layl", appName)
  }

  @Test
  fun `verify bangla date calculation`() {
    val date = java.time.LocalDate.of(2026, 9, 11)
    val banglaDate = com.example.data.calendar.BanglaDateHelper.getBanglaDate(date)
    assertEquals(1433, banglaDate.year)
    assertEquals("ভাদ্র", banglaDate.monthNameBn)
  }

  @Test
  fun `verify forbidden prayer times calculation`() {
    val sunrise = java.time.LocalTime.of(6, 0)
    val dhuhr = java.time.LocalTime.of(12, 5)
    val sunset = java.time.LocalTime.of(18, 12)

    val windows = com.example.data.prayer.ForbiddenPrayerTimesHelper.calculateWindows(
      sunrise = sunrise,
      dhuhr = dhuhr,
      sunset = sunset
    )

    assertEquals(3, windows.size)
    assertEquals(com.example.data.prayer.ForbiddenWindowType.SUNRISE, windows[0].type)
    assertEquals(sunrise, windows[0].startTime)
    assertEquals(sunrise.plusMinutes(15), windows[0].endTime)

    assertEquals(com.example.data.prayer.ForbiddenWindowType.ZENITH, windows[1].type)
    assertEquals(dhuhr.minusMinutes(10), windows[1].startTime)
    assertEquals(dhuhr, windows[1].endTime)

    assertEquals(com.example.data.prayer.ForbiddenWindowType.SUNSET, windows[2].type)
    // Muslim 831: "when it inclines to setting until it sets" - 15 minutes, ending at true sunset.
    assertEquals(sunset.minusMinutes(15), windows[2].startTime)
    assertEquals(sunset, windows[2].endTime)
  }

  @Test
  fun `verify notification channels initialization`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.data.notification.PrayerNotificationManager.createNotificationChannels(context)
    val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    val prayerChannel = notifManager.getNotificationChannel(com.example.data.notification.PrayerNotificationManager.CHANNEL_PRAYER_ID)
    val forbiddenChannel = notifManager.getNotificationChannel(com.example.data.notification.PrayerNotificationManager.CHANNEL_FORBIDDEN_ID)
    org.junit.Assert.assertNotNull(prayerChannel)
    org.junit.Assert.assertNotNull(forbiddenChannel)
  }
}
