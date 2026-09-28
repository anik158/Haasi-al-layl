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

typealias JuristicMethod = IslamicMadhab

object PrayerCalculator {

  /** Minutes Maghrib is set after the computed sunset. */
  const val MAGHRIB_MARGIN_MINUTES = 3L

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
    method: CalculationMethod = CalculationMethod.UMM_AL_QURA,
    madhab: IslamicMadhab = IslamicMadhab.DEFAULT
  ): DailyPrayerSchedule {
    val timesMap = computePrayerTimes(date, location.latitude, location.longitude, location.timeZoneId, method, madhab)

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

    // The last main prayer that has started today. Only the progress bar's start point: it is not
    // the "current" prayer, since Fajr's time ends at sunrise, and before Fajr it is still Isha.
    var currentPrayerName: PrayerName? = null
    for (i in mainPrayers.indices.reversed()) {
      val p = mainPrayers[i]
      val t = timesMap[p] ?: continue
      if (currentTime.isAfter(t) || currentTime == t) {
        currentPrayerName = p
        break
      }
    }

    val window = currentWindow(date, now, timesMap)

    for (p in prayerOrder) {
      val time = timesMap[p] ?: LocalTime.of(12, 0)
      val isNext = (p == nextMainPrayer)
      val isCurrent = (p == window?.prayer)
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
    val sunriseTime = timesMap[PrayerName.SUNRISE] ?: LocalTime.of(6, 0)
    val dhuhrTime = timesMap[PrayerName.DHUHR] ?: LocalTime.of(12, 10)
    val maghribTime = timesMap[PrayerName.MAGHRIB] ?: LocalTime.of(18, 15)
    val fajrTime = timesMap[PrayerName.FAJR] ?: LocalTime.of(4, 30)
    // True sunset: Maghrib minus its calculation margin. The forbidden window ends at sunset.
    val sunsetTime = maghribTime.minusMinutes(MAGHRIB_MARGIN_MINUTES)
    val forbiddenWindows = ForbiddenPrayerTimesHelper.calculateWindows(sunriseTime, dhuhrTime, sunsetTime)

    val maghribMinutes = maghribTime.hour * 60 + maghribTime.minute
    val fajrMinutes = fajrTime.hour * 60 + fajrTime.minute
    val nightLengthMinutes = (24 * 60 - maghribMinutes) + fajrMinutes
    val lastThirdDuration = nightLengthMinutes / 3
    
    val tahajjudStartMinutes = fajrMinutes - lastThirdDuration
    val currentMinutes = now.hour * 60 + now.minute
    
    val isTahajjudTime = if (tahajjudStartMinutes < 0) {
        currentMinutes >= (24*60 + tahajjudStartMinutes) || currentMinutes < fajrMinutes
    } else {
        currentMinutes >= tahajjudStartMinutes && currentMinutes < fajrMinutes
    }

    val currentItem = window?.let { w -> list.find { it.prayer == w.prayer } }
    val currentLeft = window?.let { hms(Duration.between(now, it.end)) }
    val currentProgress = window?.let { w ->
      val total = Duration.between(w.start, w.end).seconds.coerceAtLeast(1)
      (Duration.between(w.start, now).seconds.toFloat() / total).coerceIn(0f, 1f)
    } ?: 0f

    return DailyPrayerSchedule(
      date = date,
      city = location.name,
      country = location.country,
      calculationMethodName = method.title,
      madhab = madhab,
      items = list,
      nextPrayer = nextItem,
      timeUntilNextFormatted = timeUntilFormatted,
      progressToNext = progress,
      currentTime = now.toLocalTime(),
      forbiddenTimes = forbiddenWindows,
      trueSunset = sunsetTime,
      isTahajjudTime = isTahajjudTime,
      // floorMod normalises the past-midnight case, where the last third starts before 00:00.
      tahajjudStart = LocalTime.of(
        Math.floorMod(tahajjudStartMinutes, 24 * 60) / 60,
        Math.floorMod(tahajjudStartMinutes, 24 * 60) % 60
      ),
      currentPrayer = currentItem,
      currentPrayerEndsAt = window?.end?.toLocalTime(),
      timeLeftInCurrentFormatted = currentLeft,
      progressInCurrent = currentProgress
    )
  }

  private data class PrayerWindow(val prayer: PrayerName, val start: LocalDateTime, val end: LocalDateTime)

  /**
   * The obligatory prayer whose time contains [now]: Fajr to sunrise, Dhuhr to Asr, Asr to
   * Maghrib, Maghrib to Isha, Isha to the next Fajr. Null between sunrise and Dhuhr.
   *
   * Before today's Fajr it is still last night's Isha. Yesterday's Isha and tomorrow's Fajr are
   * taken from today's times - they differ by a minute or so, which a countdown can live with.
   */
  private fun currentWindow(
    date: LocalDate,
    now: LocalDateTime,
    times: Map<PrayerName, LocalTime>
  ): PrayerWindow? {
    fun at(p: PrayerName) = times[p]?.let { LocalDateTime.of(date, it) }
    val fajr = at(PrayerName.FAJR) ?: return null
    val sunrise = at(PrayerName.SUNRISE) ?: return null
    val dhuhr = at(PrayerName.DHUHR) ?: return null
    val asr = at(PrayerName.ASR) ?: return null
    val maghrib = at(PrayerName.MAGHRIB) ?: return null
    val isha = at(PrayerName.ISHA) ?: return null
    return when {
      now.isBefore(fajr) -> PrayerWindow(PrayerName.ISHA, isha.minusDays(1), fajr)
      now.isBefore(sunrise) -> PrayerWindow(PrayerName.FAJR, fajr, sunrise)
      now.isBefore(dhuhr) -> null
      now.isBefore(asr) -> PrayerWindow(PrayerName.DHUHR, dhuhr, asr)
      now.isBefore(maghrib) -> PrayerWindow(PrayerName.ASR, asr, maghrib)
      now.isBefore(isha) -> PrayerWindow(PrayerName.MAGHRIB, maghrib, isha)
      else -> PrayerWindow(PrayerName.ISHA, isha, fajr.plusDays(1))
    }
  }

  private fun hms(duration: Duration): String {
    val s = duration.seconds.coerceAtLeast(0)
    return String.format("%02d:%02d:%02d", s / 3600, (s / 60) % 60, s % 60)
  }

  private fun computePrayerTimes(
    date: LocalDate,
    lat: Double,
    lng: Double,
    tzId: String,
    method: CalculationMethod,
    madhab: IslamicMadhab
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

    // Above roughly 48 degrees of latitude the sun stops dipping far enough below the horizon
    // in midsummer for the Fajr and Isha angles to occur at all - in London it only reaches
    // about -15 degrees. Clamping the hour angle there would silently collapse both prayers onto
    // solar midnight, so we fall back to the one-seventh-of-the-night rule instead.
    val nightLength = 24.0 - (sunset - sunrise)

    // Fajr
    val fajr = sunAngleTimeOrNull(90.0 + method.fajrAngle, lat, declination)
      ?.let { noon - it }
      ?: (sunrise - nightLength / 7.0)

    // Imsak (10 min before Fajr)
    val imsak = fajr - (10.0 / 60.0)

    // Asr
    val asrAngle = Math.toDegrees(
      atan(1.0 / (madhab.shadowFactor + tan(Math.toRadians(abs(lat - declination)))))
    )
    val asrDiff = sunAngleTime(90.0 - asrAngle, lat, declination)
    val asr = noon + asrDiff

    // Maghrib: sunset plus a small margin for calculation error (refraction, elevation). The
    // sunnah is to hasten iftar at sunset (Bukhari 1957); the margin is computational, not fiqh.
    val maghrib = sunset + (MAGHRIB_MARGIN_MINUTES / 60.0)

    // Isha. Umm al-Qura sets it by a fixed interval after Maghrib rather than an angle, and
    // widens that interval from 90 to 120 minutes for Ramadan.
    val ishaInterval = method.ishaIntervalMinutes?.let { interval ->
      if (method == CalculationMethod.UMM_AL_QURA && HijriDateHelper.isRamadan(date)) 120 else interval
    }
    val isha: Double = if (ishaInterval != null) {
      maghrib + (ishaInterval.toDouble() / 60.0)
    } else {
      sunAngleTimeOrNull(90.0 + method.ishaAngle, lat, declination)
        ?.let { noon + it }
        ?: (sunset + nightLength / 7.0)
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

  private fun cosHourAngle(angle: Double, lat: Double, declination: Double): Double =
    (cos(Math.toRadians(angle)) -
      sin(Math.toRadians(lat)) * sin(Math.toRadians(declination))) /
      (cos(Math.toRadians(lat)) * cos(Math.toRadians(declination)))

  /**
   * Hours between solar noon and the moment the sun sits at [angle]. Clamped, so it always
   * returns something usable - only appropriate for sunrise, sunset and Asr, which do occur at
   * the latitudes this app supports.
   */
  private fun sunAngleTime(angle: Double, lat: Double, declination: Double): Double {
    val clamped = cosHourAngle(angle, lat, declination).coerceIn(-1.0, 1.0)
    return Math.toDegrees(acos(clamped)) / 15.0
  }

  /** As [sunAngleTime], but null when the sun never reaches [angle] on this date. */
  private fun sunAngleTimeOrNull(angle: Double, lat: Double, declination: Double): Double? {
    val cosH = cosHourAngle(angle, lat, declination)
    if (cosH < -1.0 || cosH > 1.0) return null
    return Math.toDegrees(acos(cosH)) / 15.0
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
