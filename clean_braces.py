import re

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "r") as f:
    text = f.read()

# Replace any sequence of multiple isolated braces with proper formatting
# But wait, it's easier to just fix the specific nested functions.
# The ONLY nested functions are `getPrayerDisplayName` and `getMoonPhaseName`.

# Let's fix getPrayerDisplayName manually by replacing the entire function!
prayer_display_name = """  fun getPrayerDisplayName(prayer: PrayerName, language: AppLanguage): String = when (prayer) {
    PrayerName.IMSAK -> when (language) {
      AppLanguage.ARABIC -> "الإمساك"
      AppLanguage.BENGALI -> "ইমসাক"
      AppLanguage.SPANISH -> "Imsak"
      AppLanguage.PORTUGUESE -> "Imsak"
      AppLanguage.ENGLISH -> "Imsak"
    }
    PrayerName.FAJR -> when (language) {
      AppLanguage.ARABIC -> "الفجر"
      AppLanguage.BENGALI -> "ফজর"
      AppLanguage.SPANISH -> "Fayer"
      AppLanguage.PORTUGUESE -> "Fajr"
      AppLanguage.ENGLISH -> "Fajr"
    }
    PrayerName.SUNRISE -> when (language) {
      AppLanguage.ARABIC -> "الشروق"
      AppLanguage.BENGALI -> "সূর্যোদয়"
      AppLanguage.SPANISH -> "Salida del Sol"
      AppLanguage.PORTUGUESE -> "Nascer do Sol"
      AppLanguage.ENGLISH -> "Sunrise"
    }
    PrayerName.DHUHR -> when (language) {
      AppLanguage.ARABIC -> "الظهر"
      AppLanguage.BENGALI -> "জোহর"
      AppLanguage.SPANISH -> "Dhuhr"
      AppLanguage.PORTUGUESE -> "Dhuhr"
      AppLanguage.ENGLISH -> "Dhuhr"
    }
    PrayerName.ASR -> when (language) {
      AppLanguage.ARABIC -> "العصر"
      AppLanguage.BENGALI -> "আসর"
      AppLanguage.SPANISH -> "Asr"
      AppLanguage.PORTUGUESE -> "Asr"
      AppLanguage.ENGLISH -> "Asr"
    }
    PrayerName.MAGHRIB -> when (language) {
      AppLanguage.ARABIC -> "المغرب"
      AppLanguage.BENGALI -> "মাগরিব"
      AppLanguage.SPANISH -> "Magrib"
      AppLanguage.PORTUGUESE -> "Maghrib"
      AppLanguage.ENGLISH -> "Maghrib"
    }
    PrayerName.ISHA -> when (language) {
      AppLanguage.ARABIC -> "العشاء"
      AppLanguage.BENGALI -> "এশা"
      AppLanguage.SPANISH -> "Isha"
      AppLanguage.PORTUGUESE -> "Isha"
      AppLanguage.ENGLISH -> "Isha"
    }
  }"""

moon_phase_name = """  fun getMoonPhaseName(phaseKey: String, language: AppLanguage): String = when (phaseKey) {
    "NEW_MOON" -> when (language) {
      AppLanguage.ARABIC -> "محاق"
      AppLanguage.BENGALI -> "অমাবস্যা"
      AppLanguage.SPANISH -> "Luna Nueva"
      AppLanguage.PORTUGUESE -> "Lua Nova"
      AppLanguage.ENGLISH -> "New Moon"
    }
    "WAXING_CRESCENT" -> when (language) {
      AppLanguage.ARABIC -> "هلال متزايد"
      AppLanguage.BENGALI -> "শুক্লপক্ষের প্রথমাংশ"
      AppLanguage.SPANISH -> "Luna Creciente"
      AppLanguage.PORTUGUESE -> "Lua Crescente"
      AppLanguage.ENGLISH -> "Waxing Crescent"
    }
    "FIRST_QUARTER" -> when (language) {
      AppLanguage.ARABIC -> "تربيع أول"
      AppLanguage.BENGALI -> "প্রথম চতুর্থাংশ"
      AppLanguage.SPANISH -> "Cuarto Creciente"
      AppLanguage.PORTUGUESE -> "Quarto Crescente"
      AppLanguage.ENGLISH -> "First Quarter"
    }
    "WAXING_GIBBOUS" -> when (language) {
      AppLanguage.ARABIC -> "أحدب متزايد"
      AppLanguage.BENGALI -> "শুক্লপক্ষের দ্বিতীয়াংশ"
      AppLanguage.SPANISH -> "Luna Gibosa Creciente"
      AppLanguage.PORTUGUESE -> "Lua Gibosa Crescente"
      AppLanguage.ENGLISH -> "Waxing Gibbous"
    }
    "FULL_MOON" -> when (language) {
      AppLanguage.ARABIC -> "بدر"
      AppLanguage.BENGALI -> "পূর্ণিমা"
      AppLanguage.SPANISH -> "Luna Llena"
      AppLanguage.PORTUGUESE -> "Lua Cheia"
      AppLanguage.ENGLISH -> "Full Moon"
    }
    "WANING_GIBBOUS" -> when (language) {
      AppLanguage.ARABIC -> "أحدب متناقص"
      AppLanguage.BENGALI -> "কৃষ্ণপক্ষের প্রথমাংশ"
      AppLanguage.SPANISH -> "Luna Gibosa Menguante"
      AppLanguage.PORTUGUESE -> "Lua Gibosa Minguante"
      AppLanguage.ENGLISH -> "Waning Gibbous"
    }
    "LAST_QUARTER" -> when (language) {
      AppLanguage.ARABIC -> "تربيع أخير"
      AppLanguage.BENGALI -> "শেষ চতুর্থাংশ"
      AppLanguage.SPANISH -> "Cuarto Menguante"
      AppLanguage.PORTUGUESE -> "Quarto Minguante"
      AppLanguage.ENGLISH -> "Last Quarter"
    }
    "WANING_CRESCENT" -> when (language) {
      AppLanguage.ARABIC -> "هلال متناقص"
      AppLanguage.BENGALI -> "কৃষ্ণপক্ষের দ্বিতীয়াংশ"
      AppLanguage.SPANISH -> "Luna Menguante"
      AppLanguage.PORTUGUESE -> "Lua Minguante"
      AppLanguage.ENGLISH -> "Waning Crescent"
    }
    else -> when (language) {
      AppLanguage.ARABIC -> "محاق"
      AppLanguage.BENGALI -> "অমাবস্যা (মাহক)"
      AppLanguage.SPANISH -> "Luna Oscura (Mahq)"
      AppLanguage.PORTUGUESE -> "Lua Escura (Mahq)"
      AppLanguage.ENGLISH -> "Dark Moon (Mahq)"
    }
  }"""

text = re.sub(r'  fun getPrayerDisplayName.*?  // Celestial Horizon strings', prayer_display_name + '\n\n  // Celestial Horizon strings', text, flags=re.DOTALL)
text = re.sub(r'  fun getMoonPhaseName.*?  fun getIlluminationLabel', moon_phase_name + '\n\n  fun getIlluminationLabel', text, flags=re.DOTALL)

# For all other standard functions, they should NOT have duplicate braces.
# We can fix them by replacing any instance of:
#      AppLanguage.ENGLISH -> "..."
#   }
#   }
# with a single brace.
text = re.sub(r'(AppLanguage\.ENGLISH -> "[^"]*"\n)\s*\}\n\s*\}', r'\1  }', text)

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "w") as f:
    f.write(text)
