package com.example.data.model

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
  val items: List<PrayerTimeItem>,
  val nextPrayer: PrayerTimeItem?,
  val timeUntilNextFormatted: String,
  val progressToNext: Float // 0.0f to 1.0f
)

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
