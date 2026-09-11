package com.example.data.localization

import com.example.data.model.PrayerName

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String, val flag: String) {
  ENGLISH("en", "English", "English", "🌐"),
  ARABIC("ar", "Arabic", "العربية", "🇸🇦"),
  BENGALI("bn", "Bengali", "বাংলা", "🇧🇩"),
  SPANISH("es", "Spanish", "Español", "🇪🇸"),
  PORTUGUESE("pt", "Portuguese", "Português", "🇵🇹");

  val englishName: String get() = displayName
}

object LocalizationManager {

  private val ARABIC_COUNTRIES = setOf(
    "Saudi Arabia", "United Arab Emirates", "UAE", "Egypt", "Qatar", "Kuwait",
    "Oman", "Bahrain", "Jordan", "Lebanon", "Iraq", "Palestine", "Yemen",
    "Morocco", "Algeria", "Tunisia", "Libya", "Sudan", "Syria", "Mauritania", "Somalia"
  )

  private val SPANISH_COUNTRIES = setOf(
    "Spain", "Mexico", "Colombia", "Argentina", "Peru", "Venezuela", "Chile",
    "Guatemala", "Ecuador", "Cuba", "Bolivia", "Dominican Republic", "Honduras",
    "Paraguay", "El Salvador", "Nicaragua", "Costa Rica", "Panama", "Puerto Rico",
    "Uruguay", "Equatorial Guinea"
  )

  private val PORTUGUESE_COUNTRIES = setOf(
    "Brazil", "Portugal", "Angola", "Mozambique", "Cape Verde", "Guinea-Bissau",
    "Sao Tome and Principe", "Timor-Leste"
  )

  fun detectLanguageForCountry(country: String): AppLanguage {
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

  // Navigation tab titles
  fun getNavToday(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "اليوم"
    AppLanguage.BENGALI -> "আজ"
    AppLanguage.SPANISH -> "Hoy"
    AppLanguage.PORTUGUESE -> "Hoje"
    AppLanguage.ENGLISH -> "Today"
  }

  fun getNavWeather(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الطقس"
    AppLanguage.BENGALI -> "আবহাওয়া"
    AppLanguage.SPANISH -> "Clima"
    AppLanguage.PORTUGUESE -> "Clima"
    AppLanguage.ENGLISH -> "Weather"
  }

  fun getNavQibla(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "القبلة"
    AppLanguage.BENGALI -> "কিবলা"
    AppLanguage.SPANISH -> "Alquibla"
    AppLanguage.PORTUGUESE -> "Qibla"
    AppLanguage.ENGLISH -> "Qibla"
  }

  fun getNavTasbih(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "التسبيح"
    AppLanguage.BENGALI -> "তাসবিহ"
    AppLanguage.SPANISH -> "Tasbih"
    AppLanguage.PORTUGUESE -> "Tasbih"
    AppLanguage.ENGLISH -> "Tasbih"
  }

  fun getNavDuas(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الأدعية"
    AppLanguage.BENGALI -> "দোয়া"
    AppLanguage.SPANISH -> "Súplicas"
    AppLanguage.PORTUGUESE -> "Súplicas"
    AppLanguage.ENGLISH -> "Duas"
  }

  // Prayer hero texts
  fun getNextPrayerLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الصلاة القادمة"
    AppLanguage.BENGALI -> "পরবর্তী নামাজ"
    AppLanguage.SPANISH -> "PRÓXIMA ORACIÓN"
    AppLanguage.PORTUGUESE -> "PRÓXIMA ORAÇÃO"
    AppLanguage.ENGLISH -> "NEXT PRAYER"
  }

  fun getPrayerDisplayName(prayer: PrayerName, language: AppLanguage): String = when (prayer) {
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
  }

