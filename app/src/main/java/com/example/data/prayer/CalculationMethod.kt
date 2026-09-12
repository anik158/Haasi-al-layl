package com.example.data.prayer

import com.example.data.localization.AppLanguage

/**
 * How Fajr and Isha are derived.
 *
 * There is no single universally agreed convention: both depend on how far below the horizon the
 * sun must sit, and that angle was never settled by consensus. Umm al-Qura is the standard Saudi
 * Arabia publishes for Makkah and Madinah, and is the default here; it sets Isha by a fixed
 * interval after Maghrib rather than by an angle.
 *
 * Names are carried on the enum rather than in LocalizationManager because they belong to the
 * entry, exactly as with [IslamicMadhab]. Proper nouns stay transliterated, never translated.
 */
enum class CalculationMethod(
  val title: String,
  /** Short label for chips and pickers, where [title] is too long to read. */
  val shortTitle: String,
  val titleAr: String,
  val titleBn: String,
  val titleEs: String,
  val titlePt: String,
  val shortTitleAr: String,
  val shortTitleBn: String,
  val shortTitleEs: String,
  val shortTitlePt: String,
  val fajrAngle: Double,
  val ishaAngle: Double,
  /** When set, Isha is this many minutes after Maghrib instead of a twilight angle. */
  val ishaIntervalMinutes: Int? = null
) {
  UMM_AL_QURA(
    title = "Umm al-Qura University, Makkah",
    shortTitle = "Makkah",
    titleAr = "جامعة أم القرى، مكة المكرمة",
    titleBn = "উম্মুল কুরা বিশ্ববিদ্যালয়, মক্কা",
    titleEs = "Universidad Umm al-Qura, La Meca",
    titlePt = "Universidade Umm al-Qura, Meca",
    shortTitleAr = "مكة",
    shortTitleBn = "মক্কা",
    shortTitleEs = "La Meca",
    shortTitlePt = "Meca",
    fajrAngle = 18.5,
    ishaAngle = 0.0,
    ishaIntervalMinutes = 90
  ),
  MUSLIM_WORLD_LEAGUE(
    title = "Muslim World League",
    shortTitle = "World League",
    titleAr = "رابطة العالم الإسلامي",
    titleBn = "মুসলিম ওয়ার্ল্ড লীগ",
    titleEs = "Liga Mundial Islámica",
    titlePt = "Liga Mundial Muçulmana",
    shortTitleAr = "رابطة العالم",
    shortTitleBn = "ওয়ার্ল্ড লীগ",
    shortTitleEs = "Liga Mundial",
    shortTitlePt = "Liga Mundial",
    fajrAngle = 18.0,
    ishaAngle = 17.0
  ),
  ISNA(
    title = "Islamic Society of North America (ISNA)",
    shortTitle = "ISNA",
    titleAr = "الجمعية الإسلامية لأمريكا الشمالية (ISNA)",
    titleBn = "ইসলামিক সোসাইটি অব নর্থ আমেরিকা (ISNA)",
    titleEs = "Sociedad Islámica de Norteamérica (ISNA)",
    titlePt = "Sociedade Islâmica da América do Norte (ISNA)",
    shortTitleAr = "ISNA",
    shortTitleBn = "ISNA",
    shortTitleEs = "ISNA",
    shortTitlePt = "ISNA",
    fajrAngle = 15.0,
    ishaAngle = 15.0
  ),
  EGYPTIAN(
    title = "Egyptian General Authority",
    shortTitle = "Egyptian",
    titleAr = "الهيئة المصرية العامة للمساحة",
    titleBn = "মিশরীয় জেনারেল অথরিটি",
    titleEs = "Autoridad General Egipcia",
    titlePt = "Autoridade Geral Egípcia",
    shortTitleAr = "المصرية",
    shortTitleBn = "মিশরীয়",
    shortTitleEs = "Egipcia",
    shortTitlePt = "Egípcia",
    fajrAngle = 19.5,
    ishaAngle = 17.5
  ),
  KARACHI(
    title = "University of Islamic Sciences, Karachi",
    shortTitle = "Karachi",
    titleAr = "جامعة العلوم الإسلامية، كراتشي",
    titleBn = "ইউনিভার্সিটি অব ইসলামিক সায়েন্সেস, করাচি",
    titleEs = "Universidad de Ciencias Islámicas, Karachi",
    titlePt = "Universidade de Ciências Islâmicas, Carachi",
    shortTitleAr = "كراتشي",
    shortTitleBn = "করাচি",
    shortTitleEs = "Karachi",
    shortTitlePt = "Carachi",
    fajrAngle = 18.0,
    ishaAngle = 18.0
  ),
  DUBAI(
    title = "Dubai Religious Affairs",
    shortTitle = "Dubai",
    titleAr = "دائرة الشؤون الإسلامية، دبي",
    titleBn = "দুবাই ধর্মবিষয়ক কর্তৃপক্ষ",
    titleEs = "Asuntos Religiosos de Dubái",
    titlePt = "Assuntos Religiosos do Dubai",
    shortTitleAr = "دبي",
    shortTitleBn = "দুবাই",
    shortTitleEs = "Dubái",
    shortTitlePt = "Dubai",
    fajrAngle = 18.2,
    ishaAngle = 18.2
  );

  fun getLocalizedName(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> titleAr
    AppLanguage.BENGALI -> titleBn
    AppLanguage.SPANISH -> titleEs
    AppLanguage.PORTUGUESE -> titlePt
    AppLanguage.ENGLISH -> title
  }

  fun getLocalizedShortName(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> shortTitleAr
    AppLanguage.BENGALI -> shortTitleBn
    AppLanguage.SPANISH -> shortTitleEs
    AppLanguage.PORTUGUESE -> shortTitlePt
    AppLanguage.ENGLISH -> shortTitle
  }
}
