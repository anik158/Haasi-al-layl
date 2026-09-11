package com.example.data.prayer

import com.example.data.localization.AppLanguage

/**
 * IslamicMadhab
 * Represents the four major Sunni jurisprudence schools (Madhabs):
 * 1. Shafi'i (Default) - Standard Asr time (shadow factor = 1.0)
 * 2. Hanafi - Later Asr time (shadow factor = 2.0)
 * 3. Maliki - Standard Asr time (shadow factor = 1.0)
 * 4. Hanbali - Standard Asr time (shadow factor = 1.0)
 */
enum class IslamicMadhab(
  val id: String,
  val englishName: String,
  val arabicName: String,
  val bengaliName: String,
  val spanishName: String,
  val portugueseName: String,
  val shadowFactor: Double,
  val descriptionEn: String,
  val descriptionBn: String,
  val descriptionAr: String,
  val descriptionEs: String,
  val descriptionPt: String,
  val isDefault: Boolean = false
) {
  SHAFI(
    id = "shafi",
    englishName = "Imam Shafi'i",
    arabicName = "الإمام الشافعي",
    bengaliName = "ইমাম শাফেয়ী",
    spanishName = "Imán Shafi'i",
    portugueseName = "Imame Shafi'i",
    shadowFactor = 1.0,
    descriptionEn = "Standard Asr time when shadow equals object length (1x). Default madhab.",
    descriptionBn = "আসর ওয়াক্ত শুরু যখন ছায়া বস্তুর সমান (১ গুণ)। ডিফল্ট মাজহাব।",
    descriptionAr = "يبدأ وقت العصر إذا صار ظل كل شيء مثله (1x). المذهب الافتراضي.",
    descriptionEs = "Hora de Asr estándar cuando la sombra es igual a la longitud del objeto (1x). Por defecto.",
    descriptionPt = "Horário de Asr padrão quando a sombra é igual ao comprimento do objeto (1x). Padrão.",
    isDefault = true
  ),
  HANAFI(
    id = "hanafi",
    englishName = "Imam Abu Hanifa",
    arabicName = "الإمام أبو حنيفة",
    bengaliName = "ইমাম আবু হানিফা",
    spanishName = "Imán Abu Hanifa",
    portugueseName = "Imame Abu Hanifa",
    shadowFactor = 2.0,
    descriptionEn = "Later Asr time when shadow equals twice the object length (2x).",
    descriptionBn = "আসর ওয়াক্ত শুরু যখন ছায়া বস্তুর দ্বিগুণ (২ গুণ)।",
    descriptionAr = "يبدأ وقت العصر إذا صار ظل الشيء مثليه (2x).",
    descriptionEs = "Hora de Asr más tardía cuando la sombra duplica la longitud del objeto (2x).",
    descriptionPt = "Horário de Asr mais tardio quando a sombra é o dobro do objeto (2x)."
  ),
  MALIKI(
    id = "maliki",
    englishName = "Imam Malik",
    arabicName = "الإمام مالك",
    bengaliName = "ইমাম মালিক",
    spanishName = "Imán Malik",
    portugueseName = "Imame Malik",
    shadowFactor = 1.0,
    descriptionEn = "Standard Asr time when shadow equals object length (1x).",
    descriptionBn = "আসর ওয়াক্ত শুরু যখন ছায়া বস্তুর সমান (১ গুণ)।",
    descriptionAr = "يبدأ وقت العصر إذا صار ظل كل شيء مثله (1x).",
    descriptionEs = "Hora de Asr estándar cuando la sombra es igual a la longitud del objeto (1x).",
    descriptionPt = "Horário de Asr padrão quando a sombra é igual ao comprimento do objeto (1x)."
  ),
  HANBALI(
    id = "hanbali",
    englishName = "Imam Ahmad ibn Hanbal",
    arabicName = "الإمام أحمد بن حنبل",
    bengaliName = "ইমাম আহমদ ইবনে হাম্বল",
    spanishName = "Imán Ahmad ibn Hanbal",
    portugueseName = "Imame Ahmad ibn Hanbal",
    shadowFactor = 1.0,
    descriptionEn = "Standard Asr time when shadow equals object length (1x).",
    descriptionBn = "আসর ওয়াক্ত শুরু যখন ছায়া বস্তুর সমান (১ গুণ)।",
    descriptionAr = "يبدأ وقت العصر إذا صار ظل كل شيء مثله (1x).",
    descriptionEs = "Hora de Asr estándar cuando la sombra es igual a la longitud del objeto (1x).",
    descriptionPt = "Horário de Asr padrão quando a sombra é igual ao comprimento do objeto (1x)."
  );

  fun getLocalizedName(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> arabicName
    AppLanguage.BENGALI -> bengaliName
    AppLanguage.SPANISH -> spanishName
    AppLanguage.PORTUGUESE -> portugueseName
    AppLanguage.ENGLISH -> englishName
  }

  fun getLocalizedDescription(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> descriptionAr
    AppLanguage.BENGALI -> descriptionBn
    AppLanguage.SPANISH -> descriptionEs
    AppLanguage.PORTUGUESE -> descriptionPt
    AppLanguage.ENGLISH -> descriptionEn
  }
}
