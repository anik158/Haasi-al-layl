package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.data.prayer.FastingStatus
import com.example.ui.theme.DarkForestInk
import com.example.ui.theme.LinenBorderLight
import com.example.ui.theme.WarmGoldAccent

/**
 * Sehri end and Iftar for the fast that matters now - today's until Iftar, then tomorrow's.
 *
 * Two looks for two places. During Ramadan it sits at the top, under the prayer card, on the same
 * dark green as the current-prayer panel so the two read as the day's headline. The rest of the
 * year it is a plain card lower down, for voluntary fasts.
 */
@Composable
fun SehriIftarCard(
  status: FastingStatus,
  language: AppLanguage,
  modifier: Modifier = Modifier
) {
  val ramadan = status.day.isRamadan
  val ink = if (ramadan) Color.White else DarkForestInk
  val muted = ink.copy(alpha = 0.72f)
  val shape = RoundedCornerShape(if (ramadan) 22.dp else 18.dp)

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .then(
        if (ramadan) {
          Modifier.background(
            Brush.linearGradient(listOf(DarkForestInk, DarkForestInk.copy(alpha = 0.9f)))
          )
        } else {
          Modifier
            .background(Color.White)
            .border(1.dp, LinenBorderLight, shape)
        }
      )
      .padding(18.dp)
      .testTag(if (ramadan) "sehri_iftar_ramadan" else "sehri_iftar_card")
  ) {
    // Title row: the name, and a Ramadan-day badge or the "voluntary" note.
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = LocalizationManager.getSehriIftarTitle(language),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = ink
        )
        val subtitle = when {
          ramadan -> null
          else -> LocalizationManager.getSehriIftarVoluntarySubtitle(language)
        }
        if (subtitle != null) {
          Text(text = subtitle, style = MaterialTheme.typography.labelMedium, color = muted)
        }
      }
      if (ramadan) {
        Surface(shape = RoundedCornerShape(10.dp), color = WarmGoldAccent) {
          Text(
            text = LocalizationManager.getRamadanDayLabel(status.day.ramadanDay, language),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = DarkForestInk,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      TimeTile(
        icon = Icons.Filled.DarkMode,
        label = LocalizationManager.getSehriEndsLabel(language),
        time = LocalizationManager.formatTime(status.day.sehriEnd, language),
        // Before dawn the next thing is Sehri ending; once the fast is under way it is Iftar.
        highlighted = !status.isFasting,
        ramadan = ramadan,
        modifier = Modifier.weight(1f)
      )
      TimeTile(
        icon = Icons.Filled.WbTwilight,
        label = LocalizationManager.getIftarLabel(language),
        time = LocalizationManager.formatTime(status.day.iftar, language),
        highlighted = status.isFasting,
        ramadan = ramadan,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // The countdown to whichever comes next, with "tomorrow" said out loud after today's Iftar.
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = LocalizationManager.localizeDigits(status.countdownFormatted, language),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = ink
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = if (status.isFasting) {
          LocalizationManager.getUntilIftarLabel(language)
        } else {
          LocalizationManager.getUntilSehriEndsLabel(language)
        },
        style = MaterialTheme.typography.labelMedium,
        color = muted
      )
      if (status.isTomorrow) {
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "· " + LocalizationManager.getTomorrowLabel(language),
          style = MaterialTheme.typography.labelMedium,
          color = muted
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = LocalizationManager.getSehriIftarFootnote(language),
      style = MaterialTheme.typography.bodySmall,
      fontSize = 11.sp,
      color = ink.copy(alpha = 0.6f)
    )
  }
}

@Composable
private fun TimeTile(
  icon: ImageVector,
  label: String,
  time: String,
  highlighted: Boolean,
  ramadan: Boolean,
  modifier: Modifier = Modifier
) {
  val background = when {
    ramadan && highlighted -> Color.White.copy(alpha = 0.16f)
    ramadan -> Color.White.copy(alpha = 0.07f)
    highlighted -> WarmGoldAccent.copy(alpha = 0.14f)
    else -> DarkForestInk.copy(alpha = 0.04f)
  }
  val ink = if (ramadan) Color.White else DarkForestInk
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .background(background)
      .padding(horizontal = 12.dp, vertical = 10.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(icon, contentDescription = null, tint = WarmGoldAccent, modifier = Modifier.size(15.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = ink.copy(alpha = 0.75f),
        fontWeight = FontWeight.SemiBold
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = time,
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.ExtraBold,
      color = ink
    )
  }
}
