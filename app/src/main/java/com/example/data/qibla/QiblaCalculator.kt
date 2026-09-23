package com.example.data.qibla

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object QiblaCalculator {
  // Coordinates of the Holy Kaaba in Makkah
  const val KAABA_LATITUDE = 21.422487
  const val KAABA_LONGITUDE = 39.826206
  private const val EARTH_RADIUS_KM = 6371.0

  /**
   * Calculates true bearing (in degrees from 0 to 360 clockwise from True North) towards the Kaaba.
   */
  fun calculateQiblaBearing(userLat: Double, userLng: Double): Float {
    val phi1 = Math.toRadians(userLat)
    val phi2 = Math.toRadians(KAABA_LATITUDE)
    val deltaLambda = Math.toRadians(KAABA_LONGITUDE - userLng)

    val y = sin(deltaLambda) * cos(phi2)
    val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
    val bearingRad = atan2(y, x)

    val bearingDeg = (Math.toDegrees(bearingRad) + 360.0) % 360.0
    return bearingDeg.toFloat()
  }

  /**
   * Calculates great circle distance to the Kaaba in kilometers.
   */
  /**
   * Great-circle distance between any two points, in kilometres.
   *
   * Extracted from the Kaaba-specific helper because "has the user moved far enough to matter?"
   * is the same question with different endpoints, and a second copy of the haversine would be
   * a second thing to get wrong.
   */
  fun distanceKm(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
  ): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
      kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
      kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
    return EARTH_RADIUS_KM * 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
  }

  fun calculateDistanceToKaabaKm(userLat: Double, userLng: Double): Int {
    val dLat = Math.toRadians(KAABA_LATITUDE - userLat)
    val dLon = Math.toRadians(KAABA_LONGITUDE - userLng)
    val rLat1 = Math.toRadians(userLat)
    val rLat2 = Math.toRadians(KAABA_LATITUDE)

    val a = sin(dLat / 2) * sin(dLat / 2) +
      cos(rLat1) * cos(rLat2) * sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return (EARTH_RADIUS_KM * c).toInt()
  }
}
