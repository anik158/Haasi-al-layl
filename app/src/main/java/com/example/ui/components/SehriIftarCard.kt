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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.data.prayer.FastDayKind
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
  modifier: Modifier = Modifier,
  /** A voluntary fast was marked or unmarked: the Sehri reminder re-arms. */
  onFastingChanged: () -> Unit = {}
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

    // What the sunnah says about fasting this day: forbidden, discouraged or recommended, each with
    // its reference. Ramadan's own rule is the badge above, so it is not repeated here.
    val dayRules = status.rules.filter { it.kind != FastDayKind.OBLIGATORY }
    if (dayRules.isNotEmpty()) {
      Spacer(modifier = Modifier.height(12.dp))
      dayRules.forEach { rule ->
        val (tint, background) = when (rule.kind) {
          FastDayKind.PROHIBITED -> Color(0xFFB3261E) to Color(0xFFFDECEA)
          FastDayKind.DISCOURAGED -> Color(0xFF8A5A00) to Color(0xFFFFF4E0)
          else -> Color(0xFF1B5E20) to Color(0xFFE8F5E9)
        }
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("fast_rule_${rule.key}")
        ) {
          Text(
            text = LocalizationManager.getFastKindTitle(rule.kind, language),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = tint
          )
          Text(
            text = "${LocalizationManager.getFastRuleName(rule.key, language)} · ${rule.reference}",
            style = MaterialTheme.typography.bodySmall,
            color = tint
          )
        }
      }
    }

    // How sure the date is, on every day where a day's error changes whether to fast.
    val hijriDay = com.example.data.prayer.HijriDateHelper.dayInfo(status.day.date)
    if (com.example.data.hijri.MonthStartWarning.applies(hijriDay, status.rules)) {
      Spacer(modifier = Modifier.height(8.dp))
      MonthStartNote(hijriDay, ramadan, language, ink)
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
    // Not on a forbidden day: a countdown would read as an invitation to fast.
    if (!status.isProhibited) Row(verticalAlignment = Alignment.CenterVertically) {
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

    // A voluntary fast: the user marks it, and the Sehri reminder rings for it. Only while the
    // reminder is on, before that day's Sehri ends, and never on a forbidden day.
    val context = androidx.compose.ui.platform.LocalContext.current
    if (!ramadan && !status.isProhibited && !status.isFasting &&
      com.example.data.notification.PrayerNotificationManager.isSehriReminderEnabled(context)
    ) {
      val today = java.time.LocalDate.now()
      var marked by androidx.compose.runtime.remember(status.day.date) {
        androidx.compose.runtime.mutableStateOf(
          status.day.date in com.example.data.notification.PrayerNotificationManager.voluntaryFastDays(context)
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      Surface(
        onClick = {
          marked = !marked
          com.example.data.notification.PrayerNotificationManager.setVoluntaryFast(context, status.day.date, marked, today)
          onFastingChanged()
        },
        shape = RoundedCornerShape(12.dp),
        color = if (marked) DarkForestInk else WarmGoldAccent.copy(alpha = 0.14f),
        modifier = Modifier.fillMaxWidth().testTag("fasting_mark")
      ) {
        Text(
          text = if (marked) {
            com.example.data.localization.HijriStrings.fastingMarked(language)
          } else {
            com.example.data.localization.HijriStrings.fastingTomorrowButton(status.isTomorrow, language)
          },
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold,
          color = if (marked) Color.White else DarkForestInk,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
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

/**
 * "Ramadan's start follows the moon sighting announced in your country", then where this
 * month's start came from: the user, an announcement, Saudi Arabia plus the moon, or the
 * calculation.
 */
@Composable
private fun MonthStartNote(
  day: com.example.data.hijri.HijriDay?,
  ramadan: Boolean,
  language: AppLanguage,
  ink: Color
) {
  val context = androidx.compose.ui.platform.LocalContext.current
  val strings = com.example.data.localization.HijriStrings
  val header = if (ramadan || day?.month == com.example.data.hijri.HijriMonthId.SHABAN ||
    day?.month == com.example.data.hijri.HijriMonthId.SHAWWAL
  ) {
    strings.ramadanFollowsSighting(language)
  } else {
    strings.dateDependsOnSighting(language)
  }
  val start = day?.monthStart
  val source = when (start?.source) {
    null, com.example.data.hijri.MonthStartSource.CALCULATED -> LocalizationManager.getHijriCalculatedNote(language)
    com.example.data.hijri.MonthStartSource.USER -> strings.sourceUser(
      com.example.data.hijri.HijriStore(context).userStarts()[start.id]?.heardFrom,
      language
    )
    com.example.data.hijri.MonthStartSource.ANNOUNCED -> strings.sourceAnnounced(start.authority, language)
    com.example.data.hijri.MonthStartSource.MOON_ESTIMATE -> strings.sourceMoonEstimate(
      month = com.example.data.prayer.HijriDateHelper.monthName(start.id.month, language),
      saudiDate = start.authorityStart?.let { com.example.data.prayer.HijriDateHelper.shortGregorian(it, language) },
      localDate = com.example.data.prayer.HijriDateHelper.shortGregorian(start.startsOn, language),
      saudiStarted = start.authorityStart?.let { !it.isAfter(java.time.LocalDate.now()) } ?: false,
      l = language
    )
  }
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(if (ramadan) Color.White.copy(alpha = 0.1f) else Color(0xFFFFF4E0))
      .padding(horizontal = 12.dp, vertical = 8.dp)
      // Read as one note by TalkBack, header and source together.
      .semantics(mergeDescendants = true) {}
      .testTag("month_start_note")
  ) {
    Text(
      text = header,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = if (ramadan) ink else Color(0xFF8A5A00)
    )
    Text(
      text = source,
      style = MaterialTheme.typography.bodySmall,
      fontSize = 11.sp,
      color = if (ramadan) ink.copy(alpha = 0.8f) else Color(0xFF8A5A00)
    )
  }
}
