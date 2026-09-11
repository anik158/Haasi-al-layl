package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyPrayerSchedule
import com.example.data.prayer.HijriDateHelper
import com.example.data.weather.CurrentWeather
import com.example.ui.theme.WarmGoldAccent

@Composable
fun PrayerNextHeroCard(
  schedule: DailyPrayerSchedule,
  currentWeather: CurrentWeather?,
  temperatureFormatted: String,
  onLocationClick: () -> Unit,
  onWeatherClick: () -> Unit
) {
  val next = schedule.nextPrayer
  val animatedProgress by animateFloatAsState(
    targetValue = schedule.progressToNext,
    label = "prayer_progress"
  )

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("hero_prayer_card"),
    shape = RoundedCornerShape(24.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xFF22382C),
              Color(0xFF1B2E24),
              Color(0xFF13231A)
            )
          )
        )
        .padding(20.dp)
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {

        // Top Row: Location chip & Weather pill
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Location Selector Chip
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable { onLocationClick() }
              .testTag("location_chip"),
            color = Color(0x2EFFFFFF),
            contentColor = Color.White
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = WarmGoldAccent
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${schedule.city}, ${schedule.country}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
              )
            }
          }

          // Weather quick pill
          if (currentWeather != null) {
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onWeatherClick() }
                .testTag("weather_pill"),
              color = Color(0x2EFFFFFF),
              contentColor = Color.White
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                val weatherIcon = when {
                  currentWeather.weatherCode in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82) -> Icons.Outlined.Grain
                  currentWeather.weatherCode in listOf(1, 2, 3) -> Icons.Outlined.Cloud
                  else -> Icons.Default.WbSunny
                }
                Icon(
                  weatherIcon,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = WarmGoldAccent
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = temperatureFormatted,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Date Display: Hijri & Gregorian
        Text(
          text = HijriDateHelper.getTodayHijriFormatted(schedule.date),
          style = MaterialTheme.typography.titleSmall,
          color = WarmGoldAccent,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = HijriDateHelper.getGregorianFormatted(schedule.date),
          style = MaterialTheme.typography.bodySmall,
          color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Next Prayer Hero details
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Bottom
        ) {
          Column {
            Text(
              text = "NEXT PRAYER",
              style = MaterialTheme.typography.labelSmall,
              letterSpacing = 1.5.sp,
              color = Color.White.copy(alpha = 0.7f),
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = next?.prayer?.displayName ?: "Prayer",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = next?.prayer?.arabicName ?: "",
                style = MaterialTheme.typography.titleLarge,
                color = WarmGoldAccent
              )
            }
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = next?.formattedTime ?: "--:--",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(top = 2.dp)
            ) {
              Icon(
                Icons.Default.AccessTime,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = WarmGoldAccent
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "-${schedule.timeUntilNextFormatted}",
                style = MaterialTheme.typography.labelMedium,
                color = WarmGoldAccent,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Countdown progress bar
        LinearProgressIndicator(
          progress = { animatedProgress },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape),
          color = WarmGoldAccent,
          trackColor = Color(0x33FFFFFF)
        )
      }
    }
  }
}
