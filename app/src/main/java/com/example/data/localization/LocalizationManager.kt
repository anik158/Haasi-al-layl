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
  /** Accessibility label for the icon-only location button in the header. */
  /** After the city on the chip, when the location is only an IP estimate. */
  fun getApproximateLocationLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تقريبي"
    AppLanguage.BENGALI -> "আনুমানিক"
    AppLanguage.SPANISH -> "aproximado"
    AppLanguage.PORTUGUESE -> "aproximado"
    AppLanguage.ENGLISH -> "approximate"
  }

  fun getChangeLocationLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تغيير الموقع"
    AppLanguage.BENGALI -> "অবস্থান পরিবর্তন করুন"
    AppLanguage.SPANISH -> "Cambiar ubicación"
    AppLanguage.PORTUGUESE -> "Alterar localização"
    AppLanguage.ENGLISH -> "Change location"
  }

  /** Accessibility label for the settings button in the header. */
  fun getSettingsButtonLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الإعدادات والتنبيهات"
    AppLanguage.BENGALI -> "সেটিংস ও বিজ্ঞপ্তি"
    AppLanguage.SPANISH -> "Ajustes y avisos"
    AppLanguage.PORTUGUESE -> "Definições e avisos"
    AppLanguage.ENGLISH -> "Settings and notifications"
  }

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
  fun getSehriIftarTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "السحور والإفطار"
    AppLanguage.BENGALI -> "সাহরি ও ইফতার"
    AppLanguage.SPANISH -> "Suhur e Iftar"
    AppLanguage.PORTUGUESE -> "Suhur e Iftar"
    AppLanguage.ENGLISH -> "Sehri & Iftar"
  }

  /** Under the title outside Ramadan, so the card does not read as a claim that it is Ramadan. */
  fun getSehriIftarVoluntarySubtitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "لصيام التطوع"
    AppLanguage.BENGALI -> "নফল রোজার জন্য"
    AppLanguage.SPANISH -> "Para el ayuno voluntario"
    AppLanguage.PORTUGUESE -> "Para o jejum voluntário"
    AppLanguage.ENGLISH -> "For voluntary fasts"
  }

  fun getSehriEndsLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "انتهاء السحور"
    AppLanguage.BENGALI -> "সাহরির শেষ সময়"
    AppLanguage.SPANISH -> "Fin del suhur"
    AppLanguage.PORTUGUESE -> "Fim do suhur"
    AppLanguage.ENGLISH -> "Sehri ends"
  }

  fun getIftarLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الإفطار"
    AppLanguage.BENGALI -> "ইফতার"
    AppLanguage.SPANISH -> "Iftar"
    AppLanguage.PORTUGUESE -> "Iftar"
    AppLanguage.ENGLISH -> "Iftar"
  }

  /** "Ramadan, day 5". [day] is localized to the language's digits. */
  fun getRamadanDayLabel(day: Int, language: AppLanguage): String = localizeDigits(
    when (language) {
      AppLanguage.ARABIC -> "رمضان، اليوم $day"
      AppLanguage.BENGALI -> "রমজান, $day তম দিন"
      AppLanguage.SPANISH -> "Ramadán, día $day"
      AppLanguage.PORTUGUESE -> "Ramadão, dia $day"
      AppLanguage.ENGLISH -> "Ramadan, day $day"
    },
    language
  )

  fun getUntilSehriEndsLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "حتى انتهاء السحور"
    AppLanguage.BENGALI -> "সাহরি শেষ হতে বাকি"
    AppLanguage.SPANISH -> "hasta el fin del suhur"
    AppLanguage.PORTUGUESE -> "até o fim do suhur"
    AppLanguage.ENGLISH -> "until Sehri ends"
  }

  fun getUntilIftarLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "حتى الإفطار"
    AppLanguage.BENGALI -> "ইফতার হতে বাকি"
    AppLanguage.SPANISH -> "hasta el iftar"
    AppLanguage.PORTUGUESE -> "até o iftar"
    AppLanguage.ENGLISH -> "until Iftar"
  }

  /** Tomorrow's times, shown after today's Iftar. */
  fun getTomorrowLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "غداً"
    AppLanguage.BENGALI -> "আগামীকাল"
    AppLanguage.SPANISH -> "Mañana"
    AppLanguage.PORTUGUESE -> "Amanhã"
    AppLanguage.ENGLISH -> "Tomorrow"
  }

  /** Where the two times come from. References only, no quoted wording. */
  fun getSehriIftarFootnote(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "السحور حتى الفجر الصادق (البقرة ١٨٧؛ البخاري ١٩١٩)، والإفطار عند الغروب (البخاري ١٩٥٤، ١٩٥٧)."
    AppLanguage.BENGALI -> "সাহরি সুবহে সাদিক পর্যন্ত (আল-বাকারা ১৮৭; বুখারি ১৯১৯), ইফতার সূর্যাস্তে (বুখারি ১৯৫৪, ১৯৫৭)।"
    AppLanguage.SPANISH -> "El suhur hasta el alba verdadera (Corán 2:187; Bujari 1919); el iftar a la puesta del sol (Bujari 1954, 1957)."
    AppLanguage.PORTUGUESE -> "O suhur até a aurora verdadeira (Alcorão 2:187; Bukhari 1919); o iftar ao pôr do sol (Bukhari 1954, 1957)."
    AppLanguage.ENGLISH -> "Sehri until true dawn (Qur'an 2:187; Bukhari 1919); Iftar at sunset (Bukhari 1954, 1957)."
  }

  fun getHijriAdjustmentLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تعديل التاريخ الهجري"
    AppLanguage.BENGALI -> "হিজরি তারিখ সমন্বয়"
    AppLanguage.SPANISH -> "Ajuste de la fecha hégira"
    AppLanguage.PORTUGUESE -> "Ajuste da data hégira"
    AppLanguage.ENGLISH -> "Hijri date adjustment"
  }

  fun getHijriAdjustmentDesc(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "يُضبط تلقائياً حسب بلدك. غيّره فقط إذا بدأ الشهر عندكم برؤية الهلال في يوم آخر. يغيّر بداية رمضان أيضاً."
    AppLanguage.BENGALI -> "আপনার দেশ অনুযায়ী স্বয়ংক্রিয়ভাবে ঠিক হয়। শুধু আপনার এলাকায় চাঁদ দেখে মাস অন্য দিনে শুরু হলে বদলান। রমজানের শুরুও এতে বদলায়।"
    AppLanguage.SPANISH -> "Se ajusta solo según su país. Cámbielo solo si en su zona el mes empieza otro día por el avistamiento. También mueve el inicio del Ramadán."
    AppLanguage.PORTUGUESE -> "Ajusta-se sozinho conforme o seu país. Altere só se na sua região o mês começar noutro dia pelo avistamento. Também muda o início do Ramadão."
    AppLanguage.ENGLISH -> "Set automatically from your country. Change it only if your local moon sighting starts the month on another day. This also moves the start of Ramadan."
  }

  fun getAutomaticLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تلقائي"
    AppLanguage.BENGALI -> "স্বয়ংক্রিয়"
    AppLanguage.SPANISH -> "Automático"
    AppLanguage.PORTUGUESE -> "Automático"
    AppLanguage.ENGLISH -> "Automatic"
  }

  fun getCurrentPrayerLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الوقت الحالي"
    AppLanguage.BENGALI -> "চলমান ওয়াক্ত"
    AppLanguage.SPANISH -> "ORACIÓN ACTUAL"
    AppLanguage.PORTUGUESE -> "ORAÇÃO ATUAL"
    AppLanguage.ENGLISH -> "CURRENT PRAYER"
  }

  /** "Ends at 05:55 PM" under the current prayer. */
  fun getPrayerEndsAtLabel(time: String, language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "ينتهي $time"
    AppLanguage.BENGALI -> "শেষ হবে $time"
    AppLanguage.SPANISH -> "Termina a las $time"
    AppLanguage.PORTUGUESE -> "Termina às $time"
    AppLanguage.ENGLISH -> "Ends at $time"
  }

  fun getTimeLeftLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "متبقٍ"
    AppLanguage.BENGALI -> "বাকি"
    AppLanguage.SPANISH -> "restante"
    AppLanguage.PORTUGUESE -> "restante"
    AppLanguage.ENGLISH -> "left"
  }

  /** Between midnight and Fajr: Isha's time has ended (Sahih Muslim 612). */
  fun getIshaPastMidnightNote(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "انتهى وقت العشاء عند منتصف الليل (مسلم ٦١٢). إن لم تصلّها فصلّها الآن."
    AppLanguage.BENGALI -> "মধ্যরাতে এশার ওয়াক্ত শেষ হয়েছে (মুসলিম ৬১২)। এখনো না পড়ে থাকলে এখনই পড়ে নিন।"
    AppLanguage.SPANISH -> "El tiempo del Isha terminó a medianoche (Muslim 612). Si no lo ha rezado, rece ahora."
    AppLanguage.PORTUGUESE -> "O tempo do Isha terminou à meia-noite (Muslim 612). Se ainda não rezou, reze agora."
    AppLanguage.ENGLISH -> "Isha's time ended at midnight (Muslim 612). If you haven't prayed it, pray it now."
  }

  /** Between sunrise and Dhuhr, when no obligatory prayer's time is running. */
  fun getNoCurrentPrayerLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "لا صلاة فريضة الآن"
    AppLanguage.BENGALI -> "এখন কোনো ফরজ নামাজের ওয়াক্ত নেই"
    AppLanguage.SPANISH -> "Ahora no hay oración obligatoria"
    AppLanguage.PORTUGUESE -> "Agora não há oração obrigatória"
    AppLanguage.ENGLISH -> "No obligatory prayer now"
  }

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

  /**
   * The sunlit share of the moon's disc - its phase. Labelled "lit" rather than "illumination",
   * which read as the amount of moonlight arriving now, whether or not the moon is even up.
   */
  fun getIlluminationLabel(illuminationPercent: Int, language: AppLanguage): String {
    val p = localizeDigits(illuminationPercent.toString(), language)
    return when (language) {
      AppLanguage.ARABIC -> "$p% مضاء"
      AppLanguage.BENGALI -> "$p% আলোকিত"
      AppLanguage.SPANISH -> "$p% iluminada"
      AppLanguage.PORTUGUESE -> "$p% iluminada"
      AppLanguage.ENGLISH -> "$p% lit"
    }
  }

  fun getMoonAboveHorizonLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "فوق الأفق"
    AppLanguage.BENGALI -> "দিগন্তের ওপরে"
    AppLanguage.SPANISH -> "sobre el horizonte"
    AppLanguage.PORTUGUESE -> "acima do horizonte"
    AppLanguage.ENGLISH -> "above the horizon"
  }

  fun getMoonBelowHorizonLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تحت الأفق"
    AppLanguage.BENGALI -> "দিগন্তের নিচে"
    AppLanguage.SPANISH -> "bajo el horizonte"
    AppLanguage.PORTUGUESE -> "abaixo do horizonte"
    AppLanguage.ENGLISH -> "below the horizon"
  }

  fun getMoonRisesAt(time: String, language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "يشرق $time"
    AppLanguage.BENGALI -> "উদয় $time"
    AppLanguage.SPANISH -> "sale a las $time"
    AppLanguage.PORTUGUESE -> "nasce às $time"
    AppLanguage.ENGLISH -> "rises $time"
  }

  fun getMoonSetsAt(time: String, language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "يغرب $time"
    AppLanguage.BENGALI -> "অস্ত $time"
    AppLanguage.SPANISH -> "se pone a las $time"
    AppLanguage.PORTUGUESE -> "põe-se às $time"
    AppLanguage.ENGLISH -> "sets $time"
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

  /**
   * The destination of the bearing, shown as "278 deg to the Kaaba".
   *
   * Deliberately short, and worded so the Kaaba reads as where the arrow *points* rather than
   * where it is measured from - "278 Towards Kaaba, Makkah" beside a compass was read on a device
   * as the app computing the direction from Makkah. The origin is stated separately, under the
   * title. Short also keeps the chip from wrapping in the row it shares with that title.
   */
  fun getTowardsKaabaLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "نحو الكعبة"
    AppLanguage.BENGALI -> "কাবার দিকে"
    AppLanguage.SPANISH -> "hacia la Kaaba"
    AppLanguage.PORTUGUESE -> "para a Caaba"
    AppLanguage.ENGLISH -> "to the Kaaba"
  }

  /** The badge on the prayer that is in progress. Short in every language, by necessity. */
  /** Shown during the last third of the night. The hadith of the divine descent (Bukhari 1145). */
  fun getSelectLocationTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "اختر الموقع"
    AppLanguage.BENGALI -> "অবস্থান নির্বাচন করুন"
    AppLanguage.SPANISH -> "Seleccionar ubicación"
    AppLanguage.PORTUGUESE -> "Selecionar localização"
    AppLanguage.ENGLISH -> "Select location"
  }

  fun getSelectLocationSubtitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "لحساب مواقيت الصلاة والطقس لمنطقتك"
    AppLanguage.BENGALI -> "আপনার এলাকার নামাজের সময় ও আবহাওয়া নির্ণয়ের জন্য"
    AppLanguage.SPANISH -> "Calcula los horarios de oración y el tiempo de tu zona"
    AppLanguage.PORTUGUESE -> "Calcula os horários de oração e o tempo da sua zona"
    AppLanguage.ENGLISH -> "Calculates prayer times and weather for your area"
  }

  fun getSearchCityPlaceholder(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "ابحث عن مدينة أو دولة..."
    AppLanguage.BENGALI -> "শহর বা দেশ খুঁজুন..."
    AppLanguage.SPANISH -> "Buscar ciudad o país..."
    AppLanguage.PORTUGUESE -> "Procurar cidade ou país..."
    AppLanguage.ENGLISH -> "Search city or country..."
  }

  /** Accessibility label for a search field icon. */
  fun getSearchLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "بحث"
    AppLanguage.BENGALI -> "খুঁজুন"
    AppLanguage.SPANISH -> "Buscar"
    AppLanguage.PORTUGUESE -> "Procurar"
    AppLanguage.ENGLISH -> "Search"
  }

  /** Accessibility label for a selected-item checkmark. */
  fun getSelectedLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "محدد"
    AppLanguage.BENGALI -> "নির্বাচিত"
    AppLanguage.SPANISH -> "Seleccionado"
    AppLanguage.PORTUGUESE -> "Selecionado"
    AppLanguage.ENGLISH -> "Selected"
  }

  fun getNotificationPermissionGranted(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تم منح إذن التنبيهات"
    AppLanguage.BENGALI -> "বিজ্ঞপ্তির অনুমতি দেওয়া হয়েছে"
    AppLanguage.SPANISH -> "Permiso de notificaciones concedido"
    AppLanguage.PORTUGUESE -> "Permissão de notificações concedida"
    AppLanguage.ENGLISH -> "Notification permission granted"
  }

  fun getEnableNotificationsInSettings(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "يرجى تفعيل التنبيهات من إعدادات النظام"
    AppLanguage.BENGALI -> "অনুগ্রহ করে সিস্টেম সেটিংসে বিজ্ঞপ্তি চালু করুন"
    AppLanguage.SPANISH -> "Activa las notificaciones en los ajustes del sistema"
    AppLanguage.PORTUGUESE -> "Ative as notificações nas definições do sistema"
    AppLanguage.ENGLISH -> "Please enable notifications in system settings"
  }

  fun getSunnahNaflLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "سنة / نافلة"
    AppLanguage.BENGALI -> "সুন্নাত / নফল"
    AppLanguage.SPANISH -> "Sunna / nafl"
    AppLanguage.PORTUGUESE -> "Suna / nafl"
    AppLanguage.ENGLISH -> "Sunnah / nafl"
  }

  fun getLoadingCelestialTimes(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "جارٍ تحميل أوقات اليوم الفلكية..."
    AppLanguage.BENGALI -> "আজকের জ্যোতির্বিদ্যাগত সময় লোড হচ্ছে..."
    AppLanguage.SPANISH -> "Cargando los horarios astronómicos de hoy..."
    AppLanguage.PORTUGUESE -> "A carregar os horários astronómicos de hoje..."
    AppLanguage.ENGLISH -> "Loading today's celestial times..."
  }

  fun getActiveNowBadge(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "نشط الآن"
    AppLanguage.BENGALI -> "এখন চলছে"
    AppLanguage.SPANISH -> "ACTIVO AHORA"
    AppLanguage.PORTUGUESE -> "ATIVO AGORA"
    AppLanguage.ENGLISH -> "ACTIVE NOW"
  }

  /** Accessibility label for a filter menu icon. */
  fun getFilterLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تصفية"
    AppLanguage.BENGALI -> "ফিল্টার"
    AppLanguage.SPANISH -> "Filtrar"
    AppLanguage.PORTUGUESE -> "Filtrar"
    AppLanguage.ENGLISH -> "Filter"
  }

  fun getAddBookmarkLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "إضافة إلى المحفوظات"
    AppLanguage.BENGALI -> "বুকমার্ক করুন"
    AppLanguage.SPANISH -> "Guardar en favoritos"
    AppLanguage.PORTUGUESE -> "Adicionar aos favoritos"
    AppLanguage.ENGLISH -> "Bookmark"
  }

  fun getRemoveBookmarkLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "إزالة من المحفوظات"
    AppLanguage.BENGALI -> "বুকমার্ক সরান"
    AppLanguage.SPANISH -> "Quitar de favoritos"
    AppLanguage.PORTUGUESE -> "Remover dos favoritos"
    AppLanguage.ENGLISH -> "Remove bookmark"
  }

  fun getRefreshWeatherLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تحديث الطقس"
    AppLanguage.BENGALI -> "আবহাওয়া হালনাগাদ করুন"
    AppLanguage.SPANISH -> "Actualizar el tiempo"
    AppLanguage.PORTUGUESE -> "Atualizar o tempo"
    AppLanguage.ENGLISH -> "Refresh weather"
  }

  /** Accessibility label for the brand logo. */
  fun getAppLogoDescription(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "شعار ${getAppName(language)}"
    AppLanguage.BENGALI -> "${getAppName(language)} লোগো"
    AppLanguage.SPANISH -> "Logotipo de ${getAppName(language)}"
    AppLanguage.PORTUGUESE -> "Logótipo de ${getAppName(language)}"
    AppLanguage.ENGLISH -> "${getAppName(language)} logo"
  }

  fun getNarratedBy(narrator: String, source: String, language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "— رواه $narrator ($source)"
    AppLanguage.BENGALI -> "— বর্ণনাকারী: $narrator ($source)"
    AppLanguage.SPANISH -> "— Narrado por $narrator ($source)"
    AppLanguage.PORTUGUESE -> "— Narrado por $narrator ($source)"
    AppLanguage.ENGLISH -> "— Narrated by $narrator ($source)"
  }

  fun getTahajjudBannerMessage(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "لقد نزل الله سبحانه وتعالى إلى السماء الدنيا، فاسألوا ما شئتم."
    AppLanguage.BENGALI -> "আল্লাহ সুবহানাহু ওয়া তায়ালা প্রথম আসমানে নেমে এসেছেন, আপনার যা খুশি চাইতে পারেন।"
    AppLanguage.SPANISH -> "Allah, glorificado y exaltado sea, ha descendido al cielo más cercano: pide lo que desees."
    AppLanguage.PORTUGUESE -> "Allah, glorificado e exaltado seja, desceu ao céu mais próximo: peça o que desejar."
    AppLanguage.ENGLISH -> "Allah, glorified and exalted, has descended to the nearest heaven: ask for whatever you wish."
  }

  /** Stands in for the prayer name before a schedule has resolved. */
  fun getPrayerPlaceholder(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الصلاة"
    AppLanguage.BENGALI -> "নামাজ"
    AppLanguage.SPANISH -> "Oración"
    AppLanguage.PORTUGUESE -> "Oração"
    AppLanguage.ENGLISH -> "Prayer"
  }

  fun getProhibitedNowBadge(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "وقت نهي الآن"
    AppLanguage.BENGALI -> "এখন নিষিদ্ধ"
    AppLanguage.SPANISH -> "PROHIBIDO AHORA"
    AppLanguage.PORTUGUESE -> "PROIBIDO AGORA"
    AppLanguage.ENGLISH -> "PROHIBITED NOW"
  }

  fun getNaflProhibitedUntil(endTime: String, minutesLeft: Long, language: AppLanguage): String {
    val m = localizeDigits(minutesLeft.toString(), language)
    return when (language) {
      AppLanguage.ARABIC -> "تُكره صلاة النافلة حتى $endTime (متبقٍّ $m دقيقة)"
      AppLanguage.BENGALI -> "$endTime পর্যন্ত নফল নামাজ নিষিদ্ধ ($m মিনিট বাকি)"
      AppLanguage.SPANISH -> "Oraciones voluntarias (nafl) prohibidas hasta las $endTime (quedan $m min)"
      AppLanguage.PORTUGUESE -> "Orações voluntárias (nafl) proibidas até às $endTime (faltam $m min)"
      AppLanguage.ENGLISH -> "Voluntary (nafl) prayers prohibited until $endTime ($m min left)"
    }
  }

  fun getPrayerPermittedNextProhibited(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الصلاة جائزة • وقت النهي القادم:"
    AppLanguage.BENGALI -> "নামাজ আদায় বৈধ • পরবর্তী নিষিদ্ধ সময়:"
    AppLanguage.SPANISH -> "Oración permitida • Próximo periodo prohibido:"
    AppLanguage.PORTUGUESE -> "Oração permitida • Próximo período proibido:"
    AppLanguage.ENGLISH -> "Prayer permitted • Next prohibited window:"
  }

  fun getAlertsChipLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "التنبيهات"
    AppLanguage.BENGALI -> "সতর্কতা"
    AppLanguage.SPANISH -> "Avisos"
    AppLanguage.PORTUGUESE -> "Avisos"
    AppLanguage.ENGLISH -> "Alerts"
  }

  fun getDetailsLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "التفاصيل"
    AppLanguage.BENGALI -> "বিস্তারিত"
    AppLanguage.SPANISH -> "Detalles"
    AppLanguage.PORTUGUESE -> "Detalhes"
    AppLanguage.ENGLISH -> "Details"
  }

  fun getNowBadge(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الآن"
    AppLanguage.BENGALI -> "এখন"
    AppLanguage.SPANISH -> "AHORA"
    AppLanguage.PORTUGUESE -> "AGORA"
    AppLanguage.ENGLISH -> "NOW"
  }

  /** The badge on the prayer that comes next. */
  fun getNextBadge(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "التالي"
    AppLanguage.BENGALI -> "পরবর্তী"
    AppLanguage.SPANISH -> "SIGUE"
    AppLanguage.PORTUGUESE -> "SEGUE"
    AppLanguage.ENGLISH -> "NEXT"
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

  fun getReliabilityFullScreenRow(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تنبيه بملء الشاشة"
    AppLanguage.BENGALI -> "পূর্ণ স্ক্রিন অ্যালার্ম"
    AppLanguage.SPANISH -> "Alarma a pantalla completa"
    AppLanguage.PORTUGUESE -> "Alarme em ecrã inteiro"
    AppLanguage.ENGLISH -> "Full-screen alarm"
  }

  fun getReliabilityFixAction(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "إصلاح"
    AppLanguage.BENGALI -> "ঠিক করুন"
    AppLanguage.SPANISH -> "Corregir"
    AppLanguage.PORTUGUESE -> "Corrigir"
    AppLanguage.ENGLISH -> "Fix"
  }

  // ---------------------------------------------------------------------------------------------
  // The full-screen alarm
  // ---------------------------------------------------------------------------------------------

  /** The single action on the alarm screen. Not "dismiss": it stops a sound that is playing. */
  fun getStopAlarmAction(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "إيقاف"
    AppLanguage.BENGALI -> "বন্ধ করুন"
    AppLanguage.SPANISH -> "Detener"
    AppLanguage.PORTUGUESE -> "Parar"
    AppLanguage.ENGLISH -> "Stop"
  }

  fun getAlarmAutoStopHint(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "يتوقف الصوت من تلقاء نفسه بعد خمس ثوانٍ."
    AppLanguage.BENGALI -> "পাঁচ সেকেন্ড পর শব্দ নিজেই বন্ধ হয়ে যাবে।"
    AppLanguage.SPANISH -> "El sonido se detiene por sí solo a los cinco segundos."
    AppLanguage.PORTUGUESE -> "O som para sozinho após cinco segundos."
    AppLanguage.ENGLISH -> "The sound stops on its own after five seconds."
  }

  // ---------------------------------------------------------------------------------------------
  // Locating diagnoses
  //
  // Each of these names the rung that failed and pairs with an action that would fix it. They
  // exist because the first version collapsed every failure - permission refused, Location
  // switched off, genuine indoor no-fix - into the same manual city picker, so on the device they
  // were indistinguishable from each other and from the app simply not trying.
  // ---------------------------------------------------------------------------------------------

  /** Shown behind the system permission dialog, so the dialog is not context-free. */
  fun getLocationRationale(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "يحتاج التطبيق إلى الوصول إلى الموقع ليقرأ إحداثياتك من الأقمار الصناعية. لا يترك ذلك هاتفك أبدًا."
    AppLanguage.BENGALI -> "স্যাটেলাইট থেকে আপনার অবস্থান পড়তে অ্যাপটির লোকেশন অনুমতি প্রয়োজন। এই তথ্য কখনও আপনার ফোন ছেড়ে যায় না।"
    AppLanguage.SPANISH -> "La app necesita permiso de ubicación para leer tus coordenadas de los satélites. Nunca salen de tu teléfono."
    AppLanguage.PORTUGUESE -> "A app precisa de permissão de localização para ler as suas coordenadas dos satélites. Nunca saem do seu telefone."
    AppLanguage.ENGLISH -> "The app needs location access to read your coordinates from the satellites. They never leave your phone."
  }

  fun getLocationPermissionDeniedTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "لا يمكن الوصول إلى الموقع"
    AppLanguage.BENGALI -> "লোকেশনের অনুমতি নেই"
    AppLanguage.SPANISH -> "Sin acceso a la ubicación"
    AppLanguage.PORTUGUESE -> "Sem acesso à localização"
    AppLanguage.ENGLISH -> "No location access"
  }

  fun getLocationPermissionDeniedBody(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "بدون هذا الإذن لا يمكن استخدام قارئ الأقمار الصناعية، وهو الطريقة الوحيدة لمعرفة موقعك دون إنترنت. يمكنك منحه من الإعدادات، أو اختيار مدينتك بنفسك."
    AppLanguage.BENGALI -> "এই অনুমতি ছাড়া স্যাটেলাইট রিসিভার ব্যবহার করা যায় না — ইন্টারনেট ছাড়া অবস্থান জানার এটিই একমাত্র উপায়। সেটিংস থেকে অনুমতি দিতে পারেন, বা নিজে শহর বেছে নিতে পারেন।"
    AppLanguage.SPANISH -> "Sin este permiso no se puede usar el receptor de satélites, que es la única forma de saber dónde estás sin internet. Puedes concederlo en los ajustes, o elegir tu ciudad a mano."
    AppLanguage.PORTUGUESE -> "Sem esta permissão não é possível usar o recetor de satélites, a única forma de saber onde está sem internet. Pode concedê-la nas definições, ou escolher a sua cidade manualmente."
    AppLanguage.ENGLISH -> "Without it the satellite receiver cannot be used, and that is the only way to know where you are with no internet. You can grant it in settings, or pick your city by hand."
  }

  fun getLocationOffTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "خدمة الموقع مغلقة"
    AppLanguage.BENGALI -> "লোকেশন বন্ধ আছে"
    AppLanguage.SPANISH -> "La ubicación está desactivada"
    AppLanguage.PORTUGUESE -> "A localização está desativada"
    AppLanguage.ENGLISH -> "Location is switched off"
  }

  fun getLocationOffBody(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "مفتاح الموقع في إعدادات الجهاز مغلق، فلا يمكن للتطبيق قراءة الأقمار الصناعية. الانتظار لن يغيّر ذلك."
    AppLanguage.BENGALI -> "ডিভাইস সেটিংসে লোকেশন সুইচ বন্ধ, তাই অ্যাপ স্যাটেলাইট পড়তে পারছে না। অপেক্ষা করলে এটি ঠিক হবে না।"
    AppLanguage.SPANISH -> "El interruptor de ubicación del sistema está apagado, así que la app no puede leer los satélites. Esperar no lo cambiará."
    AppLanguage.PORTUGUESE -> "O interruptor de localização do sistema está desligado, pelo que a app não consegue ler os satélites. Esperar não resolve."
    AppLanguage.ENGLISH -> "Your device's Location switch is off, so the app cannot read the satellites. Waiting will not change that."
  }

  fun getGpsOffTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الأقمار الصناعية غير مفعّلة"
    AppLanguage.BENGALI -> "জিপিএস চালু নেই"
    AppLanguage.SPANISH -> "El GPS no está activado"
    AppLanguage.PORTUGUESE -> "O GPS não está ativado"
    AppLanguage.ENGLISH -> "GPS is not switched on"
  }

  /**
   * The message for the case that reads as a broken app: Location on, GPS off.
   *
   * It opens by acknowledging that location *is* on, because the user has very likely just
   * turned it on and being told to do it again is what makes software feel stupid.
   */
  fun getGpsOffBody(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "خدمة الموقع مفعّلة، لكن جهازك يحدّد الموقع من شبكات الواي فاي وأبراج الاتصال فقط — وهذا لا يعمل بدون إنترنت. اضغط بالأسفل لتفعيل الدقة العالية، وهي التي تشغّل قارئ الأقمار الصناعية."
    AppLanguage.BENGALI -> "লোকেশন চালু আছে, কিন্তু আপনার ডিভাইস কেবল ওয়াই-ফাই ও মোবাইল টাওয়ার থেকে অবস্থান নির্ণয় করছে — ইন্টারনেট ছাড়া এটি কাজ করে না। নিচে চাপ দিয়ে উচ্চ নির্ভুলতা চালু করুন, এতেই স্যাটেলাইট রিসিভার চালু হয়।"
    AppLanguage.SPANISH -> "La ubicación está activada, pero tu dispositivo se posiciona solo con Wi-Fi y antenas móviles, y eso no funciona sin internet. Toca abajo para activar la alta precisión, que es lo que enciende el receptor de satélites."
    AppLanguage.PORTUGUESE -> "A localização está ativada, mas o seu dispositivo posiciona-se apenas por Wi-Fi e antenas móveis, e isso não funciona sem internet. Toque abaixo para ativar a alta precisão, que é o que liga o recetor de satélites."
    AppLanguage.ENGLISH -> "Location is on, but your device is positioning from Wi-Fi and mobile masts only, and that does not work without internet. Tap below to turn on high accuracy, which is what switches the satellite receiver on."
  }

  /** The one-tap action. Names the outcome, not the setting, because the setting is per-OEM. */
  fun getEnablePositioningAction(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تفعيل الدقة العالية"
    AppLanguage.BENGALI -> "উচ্চ নির্ভুলতা চালু করুন"
    AppLanguage.SPANISH -> "Activar alta precisión"
    AppLanguage.PORTUGUESE -> "Ativar alta precisão"
    AppLanguage.ENGLISH -> "Turn on high accuracy"
  }

  fun getOpenSettingsAction(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "فتح الإعدادات"
    AppLanguage.BENGALI -> "সেটিংস খুলুন"
    AppLanguage.SPANISH -> "Abrir ajustes"
    AppLanguage.PORTUGUESE -> "Abrir definições"
    AppLanguage.ENGLISH -> "Open settings"
  }

  fun getNoFixTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "لم تصل إشارة بعد"
    AppLanguage.BENGALI -> "এখনও সংকেত আসেনি"
    AppLanguage.SPANISH -> "Todavía sin señal"
    AppLanguage.PORTUGUESE -> "Ainda sem sinal"
    AppLanguage.ENGLISH -> "No signal yet"
  }

  /** The honest version: it is still trying, and going outside is what actually fixes this. */
  fun getNoFixBody(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "لم يصل تحديد الموقع بعد — وهذا شائع داخل المباني. التطبيق سيواصل الاستماع في الخلفية وسيضبط موقعك تلقائيًا بمجرد وصول الإشارة. حتى ذلك الحين يمكنك اختيار مدينتك."
    AppLanguage.BENGALI -> "এখনও অবস্থান পাওয়া যায়নি — ঘরের ভিতরে এটি স্বাভাবিক। অ্যাপটি পটভূমিতে শুনতে থাকবে এবং সংকেত এলেই নিজে থেকে আপনার অবস্থান ঠিক করে নেবে। ততক্ষণ আপনি শহর বেছে নিতে পারেন।"
    AppLanguage.SPANISH -> "Aún no hay posición, lo que es normal en interiores. La app seguirá escuchando en segundo plano y fijará tu ubicación en cuanto llegue la señal. Hasta entonces puedes elegir tu ciudad."
    AppLanguage.PORTUGUESE -> "Ainda não há posição, o que é normal dentro de casa. A app continuará a escutar em segundo plano e definirá a sua localização assim que o sinal chegar. Até lá pode escolher a sua cidade."
    AppLanguage.ENGLISH -> "No position yet, which is normal indoors. The app will keep listening in the background and set your location by itself as soon as a signal arrives. Until then you can pick your city."
  }

  fun getTryAgainAction(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "إعادة المحاولة"
    AppLanguage.BENGALI -> "আবার চেষ্টা করুন"
    AppLanguage.SPANISH -> "Reintentar"
    AppLanguage.PORTUGUESE -> "Tentar novamente"
    AppLanguage.ENGLISH -> "Try again"
  }

  /** The location chip when the app has no location of its own. Never a city name. */
  fun getLocationNotSetLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "اضبط موقعك"
    AppLanguage.BENGALI -> "অবস্থান নির্ধারণ করুন"
    AppLanguage.SPANISH -> "Define tu ubicación"
    AppLanguage.PORTUGUESE -> "Defina a sua localização"
    AppLanguage.ENGLISH -> "Set your location"
  }

  /** On the Qibla screen: the bearing is measured from the phone, not from the chosen city. */
  fun getQiblaFromDeviceLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "من موقعك الحالي"
    AppLanguage.BENGALI -> "আপনার বর্তমান অবস্থান থেকে"
    AppLanguage.SPANISH -> "Desde tu posición actual"
    AppLanguage.PORTUGUESE -> "Da sua posição atual"
    AppLanguage.ENGLISH -> "From your current position"
  }

  /** And the warning when it is not: a bearing from elsewhere points elsewhere. */
  fun getQiblaFromCityLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "محسوب من %s — شغّل الموقع للحصول على اتجاه دقيق"
    AppLanguage.BENGALI -> "%s থেকে গণনা করা — সঠিক দিকের জন্য লোকেশন চালু করুন"
    AppLanguage.SPANISH -> "Calculado desde %s: activa la ubicación para una dirección exacta"
    AppLanguage.PORTUGUESE -> "Calculado a partir de %s: ative a localização para uma direção exata"
    AppLanguage.ENGLISH -> "Measured from %s — turn on location for an exact direction"
  }

  // ---------------------------------------------------------------------------------------------
  // First-run satellite acquisition
  //
  // The wait is shown rather than hidden, because a cold GPS fix genuinely takes up to a minute
  // and a bare spinner for that long reads as a hang. The copy says what the phone is doing and
  // why it may not work indoors, so a failure is understood rather than mysterious.
  // ---------------------------------------------------------------------------------------------

  fun getLocatingTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "جارٍ تحديد موقعك"
    AppLanguage.BENGALI -> "আপনার অবস্থান নির্ণয় করা হচ্ছে"
    AppLanguage.SPANISH -> "Localizándote"
    AppLanguage.PORTUGUESE -> "A localizá-lo"
    AppLanguage.ENGLISH -> "Finding where you are"
  }

  fun getLocatingBody(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "يقرأ هاتفك إشارات الأقمار الصناعية مباشرة. لا يحتاج ذلك إلى إنترنت، ويمنح مواقيت صلاة دقيقة تمامًا."
    AppLanguage.BENGALI -> "আপনার ফোন সরাসরি স্যাটেলাইট থেকে সংকেত পড়ছে। এতে ইন্টারনেট লাগে না, আর নামাজের সময় একেবারে নির্ভুল হয়।"
    AppLanguage.SPANISH -> "Tu teléfono está leyendo señales de satélite directamente. No necesita internet, y da horarios de oración exactos."
    AppLanguage.PORTUGUESE -> "O seu telefone está a ler sinais de satélite diretamente. Não precisa de internet, e dá horários de oração exatos."
    AppLanguage.ENGLISH -> "Your phone is reading satellite signals directly. No internet needed, and it gives exact prayer times."
  }

  /** The honest caveat: a cold fix is slow, and indoors it may never arrive. */
  fun getLocatingHint(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "قد تستغرق المحاولة الأولى دقيقة. إن كنت داخل مبنى، فالاقتراب من نافذة يساعد كثيرًا."
    AppLanguage.BENGALI -> "প্রথমবার এক মিনিট পর্যন্ত লাগতে পারে। ঘরের ভিতরে থাকলে জানালার কাছে গেলে অনেক সাহায্য হয়।"
    AppLanguage.SPANISH -> "El primer intento puede tardar un minuto. Si estás en interiores, acercarte a una ventana ayuda mucho."
    AppLanguage.PORTUGUESE -> "A primeira tentativa pode levar um minuto. Se estiver dentro de casa, aproximar-se de uma janela ajuda muito."
    AppLanguage.ENGLISH -> "The first attempt can take a minute. Indoors, moving near a window helps a great deal."
  }

  /** Offered throughout, so the wait is never a trap. */
  fun getEnterLocationManuallyAction(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "أدخل موقعي بنفسي"
    AppLanguage.BENGALI -> "নিজে অবস্থান লিখে দিন"
    AppLanguage.SPANISH -> "Introducir mi ubicación"
    AppLanguage.PORTUGUESE -> "Introduzir a minha localização"
    AppLanguage.ENGLISH -> "Enter my location instead"
  }

  // ---------------------------------------------------------------------------------------------
  // Satellite signal diagnostics
  //
  // Exists to turn "no signal" - which covered a switched-off receiver, a receiver that hears
  // nothing, and a receiver mid-download of orbit data - into three distinguishable answers.
  // ---------------------------------------------------------------------------------------------

  fun getSatelliteSignalTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "إشارة الأقمار الصناعية"
    AppLanguage.BENGALI -> "স্যাটেলাইট সংকেত"
    AppLanguage.SPANISH -> "Señal de satélite"
    AppLanguage.PORTUGUESE -> "Sinal de satélite"
    AppLanguage.ENGLISH -> "Satellite signal"
  }

  fun getSatelliteSignalAction(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "فحص إشارة الأقمار الصناعية"
    AppLanguage.BENGALI -> "স্যাটেলাইট সংকেত পরীক্ষা করুন"
    AppLanguage.SPANISH -> "Comprobar señal de satélite"
    AppLanguage.PORTUGUESE -> "Verificar sinal de satélite"
    AppLanguage.ENGLISH -> "Check satellite signal"
  }

  fun getSignalPermissionRow(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "إذن الموقع"
    AppLanguage.BENGALI -> "লোকেশন অনুমতি"
    AppLanguage.SPANISH -> "Permiso de ubicación"
    AppLanguage.PORTUGUESE -> "Permissão de localização"
    AppLanguage.ENGLISH -> "Location permission"
  }

  fun getSignalLocationRow(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "خدمة الموقع مفعّلة"
    AppLanguage.BENGALI -> "লোকেশন চালু"
    AppLanguage.SPANISH -> "Ubicación activada"
    AppLanguage.PORTUGUESE -> "Localização ativada"
    AppLanguage.ENGLISH -> "Location switched on"
  }

  fun getSignalGpsRow(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "قارئ الأقمار الصناعية (GPS) مفعّل"
    AppLanguage.BENGALI -> "জিপিএস রিসিভার চালু"
    AppLanguage.SPANISH -> "Receptor GPS activado"
    AppLanguage.PORTUGUESE -> "Recetor GPS ativado"
    AppLanguage.ENGLISH -> "GPS receiver on"
  }

  fun getSignalNotListening(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "لا يمكن الاستماع للأقمار الصناعية حتى تُفعَّل العناصر المعلَّمة بالأحمر أعلاه."
    AppLanguage.BENGALI -> "উপরে লাল চিহ্নিত বিষয়গুলো চালু না হওয়া পর্যন্ত স্যাটেলাইট শোনা যাবে না।"
    AppLanguage.SPANISH -> "No se puede escuchar a los satélites hasta que se active lo marcado en rojo arriba."
    AppLanguage.PORTUGUESE -> "Não é possível ouvir os satélites até ativar o que está a vermelho acima."
    AppLanguage.ENGLISH -> "Cannot listen for satellites until the items marked red above are switched on."
  }

  fun getSignalVisibleLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "أقمار ضمن المدى"
    AppLanguage.BENGALI -> "দৃশ্যমান স্যাটেলাইট"
    AppLanguage.SPANISH -> "Satélites a la vista"
    AppLanguage.PORTUGUESE -> "Satélites à vista"
    AppLanguage.ENGLISH -> "Satellites in view"
  }

  fun getSignalUsedLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "مستخدمة في تحديد الموقع"
    AppLanguage.BENGALI -> "অবস্থান নির্ণয়ে ব্যবহৃত"
    AppLanguage.SPANISH -> "Usados para la posición"
    AppLanguage.PORTUGUESE -> "Usados para a posição"
    AppLanguage.ENGLISH -> "Used for the fix"
  }

  fun getSignalStrongestLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "أقوى إشارة"
    AppLanguage.BENGALI -> "সবচেয়ে শক্তিশালী সংকেত"
    AppLanguage.SPANISH -> "Señal más fuerte"
    AppLanguage.PORTUGUESE -> "Sinal mais forte"
    AppLanguage.ENGLISH -> "Strongest signal"
  }

  /** Four or more satellites in the solution: a position is seconds away. */
  fun getSignalVerdictFixing(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الاستقبال ممتاز. يجري تحديد موقعك الآن."
    AppLanguage.BENGALI -> "সংকেত চমৎকার। এখনই আপনার অবস্থান নির্ণয় হচ্ছে।"
    AppLanguage.SPANISH -> "Recepción excelente. Se está calculando tu posición ahora."
    AppLanguage.PORTUGUESE -> "Receção excelente. A sua posição está a ser calculada agora."
    AppLanguage.ENGLISH -> "Reception is good. Your position is being worked out right now."
  }

  /** Heard, but not yet enough of them - almost always the orbit-data download. */
  fun getSignalVerdictAcquiring(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الأقمار مسموعة، والهاتف لا يزال يقرأ بيانات مداراتها. هذه أبطأ خطوة؛ امنحه دقيقة تحت سماء مفتوحة."
    AppLanguage.BENGALI -> "স্যাটেলাইট শোনা যাচ্ছে, ফোন এখনও তাদের কক্ষপথের তথ্য পড়ছে। এটিই সবচেয়ে ধীর ধাপ — খোলা আকাশের নিচে এক মিনিট সময় দিন।"
    AppLanguage.SPANISH -> "Se oyen satélites; el teléfono aún está leyendo sus datos orbitales. Ese es el paso lento: dale un minuto a cielo abierto."
    AppLanguage.PORTUGUESE -> "Ouvem-se satélites; o telefone ainda está a ler os dados orbitais. Esse é o passo lento: dê-lhe um minuto a céu aberto."
    AppLanguage.ENGLISH -> "Satellites are being heard, and the phone is still reading their orbit data. That is the slow step — give it a minute under open sky."
  }

  /** Detected but too weak to use: a roof, in other words. */
  fun getSignalVerdictWeak(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الإشارات ضعيفة جدًا للاستخدام — عادةً بسبب سقف أو جدار. الاقتراب من نافذة أو الخروج يغيّر ذلك خلال ثوانٍ."
    AppLanguage.BENGALI -> "সংকেত ব্যবহারের জন্য খুব দুর্বল — সাধারণত ছাদ বা দেয়ালের কারণে। জানালার কাছে গেলে বা বাইরে বেরোলে কয়েক সেকেন্ডেই বদলে যায়।"
    AppLanguage.SPANISH -> "Las señales son demasiado débiles para usarlas, normalmente por un techo o una pared. Acercarse a una ventana o salir lo cambia en segundos."
    AppLanguage.PORTUGUESE -> "Os sinais são demasiado fracos para usar, normalmente por causa de um teto ou parede. Aproximar-se de uma janela ou sair muda isso em segundos."
    AppLanguage.ENGLISH -> "Signals are too weak to use — usually a roof or a wall. Moving near a window or stepping outside changes this within seconds."
  }

  /** Nothing at all. */
  fun getSignalVerdictNothing(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "لا تُسمع أي أقمار بعد. هذا متوقع داخل المباني؛ يحتاج القارئ إلى رؤية السماء."
    AppLanguage.BENGALI -> "এখনও কোনো স্যাটেলাইট শোনা যাচ্ছে না। ঘরের ভিতরে এটি স্বাভাবিক — রিসিভারের আকাশ দেখা দরকার।"
    AppLanguage.SPANISH -> "Todavía no se oye ningún satélite. Es lo normal en interiores: el receptor necesita ver el cielo."
    AppLanguage.PORTUGUESE -> "Ainda não se ouve nenhum satélite. É normal dentro de casa: o recetor precisa de ver o céu."
    AppLanguage.ENGLISH -> "No satellites heard yet. That is expected indoors — the receiver needs a view of the sky."
  }

  fun getTestAlarmAction(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تجربة تنبيه بعد دقيقة"
    AppLanguage.BENGALI -> "১ মিনিট পর পরীক্ষামূলক অ্যালার্ম"
    AppLanguage.SPANISH -> "Probar aviso en 1 minuto"
    AppLanguage.PORTUGUESE -> "Testar aviso em 1 minuto"
    AppLanguage.ENGLISH -> "Test alert in 1 minute"
  }

  /** Under the test button while the test alarm is pending. [seconds] is localized. */
  fun getTestAlarmCountdown(seconds: Int, language: AppLanguage): String = localizeDigits(
    when (language) {
      AppLanguage.ARABIC -> "تم ضبط التنبيه التجريبي. يرن بعد $seconds ثانية - يمكنك إغلاق التطبيق."
      AppLanguage.BENGALI -> "পরীক্ষামূলক অ্যালার্ম সেট হয়েছে। $seconds সেকেন্ড পর বাজবে - চাইলে অ্যাপ বন্ধ করতে পারেন।"
      AppLanguage.SPANISH -> "Alarma de prueba programada. Sonará en $seconds s; puede cerrar la app."
      AppLanguage.PORTUGUESE -> "Alarme de teste agendado. Toca em $seconds s; pode fechar a app."
      AppLanguage.ENGLISH -> "Test alarm set. It rings in $seconds s - you can close the app."
    },
    language
  )

  /** Once the minute is up. Points at the reliability rows if nothing rang. */
  fun getTestAlarmDue(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "كان يجب أن يرن الآن. إن لم يرن، أصلح البنود المعلَّمة أعلاه."
    AppLanguage.BENGALI -> "এখন বাজার কথা। না বাজলে ওপরের চিহ্নিত বিষয়গুলো ঠিক করুন।"
    AppLanguage.SPANISH -> "Debería haber sonado. Si no, corrija los puntos marcados arriba."
    AppLanguage.PORTUGUESE -> "Já devia ter tocado. Se não tocou, corrija os itens marcados acima."
    AppLanguage.ENGLISH -> "It should have rung by now. If it didn't, fix the items marked above."
  }

  /**
   * Shown on manufacturers whose power management drops alarms by default.
   *
   * Names the exact switches, because none of them can be set from code and vague advice leaves
   * the user hunting. On ColorOS the decisive one is **Battery > App battery usage > Run in
   * Background**: the default "Smart background running" is what silences a Fajr alarm.
   */
  fun getOemBatteryHint(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "على أجهزة أوبو وشاومي وفيفو: اختر «التشغيل في الخلفية» بدلًا من «التشغيل الذكي في الخلفية»، وفعّل التشغيل التلقائي في مدير بدء التشغيل، وأوقف تحسين وضع السكون، وثبّت التطبيق في قائمة التطبيقات الأخيرة."
    AppLanguage.BENGALI -> "OPPO/Xiaomi/Vivo ডিভাইসে: “Smart Background Running”-এর বদলে “Run in Background” বেছে নিন, Startup Manager-এ অটো-লঞ্চ চালু করুন, Sleep standby optimisation বন্ধ করুন, এবং রিসেন্ট অ্যাপে অ্যাপটি লক করুন।"
    AppLanguage.SPANISH -> "En OPPO/Xiaomi/Vivo: elige «Ejecutar en segundo plano» en lugar de «Ejecución inteligente», activa el inicio automático en el Gestor de inicio, desactiva la optimización de reposo y bloquea la app en Recientes."
    AppLanguage.PORTUGUESE -> "Em OPPO/Xiaomi/Vivo: escolha «Executar em segundo plano» em vez de «Execução inteligente», ative o arranque automático no Gestor de arranque, desative a otimização de suspensão e bloqueie a app em Recentes."
    AppLanguage.ENGLISH -> "On OPPO/Xiaomi/Vivo: set Run in Background rather than Smart background running, enable auto-launch in Startup Manager, turn off Sleep standby optimisation, and lock the app in Recents."
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

  /** The location comes from the phone; internet is only needed to put a name to it. */
  fun getAutoDetectLocationLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "استخدم موقعي"
    AppLanguage.BENGALI -> "আমার অবস্থান ব্যবহার করুন"
    AppLanguage.SPANISH -> "Usar mi ubicación"
    AppLanguage.PORTUGUESE -> "Usar a minha localização"
    AppLanguage.ENGLISH -> "Use my location"
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

  /** Native digits for Bengali and Arabic; everything else keeps Latin digits. */
  fun localizeDigits(text: String, language: AppLanguage): String {
    val digits = when (language) {
      AppLanguage.BENGALI -> "০১২৩৪৫৬৭৮৯"
      AppLanguage.ARABIC -> "٠١٢٣٤٥٦٧٨٩"
      else -> return text
    }
    return buildString(text.length) {
      text.forEach { c -> append(if (c in '0'..'9') digits[c - '0'] else c) }
    }
  }

  fun formatTime(time: java.time.LocalTime, language: AppLanguage): String {
    // Locale.ENGLISH pins the AM/PM marker. With the device default, a phone set to another
    // locale could already emit a localized marker, and the replacements would silently miss it.
    val formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.ENGLISH)
    return localizeTimeText(time.format(formatter), language)
  }

  /** Localizes an already-formatted English "hh:mm AM" string - period marker and digits. */
  fun localizeTimeText(formatted: String, language: AppLanguage): String {
    val withPeriod = when (language) {
      AppLanguage.BENGALI -> formatted.replace("AM", "পূর্বাহ্ণ").replace("PM", "অপরাহ্ণ")
      AppLanguage.ARABIC -> formatted.replace("AM", "ص").replace("PM", "م")
      else -> formatted
    }
    return localizeDigits(withPeriod, language)
  }

  fun formatTimeRange(
    start: java.time.LocalTime,
    end: java.time.LocalTime,
    language: AppLanguage
  ): String = "${formatTime(start, language)} – ${formatTime(end, language)}"
}
