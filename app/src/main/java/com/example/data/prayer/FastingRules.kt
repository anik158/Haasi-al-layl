package com.example.data.prayer

import java.time.DayOfWeek
import java.time.LocalDate

/** How the sunnah regards fasting on a given day, most binding first. */
enum class FastDayKind { OBLIGATORY, PROHIBITED, DISCOURAGED, RECOMMENDED }

/**
 * One reason a day is special for fasting. [key] names it for LocalizationManager; [reference] is
 * where it comes from, with the grade where it is below sahih.
 */
data class FastDayRule(val kind: FastDayKind, val key: String, val reference: String) {
  /** True when the rule depends on the Hijri date, which is calculated and can be a day out. */
  val dependsOnHijriDate: Boolean get() = key !in WEEKDAY_KEYS

  private companion object {
    val WEEKDAY_KEYS = setOf("monday", "thursday", "friday_alone")
  }
}

/**
 * Which days the sunnah forbids, discourages or recommends for fasting. Every reference was
 * checked on sunnah.com; the hasan ones say so.
 *
 * - Forbidden: the two Eids (Bukhari 1990, 1991; Muslim 1138) and the days of Tashreeq, 11-13
 *   Dhul-Hijjah - "days of eating and drinking" (Muslim 1141).
 * - Discouraged: a day or two before Ramadan (Bukhari 1914); Friday on its own (Bukhari 1985).
 * - Recommended: Monday, the day of Arafah and Ashura (Muslim 1162), the 9th of Muharram
 *   (Muslim 1134), six days of Shawwal (Muslim 1164); Thursday (Tirmidhi 747) and the 13th-15th
 *   (Tirmidhi 761) are hasan.
 *
 * Hijri-dated rules use the app's calculated Hijri date, which follows Umm al-Qura plus the
 * country adjustment and can differ by a day from a local moon sighting - [FastDayRule.dependsOnHijriDate]
 * lets the UI say so.
 */
object FastingRules {

  private const val MUHARRAM = 1
  private const val SHABAN = 8
  private const val RAMADAN = 9
  private const val SHAWWAL = 10
  private const val DHUL_HIJJAH = 12

  /** All rules for [date], most binding first. Empty for an ordinary day. */
  fun rulesFor(date: LocalDate): List<FastDayRule> {
    val month = HijriDateHelper.getHijriMonth(date)
    val day = HijriDateHelper.getHijriDayOfMonth(date)
    return rulesFor(month, day, date.dayOfWeek)
  }

  /** The same, from the parts - pure, for tests. */
  fun rulesFor(hijriMonth: Int, hijriDay: Int, weekday: DayOfWeek): List<FastDayRule> {
    if (hijriMonth == RAMADAN) {
      return listOf(FastDayRule(FastDayKind.OBLIGATORY, "ramadan", "Qur'an 2:183-185"))
    }

    // Forbidden days end the question: nothing else applies on them.
    when {
      hijriMonth == SHAWWAL && hijriDay == 1 ->
        return listOf(FastDayRule(FastDayKind.PROHIBITED, "eid_fitr", "Bukhari 1990, 1991; Muslim 1138"))
      hijriMonth == DHUL_HIJJAH && hijriDay == 10 ->
        return listOf(FastDayRule(FastDayKind.PROHIBITED, "eid_adha", "Bukhari 1990, 1991; Muslim 1138"))
      hijriMonth == DHUL_HIJJAH && hijriDay in 11..13 ->
        return listOf(FastDayRule(FastDayKind.PROHIBITED, "tashreeq", "Muslim 1141"))
    }

    val rules = mutableListOf<FastDayRule>()
    if (hijriMonth == SHABAN && hijriDay >= 29) {
      rules += FastDayRule(FastDayKind.DISCOURAGED, "before_ramadan", "Bukhari 1914")
    }
    if (weekday == DayOfWeek.FRIDAY) {
      rules += FastDayRule(FastDayKind.DISCOURAGED, "friday_alone", "Bukhari 1985")
    }

    if (hijriMonth == DHUL_HIJJAH && hijriDay == 9) {
      rules += FastDayRule(FastDayKind.RECOMMENDED, "arafah", "Muslim 1162")
    }
    if (hijriMonth == MUHARRAM && hijriDay == 10) {
      rules += FastDayRule(FastDayKind.RECOMMENDED, "ashura", "Muslim 1162")
    }
    if (hijriMonth == MUHARRAM && hijriDay == 9) {
      rules += FastDayRule(FastDayKind.RECOMMENDED, "tasua", "Muslim 1134")
    }
    if (hijriMonth == SHAWWAL) {
      rules += FastDayRule(FastDayKind.RECOMMENDED, "six_shawwal", "Muslim 1164")
    }
    if (hijriDay in 13..15) {
      rules += FastDayRule(FastDayKind.RECOMMENDED, "white_days", "Tirmidhi 761 (hasan)")
    }
    if (weekday == DayOfWeek.MONDAY) {
      rules += FastDayRule(FastDayKind.RECOMMENDED, "monday", "Muslim 1162")
    }
    if (weekday == DayOfWeek.THURSDAY) {
      rules += FastDayRule(FastDayKind.RECOMMENDED, "thursday", "Tirmidhi 747 (hasan)")
    }
    return rules.sortedBy { it.kind.ordinal }
  }
}
