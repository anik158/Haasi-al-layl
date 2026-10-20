package com.example.data.astronomy

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/** How visible the new crescent is on one evening, in Odeh's (2004) zones, worst first. */
enum class CrescentVisibility {
  /** The moon sets before the sun, or the new moon has not happened yet: no one can see it. */
  IMPOSSIBLE,

  /** Odeh zone D: not even with a telescope. */
  NOT_VISIBLE,

  /** Odeh zone C: a telescope or binoculars only. */
  OPTICAL_ONLY,

  /** Odeh zone B: with optical aid, and perhaps by eye in perfect conditions. */
  MAYBE_NAKED_EYE,

  /** Odeh zone A: by eye. */
  NAKED_EYE
}

/** The numbers behind one evening's verdict, for the review screen and for tests. */
data class CrescentEvening(
  val date: LocalDate,
  val sunset: Instant?,
  val moonset: Instant?,
  /** The new moon nearest that sunset. */
  val newMoon: Instant,
  /** Sunset minus the new moon; negative when the new moon is still to come. */
  val ageHours: Double,
  /** Moonset minus sunset; negative or null when the moon was already down. */
  val lagMinutes: Long?,
  /** Moon minus sun altitude at the best time, degrees. */
  val arcv: Double,
  /** Moon-sun separation at the best time, degrees. */
  val arcl: Double,
  /** Topocentric crescent width, arcminutes. */
  val widthArcmin: Double,
  /** Odeh's V; >= 5.65 is naked-eye. */
  val odehV: Double,
  val visibility: CrescentVisibility
)

/**
 * Whether the new crescent can be seen from one place on one evening.
 *
 * Moon sighting starts each Hijri month, and the sighting depends on cloud, haze and the observer -
 * none of which a calculation knows. What it does know is the geometry: whether the moon is still
 * up after sunset, how far from the sun and how thick the crescent is. Odeh's criterion (Experimental
 * Astronomy 18, 2004, from 737 recorded sightings) turns that into four zones; this adds a fifth,
 * [CrescentVisibility.IMPOSSIBLE], for evenings where no sighting can be true.
 *
 * Built on [MoonCalculator]'s low-precision series - good to a few tenths of a degree, which is
 * enough to tell the clear cases apart. Evenings near a zone boundary stay uncertain either way.
 */
object CrescentCalculator {

  private const val SUN_SET_ALTITUDE = -0.833
  private const val EARTH_RADIUS_KM = 6378.14
  private const val MOON_TO_EARTH_RADIUS = 0.2725
  private val OBLIQUITY = Math.toRadians(23.4397)

  /** Synodic rate of the elongation, degrees per day. */
  private const val ELONGATION_RATE = 12.190749

  fun evaluate(date: LocalDate, latitude: Double, longitude: Double, zone: ZoneId): CrescentEvening {
    val sunset = sunset(date, latitude, longitude, zone)
    // Somewhere without a sunset that day (polar summer), judge at local 18:00 - the verdict is
    // then meaningless, but nothing downstream crashes.
    val reference = sunset ?: date.atTime(LocalTime.of(18, 0)).atZone(zone).toInstant()
    val newMoon = nearestNewMoon(reference)
    val ageHours = Duration.between(newMoon, reference).toMinutes() / 60.0

    val moonUpAtSunset = MoonCalculator.altitude(reference, latitude, longitude) > MOON_RISE_SET_ALTITUDE
    val moonset = if (moonUpAtSunset) {
      MoonCalculator.nextCrossing(reference, latitude, longitude, rising = false)
    } else {
      null
    }
    val lagMinutes = moonset?.let { Duration.between(reference, it).toMinutes() }

    // Odeh's best time: sunset plus four-ninths of the lag.
    val best = if (lagMinutes != null && lagMinutes > 0) {
      reference.plusSeconds(lagMinutes * 60 * 4 / 9)
    } else {
      reference
    }
    val geometry = geometryAt(best, latitude, longitude)
    val v = geometry.arcv - (-0.1018 * cube(geometry.width) + 0.7319 * geometry.width * geometry.width -
      6.3226 * geometry.width + 7.1651)

    val visibility = when {
      sunset == null -> CrescentVisibility.NOT_VISIBLE
      ageHours <= 0 || lagMinutes == null || lagMinutes <= 0 -> CrescentVisibility.IMPOSSIBLE
      v >= 5.65 -> CrescentVisibility.NAKED_EYE
      v >= 2.0 -> CrescentVisibility.MAYBE_NAKED_EYE
      v >= -0.96 -> CrescentVisibility.OPTICAL_ONLY
      else -> CrescentVisibility.NOT_VISIBLE
    }
    return CrescentEvening(
      date = date,
      sunset = sunset,
      moonset = moonset,
      newMoon = newMoon,
      ageHours = ageHours,
      lagMinutes = lagMinutes,
      arcv = geometry.arcv,
      arcl = geometry.arcl,
      widthArcmin = geometry.width,
      odehV = v,
      visibility = visibility
    )
  }

  /**
   * The geocentric new moon - sun and moon at the same ecliptic longitude - nearest [instant].
   * Newton's method on the elongation, which moves at a nearly steady 12.2 deg a day; five steps
   * reach well under a minute.
   */
  fun nearestNewMoon(instant: Instant): Instant {
    var t = instant
    repeat(6) {
      val step = signedElongation(t) / ELONGATION_RATE
      t = t.minusMillis((step * 86_400_000.0).toLong())
    }
    return t
  }

