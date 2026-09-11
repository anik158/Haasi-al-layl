with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "r") as f:
    content = f.read()

import re
content = re.sub(
    r"fun detectLanguageForCountry.*?fun getNextPrayerLabel",
    """fun detectLanguageForCountry(country: String): AppLanguage {
    val clean = country.trim()
    return when {
      clean.equals("Bangladesh", ignoreCase = true) -> AppLanguage.BENGALI
      ARABIC_COUNTRIES.any { it.equals(clean, ignoreCase = true) } -> AppLanguage.ARABIC
      SPANISH_COUNTRIES.any { it.equals(clean, ignoreCase = true) } -> AppLanguage.SPANISH
      PORTUGUESE_COUNTRIES.any { it.equals(clean, ignoreCase = true) } -> AppLanguage.PORTUGUESE
      else -> AppLanguage.ENGLISH
    }
  }

  fun getAppName(language: AppLanguage): String {
    return when (language) {
      AppLanguage.ARABIC -> "حصي الليل"
      AppLanguage.BENGALI -> "হাসী আল-লাইল"
      else -> "Haasi al-layl"
    }
  }

  fun getAppSubtitle(language: AppLanguage): String {
    return when (language) {
      AppLanguage.ARABIC -> "أراقب إيقاع الليل والنهار الهادئ"
      AppLanguage.BENGALI -> "রাত ও দিনের নীরব ছন্দ পর্যবেক্ষণ"
      AppLanguage.SPANISH -> "El ritmo sereno del día y la noche"
      AppLanguage.PORTUGUESE -> "O ritmo sereno do dia e da noite"
      AppLanguage.ENGLISH -> "I watch the quiet rhythm of night and day"
    }
  }

  // Prayer hero texts
  fun getNextPrayerLabel""",
    content,
    flags=re.DOTALL
)

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "w") as f:
    f.write(content)
