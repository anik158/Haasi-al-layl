package com.example.data.hijri

import com.example.data.astronomy.CrescentVisibility
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.random.Random

/**
 * The correction rules, every answer against every kind of month - the sensitive part of the
 * calendar, so it is tested exhaustively rather than by example.
 */
class SightingCorrectionTest {

  /** A table of Umm al-Qura starts around [centre], optionally corrected. */
  private fun table(
    centre: HijriMonthId,
    user: Map<HijriMonthId, LocalDate> = emptyMap(),
    policy: SightingPolicy = SightingPolicy.FOLLOW_SAUDI
  ) = MonthStartResolver(policy, emptyMap(), user, { 20.0 }).resolve(centre + (-3), 16)

  private val everyKindOfMonth = (1..12).map { HijriMonthId(1447, it) } +
    // Across a Hijri year end, and a Gregorian one (Rajab 1447 began in Dec 2025).
    listOf(HijriMonthId(1448, 1), HijriMonthId(1447, 7), HijriMonthId(1450, 9))

  @Test
  fun `sighted starts the 30th day after the old month, not sighted the 31st`() {
    everyKindOfMonth.forEach { id ->
      val t = table(id)
      val previous = t.start(id.previous())!!.startsOn
      val options = SightingCorrection.optionsFor(id, t, hasUserStart = false)
      assertEquals(id.key, listOf(SightingAnswer.SIGHTED, SightingAnswer.NOT_SIGHTED), options.map { it.answer })
      assertEquals(id.key, previous.plusDays(29), options[0].startsOn)
      assertEquals(id.key, previous.plusDays(30), options[1].startsOn)
      options.forEach { assertEquals(previous.plusDays(28), it.sightingEvening) }
    }
  }

  @Test
  fun `no option ever makes a 28 or 31 day month`() {
    everyKindOfMonth.forEach { id ->
      listOf(SightingPolicy.FOLLOW_SAUDI, SightingPolicy.LOCAL_SIGHTING).forEach { policy ->
        val t = table(id, policy = policy)
        val previous = t.start(id.previous())!!.startsOn
        SightingCorrection.optionsFor(id, t, hasUserStart = true).forEach {
          assertTrue("${id.key} ${it.answer}", SightingCorrection.isValidStart(previous, it.startsOn))
        }
      }
    }
  }

  @Test
  fun `saving any option keeps every month 29 or 30 days`() {
    everyKindOfMonth.forEach { id ->
      val estimate = table(id)
      SightingCorrection.optionsFor(id, estimate, hasUserStart = false).forEach { option ->
        val after = table(id, user = mapOf(id to option.startsOn))
        assertEquals(id.key, option.startsOn, after.start(id)!!.startsOn)
        assertEquals(id.key, MonthStartSource.USER, after.start(id)!!.source)
        after.starts.zipWithNext().forEach { (a, b) ->
          assertTrue("${id.key} ${option.answer}: ${a.id.key}", ChronoUnit.DAYS.between(a.startsOn, b.startsOn) in 29..30)
        }
      }
    }
  }

  @Test
  fun `use estimate is offered only when there is a correction to drop`() {
    val id = HijriMonthId(1447, 9)
    assertTrue(SightingCorrection.optionsFor(id, table(id), false).none { it.answer == SightingAnswer.USE_ESTIMATE })
    val withUndo = SightingCorrection.optionsFor(id, table(id), true)
    assertEquals(table(id).start(id)!!.startsOn, withUndo.single { it.answer == SightingAnswer.USE_ESTIMATE }.startsOn)
  }

  @Test
  fun `the 28th offers the next month, the 1st to 3rd the current one, the rest nothing`() {
    val id = HijriMonthId(1447, 9)
    val t = table(id)
    val first = t.start(id)!!.startsOn
    val length = ChronoUnit.DAYS.between(first, t.start(id.next())!!.startsOn).toInt()
    (1..length).forEach { day ->
      val months = SightingCorrection.correctableMonths(first.plusDays(day - 1L), t)
      val expected = buildList {
        if (day <= 3) add(id)
        if (day >= 28) add(id.next())
      }
      assertEquals("day $day", expected, months)
    }
  }

  @Test
  fun `outside the table nothing can be corrected`() {
    assertTrue(SightingCorrection.correctableMonths(LocalDate.of(1990, 1, 1), table(HijriMonthId(1447, 9))).isEmpty())
  }

