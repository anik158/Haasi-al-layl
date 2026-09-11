package com.example.ui.components

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
import com.example.data.model.PrayerName
import com.example.data.model.PrayerTimeItem
import com.example.ui.theme.GoldAccent

@Composable
fun PrayerScheduleListCard(
  items: List<PrayerTimeItem>,
  onTogglePrayer: (String) -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("prayer_schedule_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
            text = "Today's Prayers",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Tap circle to mark as prayed",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      items.forEachIndexed { index, item ->
        PrayerRowItem(
          item = item,
          onToggleCheck = { onTogglePrayer(item.prayer.name) }
        )
        if (index < items.size - 1) {
          HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
    item.isCurrent -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    item.isNext -> GoldAccent.copy(alpha = 0.12f)
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
          if (item.isCurrent || item.isNext) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
          else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
        tint = if (item.isCurrent || item.isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Spacer(modifier = Modifier.width(12.dp))

    // Name & Arabic
    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = item.prayer.displayName,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = if (item.isNext || item.isCurrent) FontWeight.Bold else FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )
        if (item.isCurrent) {
          Spacer(modifier = Modifier.width(6.dp))
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(2.dp)
          ) {
            Text(
              text = "NOW",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onPrimary,
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
              color = Color.Black,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
      Text(
        text = item.prayer.arabicName,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    // Time
    Text(
      text = item.formattedTime,
      style = MaterialTheme.typography.bodyLarge,
      fontWeight = if (item.isCurrent || item.isNext) FontWeight.Bold else FontWeight.SemiBold,
      color = if (item.isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
          tint = if (item.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
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
