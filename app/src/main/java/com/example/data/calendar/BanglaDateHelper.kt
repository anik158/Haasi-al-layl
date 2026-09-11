package com.example.data.calendar

import java.time.LocalDate
import java.time.Year

/**
 * Helper to convert Gregorian calendar dates to the Bangladeshi Bengali Calendar
 * (standardized by the Bangla Academy in Bangladesh).
 */
object BanglaDateHelper {

  data class BanglaDate(
    val day: Int,
    val monthNameBn: String,
    val monthNameEn: String,
    val year: Int,
    val seasonBn: String,
    val seasonEn: String,
    val dayOfWeekBn: String
  ) {
    val formattedBn: String
      get() = "${toBanglaDigits(day)} $monthNameBn ${toBanglaDigits(year)} বঙ্গাব্দ ($seasonBn)"

    val formattedEn: String
      get() = "$day $monthNameEn $year BS ($seasonEn)"
  }

  private val MONTH_NAMES_BN = arrayOf(
    "বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
    "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"
  )

  private val MONTH_NAMES_EN = arrayOf(
    "Boishakh", "Joishtho", "Ashar", "Srabon", "Bhadro", "Ashwin",
    "Kartik", "Ogrohayon", "Poush", "Magh", "Falgun", "Choitro"
  )

  private val SEASONS_BN = arrayOf(
    "গ্রীষ্ম", "বর্ষা", "শরৎ", "হেমন্ত", "শীত", "বসন্ত"
  )

  private val SEASONS_EN = arrayOf(
    "Summer", "Monsoon", "Autumn", "Late Autumn", "Winter", "Spring"
  )

  private val DAYS_OF_WEEK_BN = mapOf(
    java.time.DayOfWeek.MONDAY to "সোমবার",
    java.time.DayOfWeek.TUESDAY to "মঙ্গলবার",
    java.time.DayOfWeek.WEDNESDAY to "বুধবার",
    java.time.DayOfWeek.THURSDAY to "বৃহস্পতিবার",
    java.time.DayOfWeek.FRIDAY to "শুক্রবার",
    java.time.DayOfWeek.SATURDAY to "শনিবার",
    java.time.DayOfWeek.SUNDAY to "রবিবার"
  )

  fun getBanglaDate(date: LocalDate = LocalDate.now()): BanglaDate {
    val gregYear = date.year
    val isLeap = Year.isLeap(gregYear.toLong())

    // Month days in standard Bangladesh revised Bengali calendar:
    // Boishakh to Ashwin: 31 days each (first 6 months)
    // Kartik to Magh: 30 days each (4 months)
    // Falgun: 29 days (30 in leap year)
    // Choitro: 30 days
    val falgunDays = if (isLeap) 30 else 29
    val monthLengths = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, falgunDays, 30)

    // Bengali new year begins April 14
    val pohelaBoishakh = LocalDate.of(gregYear, 4, 14)

    val banglaYear: Int
    val daysFromNewYear: Long

    if (!date.isBefore(pohelaBoishakh)) {
      banglaYear = gregYear - 593
      daysFromNewYear = java.time.temporal.ChronoUnit.DAYS.between(pohelaBoishakh, date)
    } else {
      banglaYear = gregYear - 594
      val prevPohelaBoishakh = LocalDate.of(gregYear - 1, 4, 14)
      daysFromNewYear = java.time.temporal.ChronoUnit.DAYS.between(prevPohelaBoishakh, date)
    }

    var remainingDays = daysFromNewYear.toInt()
    var banglaMonthIndex = 0

    while (banglaMonthIndex < 12 && remainingDays >= monthLengths[banglaMonthIndex]) {
      remainingDays -= monthLengths[banglaMonthIndex]
      banglaMonthIndex++
    }

    // Safety guard
    if (banglaMonthIndex >= 12) {
      banglaMonthIndex = 11
    }

    val banglaDay = remainingDays + 1
    val seasonIndex = (banglaMonthIndex / 2).coerceIn(0, 5)

    return BanglaDate(
      day = banglaDay,
      monthNameBn = MONTH_NAMES_BN[banglaMonthIndex],
      monthNameEn = MONTH_NAMES_EN[banglaMonthIndex],
      year = banglaYear,
      seasonBn = SEASONS_BN[seasonIndex],
      seasonEn = SEASONS_EN[seasonIndex],
      dayOfWeekBn = DAYS_OF_WEEK_BN[date.dayOfWeek] ?: "আজ"
    )
  }

  fun toBanglaDigits(number: Int): String {
    val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    return number.toString().map { char ->
      if (char in '0'..'9') banglaDigits[char - '0'] else char
    }.joinToString("")
  }

  fun getTodayBanglaFormatted(date: LocalDate = LocalDate.now()): String {
    return getBanglaDate(date).formattedBn
  }
}
