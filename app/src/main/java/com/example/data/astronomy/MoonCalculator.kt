package com.example.data.astronomy

import java.time.Duration
import java.time.Instant
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/** Where the moon is and how much of it is lit, at one instant, for one place. */
data class MoonState(
  /** The sunlit fraction of the disc, 0..1 - the phase, not how much moonlight reaches you. */
  val fractionLit: Double,
  /** One of the keys `LocalizationManager.getMoonPhaseName` understands. */
  val phaseKey: String,
  val isWaxing: Boolean,
  /** Geocentric altitude of the moon's centre, degrees. */
  val altitudeDegrees: Double,
  val isAboveHorizon: Boolean,
  /** The next rise and set within about a day, or null if none falls in that window. */
  val nextRise: Instant?,
  val nextSet: Instant?
) {
  val percentLit: Int get() = Math.round(fractionLit * 100).toInt()
}

/**
 * Moon phase and position, computed rather than looked up.
 *
 * The card this feeds used to take the percentage from a table keyed on the Hijri day - day 12
 * was always 80%, all day, everywhere. The real value on that day was about 88%, and the moon was
 * below the horizon at the midday the screenshot was taken, so "80% illumination" read as though
 * it described the light arriving then.
 *
 * Low-precision lunar theory - the leading terms of the lunar longitude and latitude series, the
 * same order of approximation as common almanac software. Lunar longitude is good to well under a
 * degree, which puts the illuminated fraction within about 1% and rise/set within a few minutes;
 * checked against a published table for Dhaka, moonrise on 23 September 2026 came out one minute
 * off. Pure Kotlin, no Android types, so it is unit-testable like PrayerCalculator.
 */
object MoonCalculator {

  /** Obliquity of the ecliptic. */
  private val OBLIQUITY = rad(23.4397)

  /**
   * Geocentric altitude of the moon's centre at the moment of rise or set. Positive because the
   * moon's horizontal parallax (~0.95 deg) outweighs refraction plus semi-diameter.
   */
  private const val RISE_SET_ALTITUDE = 0.125

  fun stateAt(instant: Instant, latitude: Double, longitude: Double): MoonState {
    val (fraction, elongation) = illumination(instant)
    val altitude = altitude(instant, latitude, longitude)
    return MoonState(
      fractionLit = fraction,
      phaseKey = phaseKeyFor(elongation),
      isWaxing = elongation < 180.0,
      altitudeDegrees = altitude,
      isAboveHorizon = altitude > RISE_SET_ALTITUDE,
      nextRise = nextCrossing(instant, latitude, longitude, rising = true),
      nextSet = nextCrossing(instant, latitude, longitude, rising = false)
    )
  }

  /** The sunlit fraction and the Sun-Moon elongation along the ecliptic (0..360, waxing < 180). */
  fun illumination(instant: Instant): Pair<Double, Double> {
    val d = daysSinceJ2000(instant)
    val (moonLon, moonLat) = moonEcliptic(d)
    val sunLon = sunLongitude(d)
    val elongation = norm(moonLon - sunLon)
    val separation = acos(cos(rad(moonLat)) * cos(rad(moonLon - sunLon)))
    return (1.0 - cos(separation)) / 2.0 to elongation
  }

  /**
   * The principal phases are the moments the elongation reaches 0/90/180/270 deg. Giving each a
   * window of about a day either side keeps "Full Moon" from flickering to "Waxing Gibbous" an hour
   * before it, which is what these names mean to a reader.
   */
  fun phaseKeyFor(elongation: Double): String = when (norm(elongation)) {
    in 12.0..<78.0 -> "WAXING_CRESCENT"
    in 78.0..<102.0 -> "FIRST_QUARTER"
    in 102.0..<168.0 -> "WAXING_GIBBOUS"
    in 168.0..<192.0 -> "FULL_MOON"
    in 192.0..<258.0 -> "WANING_GIBBOUS"
    in 258.0..<282.0 -> "LAST_QUARTER"
    in 282.0..<348.0 -> "WANING_CRESCENT"
    else -> "NEW_MOON"
  }

