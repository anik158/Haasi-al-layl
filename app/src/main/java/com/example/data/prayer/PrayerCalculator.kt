package com.example.data.prayer

import com.example.data.model.CityLocation
import com.example.data.model.DailyPrayerSchedule
import com.example.data.model.PrayerName
import com.example.data.model.PrayerTimeItem
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

enum class CalculationMethod(
  val title: String,
  val fajrAngle: Double,
  val ishaAngle: Double,
  val ishaIntervalMinutes: Int? = null
) {
  MUSLIM_WORLD_LEAGUE("Muslim World League", 18.0, 17.0),
  ISNA("Islamic Society of North America (ISNA)", 15.0, 15.0),
  EGYPTIAN("Egyptian General Authority", 19.5, 17.5),
  UMM_AL_QURA("Umm al-Qura University, Makkah", 18.5, 0.0, 90),
  KARACHI("University of Islamic Sciences, Karachi", 18.0, 18.0),
  DUBAI("Dubai Religious Affairs", 18.2, 18.2)
}

enum class JuristicMethod(val shadowFactor: Double) {
  STANDARD_SHAFI_MALIKI_HANBALI(1.0),
  HANAFI(2.0)
}

object PrayerCalculator {

  val PRESET_CITIES = listOf(
    CityLocation("Makkah", "Saudi Arabia", 21.4225, 39.8262, "Asia/Riyadh"),
    CityLocation("Madinah", "Saudi Arabia", 24.4672, 39.6111, "Asia/Riyadh"),
    CityLocation("Cairo", "Egypt", 30.0444, 31.2357, "Africa/Cairo"),
    CityLocation("Istanbul", "Turkey", 41.0082, 28.9784, "Europe/Istanbul"),
    CityLocation("Dubai", "UAE", 25.2048, 55.2708, "Asia/Dubai"),
    CityLocation("London", "United Kingdom", 51.5074, -0.1278, "Europe/London"),
    CityLocation("New York", "United States", 40.7128, -74.0060, "America/New_York"),
    CityLocation("Toronto", "Canada", 43.6532, -79.3832, "America/Toronto"),
    CityLocation("Jakarta", "Indonesia", -6.2088, 106.8456, "Asia/Jakarta"),
    CityLocation("Kuala Lumpur", "Malaysia", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
    CityLocation("Dhaka", "Bangladesh", 23.8103, 90.4125, "Asia/Dhaka"),
    CityLocation("Chittagong", "Bangladesh", 22.3569, 91.7832, "Asia/Dhaka"),
    CityLocation("Sylhet", "Bangladesh", 24.8949, 91.8687, "Asia/Dhaka"),
    CityLocation("Madrid", "Spain", 40.4168, -3.7038, "Europe/Madrid"),
    CityLocation("Mexico City", "Mexico", 19.4326, -99.1332, "America/Mexico_City"),
    CityLocation("Buenos Aires", "Argentina", -34.6037, -58.3816, "America/Argentina/Buenos_Aires"),
    CityLocation("Bogota", "Colombia", 4.7110, -74.0721, "America/Bogota"),
    CityLocation("Sao Paulo", "Brazil", -23.5505, -46.6333, "America/Sao_Paulo"),
    CityLocation("Lisbon", "Portugal", 38.7223, -9.1393, "Europe/Lisbon"),
    CityLocation("Karachi", "Pakistan", 24.8607, 67.0011, "Asia/Karachi"),
    CityLocation("Paris", "France", 48.8566, 2.3522, "Europe/Paris"),
    CityLocation("Berlin", "Germany", 52.5200, 13.4050, "Europe/Berlin"),
    CityLocation("Sydney", "Australia", -33.8688, 151.2093, "Australia/Sydney")
  )

  fun calculateSchedule(
    date: LocalDate,
    location: CityLocation,
    now: LocalDateTime = LocalDateTime.now(),
    completedPrayerIds: Set<String> = emptySet(),
    method: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
    juristic: JuristicMethod = JuristicMethod.STANDARD_SHAFI_MALIKI_HANBALI
  ): DailyPrayerSchedule {
    val timesMap = computePrayerTimes(date, location.latitude, location.longitude, location.timeZoneId, method, juristic)

    val list = mutableListOf<PrayerTimeItem>()
    val prayerOrder = listOf(
      PrayerName.IMSAK,
      PrayerName.FAJR,
      PrayerName.SUNRISE,
      PrayerName.DHUHR,
      PrayerName.ASR,
      PrayerName.MAGHRIB,
      PrayerName.ISHA
    )

    // Current and Next prayer determination
    val currentTime = now.toLocalTime()
    val mainPrayers = listOf(
      PrayerName.FAJR,
      PrayerName.DHUHR,
      PrayerName.ASR,
      PrayerName.MAGHRIB,
      PrayerName.ISHA
    )

    var nextMainPrayer: PrayerName? = null
    for (p in mainPrayers) {
      val t = timesMap[p] ?: continue
      if (currentTime.isBefore(t)) {
        nextMainPrayer = p
        break
      }
    }
    // If all passed today, next is tomorrow's Fajr
    if (nextMainPrayer == null) {
      nextMainPrayer = PrayerName.FAJR
    }

    var currentPrayerName: PrayerName? = null
    for (i in mainPrayers.indices.reversed()) {
      val p = mainPrayers[i]
      val t = timesMap[p] ?: continue
      if (currentTime.isAfter(t) || currentTime == t) {
        currentPrayerName = p
        break
      }
    }

    for (p in prayerOrder) {
      val time = timesMap[p] ?: LocalTime.of(12, 0)
      val isNext = (p == nextMainPrayer)
      val isCurrent = (p == currentPrayerName)
      val isCompleted = completedPrayerIds.contains(p.name)

      list.add(
        PrayerTimeItem(
          prayer = p,
          time = time,
          isCompleted = isCompleted,
          isNext = isNext,
          isCurrent = isCurrent
        )
      )
    }

    // Time until next prayer calculation
    val nextItem = list.find { it.prayer == nextMainPrayer }
    val timeUntilFormatted: String
    val progress: Float

    if (nextItem != null) {
      var nextDateTime = LocalDateTime.of(date, nextItem.time)
      if (nextDateTime.isBefore(now)) {
        nextDateTime = nextDateTime.plusDays(1)
      }
      val duration = Duration.between(now, nextDateTime)
      val hours = duration.toHours()
      val minutes = (duration.toMinutes() % 60)
      val seconds = (duration.seconds % 60)

      timeUntilFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

      // Calculate progress between previous prayer time and next prayer time
      val prevPrayerTime = currentPrayerName?.let { timesMap[it] } ?: nextItem.time.minusHours(4)
      var prevDateTime = LocalDateTime.of(date, prevPrayerTime)
      if (prevDateTime.isAfter(now)) {
        prevDateTime = prevDateTime.minusDays(1)
      }
      val totalWindow = Duration.between(prevDateTime, nextDateTime).seconds.coerceAtLeast(1)
      val elapsed = Duration.between(prevDateTime, now).seconds.coerceAtLeast(0)
      progress = (elapsed.toFloat() / totalWindow.toFloat()).coerceIn(0f, 1f)
    } else {
      timeUntilFormatted = "--:--"
      progress = 0f
    }

    return DailyPrayerSchedule(
      date = date,
      city = location.name,
      country = location.country,
      calculationMethodName = method.title,
      items = list,
      nextPrayer = nextItem,
      timeUntilNextFormatted = timeUntilFormatted,
      progressToNext = progress
    )
  }

  private fun computePrayerTimes(
    date: LocalDate,
    lat: Double,
    lng: Double,
    tzId: String,
    method: CalculationMethod,
    juristic: JuristicMethod
  ): Map<PrayerName, LocalTime> {
    val zone = try {
      ZoneId.of(tzId)
    } catch (_: Exception) {
      ZoneId.systemDefault()
    }
    val offsetHours = zone.rules.getOffset(date.atStartOfDay()).totalSeconds / 3600.0

    val julianDay = toJulianDay(date.year, date.monthValue, date.dayOfMonth) - (lng / (15.0 * 24.0))
    val t = (julianDay - 2451545.0) / 36525.0

    // Sun's Mean Anomaly & Longitude
    val meanLongitude = fixAngle(280.46646 + 36000.76983 * t)
    val meanAnomaly = fixAngle(357.52911 + 35999.05029 * t)
    val eclipticLongitude = fixAngle(
      meanLongitude + 1.914602 * sin(Math.toRadians(meanAnomaly)) +
        0.019993 * sin(Math.toRadians(2.0 * meanAnomaly))
    )

    // Obliquity of Ecliptic
    val obliquity = 23.439291 - 0.0130042 * t
    val declination = Math.toDegrees(
      asin(sin(Math.toRadians(obliquity)) * sin(Math.toRadians(eclipticLongitude)))
    )

    // Equation of Time in hours
    val y = tan(Math.toRadians(obliquity / 2.0)).let { it * it }
    val eqTime = (y * sin(2.0 * Math.toRadians(meanLongitude)) -
      2.0 * 0.016708634 * sin(Math.toRadians(meanAnomaly)) +
      4.0 * 0.016708634 * y * sin(Math.toRadians(meanAnomaly)) * cos(2.0 * Math.toRadians(meanLongitude)) -
      0.5 * y * y * sin(4.0 * Math.toRadians(meanLongitude)))
    val eqTimeHours = Math.toDegrees(eqTime) * 4.0 / 60.0

    // Solar Noon (Dhuhr)
    val noon = 12.0 + offsetHours - (lng / 15.0) - eqTimeHours

    // Sunrise & Sunset angle (sun altitude = -0.833 degrees)
    val sunriseAngle = 90.8333
    val sunriseDiff = sunAngleTime(sunriseAngle, lat, declination)
    val sunrise = noon - sunriseDiff
    val sunset = noon + sunriseDiff

    // Fajr
    val fajrDiff = sunAngleTime(90.0 + method.fajrAngle, lat, declination)
    val fajr = noon - fajrDiff

    // Imsak (10 min before Fajr)
    val imsak = fajr - (10.0 / 60.0)

    // Asr
    val asrAngle = -Math.toDegrees(
      atan(1.0 / (juristic.shadowFactor + tan(Math.toRadians(abs(lat - declination)))))
    )
    val asrDiff = sunAngleTime(90.0 - asrAngle, lat, declination)
    val asr = noon + asrDiff

    // Maghrib (same as sunset + 3 min precaution)
    val maghrib = sunset + (3.0 / 60.0)

    // Isha
    val isha: Double = if (method.ishaIntervalMinutes != null) {
      maghrib + (method.ishaIntervalMinutes.toDouble() / 60.0)
    } else {
      val ishaDiff = sunAngleTime(90.0 + method.ishaAngle, lat, declination)
      noon + ishaDiff
    }

    return mapOf(
      PrayerName.IMSAK to decimalHoursToLocalTime(imsak),
      PrayerName.FAJR to decimalHoursToLocalTime(fajr),
      PrayerName.SUNRISE to decimalHoursToLocalTime(sunrise),
      PrayerName.DHUHR to decimalHoursToLocalTime(noon),
      PrayerName.ASR to decimalHoursToLocalTime(asr),
      PrayerName.MAGHRIB to decimalHoursToLocalTime(maghrib),
      PrayerName.ISHA to decimalHoursToLocalTime(isha)
    )
  }

  private fun sunAngleTime(angle: Double, lat: Double, declination: Double): Double {
    val cosHourAngle = (cos(Math.toRadians(angle)) -
      sin(Math.toRadians(lat)) * sin(Math.toRadians(declination))) /
      (cos(Math.toRadians(lat)) * cos(Math.toRadians(declination)))
    val clamped = cosHourAngle.coerceIn(-1.0, 1.0)
    return Math.toDegrees(acos(clamped)) / 15.0
  }

  private fun fixAngle(angle: Double): Double {
    var a = angle - 360.0 * floor(angle / 360.0)
    if (a < 0.0) a += 360.0
    return a
  }

  private fun toJulianDay(year: Int, month: Int, day: Int): Double {
    var y = year
    var m = month
    if (m <= 2) {
      y -= 1
      m += 12
    }
    val a = floor(y / 100.0)
    val b = 2.0 - a + floor(a / 4.0)
    return floor(365.25 * (y + 4716.0)) + floor(30.6001 * (m + 1.0)) + day + b - 1524.5
  }

  private fun decimalHoursToLocalTime(hoursVal: Double): LocalTime {
    var h = hoursVal
    while (h < 0) h += 24.0
    while (h >= 24.0) h -= 24.0

    val hours = h.toInt().coerceIn(0, 23)
    val minutes = ((h - hours) * 60.0).toInt().coerceIn(0, 59)
    val seconds = (((h - hours) * 60.0 - minutes) * 60.0).toInt().coerceIn(0, 59)
    return LocalTime.of(hours, minutes, seconds)
  }
}
