package com.example.data.hijri

import com.example.data.astronomy.CrescentVisibility
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** What the user says was announced. */
enum class SightingAnswer {
  /** Sighted on the evening of the 29th: the month starts the next day. */
  SIGHTED,

  /** Not sighted: the old month completes 30 days. */
  NOT_SIGHTED,

  /** Drop the user's correction; back to the app's estimate. */
  USE_ESTIMATE
}

/** One choice on the "What was announced?" screen, with the date it leads to. */
data class SightingOption(
  val answer: SightingAnswer,
  /** The new month's first day; for [SightingAnswer.USE_ESTIMATE], the estimate's. */
  val startsOn: LocalDate,
  /** The evening the sighting was looked for - the 29th of the old month. */
  val sightingEvening: LocalDate
)

/** The moon's say on the user's answer: a warning to read, never a block. */
enum class MoonCheck {
  /** Nothing to remark. */
  OK,

  /** "Sighted" on an evening the crescent could not have been there. */
  SIGHTING_IMPOSSIBLE,

  /** "Not sighted" on an evening it was plainly visible - cloud happens, so only a note. */
  EASILY_VISIBLE
}

/** One line of the review screen: what moves, from and to. */
data class ChangeRow(val key: String, val from: LocalDate?, val to: LocalDate?) {
  val changed: Boolean get() = from != to
}

/**
 * The rules of the "Moon sighting announced" correction in Settings, kept free of Android so they
 * can be tested exhaustively. The flow itself (SightingFlow) only walks the screens; everything
 * that decides a date is here.
 */
object SightingCorrection {

  /**
   * The months that can start around [today]: the next month from the 28th on, and the current
   * one during its first three days (an announcement heard late). A month the user already
   * corrected stays offered while it is one of those, so it can be changed back.
   */
  fun correctableMonths(today: LocalDate, table: HijriMonthTable): List<HijriMonthId> {
    val day = table.resolve(today) ?: return emptyList()
    val months = mutableListOf<HijriMonthId>()
    if (day.day <= 3) months += day.id
    if (day.day >= 28) months += day.id.next()
    return months.filter { table.start(it.previous()) != null && table.start(it) != null }
  }

  /**
   * The answers for [id]: sighted (the 29th's evening, so the month starts on the 30th day after
   * the old month's first) or not sighted (it starts a day later), and - if the user corrected it
   * - back to the estimate. [estimateTable] is the table without the user's correction for [id].
   *
   * Only 29 or 30 days can come out, so 28- and 31-day months are never offered; and an option
   * more than a day from the estimate (which cannot happen while both are 29-30 days after the
   * same first) is dropped rather than trusted.
   */
  fun optionsFor(id: HijriMonthId, estimateTable: HijriMonthTable, hasUserStart: Boolean): List<SightingOption> {
    val previous = estimateTable.start(id.previous())?.startsOn ?: return emptyList()
    val estimate = estimateTable.start(id)?.startsOn ?: return emptyList()
    val evening = previous.plusDays(28)
    val options = listOf(
      SightingOption(SightingAnswer.SIGHTED, previous.plusDays(29), evening),
      SightingOption(SightingAnswer.NOT_SIGHTED, previous.plusDays(30), evening)
    ).filter { Math.abs(ChronoUnit.DAYS.between(estimate, it.startsOn)) <= 1 }
    return if (hasUserStart) options + SightingOption(SightingAnswer.USE_ESTIMATE, estimate, evening) else options
  }

  /** Whether the length rule allows [startsOn] after [previous]: 29 or 30 days. */
  fun isValidStart(previous: LocalDate, startsOn: LocalDate): Boolean =
    ChronoUnit.DAYS.between(previous, startsOn) in 29..30

  fun moonCheck(answer: SightingAnswer, visibilityThatEvening: CrescentVisibility, odehV: Double): MoonCheck =
    when {
      answer == SightingAnswer.SIGHTED && visibilityThatEvening == CrescentVisibility.IMPOSSIBLE ->
        MoonCheck.SIGHTING_IMPOSSIBLE
      answer == SightingAnswer.NOT_SIGHTED && odehV >= MonthStartResolver.SAME_DAY_V -> MoonCheck.EASILY_VISIBLE
      else -> MoonCheck.OK
    }

  /**
   * What changes between [before] and [after] for [id], as the review screen lists it: the month's
   * first day, and the days that hang on it - Ramadan's last day and Eid al-Fitr, Arafah,
   * Eid al-Adha and Tashreeq, Ashura.
   */
  fun changes(id: HijriMonthId, before: HijriMonthTable, after: HijriMonthTable): List<ChangeRow> {
    fun first(table: HijriMonthTable, m: HijriMonthId) = table.start(m)?.startsOn
    fun day(table: HijriMonthTable, m: HijriMonthId, n: Long) = first(table, m)?.plusDays(n - 1)
    fun row(key: String, pick: (HijriMonthTable) -> LocalDate?) = ChangeRow(key, pick(before), pick(after))

    val rows = mutableListOf(row("month_start") { first(it, id) })
    when (id.month) {
      HijriMonthId.RAMADAN -> {
        rows += row("ramadan_last_day") { first(it, id.next())?.minusDays(1) }
        rows += row("eid_fitr") { first(it, id.next()) }
      }
      HijriMonthId.SHAWWAL -> {
        rows += row("ramadan_last_day") { first(it, id)?.minusDays(1) }
        rows += row("eid_fitr") { first(it, id) }
      }
      HijriMonthId.DHUL_HIJJAH -> {
        rows += row("arafah") { day(it, id, 9) }
        rows += row("eid_adha") { day(it, id, 10) }
        rows += row("tashreeq") { day(it, id, 11) }
      }
      HijriMonthId.MUHARRAM -> rows += row("ashura") { day(it, id, 10) }
      HijriMonthId.SHABAN -> rows += row("ramadan_first_day") { first(it, id.next()) }
      HijriMonthId.DHUL_QADAH -> rows += row("eid_adha") { day(it, id.next(), 10) }
    }
    return rows
  }
}

/** Which days carry the "this depends on the moon sighting" note. */
object MonthStartWarning {

  /**
   * The days a month can start (29th, 30th), the first two and last three days of Ramadan, Eid
   * al-Fitr, Arafah through Tashreeq, and every day fasting is forbidden - wherever a day's error
   * would change whether to fast. Outside the table (no date to vouch for) any Hijri-dated rule
   * carries it.
   */
  fun applies(day: HijriDay?, rules: List<com.example.data.prayer.FastDayRule>): Boolean {
    if (rules.any { it.kind == com.example.data.prayer.FastDayKind.PROHIBITED }) return true
    if (day == null) return rules.any { it.dependsOnHijriDate }
    return day.day >= 29 ||
      (day.month == HijriMonthId.RAMADAN && (day.day <= 2 || day.day >= 28)) ||
      (day.month == HijriMonthId.SHAWWAL && day.day == 1) ||
      (day.month == HijriMonthId.DHUL_HIJJAH && day.day in 9..13)
  }
}