  // Celestial Horizon strings
  fun getCelestialHorizonTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الأفق السماوي"
    AppLanguage.BENGALI -> "আকাশ দিগন্ত"
    AppLanguage.SPANISH -> "Horizonte Celeste"
    AppLanguage.PORTUGUESE -> "Horizonte Celeste"
    AppLanguage.ENGLISH -> "Celestial Horizon"
  }

  fun getCelestialHorizonSubtitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تتبع مسار الشمس والقمر"
    AppLanguage.BENGALI -> "সূর্য ও চন্দ্রের গতিপথ পর্যবেক্ষণ"
    AppLanguage.SPANISH -> "Seguimiento del Sol y la Luna"
    AppLanguage.PORTUGUESE -> "Rastreamento do Sol e da Lua"
    AppLanguage.ENGLISH -> "Sun & Moon Tracking"
  }

  fun getEastLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الشرق"
    AppLanguage.BENGALI -> "পূর্ব"
    AppLanguage.SPANISH -> "Este"
    AppLanguage.PORTUGUESE -> "Leste"
    AppLanguage.ENGLISH -> "East"
  }

  fun getWestLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الغرب"
    AppLanguage.BENGALI -> "পশ্চিম"
    AppLanguage.SPANISH -> "Oeste"
    AppLanguage.PORTUGUESE -> "Oeste"
    AppLanguage.ENGLISH -> "West"
  }

  fun getDaySkyLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "سماء النهار"
    AppLanguage.BENGALI -> "দিনের আকাশ"
    AppLanguage.SPANISH -> "Cielo Diurno"
    AppLanguage.PORTUGUESE -> "Céu Diurno"
    AppLanguage.ENGLISH -> "Day Sky"
  }

  fun getNightSkyLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "سماء الليل"
    AppLanguage.BENGALI -> "রাতের আকাশ"
    AppLanguage.SPANISH -> "Cielo Nocturno"
    AppLanguage.PORTUGUESE -> "Céu Noturno"
    AppLanguage.ENGLISH -> "Night Sky"
  }

  fun getNextSunriseLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الشروق القادم"
    AppLanguage.BENGALI -> "পরবর্তী সূর্যোদয়"
    AppLanguage.SPANISH -> "Próximo Amanecer"
    AppLanguage.PORTUGUESE -> "Próximo Nascer do Sol"
    AppLanguage.ENGLISH -> "Next Sunrise"
  }

  fun getNextSunsetLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الغروب القادم"
    AppLanguage.BENGALI -> "পরবর্তী সূর্যাস্ত"
    AppLanguage.SPANISH -> "Próxima Puesta"
    AppLanguage.PORTUGUESE -> "Próximo Pôr do Sol"
    AppLanguage.ENGLISH -> "Next Sunset"
  }

  fun getSunsetMaghribSub(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الغروب (المغرب)"
    AppLanguage.BENGALI -> "সূর্যাস্ত (মাগরিব)"
    AppLanguage.SPANISH -> "Puesta de Sol (Magrib)"
    AppLanguage.PORTUGUESE -> "Pôr do Sol (Maghrib)"
    AppLanguage.ENGLISH -> "Sunset (Maghrib)"
  }

  fun getSunriseIshraqSub(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الشروق (الإشراق)"
    AppLanguage.BENGALI -> "সূর্যোদয় (ইশরাক)"
    AppLanguage.SPANISH -> "Amanecer (Ishraq)"
    AppLanguage.PORTUGUESE -> "Nascer do Sol (Ishraq)"
    AppLanguage.ENGLISH -> "Sunrise (Ishraq)"
  }

  fun getMoonPhaseName(phaseKey: String, language: AppLanguage): String = when (phaseKey) {
    "NEW_MOON" -> when (language) {
      AppLanguage.ARABIC -> "محاق (هلال جديد)"
      AppLanguage.BENGALI -> "নতুন চাঁদ (হিলাল)"
      AppLanguage.SPANISH -> "Luna Nueva"
      AppLanguage.PORTUGUESE -> "Lua Nova"
      AppLanguage.ENGLISH -> "New Moon"
    }
    "WAXING_CRESCENT" -> when (language) {
      AppLanguage.ARABIC -> "هلال متزايد"
      AppLanguage.BENGALI -> "বর্ধমান ক্রিসেন্ট"
      AppLanguage.SPANISH -> "Creciente Inicial"
      AppLanguage.PORTUGUESE -> "Quarto Crescente"
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
      AppLanguage.BENGALI -> "শুক্লপক্ষ গিবস"
      AppLanguage.SPANISH -> "Gibosa Creciente"
      AppLanguage.PORTUGUESE -> "Crescente Gibosa"
      AppLanguage.ENGLISH -> "Waxing Gibbous"
    }
    "FULL_MOON" -> when (language) {
      AppLanguage.ARABIC -> "بدر كامل"
      AppLanguage.BENGALI -> "পূর্ণিমা (বদর)"
      AppLanguage.SPANISH -> "Luna Llena (Badr)"
      AppLanguage.PORTUGUESE -> "Lua Cheia (Badr)"
      AppLanguage.ENGLISH -> "Full Moon (Badr)"
    }
    "WANING_GIBBOUS" -> when (language) {
      AppLanguage.ARABIC -> "أحدب متناقص"
      AppLanguage.BENGALI -> "কৃষ্ণপক্ষ গিবস"
      AppLanguage.SPANISH -> "Gibosa Menguante"
      AppLanguage.PORTUGUESE -> "Minguante Gibosa"
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
      AppLanguage.BENGALI -> "ক্ষীণ ক্রিসেন্ট"
      AppLanguage.SPANISH -> "Creciente Menguante"
      AppLanguage.PORTUGUESE -> "Minguante Côncava"
      AppLanguage.ENGLISH -> "Waning Crescent"
    }
    else -> when (language) {
      AppLanguage.ARABIC -> "محاق"
      AppLanguage.BENGALI -> "অমাবস্যা (মাহক)"
      AppLanguage.SPANISH -> "Luna Oscura (Mahq)"
      AppLanguage.PORTUGUESE -> "Lua Escura (Mahq)"
      AppLanguage.ENGLISH -> "Dark Moon (Mahq)"
    }
  }

  fun getIlluminationLabel(illuminationPercent: Int, language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "$illuminationPercent% إضاءة"
    AppLanguage.BENGALI -> "$illuminationPercent% আলো"
    AppLanguage.SPANISH -> "$illuminationPercent% iluminación"
    AppLanguage.PORTUGUESE -> "$illuminationPercent% iluminação"
    AppLanguage.ENGLISH -> "$illuminationPercent% illumination"
  }

  // Weather Card strings
  fun getWeatherCardTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الطقس والإرشاد اليومي"
    AppLanguage.BENGALI -> "আবহাওয়া ও দৈনন্দিন পরামর্শ"
    AppLanguage.SPANISH -> "Clima y Guía Diaria"
    AppLanguage.PORTUGUESE -> "Clima e Orientação Diária"
    AppLanguage.ENGLISH -> "Weather & Daily Guidance"
  }

  fun getViewForecastButton(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "عرض توقعات ٢٤ ساعة و ٧ أيام"
    AppLanguage.BENGALI -> "২৪ ঘণ্টা ও ৭ দিনের পূর্বাভাস"
    AppLanguage.SPANISH -> "Ver pronóstico de 24h y 7 días"
    AppLanguage.PORTUGUESE -> "Ver previsão de 24h e 7 dias"
    AppLanguage.ENGLISH -> "View 24h & 7-Day Forecast"
  }

  fun getDailyAdviceHeader(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "نصيحة الممارسة اليومية"
    AppLanguage.BENGALI -> "দৈনন্দিন আমল পরামর্শ"
    AppLanguage.SPANISH -> "Consejo de Práctica Diaria"
    AppLanguage.PORTUGUESE -> "Conselho de Prática Diária"
    AppLanguage.ENGLISH -> "Daily Practice Advice"
  }

  // Today's prayers list
  fun getTodayPrayersTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "صلوات اليوم الخمس"
    AppLanguage.BENGALI -> "আজকের পাঁচ ওয়াক্ত নামাজ"
    AppLanguage.SPANISH -> "Cinco Oraciones de Hoy"
    AppLanguage.PORTUGUESE -> "Cinco Orações de Hoje"
    AppLanguage.ENGLISH -> "Today's Prayers"
  }

  fun getTodaysPrayersTitle(language: AppLanguage): String = getTodayPrayersTitle(language)

  fun getTapCircleHint(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "المس الدائرة لتسجيل أداء الصلاة"
    AppLanguage.BENGALI -> "নামাজ সম্পন্ন চিহ্নিত করতে বৃত্তে ট্যাপ করুন"
    AppLanguage.SPANISH -> "Toca el círculo para marcar como rezada"
    AppLanguage.PORTUGUESE -> "Toque no círculo para marcar como orado"
    AppLanguage.ENGLISH -> "Tap circle to mark as prayed"
  }

  fun getDailyInspirationTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "إلهام اليوم"
    AppLanguage.BENGALI -> "আজকের অনুপ্রেরণা"
    AppLanguage.SPANISH -> "Inspiración Diaria"
    AppLanguage.PORTUGUESE -> "Inspiração Diária"
    AppLanguage.ENGLISH -> "Daily Inspiration"
  }

  fun getAyahOfTheDayTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "آية اليوم"
    AppLanguage.BENGALI -> "আজকের আয়াত"
    AppLanguage.SPANISH -> "Versículo del Día"
    AppLanguage.PORTUGUESE -> "Versículo do Dia"
    AppLanguage.ENGLISH -> "Ayah of the Day"
  }

  fun getHadithOfTheDayTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "حديث اليوم"
    AppLanguage.BENGALI -> "আজকের হাদিস"
    AppLanguage.SPANISH -> "Hadiz del Día"
    AppLanguage.PORTUGUESE -> "Hadith do Dia"
    AppLanguage.ENGLISH -> "Hadith of the Day"
  }
}
