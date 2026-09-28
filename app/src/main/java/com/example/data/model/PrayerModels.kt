package com.example.data.model

import com.example.data.prayer.IslamicMadhab
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class PrayerName(val displayName: String, val arabicName: String) {
  IMSAK("Imsak", "الإمساك"),
  FAJR("Fajr", "الفجر"),
  SUNRISE("Sunrise", "الشروق"),
  DHUHR("Dhuhr", "الظهر"),
  ASR("Asr", "العصر"),
  MAGHRIB("Maghrib", "المغرب"),
  ISHA("Isha", "العشاء")
}

data class PrayerTimeItem(
  val prayer: PrayerName,
  val time: LocalTime,
  val isCompleted: Boolean = false,
  val isNext: Boolean = false,
  val isCurrent: Boolean = false,
  val notificationEnabled: Boolean = true
) {
  val formattedTime: String
    get() = time.format(DateTimeFormatter.ofPattern("hh:mm a"))
}

data class DailyPrayerSchedule(
  val date: LocalDate,
  val city: String,
  val country: String,
  val calculationMethodName: String,
  val madhab: IslamicMadhab = IslamicMadhab.DEFAULT,
  val items: List<PrayerTimeItem>,
  val nextPrayer: PrayerTimeItem?,
  val timeUntilNextFormatted: String,
  val progressToNext: Float, // 0.0f to 1.0f
  val currentTime: LocalTime = LocalTime.now(),
  val forbiddenTimes: List<com.example.data.prayer.ForbiddenTimeWindow> = emptyList(),
  /** The computed sunset. Maghrib is this plus `PrayerCalculator.MAGHRIB_MARGIN_MINUTES`. */
  val trueSunset: LocalTime? = null,
  val isTahajjudTime: Boolean = false,
  /**
   * When the last third of the night begins, in the city's local time. The alarm scheduler needs
   * the instant, not just [isTahajjudTime]; null when it could not be derived.
   */
  val tahajjudStart: LocalTime? = null,
  /**
   * The obligatory prayer whose time is running now, or null between sunrise and Dhuhr. Each one
   * lasts until the next begins, except Fajr, which ends at sunrise, and Isha, which ends at
   * midnight (Sahih Muslim 612); see [ishaPastMidnight].
   */
  val currentPrayer: PrayerTimeItem? = null,
  /** When [currentPrayer]'s time ends. */
  val currentPrayerEndsAt: LocalTime? = null,
  /** HH:MM:SS until [currentPrayerEndsAt]; null with no current prayer. */
  val timeLeftInCurrentFormatted: String? = null,
  /** 0..1 through [currentPrayer]'s time. */
  val progressInCurrent: Float = 0f,
  /**
   * Between Isha's midnight and Fajr. Isha's time has ended (Sahih Muslim 612), so there is no
   * [currentPrayer]; the UI says so, and to pray Isha now if it was missed.
   */
  val ishaPastMidnight: Boolean = false,
  /** Tonight's midpoint of sunset and Fajr, where Isha's time ends. */
  val islamicMidnight: LocalTime? = null
) {
  /** Whether the current prayer ends exactly when the next begins, so one countdown serves both. */
  val currentEndsAtNext: Boolean
    get() = currentPrayer != null && currentPrayerEndsAt == nextPrayer?.time

  val activeForbiddenTime: com.example.data.prayer.ForbiddenTimeWindow?
    get() = forbiddenTimes.firstOrNull { it.isActive(currentTime) }

  val nextForbiddenTime: com.example.data.prayer.ForbiddenTimeWindow?
    get() = forbiddenTimes.filter { it.isUpcoming(currentTime) }.minByOrNull { it.startTime }

  val sunrise: LocalTime?
    get() = items.find { it.prayer == PrayerName.SUNRISE }?.time
  val maghrib: LocalTime?
    get() = items.find { it.prayer == PrayerName.MAGHRIB }?.time
  val dhuhr: LocalTime?
    get() = items.find { it.prayer == PrayerName.DHUHR }?.time
  val fajr: LocalTime?
    get() = items.find { it.prayer == PrayerName.FAJR }?.time
  val isha: LocalTime?
    get() = items.find { it.prayer == PrayerName.ISHA }?.time
}

data class CityLocation(
  val name: String,
  val country: String,
  val latitude: Double,
  val longitude: Double,
  val timeZoneId: String
) {
  /**
   * "Dhaka, Bangladesh", or just the name when there is no country - which is the case for a
   * coordinates-only location, where [name] already reads "23.81 N, 90.41 E".
   */
  val displayName: String
    get() = if (country.isBlank()) name else "$name, $country"

  /** True when only coordinates are known, because nothing could name them offline. */
  val isCoordinatesOnly: Boolean
    get() = country.isBlank()
}
