package com.example.ui.hijri

import android.app.Application
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.test.core.app.ApplicationProvider
import com.example.data.hijri.HeardFrom
import com.example.data.hijri.HijriCalendar
import com.example.data.hijri.HijriMonthId
import com.example.data.hijri.HijriStore
import com.example.data.hijri.SaudiFixture
import com.example.data.hijri.SightingAnswer
import com.example.data.localization.AppLanguage
import com.example.data.model.CityLocation
import com.example.data.prayer.CalculationMethod
import com.example.data.prayer.HijriDateHelper
import com.example.data.prayer.IslamicMadhab
import com.example.data.preferences.UserPreferences
import com.example.ui.components.SightingFlowDialog
import com.example.ui.components.SightingFlowModel
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
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * "Moon sighting announced", tapped through as a user would: every step, every way out, in every
 * language. The evening is 29 Sha'ban 1448 in Riyadh (6 Feb 2027), when Ramadan can start - and
 * when a "sighted" answer is impossible: the new moon comes about an hour after sunset.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SightingFlowUiTest {

  @get:Rule
  val compose = createComposeRule()

  private val app: Application = ApplicationProvider.getApplicationContext()
  private val riyadh = CityLocation("Riyadh", "Saudi Arabia", 24.7136, 46.6753, "Asia/Riyadh")
  private val ramadan = HijriMonthId(1448, 9)
  private val evening = ZonedDateTime.of(2027, 2, 6, 20, 0, 0, 0, ZoneId.of("Asia/Riyadh")).toInstant().toEpochMilli()
  private var saved = 0
  private var confirmWait = -1
  private var dismissed = 0

  @Before
  fun setUp() {
    HijriCalendar.http = SaudiFixture.Server()
    UserPreferences(app).selectedCity = riyadh
    HijriCalendar.refreshAnnouncements(app, force = true)
  }

  @After
  fun tearDown() {
    HijriCalendar.http = com.example.data.hijri.AladhanClient.okHttp
    HijriDateHelper.table = null
  }

  /** One fresh dialog per call; compose allows one setContent per test, so later calls re-key it. */
  private var round by mutableIntStateOf(0)
  private var shownLanguage by mutableStateOf(AppLanguage.ENGLISH)
  private var contentSet = false

  private fun show(language: AppLanguage = AppLanguage.ENGLISH): SightingFlowModel {
    val model = SightingFlowModel(app, riyadh, CalculationMethod.UMM_AL_QURA, IslamicMadhab.DEFAULT) { evening }
    shownLanguage = language
    if (!contentSet) {
      contentSet = true
      compose.setContent {
        key(round) {
          SightingFlowDialog(
            model = SightingFlowModel(app, riyadh, CalculationMethod.UMM_AL_QURA, IslamicMadhab.DEFAULT) { evening },
            language = shownLanguage,
            onSaved = { saved++ },
            onDismiss = { dismissed++ },
            onConfirmWait = { confirmWait = it }
          )
        }
      }
    } else {
      round++
    }
    compose.waitForIdle()
    return model
  }

  private fun tap(tag: String, idle: Boolean = true) {
    val node = compose.onNodeWithTag(tag)
    // Buttons in the scrolling body scroll into view; the confirm dialog and footer do not scroll.
    try {
      node.performScrollTo()
    } catch (_: AssertionError) {
    }
    node.performClick()
    if (idle) compose.waitForIdle()
  }

  private fun visible(tag: String) = compose.onNodeWithTag(tag).assertExists()

  private val nothingSaved get() = HijriStore(app).userStarts().isEmpty() && HijriStore(app).history().isEmpty()

  @Test
  fun `the evening offers Ramadan, with sighted on the 7th and not sighted on the 8th`() {
    val model = show()
    assertEquals(listOf(ramadan), model.correctableMonths())
    val options = model.options(ramadan)
    assertEquals(LocalDate.of(2027, 2, 7), options.single { it.answer == SightingAnswer.SIGHTED }.startsOn)
    assertEquals(LocalDate.of(2027, 2, 8), options.single { it.answer == SightingAnswer.NOT_SIGHTED }.startsOn)
  }

  @Test
  fun `not sighted, walked to the end, saves exactly that - once`() {
    show()
    visible("sighting_step_MONTH")
    tap("sighting_month_1448-09")
    visible("sighting_step_ANSWER")
    tap("sighting_option_NOT_SIGHTED")
    visible("sighting_step_SOURCE")
    tap("sighting_source_NATIONAL_COMMITTEE")
    visible("sighting_step_REVIEW")
    listOf("month_start", "ramadan_last_day", "eid_fitr", "first_sehri").forEach { visible("sighting_row_$it") }
    compose.onNodeWithTag("sighting_warning_impossible").assertDoesNotExist()
    assertTrue("nothing is saved at the review", nothingSaved)

    tap("sighting_review_continue")
    visible("sighting_confirm_question")
    compose.onNodeWithTag("sighting_confirm").assertIsEnabled()
    assertTrue("nothing is saved before the second confirmation", nothingSaved)

    tap("sighting_confirm")
    visible("sighting_saved")
    assertEquals(1, saved)
    val user = HijriStore(app).userStarts().getValue(ramadan)
    assertEquals(LocalDate.of(2027, 2, 8), user.startsOn)
    assertEquals(HeardFrom.NATIONAL_COMMITTEE, user.heardFrom)
    assertTrue("undo lasts until a Maghrib after now", user.undoUntilMillis > evening)
    assertTrue(user.undoUntilMillis < evening + 24 * 60 * 60 * 1000L)
    assertEquals(LocalDate.of(2027, 2, 8), HijriDateHelper.table!!.start(ramadan)!!.startsOn)
  }

  @Test
  fun `an impossible sighting warns, and the button waits three seconds`() {
    show()
    tap("sighting_month_1448-09")
    tap("sighting_option_SIGHTED")
    tap("sighting_source_TV_OR_NEWS")
    visible("sighting_warning_impossible")
    // Stepped by hand on a paused clock; the dialog reports its countdown each frame.
    compose.onNodeWithTag("sighting_review_continue").performScrollTo()
    compose.mainClock.autoAdvance = false
    compose.onNodeWithTag("sighting_review_continue").performClick()
    fun step(millis: Long) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      compose.mainClock.advanceTimeBy(millis)
    }
    step(50)
    assertEquals("locked as it opens", 3, confirmWait)
    step(1_200)
    assertEquals(2, confirmWait)
    step(1_000)
    assertEquals(1, confirmWait)
    step(1_000)
    assertEquals("unlocked after three seconds", 0, confirmWait)
    compose.mainClock.autoAdvance = true
    compose.onNodeWithTag("sighting_confirm").assertIsEnabled()
    tap("sighting_confirm")
    // Warned, not blocked: the committee has the final word.
    assertEquals(LocalDate.of(2027, 2, 7), HijriStore(app).userStarts().getValue(ramadan).startsOn)
  }

  @Test
  fun `the lock counts down 3, 2, 1, 0`() {
    val lock = { a: Long, b: Long -> com.example.ui.components.confirmLockSecondsLeft(a, b) }
    assertEquals(3, lock(3_000, 0))
    assertEquals(3, lock(3_000, 999))
    assertEquals(2, lock(3_000, 1_000))
    assertEquals(1, lock(3_000, 2_500))
    assertEquals(0, lock(3_000, 3_000))
    assertEquals(0, lock(3_000, 60_000))
    assertEquals(0, lock(0, 0))
  }

  @Test
  fun `without a warning there is no wait`() {
    show()
    tap("sighting_month_1448-09")
    tap("sighting_option_NOT_SIGHTED")
    tap("sighting_source_OTHER")
    tap("sighting_review_continue")
    assertEquals(0, confirmWait)
    compose.onNodeWithTag("sighting_confirm").assertIsEnabled()
  }

  @Test
  fun `cancel at every step saves nothing`() {
    val walks: List<List<String>> = listOf(
      emptyList(),
      listOf("sighting_month_1448-09"),
      listOf("sighting_month_1448-09", "sighting_option_SIGHTED"),
      listOf("sighting_month_1448-09", "sighting_option_SIGHTED", "sighting_source_OTHER")
    )
    walks.forEachIndexed { i, walk ->
      dismissed = 0
      show()
      walk.forEach(::tap)
      tap("sighting_cancel")
      assertEquals("walk $i", 1, dismissed)
      assertTrue("walk $i saved something", nothingSaved)
      assertEquals(0, saved)
    }
  }

  @Test
  fun `backing out of the second confirmation saves nothing, and back walks one step at a time`() {
    show()
    tap("sighting_month_1448-09")
    tap("sighting_option_SIGHTED")
    tap("sighting_source_LOCAL_MOSQUE")
    tap("sighting_review_continue")
    compose.mainClock.advanceTimeBy(4_000)
    tap("sighting_confirm_back")
    visible("sighting_step_REVIEW")
    compose.onNodeWithTag("sighting_confirm_question").assertDoesNotExist()
    tap("sighting_back")
    visible("sighting_step_SOURCE")
    tap("sighting_back")
    visible("sighting_step_ANSWER")
    tap("sighting_back")
    visible("sighting_step_MONTH")
    assertTrue(nothingSaved)
    assertEquals(0, saved)
  }

  @Test
  fun `the whole flow works in every language`() {
    AppLanguage.entries.forEach { language ->
      app.getSharedPreferences("haasi_hijri", 0).edit().remove("user_starts").remove("history").commit()
      saved = 0
      show(language)
      tap("sighting_month_1448-09")
      tap("sighting_option_NOT_SIGHTED")
      tap("sighting_source_TV_OR_NEWS")
      tap("sighting_review_continue")
      tap("sighting_confirm")
      visible("sighting_saved")
      assertEquals(language.name, 1, saved)
      assertEquals(language.name, LocalDate.of(2027, 2, 8), HijriStore(app).userStarts().getValue(ramadan).startsOn)
    }
  }

  @Test
  fun `a correction can be taken back to the estimate from the same flow`() {
    show()
    tap("sighting_month_1448-09")
    tap("sighting_option_NOT_SIGHTED")
    tap("sighting_source_OTHER")
    tap("sighting_review_continue")
    tap("sighting_confirm")
    tap("sighting_close")

    show()
    tap("sighting_month_1448-09")
    tap("sighting_option_USE_ESTIMATE")
    visible("sighting_step_REVIEW") // no source asked
    tap("sighting_review_continue")
    tap("sighting_confirm")
    assertTrue(HijriStore(app).userStarts().isEmpty())
  }
}
