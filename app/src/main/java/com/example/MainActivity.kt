package com.example

import android.os.Bundle
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.data.location.DeviceLocationProvider
import com.example.data.preferences.UserPreferences
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.localization.LocalizationManager
import com.example.ui.HaasiAlLaylViewModel
import com.example.ui.components.CitySelectionSheet
import com.example.ui.LocatingPhase
import com.example.ui.OnboardingState
import com.example.ui.PositioningRequest
import com.example.ui.components.SatelliteAcquisitionScreen
import com.example.ui.components.WelcomeDialog
import com.example.ui.screens.DuaScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.TasbihScreen
import com.example.ui.screens.WeatherScreen
import com.example.ui.theme.HaasiTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    // Must precede super.onCreate: it swaps Theme.Haasi.Splash for Theme.Haasi.
    installSplashScreen()
    super.onCreate(savedInstanceState)
    // The no-arg overload picks system-bar icon colour from the system dark-mode flag, which put
    // white icons on the parchment background. The app is light-only, so say so explicitly.
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    )
    setContent {
      HaasiTheme {
        MainAppContent()
      }
    }
  }
}

private data class NavDestination(
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String
)

@Composable
fun MainAppContent(viewModel: HaasiAlLaylViewModel = viewModel()) {
  // Re-checks the location each time the app comes forward, not only on a cold start.
  // Re-checks the location each time the app comes forward; stops the GPS watch when it leaves.
  LifecycleResumeEffect(Unit) {
    viewModel.onAppResumed()
    onPauseOrDispose { viewModel.onAppPaused() }
  }
  val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
  val prayerSchedule by viewModel.prayerSchedule.collectAsStateWithLifecycle()
  val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()
  val currentCity by viewModel.selectedCity.collectAsStateWithLifecycle()
  val isFahrenheit by viewModel.isFahrenheit.collectAsStateWithLifecycle()
  val compassHeading by viewModel.compassHeading.collectAsStateWithLifecycle()
  val qiblaBearing by viewModel.qiblaBearing.collectAsStateWithLifecycle()
  val distanceToKaaba by viewModel.distanceToKaabaKm.collectAsStateWithLifecycle()
  val isFacingQibla by viewModel.isFacingQibla.collectAsStateWithLifecycle()
  val magneticDeclination by viewModel.magneticDeclination.collectAsStateWithLifecycle()
  val needsCompassCalibration by viewModel.needsCompassCalibration.collectAsStateWithLifecycle()
  val placeResults by viewModel.placeResults.collectAsStateWithLifecycle()
  val isSearchingPlaces by viewModel.isSearchingPlaces.collectAsStateWithLifecycle()
  val isDetectingLocation by viewModel.isDetectingLocation.collectAsStateWithLifecycle()
  val isLocationApproximate by viewModel.isLocationApproximate.collectAsStateWithLifecycle()
  val tasbihList by viewModel.tasbihList.collectAsStateWithLifecycle()
  val selectedTasbih by viewModel.selectedTasbih.collectAsStateWithLifecycle()
  val bookmarkedDuaIds by viewModel.bookmarkedDuaIds.collectAsStateWithLifecycle()
  val allDuas by viewModel.allDuas.collectAsStateWithLifecycle()
  val hisnChapters by viewModel.hisnChapters.collectAsStateWithLifecycle()
  val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
  val selectedMadhab by viewModel.selectedMadhab.collectAsStateWithLifecycle()
  val selectedMethod by viewModel.selectedCalculationMethod.collectAsStateWithLifecycle()
  val dailyInspiration by viewModel.dailyInspiration.collectAsStateWithLifecycle()
  val qiblaOrigin by viewModel.qiblaOrigin.collectAsStateWithLifecycle()

  val context = LocalContext.current
  var showCitySheet by rememberSaveable { mutableStateOf(false) }
  val onboardingState by viewModel.onboardingState.collectAsStateWithLifecycle()
  val fixElapsedSeconds by viewModel.fixElapsedSeconds.collectAsStateWithLifecycle()
  val locatingPhase by viewModel.locatingPhase.collectAsStateWithLifecycle()
  val hasResolvedLocation by viewModel.hasResolvedLocation.collectAsStateWithLifecycle()
  val qiblaUsesDeviceLocation by viewModel.qiblaUsesDeviceLocation.collectAsStateWithLifecycle()

  // Two permissions, asked one at a time and in this order. Location comes first because it
  // gates the app's whole reason to exist - exact prayer times, offline - and because two
  // system dialogs raced into the same frame means one of them is dropped.
  val firstRunLocationLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { granted ->
    viewModel.onLocationPermissionResult(granted.values.any { it })
  }

  // Asked once on the first launch, and deliberately **not** tied to the blocking ladder any
  // more. Two reasons. Deferring it to the "detect" button was the original bug - the cached-fix
  // read returns null before it consults the receiver without this permission, so an offline
  // first run never tried GPS at all. But gating it on the ladder was the opposite mistake: when
  // there is a network the app resolves by IP in under a second and shows no ladder, and it still
  // needs this permission for the Qibla compass and for the background fix that upgrades the IP
  // estimate to real coordinates.
  var hasLaunchedLocationRequest by rememberSaveable { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    if (hasLaunchedLocationRequest) return@LaunchedEffect
    val prefs = UserPreferences(context)
    if (DeviceLocationProvider.hasPermission(context) || prefs.hasAskedLocationPermission) {
      return@LaunchedEffect
    }
    hasLaunchedLocationRequest = true
    firstRunLocationLauncher.launch(
      arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
      )
    )
  }

  // The notification prompt waits until the location question has settled, so it does not land
  // on top of the acquisition screen or the welcome dialog. The only earlier request sat behind
  // the notification toggles, which default to on - so the switches rendered already-enabled,
  // the off-to-on transition never happened, and every notify() was a silent no-op.
  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { /* Either way, the flag below stops us asking again. */ }
  LaunchedEffect(onboardingState) {
    if (onboardingState != OnboardingState.NOT_NEEDED) return@LaunchedEffect
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val prefs = UserPreferences(context)
      val granted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
      // The system stops showing the dialog after two denials, so only ask once ourselves.
      if (!granted && !prefs.hasAskedNotificationPermission) {
        prefs.hasAskedNotificationPermission = true
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }

  // Getting the user to a positioning switch: exactly one of the dialog or the settings screen,
  // never both. An IntentSender can only be launched from an Activity, so the ViewModel decides
  // which route is available and this carries it out.
  val positioningRequest by viewModel.positioningRequest.collectAsStateWithLifecycle()
  val positioningDialogLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartIntentSenderForResult()
  ) {
    // The result code is not trusted: the user may have changed the switches by hand inside the
    // dialog, or backed out of it. Re-read them instead.
    viewModel.onReturnedFromLocationSettings()
  }
  val openLocationSettings = {
    runCatching {
      context.startActivity(
        Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
          .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      )
    }
    Unit
  }
  LaunchedEffect(positioningRequest) {
    when (val request = positioningRequest) {
      null -> Unit
      is PositioningRequest.SettingsScreen -> {
        viewModel.clearPositioningRequest()
        openLocationSettings()
      }
      is PositioningRequest.Dialog -> {
        viewModel.clearPositioningRequest()
        runCatching {
          positioningDialogLauncher.launch(
            IntentSenderRequest.Builder(request.pendingIntent.intentSender).build()
          )
        }.onFailure {
          // Play Services handed back something unlaunchable. The settings screen still works.
          openLocationSettings()
        }
      }
    }
  }

  // The "detect" button in the city picker, which means something different from the first-run
  // request: on a grant it starts a detection rather than answering the onboarding handshake.
  val detectLocationLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { granted ->
    if (granted.values.any { it }) viewModel.autoDetectLocation()
  }

  // Held in front of everything while the fix is in flight, on the same night navy as the launch
  // window, so the app does not show a half-populated Makkah home screen it is about to replace.
  // RESOLVING is included deliberately: on a fresh install with location switched off it covers
  // the IP lookup as well, rather than flashing a placeholder home screen for five seconds
  // first. A user who already has a location starts at NOT_NEEDED and never sees this.
  if (onboardingState == OnboardingState.RESOLVING ||
    onboardingState == OnboardingState.LOCATING
  ) {
    SatelliteAcquisitionScreen(
      // RESOLVING shows the animation alone. An online first run passes through it in under a
      // second, and a flash of "turn on location" for someone the network can already place is
      // exactly the noise this avoids.
      phase = if (onboardingState == OnboardingState.RESOLVING) {
        LocatingPhase.STARTING
      } else {
        locatingPhase
      },
      elapsedSeconds = fixElapsedSeconds,
      timeoutSeconds = viewModel.fixTimeoutSeconds,
      language = currentLanguage,
      onEnterManually = { viewModel.skipToManualEntry() },
      onRetry = { viewModel.startDetection() },
      // One route or the other, decided in the ViewModel and carried out above. This used to
      // fire the dialog request *and* open the settings screen unconditionally, so a device that
      // could offer the dialog got the settings screen with the dialog thrown on top of it.
      onEnablePositioning = { viewModel.requestPositioningFix() },
      onOpenAppSettings = {
        runCatching {
          context.startActivity(
            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
              .setData(android.net.Uri.fromParts("package", context.packageName, null))
              .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          )
        }
      }
    )
    return
  }

  // A bearing is a property of where the needle is, so the compass wants a fix taken now - not
  // one from an hour and a bus ride ago.
  LaunchedEffect(selectedTab) {
    if (selectedTab == 2) viewModel.refreshDeviceLocationForQibla()
  }

  val destinations = listOf(
    NavDestination(LocalizationManager.getNavToday(currentLanguage), Icons.Filled.Home, Icons.Outlined.Home, "nav_today"),
    NavDestination(LocalizationManager.getNavWeather(currentLanguage), Icons.Filled.WbSunny, Icons.Outlined.WbSunny, "nav_weather"),
    NavDestination(LocalizationManager.getNavQibla(currentLanguage), Icons.Filled.Explore, Icons.Outlined.Explore, "nav_qibla"),
    NavDestination(LocalizationManager.getNavTasbih(currentLanguage), Icons.Filled.TouchApp, Icons.Outlined.TouchApp, "nav_tasbih"),
    NavDestination(LocalizationManager.getNavDuas(currentLanguage), Icons.Filled.AutoStories, Icons.Outlined.AutoStories, "nav_duas"),
    // Last, so the hardcoded indices above (Weather 1, Qibla 2) do not shift. The label is the
    // same in every language: the name of Allah and the number.
    NavDestination(
      // ﷲ then the number in the app's digits. The left-to-right mark keeps the order: next to
      // the Arabic ligature, Western digits otherwise rendered as "99ﷲ".
      "\uFDF2\u200E" + LocalizationManager.localizeDigits("99", currentLanguage),
      Icons.Filled.AutoAwesome,
      Icons.Outlined.AutoAwesome,
      "nav_asma"
    )
  )

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets.safeDrawing,
    bottomBar = {
      NavigationBar(
        windowInsets = WindowInsets.navigationBars,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("main_bottom_nav")
      ) {
        destinations.forEachIndexed { index, destination ->
          val isSelected = selectedTab == index
          NavigationBarItem(
            selected = isSelected,
            onClick = { viewModel.setTab(index) },
            icon = {
              Icon(
                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                contentDescription = destination.label
              )
            },
            label = { Text(destination.label) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
              indicatorColor = MaterialTheme.colorScheme.primaryContainer,
              selectedTextColor = MaterialTheme.colorScheme.primary,
              unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
              unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag(destination.testTag)
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (selectedTab) {
        0 -> HomeScreen(
          schedule = prayerSchedule,
          weatherState = weatherState,
          currentCity = currentCity,
          hasResolvedLocation = hasResolvedLocation,
          isLocationApproximate = isLocationApproximate,
          formatTemperature = { viewModel.formatTemperature(it) },
          inspiration = dailyInspiration,
          language = currentLanguage,
          madhab = selectedMadhab,
          method = selectedMethod,
          onLocationClick = { showCitySheet = true },
          onNavigateToWeather = { viewModel.setTab(1) },
          onLanguageSelect = { lang -> viewModel.setLanguage(lang) },
          onResetLanguageToLocation = { viewModel.resetLanguageToLocation() },
          onMadhabSelect = { madhab -> viewModel.setMadhab(madhab) },
          onMethodSelect = { method -> viewModel.setCalculationMethod(method) },
          onSettingsChanged = { viewModel.rescheduleAlarms() },
          onTogglePrayer = { prayerKey -> viewModel.togglePrayerCheck(prayerKey) }
        )

        1 -> WeatherScreen(
          weatherState = weatherState,
          currentCity = currentCity,
          isFahrenheit = isFahrenheit,
          formatTemperature = { viewModel.formatTemperature(it) },
          onRefresh = { viewModel.refreshWeather() },
          onLocationClick = { showCitySheet = true },
          onToggleUnit = { viewModel.toggleTemperatureUnit() },
          language = currentLanguage
        )

        2 -> QiblaScreen(
          currentCity = qiblaOrigin,
          usesDeviceLocation = qiblaUsesDeviceLocation,
          compassHeading = compassHeading,
          qiblaBearing = qiblaBearing,
          distanceToKaabaKm = distanceToKaaba,
          isFacingQibla = isFacingQibla,
          magneticDeclination = magneticDeclination,
          needsCalibration = needsCompassCalibration,
          onLocationClick = { showCitySheet = true },
          language = currentLanguage
        )

        3 -> TasbihScreen(
          tasbihList = tasbihList,
          selectedTasbih = selectedTasbih,
          onSelectTasbih = { id -> viewModel.selectTasbih(id) },
          onIncrement = { viewModel.incrementTasbih() },
          onReset = { viewModel.resetTasbih() },
          language = currentLanguage
        )

        4 -> DuaScreen(
          allDuas = allDuas,
          bookmarkedDuaIds = bookmarkedDuaIds,
          onToggleBookmark = { id -> viewModel.toggleDuaBookmark(id) },
          language = currentLanguage,
          hisnChapters = hisnChapters
        )
        5 -> com.example.ui.screens.AsmaUlHusnaScreen(language = currentLanguage)
      }
    }

    
      // Only surfaces when the app could not work out language and location on its own,
      // and only until the user has been through it once.
      if (onboardingState == OnboardingState.REQUIRED) {
        WelcomeDialog(
          language = currentLanguage,
          onLanguageSelect = { viewModel.setLanguage(it) },
          onOpenCitySelection = { showCitySheet = true },
          onDismiss = { viewModel.markOnboardingComplete() }
        )
      }
      
      if (showCitySheet) {
      CitySelectionSheet(
        onAutoDetect = {
          if (DeviceLocationProvider.hasPermission(context)) {
            viewModel.autoDetectLocation()
          } else {
            detectLocationLauncher.launch(
              arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
              )
            )
          }
        },
        onSearch = { query -> viewModel.searchPlaces(query) },
        searchResults = placeResults,
        isSearching = isSearchingPlaces,
        isDetecting = isDetectingLocation,
        detectingSeconds = fixElapsedSeconds,
        language = currentLanguage,
        currentCity = currentCity,
        onCitySelected = { city -> viewModel.selectCity(city) },
        onDismiss = { showCitySheet = false }
      )
    }
  }
}
