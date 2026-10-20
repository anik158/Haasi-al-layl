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
 * Lunar theory from Meeus ch. 47 (truncated ELP-2000/82) - longitude good to about 0.003 deg, enough
 * for the new moon to within minutes, which the crescent check (CrescentCalculator) needs. The
 * illuminated fraction is within about 1% and rise/set within a few minutes;
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

  /**
   * Ecliptic longitude and latitude of the moon, degrees - Meeus, *Astronomical Algorithms*
   * ch. 47 (ELP-2000/82, truncated): every longitude term above 0.0003 deg and the latitude terms
   * above 0.001 deg. Longitude is good to about 0.003 deg, which puts the new moon within a few
   * minutes - the earlier eleven-term series was up to an hour out, too coarse for sighting nights.
   */
  internal fun moonEcliptic(d: Double): Pair<Double, Double> {
    val a = LunarArguments(d)
    var sumL = 0.0
    LONGITUDE_TERMS.forEach { t ->
      sumL += t[4] * a.eccentricity(t[1]) * sin(a.argument(t))
    }
    var sumB = 0.0
    LATITUDE_TERMS.forEach { t ->
      sumB += t[4] * a.eccentricity(t[1]) * sin(a.argument(t))
    }
    sumL += 3958 * sin(a.a1) + 1962 * sin(a.lPrime - a.f) + 318 * sin(a.a2)
    sumB += -2235 * sin(a.lPrime) + 382 * sin(a.a3) + 175 * sin(a.a1 - a.f) +
      175 * sin(a.a1 + a.f) + 127 * sin(a.lPrime - a.mPrime) - 115 * sin(a.lPrime + a.mPrime)
    return norm(deg(a.lPrime) + sumL / 1e6) to sumB / 1e6
  }

  /** Earth-moon distance, km, from the same theory - good to about 10 km. */
  internal fun moonDistanceKm(d: Double): Double {
    val a = LunarArguments(d)
    var sumR = 0.0
    LONGITUDE_TERMS.forEach { t ->
      sumR += t[5] * a.eccentricity(t[1]) * cos(a.argument(t))
    }
    return 385000.56 + sumR / 1000.0
  }

  /** Meeus 47.1-47.6, in radians. [d] is UT; terrestrial time runs about 69 s ahead. */
  private class LunarArguments(d: Double) {
    private val t = (d + 69.0 / 86_400.0) / 36525.0
    val lPrime = rad(218.3164477 + 481267.88123421 * t - 0.0015786 * t * t)
    val elongation = rad(297.8501921 + 445267.1114034 * t - 0.0018819 * t * t)
    val sunAnomaly = rad(357.5291092 + 35999.0502909 * t - 0.0001536 * t * t)
    val mPrime = rad(134.9633964 + 477198.8675055 * t + 0.0087414 * t * t)
    val f = rad(93.2720950 + 483202.0175233 * t - 0.0036539 * t * t)
    val a1 = rad(119.75 + 131.849 * t)
    val a2 = rad(53.09 + 479264.290 * t)
    val a3 = rad(313.45 + 481266.484 * t)
    private val e = 1 - 0.002516 * t - 0.0000074 * t * t

    fun argument(term: DoubleArray) =
      term[0] * elongation + term[1] * sunAnomaly + term[2] * mPrime + term[3] * f

    /** Terms with the sun's anomaly shrink with the earth's orbital eccentricity (Meeus 47.6). */
    fun eccentricity(sunMultiple: Double) = when (Math.abs(sunMultiple)) {
      1.0 -> e
      2.0 -> e * e
      else -> 1.0
    }
  }

  /**
   * Apparent ecliptic longitude of the sun, degrees (Meeus 25.2-25.8, good to 0.01 deg). The mean
   * longitude has its own rate: deriving it from the anomaly, as this used to, ignores the
   * perihelion's drift and put the sun 0.4 deg out by 2024 - the new moon fifty minutes early.
   */
  internal fun sunLongitude(d: Double): Double {
    val t = d / 36525.0
    val meanLongitude = 280.46646 + 36000.76983 * t + 0.0003032 * t * t
    val m = rad(357.52911 + 35999.05029 * t - 0.0001537 * t * t)
    val centre = (1.914602 - 0.004817 * t) * sin(m) + (0.019993 - 0.000101 * t) * sin(2 * m) +
      0.000289 * sin(3 * m)
    val omega = rad(125.04 - 1934.136 * t)
    return norm(meanLongitude + centre - 0.00569 - 0.00478 * sin(omega))
  }

  internal fun daysSinceJ2000(instant: Instant): Double =
    instant.toEpochMilli() / 86_400_000.0 + 2440587.5 - 2451545.0

  private fun norm(degrees: Double): Double {
    val r = degrees % 360.0
    return if (r < 0) r + 360.0 else r
  }

  private fun rad(degrees: Double) = Math.toRadians(degrees)
  private fun deg(radians: Double) = Math.toDegrees(radians)

  // Meeus table 47.A: D, M, M', F, longitude (1e-6 deg), distance (1e-3 km).
  private val LONGITUDE_TERMS: List<DoubleArray> = listOf(
    t(0, 0, 1, 0, 6288774, -20905355), t(2, 0, -1, 0, 1274027, -3699111),
    t(2, 0, 0, 0, 658314, -2955968), t(0, 0, 2, 0, 213618, -569925),
    t(0, 1, 0, 0, -185116, 48888), t(0, 0, 0, 2, -114332, -3149),
    t(2, 0, -2, 0, 58793, 246158), t(2, -1, -1, 0, 57066, -152138),
    t(2, 0, 1, 0, 53322, -170733), t(2, -1, 0, 0, 45758, -204586),
    t(0, 1, -1, 0, -40923, -129620), t(1, 0, 0, 0, -34720, 108743),
    t(0, 1, 1, 0, -30383, 104755), t(2, 0, 0, -2, 15327, 10321),
    t(0, 0, 1, 2, -12528, 0), t(0, 0, 1, -2, 10980, 79661),
    t(4, 0, -1, 0, 10675, -34782), t(0, 0, 3, 0, 10034, -23210),
    t(4, 0, -2, 0, 8548, -21636), t(2, 1, -1, 0, -7888, 24208),
    t(2, 1, 0, 0, -6766, 30824), t(1, 0, -1, 0, -5163, -8379),
    t(1, 1, 0, 0, 4987, -16675), t(2, -1, 1, 0, 4036, -12831),
    t(2, 0, 2, 0, 3994, -10445), t(4, 0, 0, 0, 3861, -11650),
    t(2, 0, -3, 0, 3665, 14403), t(0, 1, -2, 0, -2689, -7003),
    t(2, 0, -1, 2, -2602, 0), t(2, -1, -2, 0, 2390, 10056),
    t(1, 0, 1, 0, -2348, 6322), t(2, -2, 0, 0, 2236, -9884),
    t(0, 1, 2, 0, -2120, 5751), t(0, 2, 0, 0, -2069, 0),
    t(2, -2, -1, 0, 2048, -4950), t(2, 0, 1, -2, -1773, 4130),
    t(2, 0, 0, 2, -1595, 0), t(4, -1, -1, 0, 1215, -3958),
    t(0, 0, 2, 2, -1110, 0), t(3, 0, -1, 0, -892, 3258),
    t(2, 1, 1, 0, -810, 2616), t(4, -1, -2, 0, 759, -1897),
    t(0, 2, -1, 0, -713, -2117), t(2, 2, -1, 0, -700, 2354),
    t(2, 1, -2, 0, 691, 0), t(2, -1, 0, -2, 596, 0),
    t(4, 0, 1, 0, 549, -1423), t(0, 0, 4, 0, 537, -1117),
    t(4, -1, 0, 0, 520, -1571), t(1, 0, -2, 0, -487, -1739),
    t(2, 1, 0, -2, -399, 0), t(0, 0, 2, -2, -381, -4421),
    t(1, 1, 1, 0, 351, 0), t(3, 0, -2, 0, -340, 0),
    t(4, 0, -3, 0, 330, 0), t(2, -1, 2, 0, 327, 0),
    t(0, 2, 1, 0, -323, 1165), t(1, 1, -1, 0, 299, 0),
    t(2, 0, 3, 0, 294, 0), t(2, 0, -1, -2, 0, 8752)
  )

  // Meeus table 47.B: D, M, M', F, latitude (1e-6 deg); the last column is unused.
  private val LATITUDE_TERMS: List<DoubleArray> = listOf(
    t(0, 0, 0, 1, 5128122), t(0, 0, 1, 1, 280602), t(0, 0, 1, -1, 277693),
    t(2, 0, 0, -1, 173237), t(2, 0, -1, 1, 55413), t(2, 0, -1, -1, 46271),
    t(2, 0, 0, 1, 32573), t(0, 0, 2, 1, 17198), t(2, 0, 1, -1, 9266),
    t(0, 0, 2, -1, 8822), t(2, -1, 0, -1, 8216), t(2, 0, -2, -1, 4324),
    t(2, 0, 1, 1, 4200), t(2, 1, 0, -1, -3359), t(2, -1, -1, 1, 2463),
    t(2, -1, 0, 1, 2211), t(2, -1, -1, -1, 2065), t(0, 1, -1, -1, -1870),
    t(4, 0, -1, -1, 1828), t(0, 1, 0, 1, -1794), t(0, 0, 0, 3, -1749),
    t(0, 1, -1, 1, -1565), t(1, 0, 0, 1, -1491), t(0, 1, 1, 1, -1475),
    t(0, 1, 1, -1, -1410), t(0, 1, 0, -1, -1344), t(1, 0, 0, -1, -1335),
    t(0, 0, 3, 1, 1107), t(4, 0, 0, -1, 1021), t(4, 0, -1, 1, 833)
  )

  private fun t(d: Int, m: Int, mp: Int, f: Int, coefficient: Int, distance: Int = 0) =
    doubleArrayOf(d.toDouble(), m.toDouble(), mp.toDouble(), f.toDouble(), coefficient.toDouble(), distance.toDouble())
}