  /** Sunset on [date] in [zone], or null where the sun does not set (or rise) that day. */
  fun sunset(date: LocalDate, latitude: Double, longitude: Double, zone: ZoneId): Instant? {
    val noon = date.atTime(LocalTime.NOON).atZone(zone).toInstant()
    val step = Duration.ofMinutes(10)
    var t = noon
    var previous = sunAltitude(t, latitude, longitude) - SUN_SET_ALTITUDE
    repeat(14 * 6) {
      val next = t.plus(step)
      val current = sunAltitude(next, latitude, longitude) - SUN_SET_ALTITUDE
      if (previous >= 0 && current < 0) {
        var a = t
        var b = next
        repeat(12) {
          val mid = a.plusMillis(Duration.between(a, b).toMillis() / 2)
          if (sunAltitude(mid, latitude, longitude) - SUN_SET_ALTITUDE >= 0) a = mid else b = mid
        }
        return b
      }
      t = next
      previous = current
    }
    return null
  }

  fun sunAltitude(instant: Instant, latitude: Double, longitude: Double): Double =
    horizontal(instant, latitude, longitude, sunEquatorial(instant)).first

  private data class Geometry(val arcv: Double, val arcl: Double, val width: Double)

  private fun geometryAt(instant: Instant, latitude: Double, longitude: Double): Geometry {
    val d = MoonCalculator.daysSinceJ2000(instant)
    val (sunAlt, sunAz) = horizontal(instant, latitude, longitude, sunEquatorial(instant))
    val (moonAltGeo, moonAz) = horizontal(instant, latitude, longitude, moonEquatorial(d))
    val parallax = Math.toDegrees(asin(EARTH_RADIUS_KM / MoonCalculator.moonDistanceKm(d)))
    // Parallax lowers the moon by up to a degree - as much as the whole margin on a hard evening.
    val moonAlt = moonAltGeo - parallax * cos(Math.toRadians(moonAltGeo))
    val arcl = separation(sunAlt, sunAz, moonAlt, moonAz)
    val semiDiameterArcmin = MOON_TO_EARTH_RADIUS * parallax * 60.0 *
      (1 + sin(Math.toRadians(moonAlt)) * sin(Math.toRadians(parallax)))
    val width = semiDiameterArcmin * (1 - cos(Math.toRadians(arcl)))
    return Geometry(arcv = moonAlt - sunAlt, arcl = arcl, width = width)
  }

  private fun separation(alt1: Double, az1: Double, alt2: Double, az2: Double): Double {
    val a1 = Math.toRadians(alt1)
    val a2 = Math.toRadians(alt2)
    val c = sin(a1) * sin(a2) + cos(a1) * cos(a2) * cos(Math.toRadians(az1 - az2))
    return Math.toDegrees(acos(c.coerceIn(-1.0, 1.0)))
  }

  /** Right ascension and declination, radians. */
  private fun sunEquatorial(instant: Instant): Pair<Double, Double> {
    val lon = Math.toRadians(MoonCalculator.sunLongitude(MoonCalculator.daysSinceJ2000(instant)))
    return atan2(sin(lon) * cos(OBLIQUITY), cos(lon)) to asin(sin(OBLIQUITY) * sin(lon))
  }

  private fun moonEquatorial(d: Double): Pair<Double, Double> {
    val (lonDeg, latDeg) = MoonCalculator.moonEcliptic(d)
    val l = Math.toRadians(lonDeg)
    val b = Math.toRadians(latDeg)
    val ra = atan2(sin(l) * cos(OBLIQUITY) - tan(b) * sin(OBLIQUITY), cos(l))
    val dec = asin(sin(b) * cos(OBLIQUITY) + cos(b) * sin(OBLIQUITY) * sin(l))
    return ra to dec
  }

  /** Geocentric altitude and azimuth, degrees, of a body at [equatorial]. */
  private fun horizontal(
    instant: Instant,
    latitude: Double,
    longitude: Double,
    equatorial: Pair<Double, Double>
  ): Pair<Double, Double> {
    val (ra, dec) = equatorial
    val d = MoonCalculator.daysSinceJ2000(instant)
    val hourAngle = Math.toRadians(280.16 + 360.9856235 * d + longitude) - ra
    val phi = Math.toRadians(latitude)
    val alt = asin(sin(phi) * sin(dec) + cos(phi) * cos(dec) * cos(hourAngle))
    val az = atan2(sin(hourAngle), cos(hourAngle) * sin(phi) - tan(dec) * cos(phi))
    return Math.toDegrees(alt) to Math.toDegrees(az)
  }

  /** Moon minus sun ecliptic longitude, -180..180. */
  private fun signedElongation(instant: Instant): Double {
    val d = MoonCalculator.daysSinceJ2000(instant)
    val e = (MoonCalculator.moonEcliptic(d).first - MoonCalculator.sunLongitude(d)) % 360.0
    return when {
      e > 180.0 -> e - 360.0
      e <= -180.0 -> e + 360.0
      else -> e
    }
  }

  /** As in [MoonCalculator]: the moon's centre at rise or set, geocentric. */
  private const val MOON_RISE_SET_ALTITUDE = 0.125

  private fun cube(x: Double) = x * x * x
}
