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
      AppLanguage.ARABIC -> "عند اصفرار الشمس وتدليها للغروب بنحو ٢٠ دقيقة حتى تغرب تماماً ويدخل المغرب."
      AppLanguage.BENGALI -> "সূর্য হলুদ বর্ণ ধারণ করা থেকে সূর্যাস্ত সম্পন্ন হওয়া ও মাগরিব প্রবেশ পর্যন্ত (~২০ মিনিট)।"
      AppLanguage.SPANISH -> "Cuando el sol palidece y se pone (~20 min antes del Maghrib). Prohibida la oración voluntaria."
      AppLanguage.PORTUGUESE -> "Quando o sol empalidece e se põe (~20 min antes de Maghrib). Proibida a oração voluntária."
      AppLanguage.ENGLISH -> "When the sun turns pale until it sets completely (~20 min before Maghrib). Voluntary prayer prohibited."
    }
  }
}

object ForbiddenPrayerTimesHelper {

  fun calculateWindows(
    sunrise: LocalTime,
    dhuhr: LocalTime,
    maghrib: LocalTime
  ): List<ForbiddenTimeWindow> {
    val sunriseWindow = ForbiddenTimeWindow(
      type = ForbiddenWindowType.SUNRISE,
      startTime = sunrise,
      endTime = sunrise.plusMinutes(15),
      hadithReference = "Sahih Muslim 831 (Uqbah ibn Amir: The Messenger of Allah prohibited us from praying at three times...)"
    )

    val zenithWindow = ForbiddenTimeWindow(
      type = ForbiddenWindowType.ZENITH,
      startTime = dhuhr.minusMinutes(10),
      endTime = dhuhr,
      hadithReference = "Sahih Muslim 832 (When the sun stands in the middle of the sky until it passes the meridian...)"
    )

    val sunsetWindow = ForbiddenTimeWindow(
      type = ForbiddenWindowType.SUNSET,
      startTime = maghrib.minusMinutes(20),
      endTime = maghrib,
      hadithReference = "Sahih al-Bukhari 586 / Muslim 831 (When the sun inclines to set until it has completely set...)"
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
