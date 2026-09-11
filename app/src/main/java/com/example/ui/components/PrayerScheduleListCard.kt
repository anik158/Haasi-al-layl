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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Brightness5
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.Brightness7
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.data.model.PrayerName
import com.example.data.model.PrayerTimeItem
import com.example.ui.theme.CoolMintGradientBase
import com.example.ui.theme.DarkForestInk
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LinenBorderLight
import com.example.ui.theme.OutlineDarkBorder

@Composable
fun PrayerScheduleListCard(
  items: List<PrayerTimeItem>,
  modifier: Modifier = Modifier,
  language: AppLanguage = AppLanguage.ENGLISH,
  onTogglePrayer: (String) -> Unit
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("prayer_schedule_card"),
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
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = LocalizationManager.getTodaysPrayersTitle(language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DarkForestInk
          )
          Text(
            text = LocalizationManager.getTapCircleHint(language),
            style = MaterialTheme.typography.bodySmall,
            color = DarkForestInk.copy(alpha = 0.7f)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      items.forEachIndexed { index, item ->
        PrayerRowItem(
          item = item,
          language = language,
          onToggleCheck = { onTogglePrayer(item.prayer.name) }
        )
        if (index < items.size - 1) {
          HorizontalDivider(
            color = LinenBorderLight,
            thickness = 0.8.dp,
            modifier = Modifier.padding(horizontal = 4.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun PrayerRowItem(
  item: PrayerTimeItem,
  language: AppLanguage,
  onToggleCheck: () -> Unit
) {
  val isMainPrayer = item.prayer in listOf(
    PrayerName.FAJR,
    PrayerName.DHUHR,
    PrayerName.ASR,
    PrayerName.MAGHRIB,
    PrayerName.ISHA
  )

  val backgroundColor = when {
    item.isCurrent -> CoolMintGradientBase.copy(alpha = 0.5f)
    item.isNext -> GoldAccent.copy(alpha = 0.15f)
    else -> Color.Transparent
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(backgroundColor)
      .padding(horizontal = 8.dp, vertical = 10.dp)
      .testTag("prayer_row_${item.prayer.name.lowercase()}"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Prayer Icon
    val icon = getPrayerIcon(item.prayer)
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(
          if (item.isCurrent || item.isNext) CoolMintGradientBase
          else LinenBorderLight.copy(alpha = 0.6f)
        )
        .border(0.5.dp, OutlineDarkBorder, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
        tint = DarkForestInk
      )
    }

    Spacer(modifier = Modifier.width(12.dp))

    // Name & Arabic
    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = LocalizationManager.getPrayerDisplayName(item.prayer, language),
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = if (item.isNext || item.isCurrent) FontWeight.Bold else FontWeight.Medium,
          color = DarkForestInk
        )
        if (item.isCurrent) {
          Spacer(modifier = Modifier.width(6.dp))
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = DarkForestInk,
            modifier = Modifier.padding(2.dp)
          ) {
            Text(
              text = "NOW",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              fontWeight = FontWeight.Bold
            )
          }
        } else if (item.isNext) {
          Spacer(modifier = Modifier.width(6.dp))
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = GoldAccent,
            modifier = Modifier.padding(2.dp)
          ) {
            Text(
              text = "NEXT",
              style = MaterialTheme.typography.labelSmall,
              color = DarkForestInk,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
      Text(
        text = item.prayer.arabicName,
        style = MaterialTheme.typography.bodySmall,
        color = DarkForestInk.copy(alpha = 0.65f)
      )
    }

    // Time
    Text(
      text = item.formattedTime,
      style = MaterialTheme.typography.bodyLarge,
      fontWeight = if (item.isCurrent || item.isNext) FontWeight.Bold else FontWeight.SemiBold,
      color = DarkForestInk
    )

    Spacer(modifier = Modifier.width(12.dp))

    // Checkbox for prayer completion (only for 5 fardh prayers)
    if (isMainPrayer) {
      IconButton(
        onClick = onToggleCheck,
        modifier = Modifier
          .size(36.dp)
          .testTag("prayer_check_${item.prayer.name.lowercase()}")
      ) {
        Icon(
          imageVector = if (item.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
          contentDescription = "Mark ${item.prayer.displayName} as completed",
          tint = if (item.isCompleted) DarkForestInk else DarkForestInk.copy(alpha = 0.4f)
        )
      }
    } else {
      Spacer(modifier = Modifier.width(36.dp))
    }
  }
}

private fun getPrayerIcon(name: PrayerName): ImageVector {
  return when (name) {
    PrayerName.IMSAK -> Icons.Outlined.NightsStay
    PrayerName.FAJR -> Icons.Outlined.WbTwilight
    PrayerName.SUNRISE -> Icons.Outlined.Brightness5
    PrayerName.DHUHR -> Icons.Outlined.WbSunny
    PrayerName.ASR -> Icons.Outlined.Brightness6
    PrayerName.MAGHRIB -> Icons.Outlined.Brightness7
    PrayerName.ISHA -> Icons.Outlined.DarkMode
  }
}
