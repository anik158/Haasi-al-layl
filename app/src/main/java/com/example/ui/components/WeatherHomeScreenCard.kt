package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.data.weather.CurrentWeather
import com.example.ui.theme.CoolMintGradientBase
import com.example.ui.theme.DarkForestInk
import com.example.ui.theme.LinenBorderLight
import com.example.ui.theme.OutlineDarkBorder
import com.example.ui.theme.WarmGoldAccent

@Composable
fun WeatherHomeScreenCard(
  weather: CurrentWeather?,
  temperatureFormatted: String,
  highFormatted: String,
  lowFormatted: String,
  language: AppLanguage = AppLanguage.ENGLISH,
  onViewForecastClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("weather_home_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = Color.White
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(CoolMintGradientBase)
              .border(1.dp, OutlineDarkBorder, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Outlined.LightMode,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
              tint = DarkForestInk
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = LocalizationManager.getWeatherCardTitle(language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DarkForestInk
          )
        }

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = Color.White,
          border = androidx.compose.foundation.BorderStroke(1.dp, OutlineDarkBorder)
        ) {
          Text(
            text = "LIVE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = DarkForestInk,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      if (weather != null) {
        // Main Weather Metrics Banner
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CoolMintGradientBase.copy(alpha = 0.4f))
            .border(1.dp, LinenBorderLight, RoundedCornerShape(16.dp))
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = temperatureFormatted,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = DarkForestInk
              )
              Text(
                text = weather.conditionTitle,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = DarkForestInk
              )
              Text(
                text = "H: $highFormatted  •  L: $lowFormatted",
                style = MaterialTheme.typography.bodySmall,
                color = DarkForestInk.copy(alpha = 0.7f)
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              val weatherIcon = when {
                weather.weatherCode in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82) -> Icons.Outlined.Grain
                weather.weatherCode in listOf(1, 2, 3) -> Icons.Outlined.Cloud
                else -> Icons.Default.WbSunny
              }
              Icon(
                imageVector = weatherIcon,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = WarmGoldAccent
              )
              Spacer(modifier = Modifier.height(6.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  Icons.Default.WaterDrop,
                  contentDescription = null,
                  modifier = Modifier.size(13.dp),
                  tint = DarkForestInk
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "${weather.humidityPercent}%",
                  style = MaterialTheme.typography.labelSmall,
                  color = DarkForestInk.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                  Icons.Default.Air,
                  contentDescription = null,
                  modifier = Modifier.size(13.dp),
                  tint = DarkForestInk
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "%.0f km/h".format(weather.windSpeedKmh),
                  style = MaterialTheme.typography.labelSmall,
                  color = DarkForestInk.copy(alpha = 0.8f)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Islamic Weather Advisory Pill
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color.White,
          border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "☀️ ${LocalizationManager.getDailyAdviceHeader(language)}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = DarkForestInk
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = weather.islamicWeatherAdvice,
              style = MaterialTheme.typography.bodySmall,
              lineHeight = 18.sp,
              color = DarkForestInk.copy(alpha = 0.85f)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // View Full Forecast Button
        OutlinedButton(
          onClick = onViewForecastClick,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_view_full_weather"),
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, OutlineDarkBorder),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = DarkForestInk
          )
        ) {
          Text(
            text = LocalizationManager.getViewForecastButton(language),
            fontWeight = FontWeight.SemiBold,
            color = DarkForestInk
          )
          Spacer(modifier = Modifier.width(6.dp))
          Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = DarkForestInk
          )
        }
      } else {
        Text(
          text = "Loading weather information...",
          style = MaterialTheme.typography.bodyMedium,
          color = DarkForestInk.copy(alpha = 0.7f)
        )
      }
    }
  }
}
