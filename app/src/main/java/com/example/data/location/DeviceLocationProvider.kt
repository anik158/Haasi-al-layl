package com.example.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

  /** How long to wait for a live fix when the caller does not say. */
  const val LIVE_FIX_TIMEOUT_MS = 20_000L

  /**
   * How long a first run waits for a cold fix.
   *
   * A receiver with no almanac has to read the ephemeris off the satellites at 50 bits per
   * second, which takes 30 to 60 seconds with a clear view of the sky. Twenty seconds gives up
   * while that download is still in flight, which is why a first launch gets its own budget - and
   * why the wait is shown rather than hidden.
   */
  const val COLD_FIX_TIMEOUT_MS = 90_000L

  /**
   * Zero, deliberately, even though [observeFixes] only wants one fix.
   *
   * A non-zero `minTime` lets the provider power the receiver down between updates, and a
   * duty-cycled receiver is the last thing you want while *acquiring* a first fix - that is
   * precisely the case this watch exists for. The cost is bounded by cancelling the watch the
   * moment a fix arrives, not by asking for fewer of them.
   */
  private const val WATCH_MIN_TIME_MS = 0L
  private const val WATCH_MIN_DISTANCE_M = 0f

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
   * Whether the **satellite** receiver specifically is switched on.
   *
   * Distinct from [isLocationEnabled], and the distinction matters more than it looks. On ColorOS
   * the master Location toggle can be on while the GPS provider is off, so the phone positions
   * from Wi-Fi and cell towers alone. Online that is merely imprecise; offline it produces no fix
   * at all - while looking, to the user, exactly like location being enabled. "Turn on location"
   * is unhelpful advice to someone who has already done it.
   */
  fun isGpsProviderEnabled(context: Context): Boolean {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
      ?: return false
    return runCatching { manager.isProviderEnabled(LocationManager.GPS_PROVIDER) }
      .getOrDefault(false)
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
   * Every fix the receiver produces, for as long as the flow is collected.
   *
   * The single-shot [requestFreshFix] is the wrong tool for the hardest case this app has: a
   * first run, indoors, with no network and no cached fix. There the fix does not arrive in
   * twenty seconds or in ninety - it arrives when the user next walks outside, which might be
   * minutes later. A timeout cannot help with that; staying subscribed can.
   *
   * The caller is expected to collect this only while it genuinely has no location of its own,
   * and to stop as soon as it has one. A listener left running is a real battery cost, and that
   * - rather than throttling the updates - is how the cost is kept bounded; see
   * [WATCH_MIN_TIME_MS].
   */
  @Suppress("MissingPermission")
  fun observeFixes(context: Context): Flow<Location> = callbackFlow {
    if (!hasPermission(context) || !isLocationEnabled(context)) {
      close()
      return@callbackFlow
    }
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    if (manager == null) {
      close()
      return@callbackFlow
    }

    val listener = object : android.location.LocationListener {
      override fun onLocationChanged(location: Location) {
        trySend(location)
      }

      // Required on API 24-28, where the interface has no default implementations.
      override fun onStatusChanged(p: String?, s: Int, e: android.os.Bundle?) = Unit
      override fun onProviderEnabled(p: String) = Unit
      override fun onProviderDisabled(p: String) = Unit
    }

    // Both providers at once, unlike requestFreshFix's either-or. Whichever answers first wins,
    // and offline only the GPS one can answer anyway.
    var registered = false
    listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER).forEach { provider ->
      if (manager.isProviderEnabled(provider)) {
        try {
          manager.requestLocationUpdates(
            provider,
            WATCH_MIN_TIME_MS,
            WATCH_MIN_DISTANCE_M,
            listener,
            context.mainLooper
          )
          registered = true
        } catch (e: SecurityException) {
          Log.w(TAG, "could not watch $provider", e)
        }
      }
    }
    if (!registered) {
      close()
      return@callbackFlow
    }

    awaitClose { runCatching { manager.removeUpdates(listener) } }
  }

  /**
   * Asks for one live fix, giving up after [timeoutMs].
   *
   * Single-shot rather than continuous updates: prayer times do not change as the user walks
   * around, and a continuous listener is a battery cost with nothing to show for it.
   *
   * The GPS provider is preferred whenever it is on, even when the network provider would answer
   * sooner, because the network provider needs connectivity - and being useful without it is the
   * whole point.
   */
  @Suppress("MissingPermission")
  suspend fun requestFreshFix(
    context: Context,
    timeoutMs: Long = LIVE_FIX_TIMEOUT_MS
  ): Location? {
    if (!hasPermission(context) || !isLocationEnabled(context)) return null
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
      ?: return null

    val provider = when {
      manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
      manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
      else -> return null
    }

    return withTimeoutOrNull(timeoutMs) {
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
