package com.example.data.localization

import com.example.data.model.PrayerName

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String, val flag: String) {
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

  // Geolocation lookups report ISO 3166-1 alpha-2 codes ("BD", "SA") rather than the full
  // names the sets above hold, so both spellings have to resolve to the same language.
  private val ARABIC_COUNTRY_CODES = setOf(
    "SA", "AE", "EG", "QA", "KW", "OM", "BH", "JO", "LB", "IQ", "PS", "YE",
    "MA", "DZ", "TN", "LY", "SD", "SY", "MR", "SO", "DJ", "KM"
  )

  private val SPANISH_COUNTRY_CODES = setOf(
    "ES", "MX", "CO", "AR", "PE", "VE", "CL", "GT", "EC", "CU", "BO", "DO",
    "HN", "PY", "SV", "NI", "CR", "PA", "PR", "UY", "GQ"
  )

  private val PORTUGUESE_COUNTRY_CODES = setOf(
    "BR", "PT", "AO", "MZ", "CV", "GW", "ST", "TL"
  )

  /**
   * Picks the app language for a country, accepting either a display name ("Bangladesh") or an
   * ISO 3166-1 alpha-2 code ("BD"). Bangladesh reads Bengali, the Arab world Arabic, and the
   * Spanish- and Portuguese-speaking worlds their own languages; everywhere else falls back to
   * English.
   */
  fun detectLanguageForCountry(country: String): AppLanguage {
    val clean = country.trim()
    val code = clean.uppercase()
    return when {
      clean.equals("Bangladesh", ignoreCase = true) || code == "BD" -> AppLanguage.BENGALI
      code in ARABIC_COUNTRY_CODES ||
        ARABIC_COUNTRIES.any { it.equals(clean, ignoreCase = true) } -> AppLanguage.ARABIC
      code in SPANISH_COUNTRY_CODES ||
        SPANISH_COUNTRIES.any { it.equals(clean, ignoreCase = true) } -> AppLanguage.SPANISH
      code in PORTUGUESE_COUNTRY_CODES ||
        PORTUGUESE_COUNTRIES.any { it.equals(clean, ignoreCase = true) } -> AppLanguage.PORTUGUESE
      else -> AppLanguage.ENGLISH
    }
  }

  /**
   * The name as shown inside the app, carrying the h-dot-below of the scholarly transliteration.
   * The launcher label and the store title use the plain ASCII spelling in `strings.xml` instead,
   * because a store title is a search key.
   */
  fun getAppName(language: AppLanguage): String = when (language) {
      AppLanguage.ARABIC -> "حَاسِي اللَّيْل"
      AppLanguage.BENGALI -> "হাসী আল-লাইল"
      else -> "Ḥaasi al-Layl"
    }

  /** A literal gloss of the name, distinct from [getAppSubtitle]. Shown in the About dialog. */
  fun getAppTagline(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "“الذي يرتشف الليل”"
    AppLanguage.BENGALI -> "“যে রাত চুমুক দিয়ে পান করে”"
    AppLanguage.SPANISH -> "“El que sorbe la noche”"
    AppLanguage.PORTUGUESE -> "“Aquele que sorve a noite”"
    AppLanguage.ENGLISH -> "“The One Who Sips the Night”"
  }

  fun getAppSubtitle(language: AppLanguage): String = when (language) {
      AppLanguage.ARABIC -> "أراقب إيقاع الليل والنهار الهادئ"
      AppLanguage.BENGALI -> "রাত ও দিনের নীরব ছন্দ পর্যবেক্ষণ"
      AppLanguage.SPANISH -> "El ritmo sereno del día y la noche"
      AppLanguage.PORTUGUESE -> "O ritmo sereno do dia e da noite"
      AppLanguage.ENGLISH -> "I watch the quiet rhythm of night and day"
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

  // =========================================================================
  // Navigation Bar Labels
  // =========================================================================
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
    AppLanguage.SPANISH -> "Qibla"
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

  // =========================================================================
  // Weather Screen Strings
  // =========================================================================
  fun getWeatherForecastTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "توقعات الطقس"
    AppLanguage.BENGALI -> "আবহাওয়া পূর্বাভাস"
    AppLanguage.SPANISH -> "Pronóstico del Clima"
    AppLanguage.PORTUGUESE -> "Previsão do Tempo"
    AppLanguage.ENGLISH -> "Weather Forecast"
  }

  fun getHourlyForecastTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "التوقعات بالساعات (٢٤ ساعة)"
    AppLanguage.BENGALI -> "ঘণ্টায় ঘণ্টায় পূর্বাভাস (২৪ ঘণ্টা)"
    AppLanguage.SPANISH -> "Pronóstico por Horas (24h)"
    AppLanguage.PORTUGUESE -> "Previsão por Horas (24h)"
    AppLanguage.ENGLISH -> "Hourly Forecast (24 Hours)"
  }

  fun getDailyForecastTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "توقعات ٧ أيام"
    AppLanguage.BENGALI -> "৭ দিনের আবহাওয়া পূর্বাভাস"
    AppLanguage.SPANISH -> "Pronóstico de 7 Días"
    AppLanguage.PORTUGUESE -> "Previsão de 7 Dias"
    AppLanguage.ENGLISH -> "7-Day Forecast"
  }

  fun getFeelsLikeLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الشعور الحقيقي"
    AppLanguage.BENGALI -> "অনুভূত তাপমাত্রা"
    AppLanguage.SPANISH -> "Sensación térmica"
    AppLanguage.PORTUGUESE -> "Sensação térmica"
    AppLanguage.ENGLISH -> "Feels Like"
  }








  fun getPressureLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الضغط الجوي"
    AppLanguage.BENGALI -> "বায়ুমণ্ডলীয় চাপ"
    AppLanguage.SPANISH -> "Presión"
    AppLanguage.PORTUGUESE -> "Pressão"
    AppLanguage.ENGLISH -> "Pressure"
  }

  // =========================================================================
  // Qibla Screen Strings
  // =========================================================================
  fun getQiblaTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "اتجاه القبلة"
    AppLanguage.BENGALI -> "কিবলা নির্দেশক"
    AppLanguage.SPANISH -> "Dirección de la Qibla"
    AppLanguage.PORTUGUESE -> "Direção da Qibla"
    AppLanguage.ENGLISH -> "Qibla Direction"
  }

  fun getQiblaSubtitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تحديد اتجاه الكعبة المشرفة بمكة المكرمة"
    AppLanguage.BENGALI -> "পবিত্র কাবা শরিফের সঠিক দিক নির্দেশক"
    AppLanguage.SPANISH -> "Hacia la Sagrada Kaaba en La Meca"
    AppLanguage.PORTUGUESE -> "Em direção à Sagrada Caaba em Meca"
    AppLanguage.ENGLISH -> "Find direction towards the Holy Kaaba"
  }

  fun getFacingQiblaText(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "أنت تواجه القبلة الشريفة!"
    AppLanguage.BENGALI -> "আপনি কিবলার সম্মুখমুখী আছেন!"
    AppLanguage.SPANISH -> "¡Estás frente a la Qibla!"
    AppLanguage.PORTUGUESE -> "Você está de frente para a Qibla!"
    AppLanguage.ENGLISH -> "Facing Qibla!"
  }

  fun getAlignNeedleHint(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "قم بتدوير الهاتف حتى يشير السهم الذهبي للأعلى"
    AppLanguage.BENGALI -> "তীরচিহ্নটি সোজা না হওয়া পর্যন্ত ফোনটি ঘুরান"
    AppLanguage.SPANISH -> "Gira el teléfono hasta que la aguja apunte arriba"
    AppLanguage.PORTUGUESE -> "Gire o telefone até que a agulha aponte para cima"
    AppLanguage.ENGLISH -> "Align device until needle points straight up"
  }

  fun getTowardsKaabaLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "نحو الكعبة المشرفة (مكة المكرمة)"
    AppLanguage.BENGALI -> "পবিত্র কাবা শরিফ, মক্কার দিকে"
    AppLanguage.SPANISH -> "Hacia la Kaaba (La Meca)"
    AppLanguage.PORTUGUESE -> "Em direção à Caaba (Meca)"
    AppLanguage.ENGLISH -> "Towards Kaaba, Makkah"
  }

  fun getDistanceToKaabaLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "المسافة إلى الكعبة"
    AppLanguage.BENGALI -> "কাবা শরিফ থেকে দূরত্ব"
    AppLanguage.SPANISH -> "Distancia a la Kaaba"
    AppLanguage.PORTUGUESE -> "Distância até a Caaba"
    AppLanguage.ENGLISH -> "Distance to Kaaba"
  }

  fun getQiblaBearingLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "اتجاه القبلة"
    AppLanguage.BENGALI -> "কিবলার দিককোণ"
    AppLanguage.SPANISH -> "Rumbo de la Quibla"
    AppLanguage.PORTUGUESE -> "Direção da Quibla"
    AppLanguage.ENGLISH -> "Qibla Bearing"
  }

  fun getTrueNorthLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "من الشمال الحقيقي"
    AppLanguage.BENGALI -> "প্রকৃত উত্তর দিক থেকে"
    AppLanguage.SPANISH -> "Desde el norte verdadero"
    AppLanguage.PORTUGUESE -> "A partir do norte verdadeiro"
    AppLanguage.ENGLISH -> "From True North"
  }

  fun getGreatCircleDistanceLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "المسافة الجغرافية"
    AppLanguage.BENGALI -> "ভৌগোলিক দূরত্ব"
    AppLanguage.SPANISH -> "Distancia ortodrómica"
    AppLanguage.PORTUGUESE -> "Distância ortodrómica"
    AppLanguage.ENGLISH -> "Great-circle distance"
  }

  /** Shows the magnetic-to-true correction, so the reading can be checked against a plain compass. */
  fun getDeclinationNote(declinationDegrees: Float, language: AppLanguage): String {
    val value = "%+.1f°".format(declinationDegrees)
    return when (language) {
      AppLanguage.ARABIC -> "تم تطبيق الانحراف المغناطيسي $value"
      AppLanguage.BENGALI -> "চৌম্বক বিচ্যুতি $value প্রয়োগ করা হয়েছে"
      AppLanguage.SPANISH -> "Declinación magnética $value aplicada"
      AppLanguage.PORTUGUESE -> "Declinação magnética $value aplicada"
      AppLanguage.ENGLISH -> "Magnetic declination $value applied"
    }
  }

  fun getCompassCalibrationHint(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "لزيادة الدقة، حرك الهاتف على شكل الرقم 8"
    AppLanguage.BENGALI -> "কম্পাসের নির্ভুলতার জন্য ফোনটিকে ইংরেজি \'8\' (আট) আকৃতিতে ঘোরান"
    AppLanguage.SPANISH -> "Para calibrar, mueva el teléfono en forma de 8"
    AppLanguage.PORTUGUESE -> "Para calibrar, mova o telefone em forma de 8"
    AppLanguage.ENGLISH -> "Calibrate sensor by moving phone in figure-8"
  }

  // =========================================================================
  // Tasbih Screen Strings
  // =========================================================================
  /**
   * Display name for a seeded dhikr, keyed by [com.example.data.local.TasbihEntity.id].
   *
   * The names are held here rather than in the Room row because the row is storage: it is written
   * once at seed time and would otherwise pin the user to whatever language was current then.
   * [fallback] covers ids this build does not know about.
   */
  /** Announces both the prayer and the current state, so the toggle is usable with TalkBack. */
  fun getPrayerToggleDescription(
    prayer: PrayerName,
    isCompleted: Boolean,
    language: AppLanguage
  ): String {
    val name = getPrayerDisplayName(prayer, language)
    return if (isCompleted) {
      when (language) {
        AppLanguage.ARABIC -> "$name - مُسجَّلة كمؤداة، اضغط للتراجع"
        AppLanguage.BENGALI -> "$name - আদায় করা হয়েছে, বাতিল করতে ট্যাপ করুন"
        AppLanguage.SPANISH -> "$name - marcada como rezada, toca para deshacer"
        AppLanguage.PORTUGUESE -> "$name - marcada como rezada, toque para desfazer"
        AppLanguage.ENGLISH -> "$name - marked as prayed, tap to undo"
      }
    } else {
      when (language) {
        AppLanguage.ARABIC -> "$name - غير مُسجَّلة، اضغط بعد أدائها"
        AppLanguage.BENGALI -> "$name - এখনো চিহ্নিত নয়, আদায়ের পর ট্যাপ করুন"
        AppLanguage.SPANISH -> "$name - sin marcar, toca cuando hayas rezado"
        AppLanguage.PORTUGUESE -> "$name - sem marcar, toque quando tiver rezado"
        AppLanguage.ENGLISH -> "$name - not yet marked, tap when you have prayed"
      }
    }
  }

  /** "3 of 5 prayed today", so the circles read as a tracker rather than decoration. */
  fun getPrayedTodaySummary(done: Int, total: Int, language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "$done من $total صلوات اليوم"
    AppLanguage.BENGALI -> "আজ $total ওয়াক্তের মধ্যে $done টি আদায় হয়েছে"
    AppLanguage.SPANISH -> "$done de $total rezadas hoy"
    AppLanguage.PORTUGUESE -> "$done de $total rezadas hoje"
    AppLanguage.ENGLISH -> "$done of $total prayed today"
  }

  fun getTasbihName(id: String, language: AppLanguage, fallback: String): String = when (id) {
    "default", "subhanallah" -> when (language) {
      AppLanguage.ARABIC -> "سُبْحَانَ اللَّهِ"
      AppLanguage.BENGALI -> "সুবহানাল্লাহ"
      AppLanguage.SPANISH -> "SubhanAllah"
      AppLanguage.PORTUGUESE -> "SubhanAllah"
      AppLanguage.ENGLISH -> "SubhanAllah"
    }
    "alhamdulillah" -> when (language) {
      AppLanguage.ARABIC -> "الْحَمْدُ لِلَّهِ"
      AppLanguage.BENGALI -> "আলহামদুলিল্লাহ"
      AppLanguage.SPANISH -> "Alhamdulillah"
      AppLanguage.PORTUGUESE -> "Alhamdulillah"
      AppLanguage.ENGLISH -> "Alhamdulillah"
    }
    "allahu_akbar" -> when (language) {
      AppLanguage.ARABIC -> "اللَّهُ أَكْبَرُ"
      AppLanguage.BENGALI -> "আল্লাহু আকবার"
      AppLanguage.SPANISH -> "Allahu Akbar"
      AppLanguage.PORTUGUESE -> "Allahu Akbar"
      AppLanguage.ENGLISH -> "Allahu Akbar"
    }
    "astaghfirullah" -> when (language) {
      AppLanguage.ARABIC -> "أَسْتَغْفِرُ اللَّهَ"
      AppLanguage.BENGALI -> "আস্তাগফিরুল্লাহ"
      AppLanguage.SPANISH -> "Astaghfirullah"
      AppLanguage.PORTUGUESE -> "Astaghfirullah"
      AppLanguage.ENGLISH -> "Astaghfirullah"
    }
    "la_ilaha_illallah" -> when (language) {
      AppLanguage.ARABIC -> "لَا إِلَهَ إِلَّا اللَّهُ"
      AppLanguage.BENGALI -> "লা ইলাহা ইল্লাল্লাহ"
      AppLanguage.SPANISH -> "La ilaha illallah"
      AppLanguage.PORTUGUESE -> "La ilaha illallah"
      AppLanguage.ENGLISH -> "La Ilaha Illallah"
    }
    "salawat" -> when (language) {
      AppLanguage.ARABIC -> "الصلاة على النبي ﷺ"
      AppLanguage.BENGALI -> "নবী ﷺ-এর উপর দরুদ"
      AppLanguage.SPANISH -> "Salawat sobre el Profeta ﷺ"
      AppLanguage.PORTUGUESE -> "Salawat sobre o Profeta ﷺ"
      AppLanguage.ENGLISH -> "Salawat upon the Prophet ﷺ"
    }
    else -> fallback
  }

  /** Meaning of a seeded dhikr. See [getTasbihName] for why these are not stored on the row. */
  fun getTasbihMeaning(id: String, language: AppLanguage, fallback: String): String = when (id) {
    "default", "subhanallah" -> when (language) {
      AppLanguage.ARABIC -> "تنزيه الله عن كل نقص"
      AppLanguage.BENGALI -> "আল্লাহর পবিত্রতা বর্ণনা"
      AppLanguage.SPANISH -> "Gloria a Alá"
      AppLanguage.PORTUGUESE -> "Glória a Allah"
      AppLanguage.ENGLISH -> "Glory be to Allah"
    }
    "alhamdulillah" -> when (language) {
      AppLanguage.ARABIC -> "الثناء على الله"
      AppLanguage.BENGALI -> "সমস্ত প্রশংসা আল্লাহর"
      AppLanguage.SPANISH -> "Alabado sea Alá"
      AppLanguage.PORTUGUESE -> "Louvado seja Allah"
      AppLanguage.ENGLISH -> "Praise be to Allah"
    }
    "allahu_akbar" -> when (language) {
      AppLanguage.ARABIC -> "الله أكبر من كل شيء"
      AppLanguage.BENGALI -> "আল্লাহ সবচেয়ে মহান"
      AppLanguage.SPANISH -> "Alá es el más grande"
      AppLanguage.PORTUGUESE -> "Allah é o Maior"
      AppLanguage.ENGLISH -> "Allah is the Greatest"
    }
    "astaghfirullah" -> when (language) {
      AppLanguage.ARABIC -> "أطلب المغفرة من الله"
      AppLanguage.BENGALI -> "আমি আল্লাহর কাছে ক্ষমা চাই"
      AppLanguage.SPANISH -> "Pido perdón a Alá"
      AppLanguage.PORTUGUESE -> "Peço perdão a Allah"
      AppLanguage.ENGLISH -> "I seek forgiveness from Allah"
    }
    "la_ilaha_illallah" -> when (language) {
      AppLanguage.ARABIC -> "لا معبود بحق إلا الله"
      AppLanguage.BENGALI -> "আল্লাহ ছাড়া কোনো উপাস্য নেই"
      AppLanguage.SPANISH -> "No hay más divinidad que Alá"
      AppLanguage.PORTUGUESE -> "Não há divindade além de Allah"
      AppLanguage.ENGLISH -> "There is no deity except Allah"
    }
    "salawat" -> when (language) {
      AppLanguage.ARABIC -> "اللهم صل على محمد ﷺ"
      AppLanguage.BENGALI -> "হে আল্লাহ, মুহাম্মদ ﷺ-এর উপর রহমত বর্ষণ করুন"
      AppLanguage.SPANISH -> "Oh Alá, envía bendiciones sobre Muhammad ﷺ"
      AppLanguage.PORTUGUESE -> "Ó Allah, envie bênçãos sobre Muhammad ﷺ"
      AppLanguage.ENGLISH -> "O Allah, send blessings upon Muhammad ﷺ"
    }
    else -> fallback
  }

  fun getLifetimeTotalLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الإجمالي الكلي"
    AppLanguage.BENGALI -> "সর্বমোট গণনা"
    AppLanguage.SPANISH -> "Total acumulado"
    AppLanguage.PORTUGUESE -> "Total acumulado"
    AppLanguage.ENGLISH -> "Lifetime Total"
  }

  fun getResetCounterDescription(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "إعادة تعيين العداد"
    AppLanguage.BENGALI -> "কাউন্টার রিসেট করুন"
    AppLanguage.SPANISH -> "Reiniciar contador"
    AppLanguage.PORTUGUESE -> "Reiniciar contador"
    AppLanguage.ENGLISH -> "Reset counter"
  }

  fun getTasbihTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "المسبحة الإلكترونية"
    AppLanguage.BENGALI -> "ডিজিটাল তাসবিহ"
    AppLanguage.SPANISH -> "Tasbih Digital"
    AppLanguage.PORTUGUESE -> "Tasbih Digital"
    AppLanguage.ENGLISH -> "Digital Tasbih"
  }

  fun getTasbihSubtitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "ذكر الله وتسبيحه في كل وقت"
    AppLanguage.BENGALI -> "আল্লাহর জিকির ও সার্বক্ষণিক স্মরণ"
    AppLanguage.SPANISH -> "Dhikr y Recuerdo de Alá"
    AppLanguage.PORTUGUESE -> "Dhikr e Lembrança de Allah"
    AppLanguage.ENGLISH -> "Dhikr & Remembrance of Allah"
  }

  fun getTapToCountHint(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "المس الدائرة للعد"
    AppLanguage.BENGALI -> "গণনা করতে বড় বৃত্তে ট্যাপ করুন"
    AppLanguage.SPANISH -> "Toca el círculo para contar"
    AppLanguage.PORTUGUESE -> "Toque no círculo para contar"
    AppLanguage.ENGLISH -> "Tap circle to count"
  }

  fun getResetLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "إعادة تعيين"
    AppLanguage.BENGALI -> "রিসেট"
    AppLanguage.SPANISH -> "Reiniciar"
    AppLanguage.PORTUGUESE -> "Redefinir"
    AppLanguage.ENGLISH -> "Reset"
  }

  fun getCompletedRoundsLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الجولات المكتملة"
    AppLanguage.BENGALI -> "সম্পূর্ণ চক্র"
    AppLanguage.SPANISH -> "Rondas Completadas"
    AppLanguage.PORTUGUESE -> "Rodadas Concluídas"
    AppLanguage.ENGLISH -> "Completed Rounds"
  }

  fun getTargetLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الهدف"
    AppLanguage.BENGALI -> "লক্ষ্য"
    AppLanguage.SPANISH -> "Objetivo"
    AppLanguage.PORTUGUESE -> "Meta"
    AppLanguage.ENGLISH -> "Target"
  }

  fun getSelectDhikrTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "اختر الذكر"
    AppLanguage.BENGALI -> "জিকির নির্বাচন করুন"
    AppLanguage.SPANISH -> "Seleccionar Dhikr"
    AppLanguage.PORTUGUESE -> "Selecionar Dhikr"
    AppLanguage.ENGLISH -> "Select Dhikr"
  }

  // =========================================================================
  // Dua Screen Strings
  // =========================================================================
  fun getDuaTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الأدعية والأذكار اليومية"
    AppLanguage.BENGALI -> "দৈনন্দিন দোয়া ও মোনাজাত"
    AppLanguage.SPANISH -> "Súplicas y Adhkar Diarios"
    AppLanguage.PORTUGUESE -> "Súplicas e Adhkar Diários"
    AppLanguage.ENGLISH -> "Daily Duas & Adhkar"
  }

  fun getDuaSubtitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "حصن المسلم من أذكار الكتاب والسنة"
    AppLanguage.BENGALI -> "হিসনুল মুসলিম (কুরআন ও সুন্নাহর দোয়া)"
    AppLanguage.SPANISH -> "Hisn al-Muslim (Fortaleza del Musulmán)"
    AppLanguage.PORTUGUESE -> "Hisn al-Muslim (Fortaleza do Muçulmano)"
    AppLanguage.ENGLISH -> "Hisn al-Muslim (Fortress of the Muslim)"
  }

  fun getSearchDuaPlaceholder(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "ابحث في الأدعية والترجمات..."
    AppLanguage.BENGALI -> "দোয়া বা অর্থ অনুসন্ধান করুন..."
    AppLanguage.SPANISH -> "Buscar súplicas, traducciones..."
    AppLanguage.PORTUGUESE -> "Pesquisar súplicas, traduções..."
    AppLanguage.ENGLISH -> "Search duas, translations..."
  }

  fun getBookmarksOnlyLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "المحفوظة"
    AppLanguage.BENGALI -> "সংরক্ষিত"
    AppLanguage.SPANISH -> "Guardadas"
    AppLanguage.PORTUGUESE -> "Salvas"
    AppLanguage.ENGLISH -> "Bookmarks"
  }

  fun getNoDuasFoundTitle(language: AppLanguage, showBookmarksOnly: Boolean): String = when (language) {
    AppLanguage.ARABIC -> if (showBookmarksOnly) "لا توجد أدعية محفوظة بعد" else "لم يتم العثور على أدعية"
    AppLanguage.BENGALI -> if (showBookmarksOnly) "এখনো কোনো দোয়া সংরক্ষণ করা হয়নি" else "কোনো দোয়া খুঁজে পাওয়া যায়নি"
    AppLanguage.SPANISH -> if (showBookmarksOnly) "No hay súplicas guardadas aún" else "No se encontraron súplicas"
    AppLanguage.PORTUGUESE -> if (showBookmarksOnly) "Nenhuma súplica salva ainda" else "Nenhuma súplica encontrada"
    AppLanguage.ENGLISH -> if (showBookmarksOnly) "No saved bookmarks yet" else "No duas found"
  }

  fun getNoDuasFoundSubtitle(language: AppLanguage, showBookmarksOnly: Boolean): String = when (language) {
    AppLanguage.ARABIC -> if (showBookmarksOnly) "اضغط على علامة الحفظ لحفظ أي دعاء هنا" else "حاول مسح فلاتر البحث"
    AppLanguage.BENGALI -> if (showBookmarksOnly) "যে কোনো দোয়ার বুকমার্ক আইকনে ট্যাপ করে এখানে সংরক্ষণ করুন" else "অনুসন্ধান ফিল্টার পরিবর্তন করে দেখুন"
    AppLanguage.SPANISH -> if (showBookmarksOnly) "Toca el ícono de marcador para guardar aquí" else "Intenta borrar tus filtros de búsqueda"
    AppLanguage.PORTUGUESE -> if (showBookmarksOnly) "Toque no ícone de marcador para salvar aqui" else "Tente limpar seus filtros de pesquisa"
    AppLanguage.ENGLISH -> if (showBookmarksOnly) "Tap the bookmark icon on any Du'a to save it here" else "Try clearing your search filters"
  }

  fun getSourceLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "المصدر"
    AppLanguage.BENGALI -> "উৎস"
    AppLanguage.SPANISH -> "Fuente"
    AppLanguage.PORTUGUESE -> "Fonte"
    AppLanguage.ENGLISH -> "Source"
  }

  // Madhab Selection Strings
  fun getSelectMethodTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "طريقة حساب مواقيت الصلاة"
    AppLanguage.BENGALI -> "নামাজের সময় গণনার পদ্ধতি"
    AppLanguage.SPANISH -> "Metodo de calculo de oracion"
    AppLanguage.PORTUGUESE -> "Metodo de calculo da oracao"
    AppLanguage.ENGLISH -> "Prayer Calculation Method"
  }

  fun getSelectMethodSubtitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تحدد زوايا الفجر والعشاء. أم القرى هي معيار مكة المكرمة."
    AppLanguage.BENGALI -> "ফজর ও ইশার কোণ নির্ধারণ করে। উম্মুল কুরা হলো মক্কার মান।"
    AppLanguage.SPANISH -> "Define los angulos de Fayr e Isha. Umm al-Qura es el estandar de La Meca."
    AppLanguage.PORTUGUESE -> "Define os angulos de Fajr e Isha. Umm al-Qura e o padrao de Meca."
    AppLanguage.ENGLISH -> "Sets the Fajr and Isha angles. Umm al-Qura is the Makkah standard."
  }

  fun getSelectMadhabTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "المذاهب الفقهية الأربعة"
    AppLanguage.BENGALI -> "চারটি ফিকহী মাজহাব নির্বাচন"
    AppLanguage.SPANISH -> "Seleccionar Escuela Jurídica (Madhab)"
    AppLanguage.PORTUGUESE -> "Selecionar Escola Jurídica (Madhab)"
    AppLanguage.ENGLISH -> "Select Juristic Madhab"
  }

  fun getSelectMadhabSubtitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "يحدد وقت صلاة العصر وفق مذهب الفقه المختار (الافتراضي: مالك)"
    AppLanguage.BENGALI -> "নির্বাচিত মাজহাব অনুযায়ী আসর নামাজের সময় নির্ধারিত হবে (ডিফল্ট: ইমাম মালিক)"
    AppLanguage.SPANISH -> "Determina la hora de Asr según el madhab elegido (Por defecto: Imam Malik)"
    AppLanguage.PORTUGUESE -> "Determina o horário de Asr de acordo com o madhab escolhido (Padrão: Imam Malik)"
    AppLanguage.ENGLISH -> "Determines Asr prayer time calculation (Default: Imam Malik)"
  }

  fun getDefaultBadge(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الافتراضي"
    AppLanguage.BENGALI -> "ডিফল্ট"
    AppLanguage.SPANISH -> "Por defecto"
    AppLanguage.PORTUGUESE -> "Padrão"
    AppLanguage.ENGLISH -> "Default"
  }

  fun getAsrShadowRatioLabel(language: AppLanguage, factor: Double): String = when (language) {
    AppLanguage.ARABIC -> "العصر: ظل ${if (factor == 1.0) "١x" else "٢x"}"
    AppLanguage.BENGALI -> "আসর: ছায়া ${if (factor == 1.0) "১ গুণ" else "২ গুণ"}"
    AppLanguage.SPANISH -> "Asr: Sombra ${if (factor == 1.0) "1x" else "2x"}"
    AppLanguage.PORTUGUESE -> "Asr: Sombra ${if (factor == 1.0) "1x" else "2x"}"
    AppLanguage.ENGLISH -> "Asr: ${if (factor == 1.0) "1x" else "2x"} Shadow"
  }


  // Settings & Notifications
  fun getSettingsTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الإعدادات"
    AppLanguage.BENGALI -> "সেটিংস"
    AppLanguage.SPANISH -> "Ajustes"
    AppLanguage.PORTUGUESE -> "Configurações"
    AppLanguage.ENGLISH -> "Settings"
  }

  fun getSettingsSubtitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "اللغة، المذهب والإشعارات"
    AppLanguage.BENGALI -> "ভাষা, মাজহাব এবং বিজ্ঞপ্তি"
    AppLanguage.SPANISH -> "Idioma, Madhab y Notificaciones"
    AppLanguage.PORTUGUESE -> "Idioma, Madhab e Notificações"
    AppLanguage.ENGLISH -> "Language, Madhab & Notifications"
  }

  fun getLanguageLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "اللغة"
    AppLanguage.BENGALI -> "ভাষা"
    AppLanguage.SPANISH -> "Idioma"
    AppLanguage.PORTUGUESE -> "Idioma"
    AppLanguage.ENGLISH -> "Language"
  }

  fun getPrayerStartAlertsLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "إشعارات دخول الوقت"
    AppLanguage.BENGALI -> "নামাজের সময় শুরু হওয়ার অ্যালার্ট"
    AppLanguage.SPANISH -> "Alertas de Inicio de Oración"
    AppLanguage.PORTUGUESE -> "Alertas de Início de Oração"
    AppLanguage.ENGLISH -> "Prayer Start Notifications"
  }

  fun getPrayerStartAlertsDesc(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تلقي إشعارات عند دخول وقت الفجر، الظهر، العصر، المغرب، والعشاء"
    AppLanguage.BENGALI -> "ফজর, জোহর, আসর, মাগরিব এবং এশার সময় শুরু হলে বিজ্ঞপ্তি পান"
    AppLanguage.SPANISH -> "Recibe notificaciones cuando comiencen Fajr, Dhuhr, Asr, Maghrib e Isha"
    AppLanguage.PORTUGUESE -> "Receba notificações quando Fajr, Dhuhr, Asr, Maghrib e Isha começarem"
    AppLanguage.ENGLISH -> "Receive notifications when Fajr, Dhuhr, Asr, Maghrib, and Isha begin"
  }

  fun getDetectingLocationLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "جارٍ تحديد الموقع..."
    AppLanguage.BENGALI -> "অবস্থান নির্ণয় করা হচ্ছে..."
    AppLanguage.SPANISH -> "Detectando ubicación..."
    AppLanguage.PORTUGUESE -> "A detetar a localização..."
    AppLanguage.ENGLISH -> "Detecting location..."
  }

  fun getSearchingLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "جارٍ البحث..."
    AppLanguage.BENGALI -> "খোঁজা হচ্ছে..."
    AppLanguage.SPANISH -> "Buscando..."
    AppLanguage.PORTUGUESE -> "A procurar..."
    AppLanguage.ENGLISH -> "Searching..."
  }

  fun getLocationPermissionRationale(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "يستخدم إذن الموقع لتحديد إحداثياتك بدقة، وهو ما يجعل مواقيت الصلاة صحيحة حتى بدون إنترنت."
    AppLanguage.BENGALI -> "সঠিক স্থানাঙ্ক পেতে অবস্থানের অনুমতি প্রয়োজন - এতে ইন্টারনেট ছাড়াই নামাজের সময় নির্ভুল থাকে।"
    AppLanguage.SPANISH -> "El permiso de ubicación se usa para obtener tus coordenadas exactas, lo que mantiene correctos los horarios de oración incluso sin internet."
    AppLanguage.PORTUGUESE -> "A permissão de localização serve para obter as suas coordenadas exatas, o que mantém os horários de oração corretos mesmo sem internet."
    AppLanguage.ENGLISH -> "Location permission gives your exact coordinates, which keeps prayer times correct even with no internet."
  }

  fun getReliabilityTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "موثوقية التنبيهات"
    AppLanguage.BENGALI -> "বিজ্ঞপ্তির নির্ভরযোগ্যতা"
    AppLanguage.SPANISH -> "Fiabilidad de los avisos"
    AppLanguage.PORTUGUESE -> "Fiabilidade dos avisos"
    AppLanguage.ENGLISH -> "Notification reliability"
  }

  fun getReliabilityNotificationsRow(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "التنبيهات مسموح بها"
    AppLanguage.BENGALI -> "বিজ্ঞপ্তি অনুমোদিত"
    AppLanguage.SPANISH -> "Notificaciones permitidas"
    AppLanguage.PORTUGUESE -> "Notificações permitidas"
    AppLanguage.ENGLISH -> "Notifications allowed"
  }

  fun getReliabilityExactAlarmsRow(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "المنبّهات الدقيقة مسموح بها"
    AppLanguage.BENGALI -> "নির্ভুল অ্যালার্ম অনুমোদিত"
    AppLanguage.SPANISH -> "Alarmas exactas permitidas"
    AppLanguage.PORTUGUESE -> "Alarmes exatos permitidos"
    AppLanguage.ENGLISH -> "Exact alarms allowed"
  }

  fun getReliabilityBatteryRow(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "البطارية غير مقيّدة"
    AppLanguage.BENGALI -> "ব্যাটারি সীমাবদ্ধ নয়"
    AppLanguage.SPANISH -> "Batería sin restricciones"
    AppLanguage.PORTUGUESE -> "Bateria sem restrições"
    AppLanguage.ENGLISH -> "Battery unrestricted"
  }

  fun getReliabilityFixAction(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "إصلاح"
    AppLanguage.BENGALI -> "ঠিক করুন"
    AppLanguage.SPANISH -> "Corregir"
    AppLanguage.PORTUGUESE -> "Corrigir"
    AppLanguage.ENGLISH -> "Fix"
  }

  fun getTestAlarmAction(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تجربة تنبيه بعد دقيقة"
    AppLanguage.BENGALI -> "১ মিনিট পর পরীক্ষামূলক অ্যালার্ম"
    AppLanguage.SPANISH -> "Probar aviso en 1 minuto"
    AppLanguage.PORTUGUESE -> "Testar aviso em 1 minuto"
    AppLanguage.ENGLISH -> "Test alert in 1 minute"
  }

  fun getTestAlarmSent(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تم ضبط التنبيه. أغلق التطبيق وانتظر دقيقة."
    AppLanguage.BENGALI -> "অ্যালার্ম সেট হয়েছে। অ্যাপ বন্ধ করে ১ মিনিট অপেক্ষা করুন।"
    AppLanguage.SPANISH -> "Aviso programado. Cierra la app y espera un minuto."
    AppLanguage.PORTUGUESE -> "Aviso agendado. Feche a app e aguarde um minuto."
    AppLanguage.ENGLISH -> "Alert scheduled. Close the app and wait one minute."
  }

  /** Shown on manufacturers whose power management drops alarms by default. */
  fun getOemBatteryHint(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "على أجهزة أوبو وشاومي وفيفو: فعّل التشغيل التلقائي، وأوقف تحسين وضع السكون، وثبّت التطبيق في قائمة التطبيقات الأخيرة."
    AppLanguage.BENGALI -> "OPPO/Xiaomi/Vivo ডিভাইসে: অটো-লঞ্চ চালু করুন, Sleep standby optimisation বন্ধ করুন, এবং রিসেন্ট অ্যাপে অ্যাপটি লক করুন।"
    AppLanguage.SPANISH -> "En OPPO/Xiaomi/Vivo: activa el inicio automático, desactiva la optimización de reposo y bloquea la app en Recientes."
    AppLanguage.PORTUGUESE -> "Em OPPO/Xiaomi/Vivo: ative o arranque automático, desative a otimização de suspensão e bloqueie a app em Recentes."
    AppLanguage.ENGLISH -> "On OPPO/Xiaomi/Vivo: enable auto-launch, turn off Sleep standby optimisation, and lock the app in Recents."
  }

  fun getFollowUpAlertsLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تذكير بعد ٥ دقائق"
    AppLanguage.BENGALI -> "৫ মিনিট পর পুনরায় মনে করিয়ে দিন"
    AppLanguage.SPANISH -> "Recordatorio a los 5 minutos"
    AppLanguage.PORTUGUESE -> "Lembrete aos 5 minutos"
    AppLanguage.ENGLISH -> "5-minute reminder"
  }

  fun getFollowUpAlertsDesc(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تذكير ثانٍ بعد خمس دقائق، ويُلغى إذا سجّلت الصلاة كمؤداة"
    AppLanguage.BENGALI -> "পাঁচ মিনিট পর দ্বিতীয় বিজ্ঞপ্তি; নামাজ আদায় চিহ্নিত করলে বাতিল হবে"
    AppLanguage.SPANISH -> "Un segundo aviso cinco minutos después, omitido si ya marcaste la oración"
    AppLanguage.PORTUGUESE -> "Um segundo aviso cinco minutos depois, omitido se já marcou a oração"
    AppLanguage.ENGLISH -> "A second nudge five minutes later, skipped once you mark the prayer done"
  }

  fun getTahajjudAlertsLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تنبيه التهجد"
    AppLanguage.BENGALI -> "তাহাজ্জুদের বিজ্ঞপ্তি"
    AppLanguage.SPANISH -> "Aviso de Tahayyud"
    AppLanguage.PORTUGUESE -> "Aviso de Tahajjud"
    AppLanguage.ENGLISH -> "Tahajjud reminder"
  }

  fun getTahajjudAlertsDesc(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تنبيه عند دخول الثلث الأخير من الليل"
    AppLanguage.BENGALI -> "রাতের শেষ তৃতীয়াংশ শুরু হলে জানানো হবে"
    AppLanguage.SPANISH -> "Avisa cuando comienza el último tercio de la noche"
    AppLanguage.PORTUGUESE -> "Avisa quando começa o último terço da noite"
    AppLanguage.ENGLISH -> "Alerts when the last third of the night begins"
  }

  fun getDiscreetAlarmsLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "إخفاء رمز المنبّه"
    AppLanguage.BENGALI -> "অ্যালার্ম আইকন লুকান"
    AppLanguage.SPANISH -> "Ocultar el icono de alarma"
    AppLanguage.PORTUGUESE -> "Ocultar o ícone de alarme"
    AppLanguage.ENGLISH -> "Hide the alarm icon"
  }

  fun getDiscreetAlarmsDesc(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "يزيل رمز المنبّه من شريط الحالة، وقد يتأخر تذكير الخمس دقائق حتى تسع دقائق أثناء نوم الهاتف"
    AppLanguage.BENGALI -> "স্ট্যাটাস বার থেকে অ্যালার্ম আইকন সরায়; ফোন ঘুমিয়ে থাকলে ৫ মিনিটের রিমাইন্ডার ৯ মিনিট পর্যন্ত দেরি হতে পারে"
    AppLanguage.SPANISH -> "Quita el icono de la barra de estado; el recordatorio de 5 minutos puede tardar hasta 9 con el teléfono dormido"
    AppLanguage.PORTUGUESE -> "Remove o ícone da barra de estado; o lembrete de 5 minutos pode demorar até 9 com o telemóvel a dormir"
    AppLanguage.ENGLISH -> "Removes the status bar alarm icon. The 5-minute reminder may then arrive up to 9 minutes late while the phone is asleep"
  }

  fun getForbiddenAlertsLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تنبيهات أوقات النهي"
    AppLanguage.BENGALI -> "নিষিদ্ধ সময়ের অ্যালার্ট"
    AppLanguage.SPANISH -> "Alertas de Tiempos Prohibidos"
    AppLanguage.PORTUGUESE -> "Alertas de Tempos Proibidos"
    AppLanguage.ENGLISH -> "Forbidden Prayer Time Alerts"
  }

  fun getForbiddenAlertsDesc(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تنبيه عند بدء وقت النهي وعند انتهائه"
    AppLanguage.BENGALI -> "নিষিদ্ধ সময় শুরু এবং শেষ হলে অ্যালার্ট পান (নফল নামাজ নিষিদ্ধ)"
    AppLanguage.SPANISH -> "Alertas cuando inician y terminan los tiempos prohibidos"
    AppLanguage.PORTUGUESE -> "Alertas quando iniciam e terminam os tempos proibidos"
    AppLanguage.ENGLISH -> "Alerts when prohibited windows start (Nafl prohibited) and when they end (prayer permitted)"
  }

  fun getTodaysProhibitedWindows(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "أوقات النهي اليوم"
    AppLanguage.BENGALI -> "আজকের নিষিদ্ধ সময়"
    AppLanguage.SPANISH -> "Tiempos Prohibidos de Hoy"
    AppLanguage.PORTUGUESE -> "Tempos Proibidos de Hoje"
    AppLanguage.ENGLISH -> "Today's Prohibited Windows"
  }

  // About Dialog
  fun getAboutContextLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "السياق الشعري والروحي:"
    AppLanguage.BENGALI -> "কাব্যিক এবং আধ্যাত্মিক প্রেক্ষাপট:"
    AppLanguage.SPANISH -> "Contexto Poético y Espiritual:"
    AppLanguage.PORTUGUESE -> "Contexto Poético e Espiritual:"
    AppLanguage.ENGLISH -> "Poetic & Spiritual Context:"
  }

  fun getAboutContextBody1(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "كلمة حاسي تأتي من الفعل حسى، وهو شرب السائل ببطء مع التأمل. الليل يعني وقت الليل."
    AppLanguage.BENGALI -> "হাসি (حاسي) শব্দটি আরবি ক্রিয়াপদ হাসা (حسَا) থেকে এসেছে, যার অর্থ ধীরে ধীরে ও মনোযোগ দিয়ে তরল পান করা। আল-লাইল (الليل) অর্থ রাত।"
    AppLanguage.SPANISH -> "Ḥaasi (حاسي) proviene del verbo árabe ḥasā (حسَا), que significa sorber un líquido lentamente con contemplación y reverencia. Al-Layl (الليل) es la noche."
    AppLanguage.PORTUGUESE -> "Ḥaasi (حاسي) vem do verbo árabe ḥasā (حسَا), que significa sorver um líquido lentamente com contemplação e reverência. Al-Layl (الليل) é a noite."
    AppLanguage.ENGLISH -> "Ḥaasi (حاسي) comes from the Arabic verb ḥasā (حسَا), meaning to sip a liquid slowly with contemplation and reverence. Al-Layl (الليل) is the night."
  }

  fun getAboutContextBody2(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تصف العابد المخلص الذي يشرب من رحيق الروحانيات في الساعات المتأخرة - التهجد والتفكر - بتأمل وتأنٍ."
    AppLanguage.BENGALI -> "এটি সেই একনিষ্ঠ ইবাদতকারীকে বোঝায় যে রাতের শেষ প্রহরে আধ্যাত্মিক সুধা পান করে - তাহাজ্জুদ, নীরব চিন্তাভাবনা এবং ভোরের আগের প্রশান্তি - ধীরে ও মনোযোগের সাথে।"
    AppLanguage.SPANISH -> "Describe al adorador devoto que bebe el néctar espiritual de las altas horas de la noche con plena conciencia."
    AppLanguage.PORTUGUESE -> "Descreve o adorador devoto que bebe o néctar espiritual das madrugadas com plena consciência."
    AppLanguage.ENGLISH -> "It describes the devout worshipper who drinks in the spiritual nectar of the late hours — Tahajjud, quiet reflection, and the serenity before dawn — with gradual, deliberate mindfulness."
  }

  fun getCloseLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "إغلاق"
    AppLanguage.BENGALI -> "বন্ধ করুন"
    AppLanguage.SPANISH -> "Cerrar"
    AppLanguage.PORTUGUESE -> "Fechar"
    AppLanguage.ENGLISH -> "Close"
  }


  // Weather Screen Additions
  fun getLiveStatus(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "مباشر"
    AppLanguage.BENGALI -> "লাইভ"
    AppLanguage.SPANISH -> "EN VIVO"
    AppLanguage.PORTUGUESE -> "AO VIVO"
    AppLanguage.ENGLISH -> "LIVE"
  }

  fun getOfflineStatus(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "غير متصل"
    AppLanguage.BENGALI -> "অফলাইন"
    AppLanguage.SPANISH -> "SIN CONEXIÓN"
    AppLanguage.PORTUGUESE -> "OFFLINE"
    AppLanguage.ENGLISH -> "OFFLINE"
  }

  fun getWeatherErrorDesc(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "بيانات الطقس غير متوفرة حالياً. يرجى التحقق من اتصالك بالإنترنت."
    AppLanguage.BENGALI -> "আবহাওয়ার তথ্য বর্তমানে অনুপলব্ধ। অনুগ্রহ করে আপনার ইন্টারনেট সংযোগ পরীক্ষা করুন।"
    AppLanguage.SPANISH -> "Los datos del clima no están disponibles. Por favor, comprueba tu conexión a internet."
    AppLanguage.PORTUGUESE -> "Os dados meteorológicos estão indisponíveis. Verifique sua conexão com a internet."
    AppLanguage.ENGLISH -> "Weather data is currently unavailable. Please check your internet connection."
  }

  fun getWeatherLoadingDesc(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "جاري تحميل معلومات الطقس..."
    AppLanguage.BENGALI -> "আবহাওয়ার তথ্য লোড হচ্ছে..."
    AppLanguage.SPANISH -> "Cargando información del clima..."
    AppLanguage.PORTUGUESE -> "Carregando informações meteorológicas..."
    AppLanguage.ENGLISH -> "Loading weather information..."
  }

  fun getHighShort(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "العظمى"
    AppLanguage.BENGALI -> "সর্বোচ্চ"
    AppLanguage.SPANISH -> "Máx"
    AppLanguage.PORTUGUESE -> "Máx"
    AppLanguage.ENGLISH -> "H"
  }

  fun getLowShort(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الصغرى"
    AppLanguage.BENGALI -> "সর্বনিম্ন"
    AppLanguage.SPANISH -> "Mín"
    AppLanguage.PORTUGUESE -> "Mín"
    AppLanguage.ENGLISH -> "L"
  }

  fun getRetryLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "إعادة المحاولة"
    AppLanguage.BENGALI -> "পুনরায় চেষ্টা করুন"
    AppLanguage.SPANISH -> "Reintentar"
    AppLanguage.PORTUGUESE -> "Tentar novamente"
    AppLanguage.ENGLISH -> "Retry"
  }

  fun getConditionsDetailsLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الظروف والتفاصيل"
    AppLanguage.BENGALI -> "আবহাওয়া বিবরণ ও পরিমাপ"
    AppLanguage.SPANISH -> "Condiciones y Detalles"
    AppLanguage.PORTUGUESE -> "Condições e Detalhes"
    AppLanguage.ENGLISH -> "Conditions & Details"
  }

  fun getIslamicWeatherGuidance(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الإرشاد الإسلامي للطقس"
    AppLanguage.BENGALI -> "ইসলামিক আবহাওয়া নির্দেশনা"
    AppLanguage.SPANISH -> "Guía Islámica del Clima"
    AppLanguage.PORTUGUESE -> "Guia Islâmico do Clima"
    AppLanguage.ENGLISH -> "Islamic Weather Guidance"
  }

  fun getRecommendedDuaLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الدعاء الموصى به:"
    AppLanguage.BENGALI -> "প্রস্তাবিত দোয়া:"
    AppLanguage.SPANISH -> "Súplica Recomendada:"
    AppLanguage.PORTUGUESE -> "Súplica Recomendada:"
    AppLanguage.ENGLISH -> "Recommended Du'a:"
  }











  fun getHumidityLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الرطوبة"
    AppLanguage.BENGALI -> "আর্দ্রতা"
    AppLanguage.SPANISH -> "Humedad"
    AppLanguage.PORTUGUESE -> "Umidade"
    AppLanguage.ENGLISH -> "Humidity"
  }


  fun getUvIndexLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الأشعة فوق البنفسجية"
    AppLanguage.BENGALI -> "ইউভি সূচক"
    AppLanguage.SPANISH -> "Índice UV"
    AppLanguage.PORTUGUESE -> "Índice UV"
    AppLanguage.ENGLISH -> "UV Index"
  }

  fun getPrecipitationLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "هطول الأمطار"
    AppLanguage.BENGALI -> "বৃষ্টিপাত"
    AppLanguage.SPANISH -> "Precipitación"
    AppLanguage.PORTUGUESE -> "Precipitação"
    AppLanguage.ENGLISH -> "Precipitation"
  }


  fun getWindSpeedLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "سرعة الرياح"
    AppLanguage.BENGALI -> "বাতাসের গতি"
    AppLanguage.SPANISH -> "Velocidad del Viento"
    AppLanguage.PORTUGUESE -> "Velocidade do Vento"
    AppLanguage.ENGLISH -> "Wind Speed"
  }

  fun getNightSkyActiveLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تتبع السماء ليلاً نشط"
    AppLanguage.BENGALI -> "রাতের আকাশ ও চাঁদ ট্র্যাকিং সক্রিয়"
    AppLanguage.SPANISH -> "Seguimiento Nocturno Activo"
    AppLanguage.PORTUGUESE -> "Rastreamento Noturno Ativo"
    AppLanguage.ENGLISH -> "Night Sky & Lunar Tracking Active"
  }

  fun getDaytimeActiveLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تتبع الشمس نهاراً نشط"
    AppLanguage.BENGALI -> "দিনের সূর্য ট্র্যাকিং সক্রিয়"
    AppLanguage.SPANISH -> "Seguimiento Solar Activo"
    AppLanguage.PORTUGUESE -> "Rastreamento Solar Ativo"
    AppLanguage.ENGLISH -> "Daytime Solar Tracking Active"
  }

  fun getTimeLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الوقت:"
    AppLanguage.BENGALI -> "সময়:"
    AppLanguage.SPANISH -> "Hora:"
    AppLanguage.PORTUGUESE -> "Tempo:"
    AppLanguage.ENGLISH -> "Time:"
  }

  fun getSolarMarkersLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "علامات الشمس:"
    AppLanguage.BENGALI -> "সূর্যের অবস্থান:"
    AppLanguage.SPANISH -> "Marcadores Solares:"
    AppLanguage.PORTUGUESE -> "Marcadores Solares:"
    AppLanguage.ENGLISH -> "Solar Markers:"
  }

  fun getLunarDetailsLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تفاصيل القمر:"
    AppLanguage.BENGALI -> "চাঁদের বিবরণ:"
    AppLanguage.SPANISH -> "Detalles Lunares:"
    AppLanguage.PORTUGUESE -> "Detalhes Lunares:"
    AppLanguage.ENGLISH -> "Lunar Details:"
  }

  fun getHijriDayLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "اليوم الهجري:"
    AppLanguage.BENGALI -> "হিজরি দিন:"
    AppLanguage.SPANISH -> "Día Hijri:"
    AppLanguage.PORTUGUESE -> "Dia Hijri:"
    AppLanguage.ENGLISH -> "Hijri Day:"
  }

  fun getGotItLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "حسناً"
    AppLanguage.BENGALI -> "ঠিক আছে"
    AppLanguage.SPANISH -> "Entendido"
    AppLanguage.PORTUGUESE -> "Entendi"
    AppLanguage.ENGLISH -> "Got it"
  }

  fun getAutoDetectLocationLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تحديد الموقع تلقائيًا (يتطلب إنترنت)"
    AppLanguage.BENGALI -> "স্বয়ংক্রিয়ভাবে অবস্থান নির্ণয় করুন (ইন্টারনেট প্রয়োজন)"
    AppLanguage.SPANISH -> "Detección Automática (Requiere Internet)"
    AppLanguage.PORTUGUESE -> "Detecção Automática (Requer Internet)"
    AppLanguage.ENGLISH -> "Auto-Detect Location (Requires Internet)"
  }

  fun getChooseLanguageLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "اختر اللغة"
    AppLanguage.BENGALI -> "ভাষা নির্বাচন করুন"
    AppLanguage.SPANISH -> "Elegir Idioma"
    AppLanguage.PORTUGUESE -> "Escolher Idioma"
    AppLanguage.ENGLISH -> "Choose Language"
  }

  fun getWelcomeSetupTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "مرحباً بك في حصي الليل"
    AppLanguage.BENGALI -> "হাসী আল-লাইল এ স্বাগতম"
    AppLanguage.SPANISH -> "Bienvenido a Ḥaasi al-Layl"
    AppLanguage.PORTUGUESE -> "Bem-vindo ao Ḥaasi al-Layl"
    AppLanguage.ENGLISH -> "Welcome to Ḥaasi al-Layl"
  }

  fun getWelcomeSetupSubtitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "يرجى تحديد موقعك ولغتك للبدء"
    AppLanguage.BENGALI -> "শুরু করতে আপনার অবস্থান ও ভাষা নির্বাচন করুন"
    AppLanguage.SPANISH -> "Seleccione su ubicación e idioma para comenzar"
    AppLanguage.PORTUGUESE -> "Selecione sua localização e idioma para começar"
    AppLanguage.ENGLISH -> "Please select your location and language to begin"
  }

  fun getWeatherConditionTitle(code: Int, language: AppLanguage): String = when (code) {
    0 -> when(language) {
      AppLanguage.ARABIC -> "سماء صافية"
      AppLanguage.BENGALI -> "পরিষ্কার আকাশ"
      AppLanguage.SPANISH -> "Cielo Despejado"
      AppLanguage.PORTUGUESE -> "Céu Limpo"
      AppLanguage.ENGLISH -> "Clear Sky"
    }
    1, 2, 3 -> when(language) {
      AppLanguage.ARABIC -> "غائم"
      AppLanguage.BENGALI -> "মেঘলা"
      AppLanguage.SPANISH -> "Nublado"
      AppLanguage.PORTUGUESE -> "Nublado"
      AppLanguage.ENGLISH -> "Cloudy"
    }
    45, 48 -> when(language) {
      AppLanguage.ARABIC -> "ضبابي"
      AppLanguage.BENGALI -> "কুয়াশাচ্ছন্ন"
      AppLanguage.SPANISH -> "Niebla"
      AppLanguage.PORTUGUESE -> "Névoa"
      AppLanguage.ENGLISH -> "Foggy"
    }
    51, 53, 55, 61, 63, 65, 80, 81, 82 -> when(language) {
      AppLanguage.ARABIC -> "ممطر"
      AppLanguage.BENGALI -> "বৃষ্টি"
      AppLanguage.SPANISH -> "Lluvia"
      AppLanguage.PORTUGUESE -> "Chuva"
      AppLanguage.ENGLISH -> "Rain"
    }
    71, 73, 75, 77, 85, 86 -> when(language) {
      AppLanguage.ARABIC -> "مثلج"
      AppLanguage.BENGALI -> "তুষারপাত"
      AppLanguage.SPANISH -> "Nieve"
      AppLanguage.PORTUGUESE -> "Neve"
      AppLanguage.ENGLISH -> "Snow"
    }
    95, 96, 99 -> when(language) {
      AppLanguage.ARABIC -> "عاصفة رعدية"
      AppLanguage.BENGALI -> "বজ্রঝড়"
      AppLanguage.SPANISH -> "Tormenta Eléctrica"
      AppLanguage.PORTUGUESE -> "Trovoada"
      AppLanguage.ENGLISH -> "Thunderstorm"
    }
    else -> when(language) {
      AppLanguage.ARABIC -> "معتدل"
      AppLanguage.BENGALI -> "স্বাভাবিক"
      AppLanguage.SPANISH -> "Justo"
      AppLanguage.PORTUGUESE -> "Justo"
      AppLanguage.ENGLISH -> "Fair"
    }
  }

  fun getIslamicWeatherAdvice(code: Int, tempC: Double, language: AppLanguage): String = when {
    code in listOf(95, 96, 99) -> when (language) {
      AppLanguage.ARABIC -> "عاصفة رعدية في المنطقة. احتمي بالداخل واقرأ دعاء الرعد."
      AppLanguage.BENGALI -> "এলাকায় বজ্রঝড়। নিরাপদ স্থানে থাকুন এবং বজ্রপাতের দোয়া পড়ুন।"
      AppLanguage.SPANISH -> "Tormenta en la zona. Refúgiate y recita la Du'a para truenos."
      AppLanguage.PORTUGUESE -> "Tempestade na área. Abrigue-se e recite a Du'a para trovões."
      AppLanguage.ENGLISH -> "Thunderstorm in the area. Take shelter indoors and recite the Du'a for lightning and thunder."
    }
    code in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82) -> when (language) {
      AppLanguage.ARABIC -> "المطر يتساقط — أشار النبي (ﷺ) إلى أن هذا وقت مبارك لإجابة الدعاء!"
      AppLanguage.BENGALI -> "বৃষ্টি পড়ছে — নবী ﷺ বলেছেন এটি দোয়া কবুলের বরকতময় সময়!"
      AppLanguage.SPANISH -> "Cae lluvia — ¡El Profeta (ﷺ) notó que es un tiempo bendito para orar!"
      AppLanguage.PORTUGUESE -> "A chuva cai — O Profeta (ﷺ) notou ser um tempo abençoado para orar!"
      AppLanguage.ENGLISH -> "Rain is descending — the Prophet (ﷺ) noted this is a blessed time for prayers to be answered!"
    }
    tempC > 36 -> when (language) {
      AppLanguage.ARABIC -> "درجات حرارة عالية اليوم. حافظ على رطوبة جسمك واسلك طرقًا مظللة إلى المسجد."
      AppLanguage.BENGALI -> "আজ তাপমাত্রা বেশি। পর্যাপ্ত পানি পান করুন এবং মসজিদে যাওয়ার সময় ছায়াযুক্ত পথ ব্যবহার করুন।"
      AppLanguage.SPANISH -> "Altas temperaturas. Mantente hidratado y busca sombra al ir a la mezquita."
      AppLanguage.PORTUGUESE -> "Altas temperaturas. Mantenha-se hidratado e busque sombra ao ir à mesquita."
      AppLanguage.ENGLISH -> "High temperatures today. Keep well-hydrated and seek shaded routes to the mosque."
    }
    tempC < 5 -> when (language) {
      AppLanguage.ARABIC -> "ظروف باردة اليوم. توضأ جيدًا بالماء الدافئ وارتدِ ملابس دافئة لصلاة الفجر والعشاء."
      AppLanguage.BENGALI -> "আজ ঠান্ডা আবহাওয়া। গরম পানি দিয়ে ভালোভাবে ওযু করুন এবং ফজর/এশার জন্য গরম কাপড় পরুন।"
      AppLanguage.SPANISH -> "Condiciones frías. Haz el wudhu completo y abrígate para Fajr/Isha."
      AppLanguage.PORTUGUESE -> "Condições frias. Faça o wudhu completo e agasalhe-se para Fajr/Isha."
      AppLanguage.ENGLISH -> "Cold conditions today. Perform wudhu thoroughly with warmth and dress warmly for Fajr/Isha."
    }
    else -> when (language) {
      AppLanguage.ARABIC -> "طقس مريح وهادئ — فرصة ممتازة للمشي إلى المسجد لأداء صلاة الجماعة."
      AppLanguage.BENGALI -> "আরামদায়ক এবং শান্ত আবহাওয়া — জামাতে নামাজ পড়ার জন্য মসজিদে হেঁটে যাওয়ার চমৎকার সুযোগ।"
      AppLanguage.SPANISH -> "Clima cómodo y sereno — excelente oportunidad para caminar a la Mezquita."
      AppLanguage.PORTUGUESE -> "Clima confortável e sereno — excelente oportunidade para caminhar até a Mesquita."
      AppLanguage.ENGLISH -> "Comfortable and serene weather — an excellent opportunity to walk to the Masjid for congregational prayer."
    }
  }

  fun formatTime(time: java.time.LocalTime, language: AppLanguage): String {
    val formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
    var formatted = time.format(formatter)
    
    if (language == AppLanguage.BENGALI) {
      formatted = formatted.replace("AM", "পূর্বাহ্ণ").replace("PM", "অপরাহ্ণ")
      formatted = formatted.replace("0", "০").replace("1", "১").replace("2", "২").replace("3", "৩").replace("4", "৪").replace("5", "৫").replace("6", "৬").replace("7", "৭").replace("8", "৮").replace("9", "৯")
    } else if (language == AppLanguage.ARABIC) {
      formatted = formatted.replace("AM", "ص").replace("PM", "م")
      formatted = formatted.replace("0", "٠").replace("1", "١").replace("2", "٢").replace("3", "٣").replace("4", "٤").replace("5", "٥").replace("6", "٦").replace("7", "٧").replace("8", "٨").replace("9", "٩")
    }
    return formatted
  }
}
