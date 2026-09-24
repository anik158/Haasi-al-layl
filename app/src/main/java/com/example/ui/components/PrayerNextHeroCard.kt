package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.data.calendar.BanglaDateHelper
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.data.model.DailyPrayerSchedule
import com.example.data.prayer.HijriDateHelper
import com.example.data.weather.CurrentWeather
import com.example.ui.theme.CoolMintGradientBase
import com.example.ui.theme.DarkForestInk
import com.example.ui.theme.LinenBorderLight
import com.example.ui.theme.MintBadgeContainer
import com.example.ui.theme.OutlineDarkBorder
import com.example.ui.theme.WarmBeigeBackground
import com.example.ui.theme.WarmGoldAccent

// Card(onClick) has been experimental on some Material 3 releases; the opt-in is harmless where it
// is not.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerNextHeroCard(
  schedule: DailyPrayerSchedule,
  /** False while the location is only the Makkah placeholder; see the location chip below. */
  hasResolvedLocation: Boolean = true,
  currentWeather: CurrentWeather?,
  temperatureFormatted: String,
  language: AppLanguage = AppLanguage.ENGLISH,
  onLocationClick: () -> Unit,
  onWeatherClick: () -> Unit,
  /** Opens today's full schedule. The location and weather chips inside keep their own taps. */
  onCardClick: () -> Unit = {}
) {
  val next = schedule.nextPrayer
  val animatedProgress by animateFloatAsState(
    targetValue = schedule.progressToNext,
    label = "prayer_progress"
  )

  val isBanglaRegion = language == AppLanguage.BENGALI || schedule.country.equals("Bangladesh", ignoreCase = true)
  val banglaDate = if (isBanglaRegion) BanglaDateHelper.getBanglaDate(schedule.date) else null

  Card(
    onClick = onCardClick,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("hero_prayer_card"),
    shape = RoundedCornerShape(22.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color.White,
              Color(0xFFF0F6F3),
              CoolMintGradientBase.copy(alpha = 0.8f)
            )
          )
        )
        .padding(18.dp)
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {

        // Top Row: Location chip & Weather pill with crisp white/dark outlines
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Location Selector Chip
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .clickable { onLocationClick() }
              .testTag("location_chip"),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineDarkBorder)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = DarkForestInk
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                // An unresolved location is a prompt, never a city name. The placeholder is
                // Makkah, and rendering it as fact is how the app came to tell a user in
                // Helsinki that they were in Saudi Arabia.
                text = if (hasResolvedLocation) {
                  "${schedule.city}, ${schedule.country}"
                } else {
                  LocalizationManager.getLocationNotSetLabel(language)
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = DarkForestInk
              )
            }
          }

          // Weather quick pill
          if (currentWeather != null) {
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { onWeatherClick() }
                .testTag("weather_pill"),
              shape = RoundedCornerShape(20.dp),
              color = Color.White,
              border = androidx.compose.foundation.BorderStroke(1.dp, OutlineDarkBorder)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
                  modifier = Modifier.size(15.dp),
                  tint = WarmGoldAccent
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = temperatureFormatted,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = DarkForestInk
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Calendar Section: Hijri, Gregorian, and conditional Bangla
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.85f))
            .border(1.dp, LinenBorderLight, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = DarkForestInk
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = HijriDateHelper.getTodayHijriFormatted(schedule.date, language),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = DarkForestInk
              )
            }

            // Bangla Calendar Badge (Shown only in Bangladesh / Bangla language)
            if (isBanglaRegion && banglaDate != null) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = MintBadgeContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineDarkBorder.copy(alpha = 0.35f))
              ) {
                Text(
                  text = "${BanglaDateHelper.toBanglaDigits(banglaDate.day)} ${banglaDate.monthNameBn}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = DarkForestInk,
                  modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = HijriDateHelper.getGregorianFormatted(schedule.date, language),
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              color = DarkForestInk.copy(alpha = 0.7f)
            )

            if (isBanglaRegion && banglaDate != null) {
              Text(
                text = "${BanglaDateHelper.toBanglaDigits(banglaDate.year)} বঙ্গাব্দ • ${banglaDate.seasonBn}",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = DarkForestInk.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Next Prayer Hero details
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Bottom
        ) {
          Column {
            Text(
              text = LocalizationManager.getNextPrayerLabel(language),
              style = MaterialTheme.typography.labelSmall,
              letterSpacing = 1.5.sp,
              color = DarkForestInk.copy(alpha = 0.65f),
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              val prayerNameDisplay = next?.let {
                LocalizationManager.getPrayerDisplayName(it.prayer, language)
              } ?: LocalizationManager.getPrayerPlaceholder(language)
              Text(
                text = prayerNameDisplay,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = DarkForestInk
              )
              if (language != AppLanguage.ARABIC) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = next?.prayer?.arabicName ?: "",
                  style = MaterialTheme.typography.titleLarge,
                  color = WarmGoldAccent,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = if (next != null) LocalizationManager.formatTime(next.time, language) else "--:--",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = DarkForestInk
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(top = 2.dp)
            ) {
              Icon(
                Icons.Default.AccessTime,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = DarkForestInk
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "-${schedule.timeUntilNextFormatted}",
                style = MaterialTheme.typography.labelMedium,
                color = DarkForestInk,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Countdown progress bar
        LinearProgressIndicator(
          progress = { animatedProgress },
          modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(CircleShape),
          color = DarkForestInk,
          trackColor = CoolMintGradientBase
        )
      }
    }
  }
}

