package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dua.IslamicContentProvider
import com.example.data.model.CityLocation
import com.example.data.model.DailyPrayerSchedule
import com.example.data.weather.CurrentWeather
import com.example.ui.WeatherUiState
import com.example.ui.components.DailyInspirationCard
import com.example.ui.components.PrayerNextHeroCard
import com.example.ui.components.PrayerScheduleListCard
import com.example.ui.components.WeatherHomeScreenCard

@Composable
fun HomeScreen(
  schedule: DailyPrayerSchedule,
  weatherState: WeatherUiState,
  currentCity: CityLocation,
  formatTemperature: (Double) -> String,
  onLocationClick: () -> Unit,
  onNavigateToWeather: () -> Unit,
  onTogglePrayer: (String) -> Unit
) {
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
      .background(MaterialTheme.colorScheme.background)
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
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            Icons.Outlined.Mosque,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Sakeenah",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "The quiet rhythm of your day",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Quick Location Selector Button
      AssistChip(
        onClick = onLocationClick,
        label = {
          Text(
            text = currentCity.name,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
          )
        },
        leadingIcon = {
          Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary
          )
        },
        shape = RoundedCornerShape(12.dp),
        colors = AssistChipDefaults.assistChipColors(
          containerColor = MaterialTheme.colorScheme.surface,
          labelColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.testTag("top_city_chip")
      )
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("home_scroll_list"),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Hero Next Prayer & Live Countdown Card
      item {
        PrayerNextHeroCard(
          schedule = schedule,
          currentWeather = currentWeather,
          temperatureFormatted = tempFormatted,
          onLocationClick = onLocationClick,
          onWeatherClick = onNavigateToWeather
        )
      }

      // 2. Weather & Daily Guidance Card
      item {
        WeatherHomeScreenCard(
          weather = currentWeather,
          temperatureFormatted = tempFormatted,
          highFormatted = highFormatted,
          lowFormatted = lowFormatted,
          onViewForecastClick = onNavigateToWeather
        )
      }

      // 3. Today's 5 Prayers Schedule with Checkboxes
      item {
        PrayerScheduleListCard(
          items = schedule.items,
          onTogglePrayer = onTogglePrayer
        )
      }

      // 4. Daily Inspiration (Ayah & Hadith)
      item {
        DailyInspirationCard(inspiration = IslamicContentProvider.DAILY_INSPIRATION)
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}
