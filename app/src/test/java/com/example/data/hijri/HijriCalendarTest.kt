package com.example.data.hijri

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CityLocation
import com.example.data.notification.PrayerAlarmScheduler
import com.example.data.prayer.FastDayKind
import com.example.data.prayer.FastingRules
import com.example.data.prayer.HijriDateHelper
import com.example.data.preferences.UserPreferences
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

/**
 * The calendar on a device: cached announcements, offline, the user's correction and Undo, and
 * that everything which hangs on the month start - Ramadan, Eid, the forbidden days, the alarms -
 * follows. Runs on today's clock, so it uses Ramadan 1448 (Feb 2027), inside the table's window.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HijriCalendarTest {

  private val app: Application = ApplicationProvider.getApplicationContext()
  private val server = SaudiFixture.Server()
  private val ramadan = HijriMonthId(1448, 9)
  private val dhaka = CityLocation("Dhaka", "Bangladesh", 23.8103, 90.4125, "Asia/Dhaka")
  private val riyadh = CityLocation("Riyadh", "Saudi Arabia", 24.7136, 46.6753, "Asia/Riyadh")
  private val istanbul = CityLocation("Istanbul", "Turkey", 41.0082, 28.9784, "Europe/Istanbul")

  @Before
  fun setUp() {
    HijriCalendar.http = server
  }

  @After
  fun tearDown() {
    HijriCalendar.http = AladhanClient.okHttp
    HijriDateHelper.table = null
  }

  private fun settle(city: CityLocation) {
    UserPreferences(app).selectedCity = city
  }

  private fun start(id: HijriMonthId = ramadan) = HijriDateHelper.table!!.start(id)!!

  @Test
  fun `offline on first launch the table still exists - Umm al-Qura and the moon`() {
    server.online = false
    settle(dhaka)
    assertFalse(HijriCalendar.refreshAnnouncements(app, force = true))
    val s = start()
    assertEquals(MonthStartSource.MOON_ESTIMATE, s.source)
    assertNull(s.authorityStart)
  }

  @Test
  fun `online, Saudi Arabia gets the announcement and Bangladesh a day later`() {
    settle(riyadh)
    assertTrue(HijriCalendar.refreshAnnouncements(app, force = true) || start().source == MonthStartSource.ANNOUNCED)
    assertEquals(MonthStartSource.ANNOUNCED, start().source)
    assertEquals(LocalDate.of(2027, 2, 8), start().startsOn)

    settle(dhaka)
    HijriCalendar.refreshAnnouncements(app, force = true)
    assertEquals(MonthStartSource.MOON_ESTIMATE, start().source)
    assertEquals(LocalDate.of(2027, 2, 8), start().authorityStart)
    assertEquals(LocalDate.of(2027, 2, 9), start().startsOn)
    assertTrue(HijriDateHelper.isRamadan(LocalDate.of(2027, 2, 9)))
    assertFalse(HijriDateHelper.isRamadan(LocalDate.of(2027, 2, 8)))
  }

  @Test
  fun `Turkey asks Diyanet`() {
    settle(istanbul)
    HijriCalendar.refreshAnnouncements(app, force = true)
    assertTrue(server.requests.isNotEmpty())
    assertTrue(server.requests.all { it.endsWith("calendarMethod=DIYANET") })
    assertEquals(Authority.TURKEY, start().authority)
  }

  @Test
  fun `the cache survives a restart with no network`() {
    settle(dhaka)
    HijriCalendar.refreshAnnouncements(app, force = true)
    val before = HijriDateHelper.table!!.signature
    server.online = false
    HijriDateHelper.table = null // a new process
    UserPreferences(app).applyHijriAdjustment()
    assertEquals(before, HijriDateHelper.table!!.signature)
    assertEquals(LocalDate.of(2027, 2, 8), start().authorityStart)
  }

  @Test
  fun `fetches are daily, hourly near a month start, and never on a fresh cache`() {
    settle(dhaka)
    val t0 = System.currentTimeMillis()
    HijriCalendar.refreshAnnouncements(app, nowMillis = t0, force = true)
    val asked = server.requests.size
    HijriCalendar.refreshAnnouncements(app, nowMillis = t0 + 60_000)
    assertEquals("a fresh cache must not refetch", asked, server.requests.size)

    val hour = 60 * 60 * 1000L
    assertFalse(HijriCalendar.isDue(2 * hour, 15))
    assertTrue(HijriCalendar.isDue(25 * hour, 15))
    assertTrue(HijriCalendar.isDue(2 * hour, 29))
    assertTrue(HijriCalendar.isDue(2 * hour, 1))
    assertFalse(HijriCalendar.isDue(10 * 60_000L, 29))
    assertTrue(HijriCalendar.isDue(-1, 15))
    assertTrue(HijriCalendar.isDue(2 * hour, null))
  }

  @Test
  fun `damaged storage falls back instead of crashing`() {
    app.getSharedPreferences("haasi_hijri", 0).edit()
      .putString("announced_HJCoSA", "1448-09=garbage,nonsense,=,1448-13=2027-01-01")
      .putString("user_starts", "|||;1448-09|not-a-date|X|1|2|")
      .putString("history", "junk;;;")
      .commit()
    settle(dhaka)
    assertNotNull(HijriDateHelper.table)
    assertEquals(MonthStartSource.MOON_ESTIMATE, start().source)
    assertTrue(HijriStore(app).userStarts().isEmpty())
    assertTrue(HijriStore(app).history().isEmpty())
  }

  @Test
  fun `a correction moves Ramadan, Eid, the forbidden days and the alarms - and Undo puts them all back`() {
    settle(dhaka)
    HijriCalendar.refreshAnnouncements(app, force = true)
    PrayerAlarmScheduler.rescheduleAll(app)
    val tableBefore = HijriDateHelper.table!!.signature
    val alarmsBefore = UserPreferences(app).lastScheduleSignature
    val estimate = start().startsOn
    val eidBefore = start(ramadan.next()).startsOn

    // Sighted a day earlier than the estimate.
    val earlier = estimate.minusDays(1)
    HijriCalendar.applyCorrection(
      app,
      SightingFlow.Decision(ramadan, SightingAnswer.SIGHTED, earlier, HeardFrom.NATIONAL_COMMITTEE),
      nowMillis = 1_000L,
      undoUntilMillis = Long.MAX_VALUE
    )
    PrayerAlarmScheduler.rescheduleAll(app)
    assertEquals(MonthStartSource.USER, start().source)
    assertEquals(earlier, start().startsOn)
    assertTrue(HijriDateHelper.isRamadan(earlier))
    assertEquals(1, HijriDateHelper.getHijriDayOfMonth(earlier))
    // Eid follows within the 29-30 day rule, and so does its "forbidden" ruling.
    val eidAfter = start(ramadan.next()).startsOn
    assertTrue(eidAfter == eidBefore || eidAfter == eidBefore.minusDays(1))
    assertTrue(FastingRules.rulesFor(eidAfter).any { it.kind == FastDayKind.PROHIBITED && it.key == "eid_fitr" })
    assertNotEquals(tableBefore, HijriDateHelper.table!!.signature)
    assertNotEquals("the alarms must re-arm for the new Ramadan", alarmsBefore, UserPreferences(app).lastScheduleSignature)
    assertEquals(ramadan, HijriCalendar.undoable(app, 2_000L)!!.id)

    HijriCalendar.undoCorrection(app, ramadan, nowMillis = 3_000L)
    PrayerAlarmScheduler.rescheduleAll(app)
    assertEquals(tableBefore, HijriDateHelper.table!!.signature)
    assertEquals(alarmsBefore, UserPreferences(app).lastScheduleSignature)
    assertNull(HijriCalendar.undoable(app, 4_000L))

    val history = HijriStore(app).history()
    assertEquals(listOf(HistoryAction.SET, HistoryAction.UNDO), history.map { it.action })
    assertEquals(earlier, history[0].to)
    assertEquals(HeardFrom.NATIONAL_COMMITTEE, history[0].heardFrom)
  }

  @Test
  fun `a second correction undoes back to the first, and the estimate clears it`() {
    settle(dhaka)
    val estimate = start().startsOn
    fun set(date: LocalDate) = HijriCalendar.applyCorrection(
      app, SightingFlow.Decision(ramadan, SightingAnswer.SIGHTED, date, HeardFrom.TV_OR_NEWS), 1L, Long.MAX_VALUE
    )
    set(estimate.minusDays(1))
    set(estimate.plusDays(1))
    HijriCalendar.undoCorrection(app, ramadan, 5L)
    assertEquals(estimate.minusDays(1), start().startsOn)
    assertEquals(MonthStartSource.USER, start().source)
    HijriCalendar.applyCorrection(app, SightingFlow.Decision(ramadan, SightingAnswer.USE_ESTIMATE, estimate, null), 6L, 0L)
    assertEquals(estimate, start().startsOn)
    assertEquals(MonthStartSource.MOON_ESTIMATE, start().source)
    assertTrue(HijriStore(app).userStarts().isEmpty())
  }

  @Test
  fun `undo is offered only until the next Maghrib`() {
    settle(dhaka)
    HijriCalendar.applyCorrection(
      app, SightingFlow.Decision(ramadan, SightingAnswer.NOT_SIGHTED, start().startsOn, HeardFrom.OTHER), 100L, undoUntilMillis = 500L
    )
    assertNotNull(HijriCalendar.undoable(app, 499L))
    assertNull(HijriCalendar.undoable(app, 500L))
  }

  @Test
  fun `moving to another country re-derives the calendar`() {
    settle(dhaka)
    HijriCalendar.refreshAnnouncements(app, force = true)
    assertEquals(LocalDate.of(2027, 2, 9), start().startsOn)
    settle(riyadh)
    assertEquals(LocalDate.of(2027, 2, 8), start().startsOn)
  }

  @Test
  fun `the store round-trips every field`() {
    val u = UserMonthStart(ramadan, LocalDate.of(2027, 2, 9), HeardFrom.LOCAL_MOSQUE, 123L, 456L, LocalDate.of(2027, 2, 8))
    assertEquals(u, HijriStore.decodeUser(HijriStore.encodeUser(u)))
    assertEquals(u.copy(previous = null), HijriStore.decodeUser(HijriStore.encodeUser(u.copy(previous = null))))
    val h = HistoryEntry(9L, HistoryAction.CLEARED, ramadan, LocalDate.of(2027, 2, 9), null, null)
    assertEquals(h, HijriStore.decodeHistory(HijriStore.encodeHistory(h)))
    val dates = mapOf(ramadan to LocalDate.of(2027, 2, 8), ramadan.next() to LocalDate.of(2027, 3, 9))
    assertEquals(dates, HijriStore.decodeDates(HijriStore.encodeDates(dates)))
  }
}
