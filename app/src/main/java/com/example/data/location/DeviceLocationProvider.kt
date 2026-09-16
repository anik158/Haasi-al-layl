package com.example.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Coordinates from the device's own satellite receiver.
 *
 * This is the part that works with no network at all: GPS is a *receiver*, solving position from
 * the timing of signals broadcast by satellites. Nothing leaves the phone. Turning on the system
 * Location toggle is a permission gate, not a network one.
 *
 * Two honest limits. A cold fix without the internet has to read orbit data from the satellites
 * themselves, which takes tens of seconds to a few minutes and needs a view of the sky; and
 * indoors it often never arrives. Both are why the caller caches the last good fix rather than
 * demanding a fresh one on every launch.
 *
 * Uses the platform [LocationManager] rather than Play Services' fused provider, which is not a
 * dependency of this project - and the fused provider would happily answer from Wi-Fi and cell
 * towers, which needs a network and would defeat the point.
 */
object DeviceLocationProvider {

  private const val TAG = "DeviceLocation"

  /** Older than this and a cached fix is only a fallback, not an answer. */
  const val FRESH_FIX_MAX_AGE_MS = 30L * 60L * 1000L

  /** A fix this imprecise cannot distinguish neighbouring towns, so prayer times would drift. */
  private const val MAX_ACCEPTABLE_ACCURACY_METRES = 5_000f

  /** How long to wait for a live fix before giving up and using whatever was cached. */
  private const val LIVE_FIX_TIMEOUT_MS = 20_000L

  fun hasPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
      PackageManager.PERMISSION_GRANTED ||
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
      PackageManager.PERMISSION_GRANTED

  /** Whether the user has the system Location toggle on at all. */
  fun isLocationEnabled(context: Context): Boolean {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
      ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      manager.isLocationEnabled
    } else {
      manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
        manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
  }

  /**
   * The best fix already on the device, without waking any radio. Instant, and usually enough:
   * a phone that has been outdoors at all today has a usable fix sitting here.
   */
  @Suppress("MissingPermission")
  fun lastKnownFix(context: Context): Location? {
    if (!hasPermission(context)) return null
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
      ?: return null
    return try {
      // GPS first: it is the offline one. Network is a fallback and needs connectivity.
      listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        .mapNotNull { provider ->
          runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
        }
        .filter { it.accuracy <= MAX_ACCEPTABLE_ACCURACY_METRES || it.accuracy == 0f }
        .maxByOrNull { it.time }
    } catch (e: SecurityException) {
      Log.w(TAG, "location permission revoked mid-flight", e)
      null
    }
  }

  /** True when [location] is recent enough to be treated as where the user is now. */
  fun isFresh(location: Location, nowMillis: Long = System.currentTimeMillis()): Boolean =
    nowMillis - location.time <= FRESH_FIX_MAX_AGE_MS

  /**
   * Asks for one live fix, giving up after [LIVE_FIX_TIMEOUT_MS].
   *
   * Single-shot rather than continuous updates: prayer times do not change as the user walks
   * around, and a continuous listener is a battery cost with nothing to show for it.
   */
  @Suppress("MissingPermission")
  suspend fun requestFreshFix(context: Context): Location? {
    if (!hasPermission(context) || !isLocationEnabled(context)) return null
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
      ?: return null

    val provider = when {
      manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
      manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
      else -> return null
    }

    return withTimeoutOrNull(LIVE_FIX_TIMEOUT_MS) {
      suspendCancellableCoroutine { continuation ->
        val listener = object : android.location.LocationListener {
          override fun onLocationChanged(location: Location) {
            manager.removeUpdates(this)
            if (continuation.isActive) continuation.resume(location)
          }

          // Required on API 24-28, where the interface has no default implementations.
          override fun onStatusChanged(p: String?, s: Int, e: android.os.Bundle?) = Unit
          override fun onProviderEnabled(p: String) = Unit
          override fun onProviderDisabled(p: String) {
            manager.removeUpdates(this)
            if (continuation.isActive) continuation.resume(null)
          }
        }

        try {
          manager.requestLocationUpdates(provider, 0L, 0f, listener, context.mainLooper)
        } catch (e: SecurityException) {
          Log.w(TAG, "could not request updates", e)
          if (continuation.isActive) continuation.resume(null)
          return@suspendCancellableCoroutine
        }

        continuation.invokeOnCancellation {
          runCatching { manager.removeUpdates(listener) }
        }
      }
    }
  }
}
