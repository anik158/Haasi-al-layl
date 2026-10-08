package com.example.ui

import android.Manifest
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Looper
import android.os.SystemClock
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CityLocation
import com.example.data.preferences.UserPreferences
import java.time.Duration
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * The location onboarding, driven through the real ViewModel against Robolectric's
 * LocationManager: Location on/off, the switch flipping, GPS fixes, and no network.
 *
 * Every scenario here is one that reached a phone as a bug: the offline search frozen at
 * "0s / 90s", the IP guess (Madaripur, Rangpur, Natore) surviving after Location was switched on,
 * and the switch read once while it was still flipping.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LocationOnboardingTest {

  private val app: Application = ApplicationProvider.getApplicationContext()
  private val locationManager = app.getSystemService(Context.LOCATION_SERVICE) as LocationManager
  private val shadowLocation = shadowOf(locationManager)

  // Panchagarh and Faridpur, where the bugs were seen; Madaripur is the IP guess that stuck.
  private val panchagarh = 26.3411 to 88.5542
  private val faridpur = 23.6070 to 89.8429
  private val madaripur = CityLocation("Madaripur", "Bangladesh", 23.1641, 90.1897, "Asia/Dhaka")

  @Before
  fun setUp() {
    shadowOf(app).grantPermissions(
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION
    )
    // Offline: no active network, so the IP rung cannot answer and the device must.
    shadowOf(app.getSystemService(ConnectivityManager::class.java)).setActiveNetworkInfo(null)
    setLocation(enabled = true)
  }

  /** Bug: an offline install sat on "Finding where you are" at "0s / 90s". */
  @Test
  fun offlineSearchCountsUpAndSettlesOnTheGpsFix() {
    val vm = HaasiAlLaylViewModel(app)
    vm.onLocationPermissionResult(true)
    assertEquals(LocatingPhase.SEARCHING, vm.locatingPhase.value)

    advance(seconds = 3)
    val before = vm.fixElapsedSeconds.value
    assertTrue("counter should run, was $before", before >= 3)

    // Some phones broadcast PROVIDERS_CHANGED just because the GPS engine started. That used to
    // restart detection in a loop and freeze the counter at 0.
    repeat(5) { vm.onLocationProvidersChanged() }
    advance(seconds = 2)
    assertTrue(
      "counter must keep counting through PROVIDERS_CHANGED, was ${vm.fixElapsedSeconds.value}",
      vm.fixElapsedSeconds.value >= before + 2
    )

    awaitSettled(vm, panchagarh)
    assertTrue(vm.hasResolvedLocation.value)
    assertEquals(OnboardingState.NOT_NEEDED, vm.onboardingState.value)
    assertTrue(UserPreferences(app).locationFromDevice)
  }

  /** Installed with Location off: switching it on must start the search, not leave it waiting. */
  @Test
  fun switchingLocationOnStartsTheSearch() {
    setLocation(enabled = false)
    val vm = HaasiAlLaylViewModel(app)
    vm.onLocationPermissionResult(true)
    advance(seconds = 1)
    assertEquals(LocatingPhase.LOCATION_SERVICES_OFF, vm.locatingPhase.value)

    setLocation(enabled = true)
    vm.onLocationProvidersChanged()
    advance(seconds = 1)

    awaitSettled(vm, faridpur)
    assertTrue(UserPreferences(app).locationFromDevice)
  }

  /** Bug: Faridpur stayed "≈ Madaripur · approximate" after Location was turned on. */
  @Test
  fun anIpEstimateIsReplacedByTheDeviceFix() {
    UserPreferences(app).apply {
      selectedCity = madaripur
      locationFromDevice = false
      hasAskedLocationPermission = true
      isOnboardingComplete = true
    }
    val vm = HaasiAlLaylViewModel(app)
    assertTrue(vm.isLocationApproximate.value)

    vm.onAppResumed()
    awaitSettled(vm, faridpur)
    assertFalse(vm.isLocationApproximate.value)
    assertTrue(UserPreferences(app).locationFromDevice)
  }

  /** The switch flips a moment after Google's dialog returns; one read used to lose that race. */
  @Test
  fun returningFromTheSettingsDialogWaitsForTheSwitch() {
    setLocation(enabled = false)
    val vm = HaasiAlLaylViewModel(app)
    vm.onLocationPermissionResult(true)
    advance(seconds = 1)

    vm.onReturnedFromLocationSettings()   // still off at this instant
    advance(millis = 700)
    setLocation(enabled = true)           // ...and on a moment later
    advance(seconds = 3)

    awaitSettled(vm, panchagarh)
  }

  /** Once on device coordinates, further PROVIDERS_CHANGED broadcasts change nothing. */
  @Test
  fun aDeviceFixIsNotDisturbedByLaterBroadcasts() {
    val vm = HaasiAlLaylViewModel(app)
    vm.onLocationPermissionResult(true)
    awaitSettled(vm, faridpur)
    val settled = vm.selectedCity.value

    setLocation(enabled = false)
    vm.onLocationProvidersChanged()
    setLocation(enabled = true)
    vm.onLocationProvidersChanged()
    advance(seconds = 2)

    assertEquals(settled, vm.selectedCity.value)
    assertEquals(OnboardingState.NOT_NEEDED, vm.onboardingState.value)
  }

  // --- helpers ---------------------------------------------------------------------------------

  private fun setLocation(enabled: Boolean) {
    shadowLocation.setLocationEnabled(enabled)
    shadowLocation.setProviderEnabled(LocationManager.GPS_PROVIDER, enabled)
    shadowLocation.setProviderEnabled(LocationManager.NETWORK_PROVIDER, enabled)
  }

  private fun advance(seconds: Long = 0, millis: Long = 0) {
    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(seconds).plusMillis(millis))
  }

  /**
   * Feeds GPS fixes at [where] until the ViewModel adopts them, or fails after 30 s. Detection
   * registers its listener from a background thread, so fixes are repeated until one is heard;
   * naming the place may reach the network, which the assertions do not depend on.
   */
  private fun awaitSettled(vm: HaasiAlLaylViewModel, where: Pair<Double, Double>) {
    val deadline = System.currentTimeMillis() + 30_000
    while (true) {
      val city = vm.selectedCity.value
      if (abs(city.latitude - where.first) < 0.01 && abs(city.longitude - where.second) < 0.01) return
      if (System.currentTimeMillis() > deadline) {
        fail("never settled on $where; at ${city.latitude},${city.longitude}, phase ${vm.locatingPhase.value}")
      }
      shadowLocation.simulateLocation(fix(where))
      shadowOf(Looper.getMainLooper()).idle()
      Thread.sleep(50)
    }
  }

  private fun fix(where: Pair<Double, Double>) = Location(LocationManager.GPS_PROVIDER).apply {
    latitude = where.first
    longitude = where.second
    accuracy = 12f
    time = System.currentTimeMillis()
    elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
  }
}
