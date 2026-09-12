package com.example

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.AutoStories
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.localization.LocalizationManager
import com.example.ui.HaasiAlLaylViewModel
import com.example.ui.components.CitySelectionSheet
import com.example.ui.OnboardingState
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
  val tasbihList by viewModel.tasbihList.collectAsStateWithLifecycle()
  val selectedTasbih by viewModel.selectedTasbih.collectAsStateWithLifecycle()
  val bookmarkedDuaIds by viewModel.bookmarkedDuaIds.collectAsStateWithLifecycle()
  val allDuas by viewModel.allDuas.collectAsStateWithLifecycle()
  val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
  val selectedMadhab by viewModel.selectedMadhab.collectAsStateWithLifecycle()
  val selectedMethod by viewModel.selectedCalculationMethod.collectAsStateWithLifecycle()

  // Ask once, on first launch. The only existing request sat behind the notification toggles,
  // which default to on - so the switches rendered already-enabled and the prompt never fired,
  // leaving POST_NOTIFICATIONS denied and every notify() a silent no-op.
  val context = LocalContext.current
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { /* Either way, the flag below stops us asking again. */ }
  LaunchedEffect(Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val prefs = UserPreferences(context)
      val granted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
      // The system stops showing the dialog after two denials, so only ask once ourselves.
      if (!granted && !prefs.hasAskedNotificationPermission) {
        prefs.hasAskedNotificationPermission = true
        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }

  var showCitySheet by rememberSaveable { mutableStateOf(false) }
  val onboardingState by viewModel.onboardingState.collectAsStateWithLifecycle()

  val destinations = listOf(
    NavDestination(LocalizationManager.getNavToday(currentLanguage), Icons.Filled.Home, Icons.Outlined.Home, "nav_today"),
    NavDestination(LocalizationManager.getNavWeather(currentLanguage), Icons.Filled.WbSunny, Icons.Outlined.WbSunny, "nav_weather"),
    NavDestination(LocalizationManager.getNavQibla(currentLanguage), Icons.Filled.Explore, Icons.Outlined.Explore, "nav_qibla"),
    NavDestination(LocalizationManager.getNavTasbih(currentLanguage), Icons.Filled.TouchApp, Icons.Outlined.TouchApp, "nav_tasbih"),
    NavDestination(LocalizationManager.getNavDuas(currentLanguage), Icons.Filled.AutoStories, Icons.Outlined.AutoStories, "nav_duas")
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
          formatTemperature = { viewModel.formatTemperature(it) },
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
          currentCity = currentCity,
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
          language = currentLanguage
        )
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
        onAutoDetect = { viewModel.autoDetectLocation(); showCitySheet = false },
        language = currentLanguage,
        currentCity = currentCity,
        onCitySelected = { city -> viewModel.selectCity(city) },
        onDismiss = { showCitySheet = false }
      )
    }
  }
}
