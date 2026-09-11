import re
with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "r") as f:
    text = f.read()

# Add a closing brace after lines starting with AppLanguage.ENGLISH -> "..." IF they don't have one
lines = text.splitlines()
new_lines = []
for line in lines:
    new_lines.append(line)
    if line.strip().startswith("AppLanguage.ENGLISH -> "):
        new_lines.append("  }")

text = "\n".join(new_lines) + "\n"

# Fix top
text = re.sub(
    r"enum class AppLanguage.*?object LocalizationManager \{",
    """enum class AppLanguage(val code: String, val displayName: String, val nativeName: String, val flag: String) {
  ENGLISH("en", "English", "English", "🌐"),
  ARABIC("ar", "Arabic", "العربية", "🇸🇦"),
  BENGALI("bn", "Bengali", "বাংলা", "🇧🇩"),
  SPANISH("es", "Spanish", "Español", "🇪🇸"),
  PORTUGUESE("pt", "Portuguese", "Português", "🇵🇹");

  val englishName: String get() = displayName

  companion object {
    fun fromCode(code: String): AppLanguage {
      return entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
    }
  }
}

object LocalizationManager {""",
    text,
    flags=re.DOTALL
)

# Fix functions
text = re.sub(
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
    text,
    flags=re.DOTALL
)

# Fix Isha
text = re.sub(
    r'AppLanguage.ENGLISH -> "Isha".*?(?=\n\s*// Celestial Horizon)',
    'AppLanguage.ENGLISH -> "Isha"\n    }\n  }',
    text,
    flags=re.DOTALL
)

# Fix Dark Moon
text = re.sub(
    r'AppLanguage.ENGLISH -> "Dark Moon \(Mahq\)".*?(?=\n\s*fun getIlluminationLabel)',
    'AppLanguage.ENGLISH -> "Dark Moon (Mahq)"\n    }\n  }',
    text,
    flags=re.DOTALL
)

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "w") as f:
    f.write(text)
