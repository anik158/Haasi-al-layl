package com.example.data.hijri

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoUnit

/** One Hijri month, by year and month (1..12). */
data class HijriMonthId(val year: Int, val month: Int) : Comparable<HijriMonthId> {
  init {
    require(month in 1..12) { "month $month" }
  }

  private val index: Int get() = year * 12 + (month - 1)

  operator fun plus(months: Int): HijriMonthId {
    val i = index + months
    return HijriMonthId(Math.floorDiv(i, 12), Math.floorMod(i, 12) + 1)
  }

  fun next() = plus(1)
  fun previous() = plus(-1)

  override fun compareTo(other: HijriMonthId) = index.compareTo(other.index)

  /** "1447-09" - the storage key. */
  val key: String get() = "%d-%02d".format(java.util.Locale.US, year, month)

  companion object {
    const val MUHARRAM = 1
    const val SHABAN = 8
    const val RAMADAN = 9
    const val SHAWWAL = 10
    const val DHUL_QADAH = 11
    const val DHUL_HIJJAH = 12

    fun parse(key: String): HijriMonthId? {
      val parts = key.split('-')
      if (parts.size != 2) return null
      val year = parts[0].toIntOrNull() ?: return null
      val month = parts[1].toIntOrNull()?.takeIf { it in 1..12 } ?: return null
      return HijriMonthId(year, month)
    }
  }
}

/** Whose announcement a country follows, and the Aladhan calendar that carries it. */
enum class Authority(val aladhanMethod: String) {
  /** Saudi Arabia's High Judiciary Council - Umm al-Qura, corrected to its sightings. */
  SAUDI("HJCoSA"),

  /** Turkey's Diyanet, which fixes its calendar in advance. */
  TURKEY("DIYANET")
}

/** How a country starts its months. */
enum class SightingPolicy(val authority: Authority) {
  /** Saudi Arabia's announced date, as it is. */
  FOLLOW_SAUDI(Authority.SAUDI),

  /** Diyanet's calendar. */
  FOLLOW_TURKEY(Authority.TURKEY),

  /** Its own sighting: Saudi Arabia's date, then the moon at the user's location decides. */
  LOCAL_SIGHTING(Authority.SAUDI)
}

/** Where a month's start came from - and so how sure the app can say it is. */
enum class MonthStartSource {
  /** Set by the user in Settings, from their country's announcement. */
  USER,

  /** The announced date of the authority this country follows. */
  ANNOUNCED,

  /** The authority's date and the moon at the user's location: a best estimate. */
  MOON_ESTIMATE,

  /** Umm al-Qura, with nothing announced or cached. */
  CALCULATED
}

data class MonthStart(
  val id: HijriMonthId,
  val startsOn: LocalDate,
  val source: MonthStartSource,
  val authority: Authority,
  /** The authority's own date, when it was fetched - shown alongside an estimate. */
  val authorityStart: LocalDate?
)

/** A Gregorian date placed in the month table. */
data class HijriDay(
  val day: Int,
  val month: Int,
  val year: Int,
  val monthStart: MonthStart,
  /** 29 or 30 once the next month's start is in the table, else null. */
  val monthLength: Int?
) {
  val id: HijriMonthId get() = monthStart.id
}

/** Consecutive month starts. Every month in it has 29 or 30 days; dates outside it resolve to null. */
class HijriMonthTable(val starts: List<MonthStart>) {

  init {
    starts.zipWithNext().forEach { (a, b) ->
      require(b.id == a.id.next()) { "months not consecutive: ${a.id.key} then ${b.id.key}" }
    }
  }

  fun start(id: HijriMonthId): MonthStart? = starts.firstOrNull { it.id == id }

  fun resolve(date: LocalDate): HijriDay? {
    val index = starts.indexOfLast { !it.startsOn.isAfter(date) }
    if (index < 0) return null
    val start = starts[index]
    val day = ChronoUnit.DAYS.between(start.startsOn, date).toInt() + 1
    val next = starts.getOrNull(index + 1)
    val length = next?.let { ChronoUnit.DAYS.between(start.startsOn, it.startsOn).toInt() }
    // The last month's end is not known; past day 30 is outside the table.
    if (length == null && day > 30) return null
    return HijriDay(day, start.id.month, start.id.year, start, length)
  }

  /** Changes whenever any start or source changes - part of the alarm signature. */
  val signature: String
    get() = starts.joinToString(",") { "${it.id.key}=${it.startsOn}/${it.source.name[0]}" }
}

/**
 * Works out when each month starts, best source first:
 *
 * 1. the user's own correction (Settings - "Moon sighting announced");
 * 2. for a country that follows Saudi Arabia or Turkey, that authority's announced date;
 * 3. for a country that sights the moon itself, Saudi Arabia's date moved by the moon at the user's
 *    location (see [localEstimate]);
 * 4. with nothing fetched, Umm al-Qura, moved by the moon the same way.
 *
 * Then every month is held to 29 or 30 days after the one before, which a sighting calendar always
 * is.
 */
