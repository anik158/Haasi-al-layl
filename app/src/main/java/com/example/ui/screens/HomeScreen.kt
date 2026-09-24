package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dua.DailyInspiration
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.data.model.CityLocation
import com.example.data.model.DailyPrayerSchedule
import com.example.data.prayer.CalculationMethod
import com.example.data.prayer.IslamicMadhab
import com.example.data.weather.CurrentWeather
import com.example.ui.WeatherUiState
import com.example.ui.components.AboutHaasiDialog
import com.example.ui.components.CelestialHorizonCard
import com.example.ui.components.DailyInspirationCard
import com.example.ui.components.ForbiddenPrayerTimesBanner
import com.example.ui.components.HaasiBrandLogo
import com.example.ui.components.MadhabSelectionDialog
import androidx.compose.material.icons.filled.Settings

// ... existing imports ...
import com.example.ui.components.SettingsSheet
import com.example.ui.components.PrayerNextHeroCard
import com.example.ui.components.PrayerScheduleListCard
import com.example.ui.components.WeatherHomeScreenCard
import com.example.ui.theme.CoolMintGradientBase
import com.example.ui.theme.DarkForestInk
import com.example.ui.theme.LinenBorderLight
import com.example.ui.theme.OutlineDarkBorder
import com.example.ui.theme.WarmBeigeBackground

