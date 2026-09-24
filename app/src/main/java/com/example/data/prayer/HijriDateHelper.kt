package com.example.data.prayer

import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object HijriDateHelper {

  private val HIJRI_MONTH_NAMES = mapOf(
    AppLanguage.ENGLISH to listOf(
      "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
      "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
      "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    ),
    AppLanguage.ARABIC to listOf(
      "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
      "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
      "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    ),
    AppLanguage.BENGALI to listOf(
      "মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি",
      "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শাবান",
      "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
    ),
    AppLanguage.SPANISH to listOf(
      "Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani",
      "Yumada al-Ula", "Yumada al-Ajira", "Rayab", "Shaabán",
      "Ramadán", "Shawwal", "Dhu al-Qada", "Dhu al-Hiyya"
    ),
    AppLanguage.PORTUGUESE to listOf(
      "Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani",
      "Jumada al-Ula", "Jumada al-Akhira", "Rajab", "Shaban",
      "Ramadão", "Shawwal", "Dhu al-Qida", "Dhu al-Hijja"
    )
  )

  /** The era marker after the year: "AH" in English, "هـ" in Arabic. */
  private fun eraLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "هـ"
    AppLanguage.BENGALI -> "হিজরি"
    AppLanguage.SPANISH -> "H."
    AppLanguage.PORTUGUESE -> "H."
    AppLanguage.ENGLISH -> "AH"
  }

  /** Day, month (1-12) and year of [date] in the Hijri calendar. */
  private fun hijriParts(date: LocalDate): Triple<Int, Int, Int> = try {
    val hijrahDate = HijrahDate.from(date)
    Triple(
      hijrahDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH),
      hijrahDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR),
      hijrahDate.get(java.time.temporal.ChronoField.YEAR)
    )
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
    Triple(d.toInt(), m, y)
  }

  /** "12 Rabi' al-Thani 1448 AH", in [language] - month names, era and digits. */
  fun getTodayHijriFormatted(
    date: LocalDate = LocalDate.now(),
    language: AppLanguage = AppLanguage.ENGLISH
  ): String {
    val (day, month, year) = hijriParts(date)
    val names = HIJRI_MONTH_NAMES[language] ?: HIJRI_MONTH_NAMES.getValue(AppLanguage.ENGLISH)
    val monthName = names.getOrElse(month - 1) { names[0] }
    return LocalizationManager.localizeDigits("$day $monthName $year ${eraLabel(language)}", language)
  }

  /**
   * True during Ramadan. Umm al-Qura stretches the Isha interval from 90 to 120 minutes for the
   * month, so the calculator needs to know.
   */
  fun isRamadan(date: LocalDate = LocalDate.now()): Boolean {
    return try {
      HijrahDate.from(date).get(java.time.temporal.ChronoField.MONTH_OF_YEAR) == 9
    } catch (_: Exception) {
      false
    }
  }

  fun getHijriDayOfMonth(date: LocalDate = LocalDate.now()): Int {
    return try {
      val hijrahDate = HijrahDate.from(date)
      hijrahDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
    } catch (_: Exception) {
      val epochDay = date.toEpochDay()
      val l = epochDay - 1948439 + 2400001
      val n = ((l - 1) / 10631).toInt()
      val l2 = l - 10631 * n + 354
      val j = ((10985 - l2) / 5316).toInt() * ((50 * l2) / 17719).toInt() +
        (l2 / 5670).toInt() * ((43 * l2) / 15238).toInt()
      val l3 = l2 - ((30 - j) / 15).toInt() * ((17719 * j) / 50).toInt() -
        (j / 16).toInt() * ((15238 * j) / 43).toInt() + 29
      val m = ((24 * l3) / 709).toInt()
      (l3 - ((709 * m) / 24)).toInt().coerceIn(1, 30)
    }
  }

  /**
   * The Gregorian date in the *app's* language. It used the device locale, so a phone set to
   * English showed an English date inside an app switched to Bengali.
   */
  fun getGregorianFormatted(
    date: LocalDate = LocalDate.now(),
    language: AppLanguage = AppLanguage.ENGLISH
  ): String {
    val (locale, pattern) = when (language) {
      AppLanguage.ENGLISH -> Locale.ENGLISH to "EEEE, MMMM d, yyyy"
      AppLanguage.ARABIC -> Locale("ar") to "EEEE، d MMMM yyyy"
      AppLanguage.BENGALI -> Locale("bn") to "EEEE, d MMMM yyyy"
      AppLanguage.SPANISH -> Locale("es") to "EEEE, d 'de' MMMM 'de' yyyy"
      AppLanguage.PORTUGUESE -> Locale("pt") to "EEEE, d 'de' MMMM 'de' yyyy"
    }
    return LocalizationManager.localizeDigits(
      date.format(DateTimeFormatter.ofPattern(pattern, locale)),
      language
    )
  }
}
