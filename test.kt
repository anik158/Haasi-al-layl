
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.Duration
import kotlin.math.*

enum class IslamicMadhab(val shadowFactor: Double) {
    SHAFI(1.0),
    HANAFI(2.0),
    MALIKI(1.0),
    HANBALI(1.0)
}

data class CalculationMethod(
    val title: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaIntervalMinutes: Int? = null
)

fun decimalHoursToLocalTime(hours: Double): LocalTime {
    if (hours.isNaN()) {
        println("NaN DETECTED IN decimalHoursToLocalTime!")
        return LocalTime.of(0,0)
    }
    val wrapped = (hours % 24.0 + 24.0) % 24.0
    val totalSeconds = (wrapped * 3600).toLong()
    val h = (totalSeconds / 3600).toInt()
    val m = ((totalSeconds % 3600) / 60).toInt()
    val s = (totalSeconds % 60).toInt()
    return LocalTime.of(h, m, s)
}

fun sunAngleTime(angle: Double, lat: Double, declination: Double): Double {
    val cosHourAngle = (cos(Math.toRadians(angle)) -
      sin(Math.toRadians(lat)) * sin(Math.toRadians(declination))) /
      (cos(Math.toRadians(lat)) * cos(Math.toRadians(declination)))
    val clamped = cosHourAngle.coerceIn(-1.0, 1.0)
    return Math.toDegrees(acos(clamped)) / 15.0
}

fun toJulianDay(year: Int, month: Int, day: Int): Double {
    var y = year
    var m = month
    if (m <= 2) {
        y -= 1
        m += 12
    }
    val a = floor(y / 100.0)
    val b = 2.0 - a + floor(a / 4.0)
    val jd = floor(365.25 * (y + 4716.0)) + floor(30.6001 * (m + 1.0)) + day.toDouble() + b - 1524.5
    return jd
}

fun computePrayerTimes(
    date: LocalDate,
    lat: Double,
    lng: Double,
    tzId: String,
    fajrAngle: Double,
    ishaAngle: Double,
    shadowFactor: Double
) {
    val zone = ZoneId.systemDefault()
    val offsetSeconds = zone.rules.getOffset(date.atStartOfDay()).totalSeconds
    val offsetHours = offsetSeconds / 3600.0

    val jd = toJulianDay(date.year, date.monthValue, date.dayOfMonth)
    val d = jd - 2451545.0
    val g = (357.529 + 0.98560028 * d) % 360.0
    val q = (280.459 + 0.98564736 * d) % 360.0
    val l = (q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g))) % 360.0
    val e = 23.439 - 0.00000036 * d
    val declination = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
    
    val eqTime = q / 15.0 - (Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0)
    var eqTimeHours = eqTime
    if (eqTimeHours > 12.0) eqTimeHours -= 24.0
    if (eqTimeHours < -12.0) eqTimeHours += 24.0

    val noon = 12.0 + offsetHours - (lng / 15.0) - eqTimeHours

    val sunriseAngle = 90.8333
    val sunriseDiff = sunAngleTime(sunriseAngle, lat, declination)
    val sunrise = noon - sunriseDiff
    val sunset = noon + sunriseDiff

    val fajrDiff = sunAngleTime(90.0 + fajrAngle, lat, declination)
    val fajr = noon - fajrDiff

    val imsak = fajr - (10.0 / 60.0)

    val asrAngle = Math.toDegrees(
      atan(1.0 / (shadowFactor + tan(Math.toRadians(abs(lat - declination)))))
    )
    val asrDiff = sunAngleTime(90.0 - asrAngle, lat, declination)
    val asr = noon + asrDiff

    val maghrib = sunset + (3.0 / 60.0)

    val ishaDiff = sunAngleTime(90.0 + ishaAngle, lat, declination)
    val isha = noon + ishaDiff

    println("Fajr: $fajr, Asr: $asr, AsrAngle: $asrAngle")
}

fun main() {
    computePrayerTimes(LocalDate.now(), 23.8, 90.4, "Asia/Dhaka", 15.0, 15.0, 1.0)
}
