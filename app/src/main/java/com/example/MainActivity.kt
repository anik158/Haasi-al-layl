package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MuslimDayViewModel
import com.example.ui.components.CitySelectionSheet
import com.example.ui.screens.DuaScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.TasbihScreen
import com.example.ui.screens.WeatherScreen
import com.example.ui.theme.HaasiTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
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
fun MainAppContent(viewModel: MuslimDayViewModel = viewModel()) {
  val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
  val prayerSchedule by viewModel.prayerSchedule.collectAsStateWithLifecycle()
  val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()
  val currentCity by viewModel.selectedCity.collectAsStateWithLifecycle()
  val isFahrenheit by viewModel.isFahrenheit.collectAsStateWithLifecycle()
  val compassHeading by viewModel.compassHeading.collectAsStateWithLifecycle()
  val qiblaBearing by viewModel.qiblaBearing.collectAsStateWithLifecycle()
  val distanceToKaaba by viewModel.distanceToKaabaKm.collectAsStateWithLifecycle()
  val isFacingQibla by viewModel.isFacingQibla.collectAsStateWithLifecycle()
  val tasbihList by viewModel.tasbihList.collectAsStateWithLifecycle()
  val selectedTasbih by viewModel.selectedTasbih.collectAsStateWithLifecycle()
  val bookmarkedDuaIds by viewModel.bookmarkedDuaIds.collectAsStateWithLifecycle()
  val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()

  var showCitySheet by remember { mutableStateOf(false) }

  val destinations = listOf(
    NavDestination("Today", Icons.Filled.Home, Icons.Outlined.Home, "nav_today"),
    NavDestination("Weather", Icons.Filled.WbSunny, Icons.Outlined.WbSunny, "nav_weather"),
    NavDestination("Qibla", Icons.Filled.Explore, Icons.Outlined.Explore, "nav_qibla"),
    NavDestination("Tasbih", Icons.Filled.TouchApp, Icons.Outlined.TouchApp, "nav_tasbih"),
    NavDestination("Duas", Icons.Filled.AutoStories, Icons.Outlined.AutoStories, "nav_duas")
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
          onLocationClick = { showCitySheet = true },
          onNavigateToWeather = { viewModel.setTab(1) },
          onLanguageSelect = { lang -> viewModel.setLanguage(lang) },
          onResetLanguageToLocation = { viewModel.resetLanguageToLocation() },
          onTogglePrayer = { prayerKey -> viewModel.togglePrayerCheck(prayerKey) }
        )

        1 -> WeatherScreen(
          weatherState = weatherState,
          currentCity = currentCity,
          isFahrenheit = isFahrenheit,
          formatTemperature = { viewModel.formatTemperature(it) },
          onRefresh = { viewModel.refreshWeather() },
          onLocationClick = { showCitySheet = true },
          onToggleUnit = { viewModel.toggleTemperatureUnit() }
        )

        2 -> QiblaScreen(
          currentCity = currentCity,
          compassHeading = compassHeading,
          qiblaBearing = qiblaBearing,
          distanceToKaabaKm = distanceToKaaba,
          isFacingQibla = isFacingQibla,
          onLocationClick = { showCitySheet = true }
        )

        3 -> TasbihScreen(
          tasbihList = tasbihList,
          selectedTasbih = selectedTasbih,
          onSelectTasbih = { id -> viewModel.selectTasbih(id) },
          onIncrement = { viewModel.incrementTasbih() },
          onReset = { viewModel.resetTasbih() }
        )

        4 -> DuaScreen(
          bookmarkedDuaIds = bookmarkedDuaIds,
          onToggleBookmark = { id -> viewModel.toggleDuaBookmark(id) }
        )
      }
    }

    if (showCitySheet) {
      CitySelectionSheet(
        currentCity = currentCity,
        onCitySelected = { city -> viewModel.selectCity(city) },
        onDismiss = { showCitySheet = false }
      )
    }
  }
}
