package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.data.model.DailyPrayerSchedule
import com.example.data.model.PrayerTimeItem
import com.example.data.prayer.PrayerInfo
import com.example.ui.theme.CoolMintGradientBase
import com.example.ui.theme.DarkForestInk
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LinenBorderLight
import com.example.ui.theme.ParchmentDialogSurface

/**
 * Today's full schedule, opened by tapping the hero card.
 *
 * The same entries the list below the hero card shows, but each with what its name means and a
 * line on the prayer itself - the list is for ticking prayers off, this is for reading. Read-only
 * on purpose, so there is exactly one place to mark a prayer as prayed.
 */
@Composable
fun TodayScheduleDialog(
  schedule: DailyPrayerSchedule,
  language: AppLanguage,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = ParchmentDialogSurface,
    modifier = Modifier.testTag("today_schedule_dialog"),
    title = {
      Text(
        text = LocalizationManager.getTodaysPrayersTitle(language),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = DarkForestInk
      )
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        schedule.items
          .filter { com.example.FeatureFlags.showsInSchedule(it.prayer) }
          .forEach { item -> ScheduleEntry(item, schedule.endOf(item.prayer), language) }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(
          LocalizationManager.getCloseLabel(language),
          color = DarkForestInk,
          fontWeight = FontWeight.Bold
        )
      }
    }
  )
}

@Composable
private fun ScheduleEntry(item: PrayerTimeItem, endsAt: java.time.LocalTime?, language: AppLanguage) {
  val highlighted = item.isCurrent || item.isNext
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = when {
      item.isCurrent -> CoolMintGradientBase.copy(alpha = 0.55f)
      item.isNext -> GoldAccent.copy(alpha = 0.18f)
      else -> Color.White
    },
    border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight),
    modifier = Modifier.fillMaxWidth().testTag("schedule_entry_${item.prayer.name.lowercase()}")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.Top
    ) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(CoolMintGradientBase.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = getPrayerIcon(item.prayer),
          contentDescription = null,
          tint = DarkForestInk,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        // Name on its own line, time underneath: the same no-contest layout as the prayer list,
        // so a long localized name never has to share a line with anything that can squeeze it.
        Text(
          text = LocalizationManager.getPrayerDisplayName(item.prayer, language),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = if (highlighted) FontWeight.Bold else FontWeight.SemiBold,
          color = DarkForestInk
        )
        Text(
          // Starting with Arabic made the whole line right-to-left, which split "04:32 AM" around
          // the name ("04:32 · الفجر AM"). A mark sets the line's direction from the app language,
          // and each part is isolated so neither reorders the other.
          text = (if (language == AppLanguage.ARABIC) "\u200F" else "\u200E") +
            "\u2068${item.prayer.arabicName}\u2069  ·  " +
            // Start - end for the five prayers ("Asr · 03:00 PM – 05:22 PM"); markers show one time.
            "\u2068${
              endsAt?.let { LocalizationManager.formatTimeRange(item.time, it, language) }
                ?: LocalizationManager.formatTime(item.time, language)
            }\u2069",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Medium,
          color = DarkForestInk.copy(alpha = 0.75f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = PrayerInfo.meaning(item.prayer, language),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = DarkForestInk
        )
        Text(
          text = PrayerInfo.description(item.prayer, language),
          style = MaterialTheme.typography.bodySmall,
          color = DarkForestInk.copy(alpha = 0.8f)
        )
      }
    }
  }
}
