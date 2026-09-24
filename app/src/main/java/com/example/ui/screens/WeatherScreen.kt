package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.data.model.CityLocation
import com.example.data.weather.DailyForecast
import com.example.data.weather.FullWeatherData
import com.example.data.weather.HourlyForecast
import com.example.ui.WeatherUiState
import com.example.ui.theme.WarmGoldAccent

@Composable
fun WeatherScreen(
  weatherState: WeatherUiState,
  currentCity: CityLocation,
  isFahrenheit: Boolean,
  formatTemperature: (Double) -> String,
  onRefresh: () -> Unit,
  onLocationClick: () -> Unit,
  onToggleUnit: () -> Unit,
  language: AppLanguage = AppLanguage.ENGLISH
) {
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
      Column {
        Text(
          text = LocalizationManager.getWeatherForecastTitle(language),
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Surface(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .padding(top = 2.dp),
          color = Color.Transparent
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 2.dp)
          ) {
            Icon(
              Icons.Default.LocationOn,
              contentDescription = null,
              modifier = Modifier.size(14.dp),
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${currentCity.name}, ${currentCity.country}",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        // Temperature unit toggle chip
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.height(32.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .background(if (!isFahrenheit) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { if (isFahrenheit) onToggleUnit() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text("°C", fontWeight = FontWeight.Bold, color = if (!isFahrenheit) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
              modifier = Modifier
                .background(if (isFahrenheit) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { if (!isFahrenheit) onToggleUnit() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text("°F", fontWeight = FontWeight.Bold, color = if (isFahrenheit) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(
          onClick = onRefresh,
          modifier = Modifier.testTag("btn_refresh_weather")
        ) {
          Icon(
            Icons.Default.Refresh,
            contentDescription = LocalizationManager.getRefreshWeatherLabel(language),
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }
    }

    when (weatherState) {
      is WeatherUiState.Loading -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .testTag("weather_loading"),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = LocalizationManager.getWeatherLoadingDesc(language),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      is WeatherUiState.Error -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .testTag("weather_error"),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = LocalizationManager.getWeatherErrorDesc(language),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = weatherState.message,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = onRefresh,
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
              Text(LocalizationManager.getRetryLabel(language))
            }
          }
        }
      }

      is WeatherUiState.Success -> {
        WeatherSuccessContent(
          data = weatherState.data,
          formatTemperature = formatTemperature,
          language = language,
          onLocationClick = onLocationClick,
          isFahrenheit = isFahrenheit
        )
      }
    }
  }
}

@Composable
private fun WeatherSuccessContent(
  data: FullWeatherData,
  formatTemperature: (Double) -> String,
  language: AppLanguage,
  onLocationClick: () -> Unit,
  isFahrenheit: Boolean
) {
  val cur = data.current

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("weather_content_list"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Hero Weather Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("weather_hero_card"),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color(0xFF263D32),
                  Color(0xFF1D3227),
                  Color(0xFF132219)
                )
              )
            )
            .padding(24.dp)
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "${data.cityName}, ${data.countryName}",
              style = MaterialTheme.typography.titleMedium,
              color = Color.White.copy(alpha = 0.9f),
              fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            val icon = getWeatherIcon(cur.weatherCode)
            Icon(
              imageVector = icon,
              contentDescription = null,
              modifier = Modifier.size(64.dp),
              tint = WarmGoldAccent
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = formatTemperature(cur.temperatureC),
              style = MaterialTheme.typography.displayMedium,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White
            )

            Text(
              text = LocalizationManager.getWeatherConditionTitle(cur.weatherCode, language),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )

            val highLowLabel =
              "${LocalizationManager.getFeelsLikeLabel(language)} ${formatTemperature(cur.feelsLikeC)}  •  ${LocalizationManager.getHighShort(language)}: ${formatTemperature(cur.highC)}  •  ${LocalizationManager.getLowShort(language)}: ${formatTemperature(cur.lowC)}"

            Text(
              text = highLowLabel,
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.8f),
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }
      }
    }

    // 2. Islamic Guidance & Du'a for Current Weather
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center
            ) {
              Text("🕌", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = LocalizationManager.getIslamicWeatherGuidance(language),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = LocalizationManager.getIslamicWeatherAdvice(cur.weatherCode, cur.temperatureC, language),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
          if (cur.islamicDuaSuggestion != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surface,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = LocalizationManager.getRecommendedDuaLabel(language),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = cur.islamicDuaSuggestion,
                  style = MaterialTheme.typography.bodySmall,
                  fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                  modifier = Modifier.padding(top = 4.dp)
                )
              }
            }
          }
        }
      }
    }

    // 3. 24-Hour Hourly Forecast Strip
    item {
      Text(
        text = LocalizationManager.getHourlyForecastTitle(language),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(8.dp))
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(data.hourly) { hour ->
          HourlyWeatherItem(hour = hour, formatTemperature = formatTemperature, language = language, isFahrenheit = isFahrenheit)
        }
      }
    }

    // 4. Metrics Grid (Humidity, Wind, UV, Precipitation)
    item {
      Text(
        text = LocalizationManager.getConditionsDetailsLabel(language),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        MetricItem(
          modifier = Modifier.weight(1f),
          title = LocalizationManager.getHumidityLabel(language),
          value = "${cur.humidityPercent}%",
          icon = Icons.Default.WaterDrop,
          iconTint = MaterialTheme.colorScheme.tertiary,
isFahrenheit = isFahrenheit
)
        MetricItem(
          modifier = Modifier.weight(1f),
          title = LocalizationManager.getWindSpeedLabel(language),
          value = "%.1f km/h".format(cur.windSpeedKmh),
          icon = Icons.Default.Air,
          iconTint = MaterialTheme.colorScheme.primary,
isFahrenheit = isFahrenheit
)
      }
      Spacer(modifier = Modifier.height(12.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        MetricItem(
          modifier = Modifier.weight(1f),
          title = LocalizationManager.getUvIndexLabel(language),
          value = "%.1f".format(cur.uvIndex),
          icon = Icons.Outlined.WbSunny,
          iconTint = WarmGoldAccent,
isFahrenheit = isFahrenheit
)
        MetricItem(
          modifier = Modifier.weight(1f),
          title = LocalizationManager.getFeelsLikeLabel(language),
          value = formatTemperature(cur.feelsLikeC),
          icon = Icons.Outlined.Thermostat,
          iconTint = MaterialTheme.colorScheme.secondary,
isFahrenheit = isFahrenheit
)
      }
    }

    // 5. 7-Day Forecast Section
    item {
      Text(
        text = LocalizationManager.getDailyForecastTitle(language),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(8.dp))
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          data.daily.forEachIndexed { index, day ->
            DailyForecastRow(day = day, formatTemperature = formatTemperature, isFahrenheit = isFahrenheit)
            if (index < data.daily.size - 1) {
              androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
              )
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun HourlyWeatherItem(
  hour: HourlyForecast,
  formatTemperature: (Double) -> String,
  language: AppLanguage,
  isFahrenheit: Boolean
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.width(76.dp)
  ) {
    Column(
      modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = LocalizationManager.localizeTimeText(hour.timeFormatted, language),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(8.dp))
      Icon(
        imageVector = getWeatherIcon(hour.weatherCode),
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = WarmGoldAccent
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = formatTemperature(hour.temperatureC),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      if (hour.precipChance > 0) {
        Text(
          text = "${hour.precipChance}%",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.tertiary,
          fontSize = 10.sp
        )
      }
    }
  }
}

@Composable
private fun MetricItem(
  modifier: Modifier = Modifier,
  title: String,
  value: String,
  icon: ImageVector,
  iconTint: Color,
  isFahrenheit: Boolean
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(iconTint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = value,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }
  }
}

@Composable
private fun DailyForecastRow(
  day: DailyForecast,
  formatTemperature: (Double) -> String,
  isFahrenheit: Boolean
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.width(90.dp)) {
      Text(
        text = day.dayName,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = day.dateFormatted,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.weight(1f)
    ) {
      Icon(
        imageVector = getWeatherIcon(day.weatherCode),
        contentDescription = null,
        modifier = Modifier.size(22.dp),
        tint = WarmGoldAccent
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = day.conditionTitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Text(
      text = "${formatTemperature(day.highC)} / ${formatTemperature(day.lowC)}",
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

private fun getWeatherIcon(code: Int): ImageVector {
  return when {
    code in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82) -> Icons.Outlined.Grain
    code in listOf(1, 2, 3) -> Icons.Outlined.Cloud
    code in listOf(45, 48) -> Icons.Outlined.Cloud
    else -> Icons.Default.WbSunny
  }
}
