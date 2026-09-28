package com.example.data.prayer

import com.example.data.localization.AppLanguage
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class ForbiddenWindowType(val id: String) {
  SUNRISE("sunrise_forbidden"),
  ZENITH("zenith_forbidden"),
  SUNSET("sunset_forbidden")
}

data class ForbiddenTimeWindow(
  val type: ForbiddenWindowType,
  val startTime: LocalTime,
  val endTime: LocalTime,
  val hadithReference: String
) {
  fun isActive(currentTime: LocalTime): Boolean {
    return !currentTime.isBefore(startTime) && currentTime.isBefore(endTime)
  }

  fun isUpcoming(currentTime: LocalTime): Boolean {
    return currentTime.isBefore(startTime)
  }

  fun minutesRemaining(currentTime: LocalTime): Long {
    return if (isActive(currentTime)) {
      Duration.between(currentTime, endTime).toMinutes().coerceAtLeast(1)
    } else 0
  }

  fun minutesUntilStart(currentTime: LocalTime): Long {
    return if (isUpcoming(currentTime)) {
      Duration.between(currentTime, startTime).toMinutes().coerceAtLeast(0)
    } else 0
  }

  val formattedRange: String
    get() {
      val fmt = DateTimeFormatter.ofPattern("hh:mm a")
      return "${startTime.format(fmt)} – ${endTime.format(fmt)}"
    }

  fun getLocalizedTitle(language: AppLanguage): String = when (type) {
    ForbiddenWindowType.SUNRISE -> when (language) {
      AppLanguage.ARABIC -> "وقت شروق الشمس (حتى الإشراق)"
      AppLanguage.BENGALI -> "সূর্যোদয় কালীন নিষিদ্ধ সময় (ইশরাক পর্যন্ত)"
      AppLanguage.SPANISH -> "Tiempo Prohibido del Amanecer (hasta Ishraq)"
      AppLanguage.PORTUGUESE -> "Tempo Proibido do Nascer do Sol (até Ishraq)"
      AppLanguage.ENGLISH -> "Sunrise Forbidden Window (until Ishraq)"
    }
    ForbiddenWindowType.ZENITH -> when (language) {
      AppLanguage.ARABIC -> "وقت استواء الشمس في كبد السماء (الزوال)"
      AppLanguage.BENGALI -> "ঠিক দ্বিপ্রহরের নিষিদ্ধ সময় (জাওয়াল)"
      AppLanguage.SPANISH -> "Tiempo Prohibido del Cenit Solar (Zawal)"
      AppLanguage.PORTUGUESE -> "Tempo Proibido do Zênite Solar (Zawal)"
      AppLanguage.ENGLISH -> "Solar Zenith Forbidden Window (Zawal)"
    }
    ForbiddenWindowType.SUNSET -> when (language) {
      AppLanguage.ARABIC -> "وقت اصفرار الشمس وغروبها"
      AppLanguage.BENGALI -> "সূর্যাস্ত কালীন নিষিদ্ধ সময়"
      AppLanguage.SPANISH -> "Tiempo Prohibido de la Puesta del Sol"
      AppLanguage.PORTUGUESE -> "Tempo Proibido do Pôr do Sol"
      AppLanguage.ENGLISH -> "Pre-Sunset Forbidden Window"
    }
  }

  fun getLocalizedDescription(language: AppLanguage): String = when (type) {
    ForbiddenWindowType.SUNRISE -> when (language) {
      AppLanguage.ARABIC -> "من طلوع قرص الشمس حتى ترتفع قيد رمح في السماء (~١٥ دقيقة). يُنهى عن صلاة النوافل فيها."
      AppLanguage.BENGALI -> "সূর্য ওঠা থেকে শুরু করে সম্পূর্ণ উঁচুতে ওঠা পর্যন্ত (~১৫ মিনিট)। এ সময়ে নফল সালাত আদায় নিষিদ্ধ।"
      AppLanguage.SPANISH -> "Desde que sale el sol hasta que se eleva sobre el horizonte (~15 min). La oración voluntaria (nafl) está prohibida."
      AppLanguage.PORTUGUESE -> "Desde o nascer do sol até que ele suba no horizonte (~15 min). A oração voluntária (nafl) é proibida."
      AppLanguage.ENGLISH -> "From sunrise until the sun rises a spear's length (~15 min). Voluntary (nafl) prayers are prohibited."
    }
    ForbiddenWindowType.ZENITH -> when (language) {
      AppLanguage.ARABIC -> "عند وقوف الشمس في كبد السماء قبل الظهر بـ ١٠ دقائق حتى تزول وتدخل صلاة الظهر."
      AppLanguage.BENGALI -> "ঠিক দ্বিপ্রহরে সূর্য মধ্যাকাশে অবস্থান করা থেকে জোহরের ওয়াক্ত শুরু হওয়া পর্যন্ত (~১০ মিনিট)।"
      AppLanguage.SPANISH -> "Cuando el sol está en el cenit exacto (~10 min antes del Dhuhr). Las oraciones voluntarias están prohibidas."
      AppLanguage.PORTUGUESE -> "Quando o sol está no zênite exato (~10 min antes de Dhuhr). As orações voluntárias são proibidas."
      AppLanguage.ENGLISH -> "When the sun is at its highest meridian until it declines (~10 min before Dhuhr). Voluntary prayer prohibited."
    }
    ForbiddenWindowType.SUNSET -> when (language) {
      AppLanguage.ARABIC -> "من ميل الشمس للغروب حتى تغرب (نحو ١٥ دقيقة قبل الغروب). يُنهى فيه عن صلاة النافلة."
      AppLanguage.BENGALI -> "সূর্য অস্ত যাওয়ার দিকে ঝুঁকে পড়া থেকে সম্পূর্ণ অস্ত যাওয়া পর্যন্ত (সূর্যাস্তের আগে ~১৫ মিনিট)। এ সময়ে নফল সালাত নিষিদ্ধ।"
      AppLanguage.SPANISH -> "Desde que el sol se inclina hacia el ocaso hasta que se pone (~15 min antes de la puesta). Prohibida la oración voluntaria."
      AppLanguage.PORTUGUESE -> "Desde que o sol se inclina para o ocaso até que se põe (~15 min antes do pôr do sol). Proibida a oração voluntária."
      AppLanguage.ENGLISH -> "From when the sun inclines to setting until it has set (~15 min before sunset). Voluntary prayer prohibited."
    }
  }
}

