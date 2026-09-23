package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * A live readout of what the satellite receiver can actually hear.
 *
 * Until this existed, "no signal" was the only thing the app could ever say about a failed fix,
 * and it covered three completely different situations: a receiver that is switched off, a
 * receiver that can hear nothing at all, and a receiver that can hear five satellites perfectly
 * well but has not yet finished reading their orbit data. On a device those are indistinguishable
 * from each other and from the app being broken - which is exactly how the first offline failures
 * were reported.
 *
 * `GnssStatus` answers it directly: how many satellites are in view, how many are actually being
 * used to compute a position, and how strong each one is. A user standing indoors sees a handful
 * of weak signals and none used; the same user by a window watches the count climb and the fix
 * arrive. That is a diagnosis anyone can act on.
 *
 * Nothing here influences the app's behaviour - it is purely observational, which is why it is
 * safe to run only while its screen is open.
 */
object GnssDiagnostics {

  private const val TAG = "GnssDiagnostics"

  /**
   * Carrier-to-noise density above which a satellite is worth counting as a usable signal.
   *
   * C/N0 is reported in dB-Hz. Below roughly 20 the satellite is detected but too weak to
   * contribute; 25-35 is an ordinary indoor-by-a-window signal; above 40 is open sky.
   */
  const val USABLE_CN0_DB_HZ = 20f

  /** One frame of receiver state. */
  data class Snapshot(
    /** Satellites the receiver can detect at all. */
    val visible: Int,
    /** Satellites actually contributing to a position fix. Three or more means a fix is close. */
    val usedInFix: Int,
    /** Of the visible ones, how many are strong enough to be useful. */
    val usable: Int,
    /** The strongest signal currently heard, in dB-Hz; null when nothing is heard. */
    val bestCn0DbHz: Float?,
    /** Signal strengths, strongest first, for the bar display. */
    val signals: List<Float>
  ) {
    companion object {
      val EMPTY = Snapshot(0, 0, 0, null, emptyList())
    }
  }

  /**
   * Emits a [Snapshot] each time the receiver reports its status, roughly once a second while
   * the GPS engine is running.
   *
   * Closes immediately when there is no permission or no receiver, rather than emitting a
   * misleading stream of zeroes - the caller shows the permission state separately.
   */
  @SuppressLint("MissingPermission")
  @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
  fun observe(context: Context): Flow<Snapshot> = callbackFlow {
    if (!DeviceLocationProvider.hasPermission(context)) {
      close()
      return@callbackFlow
    }
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    if (manager == null) {
      close()
      return@callbackFlow
    }

    val callback = object : GnssStatus.Callback() {
      override fun onSatelliteStatusChanged(status: GnssStatus) {
        trySend(status.toSnapshot())
      }
    }

    // The status callback only reports while something is actually driving the GPS engine, so
    // this screen has to drive it itself - otherwise it would sit at zero satellites next to a
    // receiver that is perfectly capable, which is precisely the confusion it exists to end.
    val engineListener = object : android.location.LocationListener {
      override fun onLocationChanged(location: android.location.Location) = Unit
      override fun onStatusChanged(p: String?, s: Int, e: android.os.Bundle?) = Unit
      override fun onProviderEnabled(p: String) = Unit
      override fun onProviderDisabled(p: String) = Unit
    }

    val handler = Handler(Looper.getMainLooper())
    val registered = try {
      manager.registerGnssStatusCallback(callback, handler)
    } catch (e: SecurityException) {
      Log.w(TAG, "GNSS status denied", e)
      false
    }
    if (!registered) {
      close()
      return@callbackFlow
    }

    val driving = runCatching {
      if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        manager.requestLocationUpdates(
          LocationManager.GPS_PROVIDER, 0L, 0f, engineListener, Looper.getMainLooper()
        )
        true
      } else {
        false
      }
    }.getOrDefault(false)

    awaitClose {
      runCatching { manager.unregisterGnssStatusCallback(callback) }
      if (driving) runCatching { manager.removeUpdates(engineListener) }
    }
  }

  private fun GnssStatus.toSnapshot(): Snapshot {
    var used = 0
    var usable = 0
    val strengths = ArrayList<Float>(satelliteCount)
    for (i in 0 until satelliteCount) {
      val cn0 = runCatching { getCn0DbHz(i) }.getOrDefault(0f)
      strengths.add(cn0)
      if (cn0 >= USABLE_CN0_DB_HZ) usable++
      if (runCatching { usedInFix(i) }.getOrDefault(false)) used++
    }
    strengths.sortDescending()
    return Snapshot(
      visible = satelliteCount,
      usedInFix = used,
      usable = usable,
      bestCn0DbHz = strengths.firstOrNull()?.takeIf { it > 0f },
      signals = strengths
    )
  }
}
