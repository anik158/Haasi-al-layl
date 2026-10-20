package com.example.ui.hijri

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.hijri.AladhanClient
import com.example.data.hijri.HijriCalendar
import com.example.data.hijri.HijriMonthId
import com.example.data.hijri.SaudiFixture
import com.example.data.localization.AppLanguage
import com.example.data.model.CityLocation
import com.example.data.notification.PrayerNotificationManager
import com.example.data.prayer.CalculationMethod
import com.example.data.prayer.FastingDay
import com.example.data.prayer.FastingRules
import com.example.data.prayer.FastingStatus
import com.example.data.prayer.HijriDateHelper
import com.example.data.prayer.IslamicMadhab
import com.example.data.preferences.UserPreferences
import com.example.ui.components.MoonSightingCard
import com.example.ui.components.SehriIftarCard
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalTime

/** The warning on Today's Sehri & Iftar card, the "I'm fasting" mark, and the Settings card. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MonthStartUiTest {

  @get:Rule
  val compose = createComposeRule()

  private val app: Application = ApplicationProvider.getApplicationContext()
  private val dhaka = CityLocation("Dhaka", "Bangladesh", 23.8103, 90.4125, "Asia/Dhaka")
  private var status by mutableStateOf<FastingStatus?>(null)
  private var changed = 0

  @Before
  fun setUp() {
    HijriCalendar.http = SaudiFixture.Server()
    UserPreferences(app).selectedCity = dhaka
    HijriCalendar.refreshAnnouncements(app, force = true)
  }

  @After
  fun tearDown() {
    HijriCalendar.http = AladhanClient.okHttp
    HijriDateHelper.table = null
  }

  private fun statusFor(date: LocalDate, isTomorrow: Boolean = true) = FastingStatus(
    day = FastingDay(date, LocalTime.of(5, 0), LocalTime.of(18, 0), HijriDateHelper.isRamadan(date),
      if (HijriDateHelper.isRamadan(date)) HijriDateHelper.getHijriDayOfMonth(date) else 0),
    isFasting = false,
    countdownFormatted = "10:00:00",
    isTomorrow = isTomorrow,
    rules = FastingRules.rulesFor(date)
  )

  private fun showCard(date: LocalDate, language: AppLanguage = AppLanguage.ENGLISH) {
    status = statusFor(date)
    compose.setContent {
      status?.let { SehriIftarCard(status = it, language = language, onFastingChanged = { changed++ }) }
    }
  }

  private fun startOf(month: Int) = HijriDateHelper.table!!.start(HijriMonthId(1448, month))!!.startsOn

  @Test
  fun `Eid al-Fitr says it follows the sighting, and how the date was reached`() {
    showCard(startOf(10))
    compose.onNodeWithTag("fast_rule_eid_fitr").assertExists()
    compose.onNodeWithTag("month_start_note")
      .assertTextContains("Ramadan's start follows the moon sighting", substring = true)
      .assertTextContains("based on the moon", substring = true)
      // Shawwal 1448 is still ahead: Saudi Arabia's date is its calendar's, not an announcement.
      .assertTextContains("Saudi Arabia's calendar: Shawwal starts", substring = true)
    // A forbidden day offers no "I'm fasting".
    compose.onNodeWithTag("fasting_mark").assertDoesNotExist()
  }

  @Test
  fun `the first day of Ramadan carries the note`() {
    showCard(startOf(9))
    compose.onNodeWithTag("sehri_iftar_ramadan").assertExists()
    compose.onNodeWithTag("month_start_note").assertExists()
  }

  @Test
  fun `an ordinary day has no note`() {
    // The 6th of a month, a Wednesday or not, has no Hijri-dated rule.
    val sixth = startOf(6).plusDays(5)
    showCard(sixth)
    compose.onNodeWithTag("month_start_note").assertDoesNotExist()
  }

  @Test
  fun `the fasting mark shows only with the reminder on, and toggles the day`() {
    val day = startOf(6).plusDays(12) // the 13th, a white day
    PrayerNotificationManager.setSehriReminderEnabled(app, true)
    showCard(day)
    compose.onNodeWithTag("fasting_mark").assertTextContains("I'm fasting tomorrow", substring = true).performClick()
    compose.waitForIdle()
    assertTrue(day in PrayerNotificationManager.voluntaryFastDays(app))
    assertEquals(1, changed)
    compose.onNodeWithTag("fasting_mark").assertTextContains("tap to cancel", substring = true).performClick()
    compose.waitForIdle()
    assertTrue(day !in PrayerNotificationManager.voluntaryFastDays(app))
    assertEquals(2, changed)
  }

  @Test
  fun `the note renders in every language`() {
    var language by mutableStateOf(AppLanguage.ENGLISH)
    status = statusFor(startOf(12).plusDays(9)) // Eid al-Adha
    compose.setContent { status?.let { SehriIftarCard(status = it, language = language) } }
    AppLanguage.entries.forEach {
      language = it
      compose.waitForIdle()
      compose.onNodeWithTag("month_start_note").assertExists()
    }
  }

  @Test
  fun `the Settings card shows today's source and, mid-month, nothing to correct`() {
    compose.setContent {
      MoonSightingCard(AppLanguage.ENGLISH, CalculationMethod.KARACHI, IslamicMadhab.DEFAULT, onSettingsChanged = {})
    }
    val today = HijriDateHelper.dayInfo(LocalDate.now(java.time.ZoneId.of("Asia/Dhaka")))!!
    compose.onNodeWithTag("moon_sighting_today").assertTextContains("estimated from Saudi Arabia and the moon", substring = true)
    if (today.day in 4..27) {
      compose.onNodeWithTag("moon_sighting_nothing").assertExists()
      compose.onNodeWithTag("moon_sighting_open").assertDoesNotExist()
    } else {
      compose.onNodeWithTag("moon_sighting_open").assertExists()
    }
  }
}