class MonthStartResolver(
  private val policy: SightingPolicy,
  /** The authority's announced starts, from Aladhan; may be empty offline. */
  private val announced: Map<HijriMonthId, LocalDate>,
  /** The user's corrections. */
  private val userStarts: Map<HijriMonthId, LocalDate>,
  /** Odeh's V for the new crescent at the user's location on that evening. */
  private val crescentV: (LocalDate) -> Double,
  /** Umm al-Qura's first of the month. */
  private val calculatedStart: (HijriMonthId) -> LocalDate = ::ummAlQuraStart
) {

  fun resolve(first: HijriMonthId, count: Int): HijriMonthTable {
    val starts = mutableListOf<MonthStart>()
    var id = first
    repeat(count) {
      starts += resolveOne(id, starts.lastOrNull()?.startsOn)
      id = id.next()
    }
    return HijriMonthTable(starts)
  }

  private fun resolveOne(id: HijriMonthId, previousStart: LocalDate?): MonthStart {
    val authorityStart = announced[id]
    val base = authorityStart ?: calculatedStart(id)
    val user = userStarts[id]
    val (estimate, source) = when {
      user != null -> user to MonthStartSource.USER
      policy == SightingPolicy.LOCAL_SIGHTING -> localEstimate(base) to MonthStartSource.MOON_ESTIMATE
      authorityStart != null -> authorityStart to MonthStartSource.ANNOUNCED
      else -> base to MonthStartSource.CALCULATED
    }
    if (previousStart == null) {
      return MonthStart(id, estimate, source, policy.authority, authorityStart)
    }
    val earliest = previousStart.plusDays(29)
    val latest = previousStart.plusDays(30)
    val held = when {
      estimate.isBefore(earliest) -> earliest
      estimate.isAfter(latest) -> latest
      else -> estimate
    }
    // A user's date moved by the length rule is no longer theirs.
    val heldSource = if (held != estimate && source == MonthStartSource.USER) {
      if (policy == SightingPolicy.LOCAL_SIGHTING) MonthStartSource.MOON_ESTIMATE else MonthStartSource.CALCULATED
    } else {
      source
    }
    return MonthStart(id, held, heldSource, policy.authority, authorityStart)
  }

  /**
   * A sighting country's month start, from the authority's first day [base].
   *
   * Tested against 35 Bangladesh announcements, 1440-1448 AH (BangladeshHistoryTest): Bangladesh
   * began a day after Saudi Arabia in 34, and two days after in one (Rabi' al-Awwal 1447), when
   * the crescent was still too thin to see in Dhaka the evening after Saudi Arabia's first. It
   * never began the same day, even on an evening the criterion called just visible (Sha'ban 1446,
   * V = 6.4) - so the same day needs a crescent plainly visible the evening before.
   */
  fun localEstimate(base: LocalDate): LocalDate = when {
    crescentV(base.minusDays(1)) >= SAME_DAY_V -> base
    crescentV(base) >= NAKED_EYE_V -> base.plusDays(1)
    else -> base.plusDays(2)
  }

  companion object {
    /** Odeh's naked-eye threshold. */
    const val NAKED_EYE_V = 5.65

    /** Well inside the naked-eye zone: a crescent no one could miss in a clear sky. */
    const val SAME_DAY_V = 8.0

    fun ummAlQuraStart(id: HijriMonthId): LocalDate =
      LocalDate.from(HijrahDate.of(id.year, id.month, 1))

    fun ummAlQuraMonth(date: LocalDate): HijriMonthId {
      val h = HijrahDate.from(date)
      return HijriMonthId(
        h.get(java.time.temporal.ChronoField.YEAR),
        h.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
      )
    }
  }
}

/** Which countries follow whom. Stored countries are ISO codes or English names, so both. */
object HijriCountryRules {

  private val LOCAL_SIGHTING = setOf(
    "BD", "BANGLADESH",
    "IN", "INDIA",
    "PK", "PAKISTAN",
    "NP", "NEPAL",
    "LK", "SRI LANKA",
    "MA", "MOROCCO"
  )

  private val TURKEY = setOf("TR", "TURKEY", "TÜRKIYE", "TURKIYE")

  /** Everywhere else follows Saudi Arabia's announcement, as the app's 0-day default did. */
  fun policyFor(country: String?): SightingPolicy {
    val key = country?.trim()?.uppercase() ?: return SightingPolicy.FOLLOW_SAUDI
    return when (key) {
      in LOCAL_SIGHTING -> SightingPolicy.LOCAL_SIGHTING
      in TURKEY -> SightingPolicy.FOLLOW_TURKEY
      else -> SightingPolicy.FOLLOW_SAUDI
    }
  }
}
