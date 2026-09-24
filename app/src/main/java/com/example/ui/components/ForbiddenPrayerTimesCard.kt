package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.data.model.DailyPrayerSchedule
import com.example.ui.theme.DarkForestInk
import com.example.ui.theme.LinenBorderLight
import com.example.ui.theme.SunGoldenGlow
import java.time.LocalTime

@Composable
fun ForbiddenPrayerTimesBanner(
  schedule: DailyPrayerSchedule?,
  language: AppLanguage,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currentTime = schedule?.currentTime ?: LocalTime.now()
  val activeWindow = schedule?.activeForbiddenTime
  val nextWindow = schedule?.nextForbiddenTime

  if (activeWindow != null) {
    // Active Forbidden Time Alert Card
    Card(
      modifier = modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .testTag("forbidden_time_active_card"),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E7)),
      border = BorderStroke(1.2.dp, Color(0xFFFFB300))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFFFFE082)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            Icons.Default.Warning,
            contentDescription = null,
            tint = Color(0xFFE65100),
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFFE65100)
            ) {
              Text(
                text = LocalizationManager.getProhibitedNowBadge(language),
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = LocalizationManager.formatTimeRange(
                activeWindow.startTime,
                activeWindow.endTime,
                language
              ),
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = Color(0xFFBF360C)
            )
          }

          Spacer(modifier = Modifier.height(3.dp))

          Text(
            text = activeWindow.getLocalizedTitle(language),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = DarkForestInk
          )

          Text(
            text = LocalizationManager.getNaflProhibitedUntil(
              LocalizationManager.formatTime(activeWindow.endTime, language),
              activeWindow.minutesRemaining(currentTime),
              language
            ),
            fontSize = 11.sp,
            color = DarkForestInk.copy(alpha = 0.8f)
          )
        }

        Icon(
          Icons.Default.ChevronRight,
          contentDescription = LocalizationManager.getDetailsLabel(language),
          tint = DarkForestInk.copy(alpha = 0.5f),
          modifier = Modifier.size(20.dp)
        )
      }
    }
  } else if (nextWindow != null) {
    // Normal Informative Card showing Next Forbidden Window
    Card(
      modifier = modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .testTag("forbidden_time_upcoming_card"),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      border = BorderStroke(1.dp, LinenBorderLight)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.Info,
              contentDescription = null,
              tint = Color(0xFF2E7D32),
              modifier = Modifier.size(16.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = LocalizationManager.getPrayerPermittedNextProhibited(language),
              fontSize = 11.sp,
              color = DarkForestInk.copy(alpha = 0.65f),
              fontWeight = FontWeight.Medium
            )
            Text(
              text = "${nextWindow.getLocalizedTitle(language)} (" +
                LocalizationManager.formatTimeRange(nextWindow.startTime, nextWindow.endTime, language) +
                ")",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = DarkForestInk
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = Color(0xFFF0FDF4),
          border = BorderStroke(0.5.dp, Color(0xFF86EFAC))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              Icons.Default.NotificationsActive,
              contentDescription = null,
              tint = Color(0xFF16A34A),
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = LocalizationManager.getAlertsChipLabel(language),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF15803D)
            )
          }
        }
      }
    }
  }
}