object ForbiddenPrayerTimesHelper {

  /** How long each end-of-day window lasts: the scholars' estimate of "a spear's length". */
  const val SUN_WINDOW_MINUTES = 15L
  const val ZENITH_WINDOW_MINUTES = 10L

  /**
   * The three windows in Sahih Muslim 831 (ʿUqbah ibn ʿĀmir): when the sun rises until it is high,
   * when it stands at midday until it passes the meridian, and when it inclines to setting until
   * it sets. Muslim 832 (ʿAmr ibn ʿAbasah) gives the height as a spear's length.
   *
   * [sunset] is the true sunset, not Maghrib: the hadith end the window when the sun has set, and
   * Maghrib here carries a 3-minute calculation margin. The old window was Maghrib − 20 to
   * Maghrib, which ran 17 minutes before sunset to 3 after, and the 20 had no source.
   */
  fun calculateWindows(
    sunrise: LocalTime,
    dhuhr: LocalTime,
    sunset: LocalTime
  ): List<ForbiddenTimeWindow> {
    val sunriseWindow = ForbiddenTimeWindow(
      type = ForbiddenWindowType.SUNRISE,
      startTime = sunrise,
      endTime = sunrise.plusMinutes(SUN_WINDOW_MINUTES),
      hadithReference = "Sahih Muslim 831, 832"
    )

    val zenithWindow = ForbiddenTimeWindow(
      type = ForbiddenWindowType.ZENITH,
      startTime = dhuhr.minusMinutes(ZENITH_WINDOW_MINUTES),
      endTime = dhuhr,
      hadithReference = "Sahih Muslim 831, 832"
    )

    val sunsetWindow = ForbiddenTimeWindow(
      type = ForbiddenWindowType.SUNSET,
      startTime = sunset.minusMinutes(SUN_WINDOW_MINUTES),
      endTime = sunset,
      hadithReference = "Sahih Muslim 831; Sahih al-Bukhari 586"
    )

    return listOf(sunriseWindow, zenithWindow, sunsetWindow)
  }

  fun getActiveWindow(windows: List<ForbiddenTimeWindow>, currentTime: LocalTime): ForbiddenTimeWindow? {
    return windows.firstOrNull { it.isActive(currentTime) }
  }

  fun getNextUpcomingWindow(windows: List<ForbiddenTimeWindow>, currentTime: LocalTime): ForbiddenTimeWindow? {
    return windows.filter { it.isUpcoming(currentTime) }.minByOrNull { it.startTime }
  }
}
