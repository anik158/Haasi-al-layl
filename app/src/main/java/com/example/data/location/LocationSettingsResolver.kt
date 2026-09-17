package com.example.data.location

import android.app.PendingIntent
import android.content.Context
import android.util.Log
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * The one-tap way to switch positioning on, instead of sending the user into Settings to find it.
 *
 * This exists because "turn on Location" is not one switch on every device. On ColorOS the master
 * Location toggle can be **on** while the GPS provider itself is off - the phone is positioning
 * from Wi-Fi and cell towers only - and with no network that combination produces no fix at all
 * while looking, from the user's side, exactly like location being enabled. Telling them to "turn
 * on location" when it is already on is useless; what they actually have to change is buried
 * under a *Scan* or positioning-mode submenu that differs per manufacturer.
 *
 * `SettingsClient.checkLocationSettings` solves precisely that: ask for high accuracy, and if the
 * device is not configured for it Play Services hands back a [PendingIntent] which shows the
 * familiar system dialog that turns everything needed on - GPS included - when the user taps OK.
 * No settings navigation, no per-OEM instructions.
 *
 * **This is the only thing Play Services is used for here.** Fixes still come from the platform
 * `LocationManager` via [DeviceLocationProvider], because the fused provider answers from Wi-Fi
 * and cell towers, needs connectivity, and would defeat the point of an offline prayer-time app.
 * Play Services is also simply absent on some devices, which is why every caller has to handle a
 * null result by falling back to the settings screen.
 */
object LocationSettingsResolver {

  private const val TAG = "LocationSettings"

  /**
   * A dialog to fix the device's positioning settings, or null if there is nothing to fix -
   * or nothing that can offer the dialog.
   *
   * Null is deliberately ambiguous between "already fine" and "Play Services unavailable": the
   * caller's response to both is the same, which is to stop offering a one-tap fix and point at
   * the settings screen instead.
   */
  suspend fun resolutionForHighAccuracy(context: Context): PendingIntent? =
    suspendCancellableCoroutine { continuation ->
      val request = LocationSettingsRequest.Builder()
        .addLocationRequest(
          // The interval is irrelevant - nothing will ever collect from this request. It exists
          // only to describe the accuracy being asked for, which is what the dialog reacts to.
          LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()
        )
        // Show the dialog even when the user has dismissed it before. They are looking at a
        // screen that says positioning is off, so suppressing it here would be a dead end.
        .setAlwaysShow(true)
        .build()

      try {
        LocationServices.getSettingsClient(context)
          .checkLocationSettings(request)
          .addOnSuccessListener {
            // Already satisfied; there is nothing to resolve.
            if (continuation.isActive) continuation.resume(null)
          }
          .addOnFailureListener { error ->
            val resolution = (error as? ResolvableApiException)?.resolution
            if (resolution == null) {
              Log.i(TAG, "location settings not resolvable through Play Services", error)
            }
            if (continuation.isActive) continuation.resume(resolution)
          }
      } catch (e: Exception) {
        // Play Services missing or disabled. Not an error worth surfacing - the caller has a
        // settings-screen fallback for exactly this.
        Log.i(TAG, "Play Services unavailable for a settings resolution", e)
        if (continuation.isActive) continuation.resume(null)
      }
    }
}
