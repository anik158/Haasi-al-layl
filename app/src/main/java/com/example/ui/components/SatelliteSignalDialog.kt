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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.data.location.DeviceLocationProvider
import com.example.data.location.GnssDiagnostics
import com.example.ui.theme.DarkForestInk
import com.example.ui.theme.GoldAccent

/**
 * What the satellite receiver can hear, live, while this dialog is open.
 *
 * The point is to turn "no signal" into something a person can act on. A receiver reports three
 * different failures identically from the outside, and this separates them: nothing in view means
 * no sky, several in view but none used means the orbit data is still downloading and patience is
 * the fix, and four or more used means a position is imminent. Watching the used count climb
 * while walking to a window is the clearest possible demonstration that the app is working and
 * the building is the problem.
 *
 * The readout drives the GPS engine itself for as long as it is open - `GnssStatus` only reports
 * while something is asking for a position - so it is deliberately a dialog rather than a panel
 * that sits on a screen.
 */
@Composable
fun SatelliteSignalDialog(
  language: AppLanguage,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  val hasPermission = DeviceLocationProvider.hasPermission(context)
  val locationOn = DeviceLocationProvider.isLocationEnabled(context)
  val gpsOn = DeviceLocationProvider.isGpsProviderEnabled(context)

  val snapshot by produceState(
    initialValue = GnssDiagnostics.Snapshot.EMPTY,
    hasPermission, gpsOn
  ) {
    if (hasPermission && gpsOn) {
      GnssDiagnostics.observe(context).collect { value = it }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = LocalizationManager.getSatelliteSignalTitle(language),
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(modifier = Modifier.testTag("satellite_signal_dialog")) {
        StateRow(LocalizationManager.getSignalPermissionRow(language), hasPermission)
        StateRow(LocalizationManager.getSignalLocationRow(language), locationOn)
        StateRow(LocalizationManager.getSignalGpsRow(language), gpsOn)

        Spacer(modifier = Modifier.height(14.dp))

        if (!hasPermission || !gpsOn) {
          // Nothing to measure, and a row of zeroes would read as "no satellites" rather than
          // "not listening", which is the opposite of what this dialog is for.
          Text(
            text = LocalizationManager.getSignalNotListening(language),
            style = MaterialTheme.typography.bodySmall,
            color = DarkForestInk.copy(alpha = 0.7f)
          )
          return@Column
        }

        CountRow(
          value = snapshot.visible,
          label = LocalizationManager.getSignalVisibleLabel(language)
        )
        CountRow(
          value = snapshot.usedInFix,
          label = LocalizationManager.getSignalUsedLabel(language),
          highlight = snapshot.usedInFix >= 4
        )
        snapshot.bestCn0DbHz?.let { best ->
          CountRow(
            value = best.toInt(),
            label = LocalizationManager.getSignalStrongestLabel(language),
            suffix = " dB-Hz"
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SignalBars(snapshot.signals)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = interpretation(snapshot, language),
          style = MaterialTheme.typography.bodySmall,
          lineHeight = 18.sp,
          color = DarkForestInk.copy(alpha = 0.8f)
        )
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(LocalizationManager.getGotItLabel(language))
      }
    }
  )
}

/** The plain-language reading of the numbers, which is the entire point of showing them. */
private fun interpretation(
  snapshot: GnssDiagnostics.Snapshot,
  language: AppLanguage
): String = when {
  snapshot.usedInFix >= 4 -> LocalizationManager.getSignalVerdictFixing(language)
  snapshot.usable > 0 -> LocalizationManager.getSignalVerdictAcquiring(language)
  snapshot.visible > 0 -> LocalizationManager.getSignalVerdictWeak(language)
  else -> LocalizationManager.getSignalVerdictNothing(language)
}

@Composable
private fun StateRow(label: String, ok: Boolean) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(if (ok) Color(0xFF2E7D32) else Color(0xFFD32F2F))
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = DarkForestInk
    )
  }
}

@Composable
private fun CountRow(
  value: Int,
  label: String,
  suffix: String = "",
  highlight: Boolean = false
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = DarkForestInk.copy(alpha = 0.8f),
      modifier = Modifier.weight(1f, fill = false),
      maxLines = 2
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = "$value$suffix",
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
      color = if (highlight) Color(0xFF2E7D32) else DarkForestInk,
      maxLines = 1,
      softWrap = false
    )
  }
}

/**
 * One bar per satellite, tallest first.
 *
 * Scaled against 45 dB-Hz rather than the strongest signal present, so the bars mean the same
 * thing from one moment to the next - a display that renormalises to its own maximum would show
 * a full-height bar for a hopeless signal.
 */
@Composable
private fun SignalBars(signals: List<Float>) {
  if (signals.isEmpty()) return
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(46.dp)
      .testTag("signal_bars"),
    verticalAlignment = Alignment.Bottom,
    horizontalArrangement = Arrangement.spacedBy(2.dp)
  ) {
    signals.take(24).forEach { cn0 ->
      val fraction = (cn0 / 45f).coerceIn(0.06f, 1f)
      Box(
        modifier = Modifier
          .weight(1f)
          .height(46.dp * fraction)
          .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
          .background(
            when {
              cn0 >= 35f -> Color(0xFF2E7D32)
              cn0 >= GnssDiagnostics.USABLE_CN0_DB_HZ -> GoldAccent
              else -> DarkForestInk.copy(alpha = 0.25f)
            }
          )
      )
    }
  }
}
