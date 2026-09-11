package com.example.data.prayer

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object HijriDateHelper {

  private val HIJRI_MONTH_NAMES = arrayOf(
    "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
    "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
    "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
  )

  fun getTodayHijriFormatted(date: LocalDate = LocalDate.now()): String {
    return try {
      val hijrahDate = HijrahDate.from(date)
      val monthIndex = hijrahDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR) - 1
      val day = hijrahDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
      val year = hijrahDate.get(java.time.temporal.ChronoField.YEAR)
      val monthName = HIJRI_MONTH_NAMES.getOrElse(monthIndex) { "Islamic Month" }
      "$day $monthName $year AH"
    } catch (_: Exception) {
      // Fallback calculation if HijrahChronology is unavailable on older runtimes
      val epochDay = date.toEpochDay()
      // Approximate Islamic lunar calendar calculation (354.367 days per year)
      val l = epochDay - 1948439 + 2400001
      val n = ((l - 1) / 10631).toInt()
      val l2 = l - 10631 * n + 354
      val j = ((10985 - l2) / 5316).toInt() * ((50 * l2) / 17719).toInt() +
        (l2 / 5670).toInt() * ((43 * l2) / 15238).toInt()
      val l3 = l2 - ((30 - j) / 15).toInt() * ((17719 * j) / 50).toInt() -
        (j / 16).toInt() * ((15238 * j) / 43).toInt() + 29
      val m = ((24 * l3) / 709).toInt()
      val d = l3 - ((709 * m) / 24).toInt()
      val y = 30 * n + j - 30
      val monthName = HIJRI_MONTH_NAMES.getOrElse(m - 1) { "Rabi' al-Awwal" }
      "$d $monthName $y AH"
    }
  }

  fun getGregorianFormatted(date: LocalDate = LocalDate.now()): String {
    return date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault()))
  }
}
