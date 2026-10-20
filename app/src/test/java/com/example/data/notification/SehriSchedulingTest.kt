package com.example.data.notification

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.hijri.AladhanClient
import com.example.data.hijri.HijriCalendar
import com.example.data.hijri.SaudiFixture
import com.example.data.model.CityLocation
import com.example.data.prayer.HijriDateHelper
import com.example.data.preferences.UserPreferences
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.ZoneId

/** The alarms actually armed: the Sehri wake-up on fasting days only, the follow-up off by default. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SehriSchedulingTest {

  private val app: Application = ApplicationProvider.getApplicationContext()
  private val alarms = shadowOf(app.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
  private val dhaka = CityLocation("Dhaka", "Bangladesh", 23.8103, 90.4125, "Asia/Dhaka")
  private val today = LocalDate.now(ZoneId.of("Asia/Dhaka"))

  @Before
  fun setUp() {
    HijriCalendar.http = SaudiFixture.Server(online = false)
    UserPreferences(app).selectedCity = dhaka
  }

  @After
  fun tearDown() {
    HijriCalendar.http = AladhanClient.okHttp
    HijriDateHelper.table = null
  }

  private fun armedCodes(): Set<String> =
    alarms.scheduledAlarms.mapNotNull { shadowOf(it.operation).savedIntent.data?.lastPathSegment }.toSet()

  private fun sehriArmed() = AlertIds.SEHRI.toString() in armedCodes()

  @Test
  fun `the follow-up is off on a fresh install, and a saved choice is kept`() {
    assertFalse(PrayerNotificationManager.isFollowUpNotificationsEnabled(app))
    PrayerAlarmScheduler.rescheduleAll(app)
    val followUps = com.example.data.model.PrayerName.entries.map {
      AlertIds.prayer(it, PrayerAlertKind.FOLLOW_UP).toString()
    }
    assertTrue(armedCodes().none { it in followUps })
    // The prayer-time alert itself is still armed.
    assertTrue(armedCodes().any { code ->
      com.example.data.model.PrayerName.entries.any { AlertIds.prayer(it, PrayerAlertKind.AT_TIME).toString() == code }
    })

    PrayerNotificationManager.setFollowUpNotificationsEnabled(app, true)
    Thread.sleep(50) // apply() is asynchronous
    assertTrue(PrayerNotificationManager.isFollowUpNotificationsEnabled(app))
    PrayerAlarmScheduler.rescheduleAll(app)
    assertTrue(armedCodes().any { it in followUps })
  }

  @Test
  fun `the Sehri reminder is off by default, 60 minutes when on`() {
    assertFalse(PrayerNotificationManager.isSehriReminderEnabled(app))
    assertEquals(60, PrayerNotificationManager.sehriLeadMinutes(app))
    PrayerAlarmScheduler.rescheduleAll(app)
    assertFalse(sehriArmed())
  }

  @Test
  fun `on, it rings for a marked voluntary fast and not otherwise`() {
    PrayerNotificationManager.setSehriReminderEnabled(app, true)
    // Outside Ramadan (Oct 2026 is Jumada), nothing to wake for.
    assertFalse(HijriDateHelper.isRamadan(today.plusDays(2)))
    PrayerAlarmScheduler.rescheduleAll(app)
    assertFalse(sehriArmed())

    // An ordinary day two days out - never a forbidden one in this month.
    val day = today.plusDays(1) // inside the 48-hour arming window whatever the hour
    PrayerNotificationManager.setVoluntaryFast(app, day, true, today)
    PrayerAlarmScheduler.rescheduleAll(app)
    assertTrue(sehriArmed())

    PrayerNotificationManager.setVoluntaryFast(app, day, false, today)
    PrayerAlarmScheduler.rescheduleAll(app)
    assertFalse(sehriArmed())
  }

  @Test
  fun `marked days in the past are dropped`() {
    PrayerNotificationManager.setVoluntaryFast(app, today.minusDays(3), true, today.minusDays(5))
    PrayerNotificationManager.setVoluntaryFast(app, today.plusDays(1), true, today)
    assertEquals(setOf(today.plusDays(1)), PrayerNotificationManager.voluntaryFastDays(app))
  }
}
