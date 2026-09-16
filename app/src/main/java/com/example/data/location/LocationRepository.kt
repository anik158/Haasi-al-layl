package com.example.data.location

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.example.data.model.CityLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.time.ZoneId
import java.util.Locale

/**
 * Where the app thinks the user is.
 *
 * The ladder, best first:
 *  1. The device's own GPS fix - exact, and the only rung that works with no network
 *  2. A name for those coordinates, if there is a network; otherwise the coordinates themselves
 *  3. IP geolocation, which resolves to the ISP's registered hub and can be a long way off
 *  4. Whatever city the user last chose by hand
 *
 * Rungs 1 and 2 are deliberately separate. Coordinates are what the prayer mathematics needs, and
 * they are available offline; the city *name* is only ever for display, and needs a lookup. Before
 * this existed the app had no rung 1 at all, so a user in Helsinki was placed in Turku - 150 km
 * away, and 8 to 11 minutes wrong.
 */
object LocationRepository {

  private const val TAG = "LocationRepository"

  private val geocodingApi by lazy { GeocodingApiService.create() }

  /**
   * Coordinates from the device, named if possible.
   *
   * [allowSlowFix] requests a live fix, which can take tens of seconds outdoors and may never
   * arrive indoors - so it is opt-in, for when the user explicitly taps "detect", rather than
   * something a launch waits on.
   */
  suspend fun detectFromDevice(
    context: Context,
    allowSlowFix: Boolean
  ): CityLocation? = withContext(Dispatchers.IO) {
    val cached = DeviceLocationProvider.lastKnownFix(context)
    val fix = when {
      cached != null && DeviceLocationProvider.isFresh(cached) -> cached
      allowSlowFix -> DeviceLocationProvider.requestFreshFix(context) ?: cached
      else -> cached
    } ?: return@withContext null

    val named = reverseGeocode(context, fix.latitude, fix.longitude)
    CityLocation(
      name = named?.first ?: formatCoordinates(fix.latitude, fix.longitude),
      country = named?.second.orEmpty(),
      latitude = fix.latitude,
      longitude = fix.longitude,
      // The user is physically standing here, so the device's own zone is the right one.
      timeZoneId = ZoneId.systemDefault().id
    )
  }

  /** Free-text place search, so the picker is not limited to the presets. Needs a network. */
  suspend fun searchPlaces(query: String): List<GeocodingResult> = withContext(Dispatchers.IO) {
    if (query.isBlank()) return@withContext emptyList()
    try {
      geocodingApi.search(query.trim()).results.orEmpty()
        .sortedByDescending { it.population ?: 0L }
    } catch (e: Exception) {
      Log.w(TAG, "place search failed for '$query'", e)
      emptyList()
    }
  }

  /**
   * Coordinates to a place name. Returns null offline, which is the signal to show the
   * coordinates instead.
   *
   * Tries the platform [Geocoder] first, then a keyless HTTP service - `Geocoder` depends on a
   * backend that is absent on devices without Google Play Services, where it silently returns
   * nothing.
   */
  suspend fun reverseGeocode(
    context: Context,
    latitude: Double,
    longitude: Double
  ): Pair<String, String>? = withContext(Dispatchers.IO) {
    platformReverseGeocode(context, latitude, longitude)
      ?: httpReverseGeocode(latitude, longitude)
  }

  @Suppress("DEPRECATION") // The async overload is API 33+; minSdk here is 24.
  private fun platformReverseGeocode(
    context: Context,
    latitude: Double,
    longitude: Double
  ): Pair<String, String>? = try {
    if (!Geocoder.isPresent()) {
      null
    } else {
      Geocoder(context, Locale.ENGLISH)
        .getFromLocation(latitude, longitude, 1)
        ?.firstOrNull()
        ?.let { address ->
          val city = address.locality
            ?: address.subAdminArea
            ?: address.adminArea
            ?: return@let null
          city to (address.countryName ?: "")
        }
    }
  } catch (e: Exception) {
    Log.i(TAG, "platform geocoder unavailable, falling back", e)
    null
  }

  private fun httpReverseGeocode(latitude: Double, longitude: Double): Pair<String, String>? = try {
    val url = URL(
      "https://api.bigdatacloud.net/data/reverse-geocode-client" +
        "?latitude=$latitude&longitude=$longitude&localityLanguage=en"
    )
    val connection = (url.openConnection() as HttpURLConnection).apply {
      requestMethod = "GET"
      connectTimeout = 8000
      readTimeout = 8000
    }
    if (connection.responseCode == 200) {
      val json = org.json.JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
      val city = listOf("city", "locality", "principalSubdivision")
        .firstNotNullOfOrNull { key -> json.optString(key, "").ifBlank { null } }
      val country = json.optString("countryName", "")
      city?.let { it to country }
    } else {
      null
    }
  } catch (e: Exception) {
    Log.w(TAG, "http reverse geocode failed", e)
    null
  }

  /** "23.81°N, 90.41°E" - shown in place of a city name when nothing can name the coordinates. */
  fun formatCoordinates(latitude: Double, longitude: Double): String {
    val ns = if (latitude >= 0) "N" else "S"
    val ew = if (longitude >= 0) "E" else "W"
    return String.format(
      Locale.US,
      "%.2f°%s, %.2f°%s",
      kotlin.math.abs(latitude), ns,
      kotlin.math.abs(longitude), ew
    )
  }
}