  @Test
  fun `the moon warns on an impossible sighting and notes an easy miss`() {
    assertEquals(MoonCheck.SIGHTING_IMPOSSIBLE, SightingCorrection.moonCheck(SightingAnswer.SIGHTED, CrescentVisibility.IMPOSSIBLE, -8.0))
    assertEquals(MoonCheck.OK, SightingCorrection.moonCheck(SightingAnswer.SIGHTED, CrescentVisibility.NOT_VISIBLE, -2.0))
    assertEquals(MoonCheck.OK, SightingCorrection.moonCheck(SightingAnswer.SIGHTED, CrescentVisibility.NAKED_EYE, 15.0))
    assertEquals(MoonCheck.EASILY_VISIBLE, SightingCorrection.moonCheck(SightingAnswer.NOT_SIGHTED, CrescentVisibility.NAKED_EYE, 15.0))
    assertEquals(MoonCheck.OK, SightingCorrection.moonCheck(SightingAnswer.NOT_SIGHTED, CrescentVisibility.NAKED_EYE, 6.0))
    assertEquals(MoonCheck.OK, SightingCorrection.moonCheck(SightingAnswer.NOT_SIGHTED, CrescentVisibility.IMPOSSIBLE, -8.0))
  }

  @Test
  fun `the review lists the days that hang on each month`() {
    fun keys(month: Int) = SightingCorrection.changes(HijriMonthId(1447, month), table(HijriMonthId(1447, month)), table(HijriMonthId(1447, month))).map { it.key }
    assertEquals(listOf("month_start", "ramadan_last_day", "eid_fitr"), keys(9))
    assertEquals(listOf("month_start", "ramadan_last_day", "eid_fitr"), keys(10))
    assertEquals(listOf("month_start", "arafah", "eid_adha", "tashreeq"), keys(12))
    assertEquals(listOf("month_start", "ashura"), keys(1))
    assertEquals(listOf("month_start", "ramadan_first_day"), keys(8))
    assertEquals(listOf("month_start", "eid_adha"), keys(11))
    assertEquals(listOf("month_start"), keys(5))
  }

  @Test
  fun `the review shows Ramadan, Eid and the forbidden days moving with the answer`() {
    val ramadan = HijriMonthId(1447, 9)
    val before = table(ramadan)
    val start = before.start(ramadan)!!.startsOn
    val after = table(ramadan, user = mapOf(ramadan to start.plusDays(1)))
    val rows = SightingCorrection.changes(ramadan, before, after).associateBy { it.key }
    assertEquals(start, rows.getValue("month_start").from)
    assertEquals(start.plusDays(1), rows.getValue("month_start").to)
    // A later Ramadan pushes Eid (a forbidden day) by at most a day, never by two.
    val eid = rows.getValue("eid_fitr")
    assertTrue(ChronoUnit.DAYS.between(eid.from, eid.to) in 0..1)

    val dhulHijjah = HijriMonthId(1447, 12)
    val dBefore = table(dhulHijjah)
    val dStart = dBefore.start(dhulHijjah)!!.startsOn
    val dRows = SightingCorrection.changes(dhulHijjah, dBefore, table(dhulHijjah, user = mapOf(dhulHijjah to dStart.minusDays(1))))
      .associateBy { it.key }
    assertEquals(dStart.plusDays(8), dRows.getValue("arafah").from)
    assertEquals(dStart.plusDays(7), dRows.getValue("arafah").to)
    assertEquals(dStart.plusDays(8), dRows.getValue("eid_adha").to)
    assertEquals(dStart.plusDays(9), dRows.getValue("tashreeq").to)
  }

  @Test
  fun `random corrections never break the calendar`() {
    val random = Random(9)
    repeat(2000) { run ->
      val id = HijriMonthId(1441 + random.nextInt(9), 1 + random.nextInt(12))
      val policy = SightingPolicy.entries[random.nextInt(3)]
      val user = mutableMapOf<HijriMonthId, LocalDate>()
      // The same moon for every table in a run, as on a phone.
      val salt = random.nextInt()
      val moon = { d: LocalDate -> Random(d.toEpochDay().toInt() xor salt).nextDouble(-5.0, 25.0) }
      // Apply a few corrections in a row, each chosen from what the flow would offer then.
      repeat(1 + random.nextInt(4)) { k ->
        val target = id + k
        val estimate = MonthStartResolver(policy, emptyMap(), user - target, moon)
          .resolve(id + (-3), 16)
        val options = SightingCorrection.optionsFor(target, estimate, target in user)
        val pick = options[random.nextInt(options.size)]
        if (pick.answer == SightingAnswer.USE_ESTIMATE) user.remove(target) else user[target] = pick.startsOn
      }
      val t = MonthStartResolver(policy, emptyMap(), user, moon).resolve(id + (-3), 16)
      t.starts.zipWithNext().forEach { (a, b) ->
        assertTrue("run $run ${a.id.key}", ChronoUnit.DAYS.between(a.startsOn, b.startsOn) in 29..30)
      }
      user.forEach { (m, d) -> assertEquals("run $run ${m.key}", d, t.start(m)!!.startsOn) }
    }
  }

  @Test
  fun `a month without its previous month in the table offers nothing`() {
    val t = table(HijriMonthId(1447, 9))
    assertTrue(SightingCorrection.optionsFor(t.starts.first().id, t, false).isEmpty())
    assertNull(t.start(t.starts.first().id.previous()))
  }
}
