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

  fun getHumidityLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الرطوبة"
    AppLanguage.BENGALI -> "আর্দ্রতা"
    AppLanguage.SPANISH -> "Humedad"
    AppLanguage.PORTUGUESE -> "Umidade"
    AppLanguage.ENGLISH -> "Humidity"
  }

  fun getWindSpeedLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "سرعة الرياح"
    AppLanguage.BENGALI -> "বাতাসের গতি"
    AppLanguage.SPANISH -> "Viento"
    AppLanguage.PORTUGUESE -> "Vento"
    AppLanguage.ENGLISH -> "Wind Speed"
  }

  fun getUvIndexLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "مؤشر الأشعة فوق البنفسجية"
    AppLanguage.BENGALI -> "ইউভি সূচক"
    AppLanguage.SPANISH -> "Índice UV"
    AppLanguage.PORTUGUESE -> "Índice UV"
    AppLanguage.ENGLISH -> "UV Index"
  }

  fun getPrecipitationLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "احتمال الهطول"
    AppLanguage.BENGALI -> "বৃষ্টির সম্ভাবনা"
    AppLanguage.SPANISH -> "Precipitación"
    AppLanguage.PORTUGUESE -> "Precipitação"
    AppLanguage.ENGLISH -> "Precipitation"
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

  fun getCompassCalibrationHint(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "لزيادة الدقة، حرك الهاتف على شكل الرقم 8"
    AppLanguage.BENGALI -> "কম্পাসের নির্ভুলতার জন্য ফোনটিকে ৮ এর মতো করে ঘোরান"
    AppLanguage.SPANISH -> "Para calibrar, mueva el teléfono en forma de 8"
    AppLanguage.PORTUGUESE -> "Para calibrar, mova o telefone em forma de 8"
    AppLanguage.ENGLISH -> "Calibrate sensor by moving phone in figure-8"
  }

  // =========================================================================
  // Tasbih Screen Strings
  // =========================================================================
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
  fun getSelectMadhabTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "المذاهب الفقهية الأربعة"
    AppLanguage.BENGALI -> "চারটি ফিকহী মাজহাব নির্বাচন"
    AppLanguage.SPANISH -> "Seleccionar Escuela Jurídica (Madhab)"
    AppLanguage.PORTUGUESE -> "Selecionar Escola Jurídica (Madhab)"
    AppLanguage.ENGLISH -> "Select Juristic Madhab"
  }

  fun getSelectMadhabSubtitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "يحدد وقت صلاة العصر وفق مذهب الفقه المختار (الافتراضي: الشافعي)"
    AppLanguage.BENGALI -> "নির্বাচিত মাজহাব অনুযায়ী আসর নামাজের সময় নির্ধারিত হবে (ডিফল্ট: ইমাম শাফেয়ী)"
    AppLanguage.SPANISH -> "Determina la hora de Asr según el madhab elegido (Por defecto: Imam Shafi'i)"
    AppLanguage.PORTUGUESE -> "Determina o horário de Asr de acordo com o madhab escolhido (Padrão: Imam Shafi'i)"
    AppLanguage.ENGLISH -> "Determines Asr prayer time calculation (Default: Imam Shafi'i)"
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
}

