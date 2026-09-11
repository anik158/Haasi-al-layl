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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dua.IslamicContentProvider
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.data.model.CityLocation
import com.example.data.model.DailyPrayerSchedule
import com.example.data.prayer.IslamicMadhab
import com.example.data.weather.CurrentWeather
import com.example.ui.WeatherUiState
import com.example.ui.components.AboutHaasiDialog
import com.example.ui.components.CelestialHorizonCard
import com.example.ui.components.DailyInspirationCard
import com.example.ui.components.ForbiddenPrayerTimesBanner
import com.example.ui.components.HaasiBrandLogo
import com.example.ui.components.MadhabSelectionDialog
import com.example.ui.components.NotificationSettingsSheet
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
  formatTemperature: (Double) -> String,
  language: AppLanguage = AppLanguage.ENGLISH,
  madhab: IslamicMadhab = IslamicMadhab.SHAFI,
  onLocationClick: () -> Unit,
  onNavigateToWeather: () -> Unit,
  onLanguageSelect: (AppLanguage) -> Unit = {},
  onResetLanguageToLocation: () -> Unit = {},
  onMadhabSelect: (IslamicMadhab) -> Unit = {},
  onTogglePrayer: (String) -> Unit
) {
  var showLanguageDialog by remember { mutableStateOf(false) }
  var showMadhabDialog by remember { mutableStateOf(false) }
  var showNotificationSheet by remember { mutableStateOf(false) }
  var showAboutDialog by remember { mutableStateOf(false) }

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
          onClick = { showAboutDialog = true }
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = LocalizationManager.getAppName(language),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = DarkForestInk
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
        // Notification & Forbidden Times Bell Button
        Surface(
          onClick = { showNotificationSheet = true },
          shape = RoundedCornerShape(20.dp),
          color = if (schedule.activeForbiddenTime != null) Color(0xFFFFF3E0) else Color.White,
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (schedule.activeForbiddenTime != null) Color(0xFFFFB74D) else OutlineDarkBorder
          ),
          modifier = Modifier.testTag("top_notifications_button")
        ) {
          Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (schedule.activeForbiddenTime != null) Icons.Default.NotificationsActive else Icons.Default.Notifications,
              contentDescription = "Notifications & Forbidden Times",
              modifier = Modifier.size(15.dp),
              tint = if (schedule.activeForbiddenTime != null) Color(0xFFE65100) else DarkForestInk
            )
          }
        }

        // Quick Language Switcher Button
        Surface(
          onClick = { showLanguageDialog = true },
          shape = RoundedCornerShape(20.dp),
          color = Color.White,
          border = androidx.compose.foundation.BorderStroke(1.dp, OutlineDarkBorder),
          modifier = Modifier.testTag("top_language_chip")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              Icons.Default.Language,
              contentDescription = "Language",
              modifier = Modifier.size(13.dp),
              tint = DarkForestInk
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = language.code.uppercase(),
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = DarkForestInk
            )
          }
        }

        // Quick Madhab Switcher Button (Default: Shafi'i)
        Surface(
          onClick = { showMadhabDialog = true },
          shape = RoundedCornerShape(20.dp),
          color = Color.White,
          border = androidx.compose.foundation.BorderStroke(1.dp, OutlineDarkBorder),
          modifier = Modifier.testTag("top_madhab_chip")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "⚖ ${madhab.getLocalizedName(language).split(" ").last()}",
              fontWeight = FontWeight.SemiBold,
              fontSize = 11.sp,
              color = DarkForestInk
            )
          }
        }

        // Quick Location Selector Button
        Surface(
          onClick = onLocationClick,
          shape = RoundedCornerShape(20.dp),
          color = Color.White,
          border = androidx.compose.foundation.BorderStroke(1.dp, OutlineDarkBorder),
          modifier = Modifier.testTag("top_city_chip")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              Icons.Default.LocationOn,
              contentDescription = null,
              modifier = Modifier.size(13.dp),
              tint = DarkForestInk
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = currentCity.name,
              fontWeight = FontWeight.SemiBold,
              fontSize = 11.sp,
              color = DarkForestInk
            )
          }
        }
      }
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
          currentWeather = currentWeather,
          temperatureFormatted = tempFormatted,
          language = language,
          onLocationClick = onLocationClick,
          onWeatherClick = onNavigateToWeather
        )
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
          onViewForecastClick = onNavigateToWeather
        )
      }

      // 4. Today's 5 Prayers Schedule with Checkboxes
      item {
        PrayerScheduleListCard(
          items = schedule.items,
          language = language,
          madhab = madhab,
          onMadhabClick = { showMadhabDialog = true },
          onTogglePrayer = onTogglePrayer
        )
      }

      // 5. Daily Inspiration (Ayah & Hadith)
      item {
        DailyInspirationCard(
          inspiration = IslamicContentProvider.DAILY_INSPIRATION,
          language = language
        )
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // Language Selection Dialog
  if (showLanguageDialog) {
    AlertDialog(
      onDismissRequest = { showLanguageDialog = false },
      containerColor = Color.White,
      title = {
        Text(
          text = "Choose Language / ভাষা নির্বাচন",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = DarkForestInk
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          // Auto Location option
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = CoolMintGradientBase.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight),
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                onResetLanguageToLocation()
                showLanguageDialog = false
              }
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "🌐 Auto (From City Location)",
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.bodyMedium,
                  color = DarkForestInk
                )
                Text(
                  text = "Detects based on ${currentCity.name}, ${currentCity.country}",
                  style = MaterialTheme.typography.labelSmall,
                  color = DarkForestInk.copy(alpha = 0.7f)
                )
              }
            }
          }

          AppLanguage.entries.forEach { appLang ->
            val isSelected = language == appLang
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) CoolMintGradientBase else Color(0xFFFAF7EE),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSelected) OutlineDarkBorder else LinenBorderLight
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  onLanguageSelect(appLang)
                  showLanguageDialog = false
                }
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "${appLang.flag} ${appLang.nativeName}",
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkForestInk
                  )
                  Text(
                    text = appLang.englishName,
                    style = MaterialTheme.typography.labelSmall,
                    color = DarkForestInk.copy(alpha = 0.7f)
                  )
                }
                if (isSelected) {
                  Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = DarkForestInk,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showLanguageDialog = false }) {
          Text("Close", color = DarkForestInk, fontWeight = FontWeight.Bold)
        }
      }
    )
  }

  // Madhab Selection Dialog (4 Madhabs with Shafi'i default)
  if (showMadhabDialog) {
    MadhabSelectionDialog(
      currentMadhab = madhab,
      language = language,
      onMadhabSelected = { selected ->
        onMadhabSelect(selected)
      },
      onDismiss = { showMadhabDialog = false }
    )
  }

  // Notification & Forbidden Prayer Times Center Sheet
  if (showNotificationSheet) {
    NotificationSettingsSheet(
      schedule = schedule,
      language = language,
      onDismiss = { showNotificationSheet = false }
    )
  }

  // About Ḥaasi al-Layl (The Night Sipper) Meaning & Brand Logo Dialog
  if (showAboutDialog) {
    AboutHaasiDialog(
      language = language,
      onDismiss = { showAboutDialog = false },
      onOpenNotifications = { showNotificationSheet = true }
    )
  }
}
