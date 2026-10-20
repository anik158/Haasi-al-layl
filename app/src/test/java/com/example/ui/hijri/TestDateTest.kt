package com.example.ui.hijri

import android.app.Application
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppClock
import com.example.data.hijri.AladhanClient
import com.example.data.hijri.HeardFrom
import com.example.data.hijri.HijriCalendar
import com.example.data.hijri.HijriMonthId
import com.example.data.hijri.HijriStore
import com.example.data.hijri.SaudiFixture
import com.example.data.hijri.SightingAnswer
import com.example.data.hijri.SightingFlow
import com.example.data.model.CityLocation
import com.example.data.prayer.HijriDateHelper
import com.example.data.preferences.UserPreferences
import com.example.ui.components.TestDateCard
import com.example.ui.components.testMoments
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.ZoneId

/** The debug "Test a date": the calendar follows it, and nothing saved under it survives. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TestDateTest {

  @get:Rule
  val compose = createComposeRule()

  private val app: Application = ApplicationProvider.getApplicationContext()
  private val dhaka = CityLocation("Dhaka", "Bangladesh", 23.8103, 90.4125, "Asia/Dhaka")
  private val zone = ZoneId.of("Asia/Dhaka")

  @Before
  fun setUp() {
    HijriCalendar.http = SaudiFixture.Server()
    UserPreferences(app).selectedCity = dhaka
    HijriCalendar.refreshAnnouncements(app, force = true)
  }

  @After
  fun tearDown() {
    AppClock.backToRealTime()
    HijriCalendar.http = AladhanClient.okHttp
    HijriDateHelper.table = null
  }

  private fun moments() = testMoments(HijriCalendar.build(dhaka, LocalDate.now(zone), HijriStore(app)), zone)

  @Test
  fun `the moments land on the days they name`() {
    val byLabel = moments().associateBy { it.label.substringBefore(" -") }
    fun hijri(label: String) = HijriCalendar.build(dhaka, LocalDate.now(zone), HijriStore(app))
      .resolve(byLabel.getValue(label).at.toLocalDate())!!
    assertEquals(8 to 29, hijri("29 Sha'ban, 8 pm").let { it.month to it.day })
    assertEquals(9 to 1, hijri("1 Ramadan, 3 am").let { it.month to it.day })
    assertEquals(9 to 29, hijri("29 Ramadan, 8 pm").let { it.month to it.day })
    assertEquals(10 to 1, hijri("Eid al-Fitr, 9 am").let { it.month to it.day })
    assertEquals(12 to 9, hijri("Day of Arafah, 9 am").let { it.month to it.day })
    assertEquals(12 to 10, hijri("Eid al-Adha, 9 am").let { it.month to it.day })
  }

  @Test
  fun `choosing a moment moves today, the Hijri date and the correction window`() {
    compose.setContent { TestDateCard(onChanged = {}) }
    compose.onNodeWithTag("test_date_now").assertTextContains("real time", substring = true)
    // 29 Sha'ban, 8 pm.
    compose.onNodeWithTag("test_date_1").performClick()
    compose.waitForIdle()
    assertTrue(AppClock.isTesting)
    val today = AppClock.today(zone)
    assertEquals(8, HijriDateHelper.getHijriMonth(today))
    assertEquals(29, HijriDateHelper.getHijriDayOfMonth(today))
    compose.onNodeWithTag("test_date_now").assertTextContains("(test)", substring = true)

    compose.onNodeWithTag("test_date_real").performClick()
    compose.waitForIdle()
    assertFalse(AppClock.isTesting)
  }

  @Test
  fun `a correction saved under a test date is gone after it, and Undo never shows in real time`() {
    val sighting = moments().first { it.label.startsWith("29 Sha'ban") }
    AppClock.testAt(sighting.at)
    HijriCalendar.rebuild(app)
    val ramadan = HijriMonthId(1448, 9)
    val estimate = HijriDateHelper.table!!.start(ramadan)!!.startsOn
    HijriCalendar.applyCorrection(
      app,
      SightingFlow.Decision(ramadan, SightingAnswer.SIGHTED, estimate.minusDays(1), HeardFrom.OTHER),
      nowMillis = AppClock.nowMillis(),
      undoUntilMillis = AppClock.nowMillis() + 3_600_000
    )
    assertEquals(estimate.minusDays(1), HijriDateHelper.table!!.start(ramadan)!!.startsOn)
    // Seen from the real clock, that Undo belongs to the future: not offered.
    assertNull(HijriCalendar.undoable(app, System.currentTimeMillis()))

    HijriCalendar.leaveTestDate(app)
    assertFalse(AppClock.isTesting)
    assertTrue(HijriStore(app).userStarts().isEmpty())
    assertTrue(HijriStore(app).history().isEmpty())
    assertEquals(estimate, HijriDateHelper.table!!.start(ramadan)!!.startsOn)
  }

  @Test
  fun `a real correction survives leaving a test date`() {
    val ramadan = HijriMonthId(1448, 9)
    val estimate = HijriDateHelper.table!!.start(ramadan)!!.startsOn
    HijriCalendar.applyCorrection(
      app, SightingFlow.Decision(ramadan, SightingAnswer.NOT_SIGHTED, estimate, HeardFrom.OTHER),
      nowMillis = System.currentTimeMillis() - 1_000, undoUntilMillis = 0L
    )
    AppClock.testAt(moments().first().at)
    HijriCalendar.leaveTestDate(app)
    assertEquals(1, HijriStore(app).userStarts().size)
  }
}
