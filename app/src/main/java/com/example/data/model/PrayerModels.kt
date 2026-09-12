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
  val isTahajjudTime: Boolean = false,
  /**
   * When the last third of the night begins, in the city's local time. The alarm scheduler needs
   * the instant, not just [isTahajjudTime]; null when it could not be derived.
   */
  val tahajjudStart: LocalTime? = null
) {
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
  val displayName: String
    get() = "$name, $country"
}