  fun altitude(instant: Instant, latitude: Double, longitude: Double): Double {
    val d = daysSinceJ2000(instant)
    val (lonDeg, latDeg) = moonEcliptic(d)
    val l = rad(lonDeg)
    val b = rad(latDeg)
    val rightAscension = atan2(sin(l) * cos(OBLIQUITY) - tan(b) * sin(OBLIQUITY), cos(l))
    val declination = asin(sin(b) * cos(OBLIQUITY) + cos(b) * sin(OBLIQUITY) * sin(l))
    val siderealTime = rad(280.16 + 360.9856235 * d) + rad(longitude)
    val hourAngle = siderealTime - rightAscension
    val phi = rad(latitude)
    return deg(asin(sin(phi) * sin(declination) + cos(phi) * cos(declination) * cos(hourAngle)))
  }

  /**
   * The next time the moon crosses the rise/set altitude in the given direction.
   *
   * Scans 26 hours in ten-minute steps - the moon rises and sets about 50 minutes later each day,
   * so a 24-hour window can miss an event entirely - then bisects the bracketing step to within a
   * few seconds. Near the poles there may be no crossing at all, which returns null.
   */
  fun nextCrossing(from: Instant, latitude: Double, longitude: Double, rising: Boolean): Instant? {
    val step = Duration.ofMinutes(10)
    var t = from
    var previous = altitude(t, latitude, longitude) - RISE_SET_ALTITUDE
    repeat(26 * 6) {
      val next = t.plus(step)
      val current = altitude(next, latitude, longitude) - RISE_SET_ALTITUDE
      val crossed = if (rising) previous < 0 && current >= 0 else previous >= 0 && current < 0
      if (crossed) return bisect(t, next, latitude, longitude, rising)
      t = next
      previous = current
    }
    return null
  }

  private fun bisect(
    start: Instant,
    end: Instant,
    latitude: Double,
    longitude: Double,
    rising: Boolean
  ): Instant {
    var a = start
    var b = end
    repeat(12) {
      val mid = a.plusMillis(Duration.between(a, b).toMillis() / 2)
      val aboveThreshold = altitude(mid, latitude, longitude) - RISE_SET_ALTITUDE >= 0
      if (aboveThreshold == rising) b = mid else a = mid
    }
    return b
  }

  /** Ecliptic longitude and latitude of the moon, degrees. */
  private fun moonEcliptic(d: Double): Pair<Double, Double> {
    val meanLongitude = 218.316 + 13.176396 * d
    val m = rad(134.963 + 13.064993 * d)        // moon's mean anomaly
    val f = rad(93.272 + 13.229350 * d)         // argument of latitude
    val e = rad(297.850 + 12.190749 * d)        // mean elongation
    val ms = rad(357.529 + 0.98560028 * d)      // sun's mean anomaly
    val longitude = meanLongitude +
      6.289 * sin(m) -
      1.274 * sin(m - 2 * e) +                  // evection
      0.658 * sin(2 * e) -                      // variation
      0.186 * sin(ms) -                         // annual equation
      0.059 * sin(2 * m - 2 * e) -
      0.057 * sin(m - 2 * e + ms) +
      0.053 * sin(m + 2 * e) +
      0.046 * sin(2 * e - ms) +
      0.041 * sin(m - ms) -
      0.035 * sin(e) -
      0.031 * sin(m + ms)
    val latitude = 5.128 * sin(f) +
      0.281 * sin(m + f) -
      0.278 * sin(f - m) -
      0.173 * sin(f - 2 * e)
    return norm(longitude) to latitude
  }

  /** Ecliptic longitude of the sun, degrees. */
  private fun sunLongitude(d: Double): Double {
    val m = rad(357.5291 + 0.98560028 * d)
    val centre = 1.9148 * sin(m) + 0.02 * sin(2 * m) + 0.0003 * sin(3 * m)
    return norm(deg(m) + centre + 102.9372 + 180.0)
  }

  private fun daysSinceJ2000(instant: Instant): Double =
    instant.toEpochMilli() / 86_400_000.0 + 2440587.5 - 2451545.0

  private fun norm(degrees: Double): Double {
    val r = degrees % 360.0
    return if (r < 0) r + 360.0 else r
  }

  private fun rad(degrees: Double) = Math.toRadians(degrees)
  private fun deg(radians: Double) = Math.toDegrees(radians)
}