@Composable
fun HomeScreen(
  schedule: DailyPrayerSchedule,
  weatherState: WeatherUiState,
  currentCity: CityLocation,
  /**
   * False while [currentCity] is only the Makkah placeholder. The location chip must say so
   * rather than announcing a city the user is not in - which is exactly what it did, to a user
   * in Helsinki.
   */
  hasResolvedLocation: Boolean = true,
  formatTemperature: (Double) -> String,
  /**
   * Today's verse and narration. Passed in rather than read from a constant here, so the card
   * actually changes from one day to the next.
   */
  inspiration: DailyInspiration,
  language: AppLanguage = AppLanguage.ENGLISH,
  madhab: IslamicMadhab = IslamicMadhab.DEFAULT,
  method: CalculationMethod = CalculationMethod.UMM_AL_QURA,
  onLocationClick: () -> Unit,
  onNavigateToWeather: () -> Unit,
  onLanguageSelect: (AppLanguage) -> Unit = {},
  onResetLanguageToLocation: () -> Unit = {},
  onMadhabSelect: (IslamicMadhab) -> Unit = {},
  onMethodSelect: (CalculationMethod) -> Unit = {},
  onSettingsChanged: () -> Unit = {},
  onTogglePrayer: (String) -> Unit
) {
  var showLanguageDialog by remember { mutableStateOf(false) }
  var showMadhabDialog by remember { mutableStateOf(false) }
  var showNotificationSheet by remember { mutableStateOf(false) }
  var showAboutDialog by remember { mutableStateOf(false) }
  var showScheduleDialog by remember { mutableStateOf(false) }

  val currentWeather: CurrentWeather? = when (weatherState) {
    is WeatherUiState.Success -> weatherState.data.current
    else -> null
  }

  val tempFormatted = currentWeather?.let { formatTemperature(it.temperatureC) } ?: "--°"
  val highFormatted = currentWeather?.let { formatTemperature(it.highC) } ?: "--°"
  val lowFormatted = currentWeather?.let { formatTemperature(it.lowC) } ?: "--°"

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(WarmBeigeBackground)
      .padding(horizontal = 16.dp)
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        modifier = Modifier
          .weight(1f, fill = false)
          .clickable { showAboutDialog = true },
        verticalAlignment = Alignment.CenterVertically
      ) {
        HaasiBrandLogo(
          size = 40.dp,
          language = language,
          onClick = { showAboutDialog = true }
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          // One line, always. At a large system font scale this wrapped to two and pushed the
          // tagline out of the header entirely; an ellipsis is the lesser evil for a name that
          // no longer fits, and the font-scale ceiling in HaasiTheme makes that rare.
          Text(
            text = LocalizationManager.getAppName(language),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = DarkForestInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = LocalizationManager.getAppSubtitle(language),
            style = MaterialTheme.typography.labelSmall,
            color = DarkForestInk.copy(alpha = 0.7f),
            maxLines = 1
          )
        }
      }

      Spacer(modifier = Modifier.width(6.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
      ) {
        // Quick Location Selector Button
        Surface(
          onClick = onLocationClick,
          shape = RoundedCornerShape(20.dp),
          color = Color.White,
          border = androidx.compose.foundation.BorderStroke(1.dp, OutlineDarkBorder),
          modifier = Modifier.testTag("top_city_chip")
        ) {
          // Icon only, sized to match the settings button beside it. The text label cost ~60dp
          // of the header, which is what truncated the app name in the longer languages.
          Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.LocationOn,
              contentDescription = LocalizationManager.getChangeLocationLabel(language),
              modifier = Modifier.size(15.dp),
              tint = DarkForestInk
            )
          }
        }

        // Settings Button (Notification, Language, Madhab)
        Surface(
          onClick = { showNotificationSheet = true },
          shape = RoundedCornerShape(20.dp),
          color = if (schedule.activeForbiddenTime != null) Color(0xFFFFF3E0) else Color.White,
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (schedule.activeForbiddenTime != null) Color(0xFFFFB74D) else OutlineDarkBorder
          ),
          modifier = Modifier.testTag("top_settings_button")
        ) {
          Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (schedule.activeForbiddenTime != null) Icons.Default.NotificationsActive else Icons.Default.Settings,
              contentDescription = LocalizationManager.getSettingsButtonLabel(language),
              modifier = Modifier.size(15.dp),
              tint = if (schedule.activeForbiddenTime != null) Color(0xFFE65100) else DarkForestInk
            )
          }
        }
      }
    }

    if (showNotificationSheet) {
      com.example.ui.components.SettingsSheet(
        schedule = schedule,
        language = language,
        madhab = madhab,
        method = method,
        onLanguageSelect = onLanguageSelect,
        onMadhabSelect = onMadhabSelect,
        onMethodSelect = onMethodSelect,
        onSettingsChanged = onSettingsChanged,
        onDismiss = { showNotificationSheet = false }
      )
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("home_scroll_list"),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Hero Next Prayer & Live Countdown Card with Triple Calendar (Hijri, Bangla, Gregorian)
      item {
        PrayerNextHeroCard(
          schedule = schedule,
          hasResolvedLocation = hasResolvedLocation,
          currentWeather = currentWeather,
          temperatureFormatted = tempFormatted,
          language = language,
          onLocationClick = onLocationClick,
          onWeatherClick = onNavigateToWeather,
          onCardClick = { showScheduleDialog = true }
        )

      }

      if (schedule.isTahajjudTime) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
          ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
              Spacer(modifier = Modifier.width(16.dp))
              Text(
                text = LocalizationManager.getTahajjudBannerMessage(language),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
              )
            }
          }
        }
      }

      // 2. Forbidden Prayer Times Live Status Banner (Clickable to open Notification Center)
      item {
        ForbiddenPrayerTimesBanner(
          schedule = schedule,
          language = language,
          onClick = { showNotificationSheet = true }
        )
      }

      // 2. Celestial Horizon: Live Sun & Moon tracking with day/night arc & botanical reeds
      item {
        Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          CelestialHorizonCard(
            schedule = schedule,
            language = language
          )
        }
      }

      // 3. Weather & Daily Guidance Card
      item {
        WeatherHomeScreenCard(
          weather = currentWeather,
          temperatureFormatted = tempFormatted,
          highFormatted = highFormatted,
          lowFormatted = lowFormatted,
          language = language,
          isLoading = weatherState is WeatherUiState.Loading,
          isError = weatherState is WeatherUiState.Error,
          onViewForecastClick = onNavigateToWeather
        )
      }

      // 4. Today's 5 Prayers Schedule with Checkboxes
      item {
        PrayerScheduleListCard(
          items = schedule.items,
          language = language,
          madhab = madhab,
          onMadhabClick = { showNotificationSheet = true },
          onTogglePrayer = onTogglePrayer
        )
      }

      // 5. Daily Inspiration (Ayah & Hadith)
      item {
        DailyInspirationCard(
          inspiration = inspiration,
          language = language
        )
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // About Ḥaasi al-Layl (The Night Sipper) Meaning & Brand Logo Dialog
  if (showScheduleDialog) {
    com.example.ui.components.TodayScheduleDialog(
      schedule = schedule,
      language = language,
      onDismiss = { showScheduleDialog = false }
    )
  }

  if (showAboutDialog) {
    AboutHaasiDialog(
      language = language,
      onDismiss = { showAboutDialog = false }
    )
  }
}
