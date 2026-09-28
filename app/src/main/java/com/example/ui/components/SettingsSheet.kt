package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.localization.AppLanguage
import androidx.core.app.NotificationManagerCompat
import android.content.Intent
import com.example.data.notification.PrayerAlarmScheduler
import androidx.compose.material3.TextButton
import android.content.Context
import com.example.data.localization.LocalizationManager
import com.example.data.model.DailyPrayerSchedule
import com.example.data.notification.PrayerNotificationManager
import com.example.data.prayer.ForbiddenTimeWindow
import com.example.data.prayer.CalculationMethod
import com.example.data.prayer.IslamicMadhab
import com.example.ui.theme.DarkForestInk
import com.example.ui.theme.LinenBorderLight
import com.example.ui.theme.OutlineDarkBorder
import com.example.ui.theme.SunGoldenGlow
import com.example.ui.theme.WarmBeigeBackground
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsSheet(
  schedule: DailyPrayerSchedule?,
  language: AppLanguage,
  madhab: IslamicMadhab,
  method: CalculationMethod,
  onLanguageSelect: (AppLanguage) -> Unit,
  onMadhabSelect: (IslamicMadhab) -> Unit,
  onMethodSelect: (CalculationMethod) -> Unit,
  /** Re-arms the alarm schedule after a toggle that changes which alerts exist. */
  onSettingsChanged: () -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var prayerNotifsEnabled by remember {
    mutableStateOf(PrayerNotificationManager.isPrayerNotificationsEnabled(context))
  }
  var forbiddenNotifsEnabled by remember {
    mutableStateOf(PrayerNotificationManager.isForbiddenNotificationsEnabled(context))
  }
  var followUpEnabled by remember {
    mutableStateOf(PrayerNotificationManager.isFollowUpNotificationsEnabled(context))
  }
  var tahajjudEnabled by remember {
    mutableStateOf(PrayerNotificationManager.isTahajjudNotificationsEnabled(context))
  }

  var testSentMessage by remember { mutableStateOf<String?>(null) }
  // When the test alarm is due, so the confirmation can sit under the button with a countdown -
  // the old message went to the top of the sheet, out of sight of the button that was tapped.
  var testAlarmDueAt by remember { mutableStateOf<Long?>(null) }
  var testSecondsLeft by remember { mutableStateOf(0) }
  var showSatelliteSignal by remember { mutableStateOf(false) }

  if (showSatelliteSignal) {
    SatelliteSignalDialog(
      language = language,
      onDismiss = { showSatelliteSignal = false }
    )
  }

  // Permission launcher for Android 13+ (Tiramisu)
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      testSentMessage = LocalizationManager.getNotificationPermissionGranted(language)
    } else {
      testSentMessage = LocalizationManager.getEnableNotificationsInSettings(language)
    }
  }

  fun checkAndRequestPermission(onGranted: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val status = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
      if (status == PackageManager.PERMISSION_GRANTED) {
        onGranted()
      } else {
        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    } else {
      onGranted()
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color.White,
    modifier = Modifier.testTag("settings_sheet")
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F3E2F)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.Settings,
                contentDescription = null,
                tint = SunGoldenGlow,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = LocalizationManager.getSettingsTitle(language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = DarkForestInk
              )
              Text(
                text = LocalizationManager.getSettingsSubtitle(language),
                style = MaterialTheme.typography.bodySmall,
                color = DarkForestInk.copy(alpha = 0.7f)
              )
            }
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = LocalizationManager.getCloseLabel(language), tint = DarkForestInk)
          }
        }
      }

      // Test Notification Feedback Banner
      if (testSentMessage != null) {
        item {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFE8F5E9),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = testSentMessage ?: "",
                color = Color(0xFF1B5E20),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
              )
            }
          }
        }
      }

      // Language Selection
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmBeigeBackground),
          border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = LocalizationManager.getLanguageLabel(language),
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = DarkForestInk
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              AppLanguage.entries.forEach { lang ->
                val isSelected = language == lang
                Surface(
                  onClick = { onLanguageSelect(lang) },
                  shape = RoundedCornerShape(20.dp),
                  color = if (isSelected) Color(0xFF0F3E2F) else Color.White,
                  border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF0F3E2F) else OutlineDarkBorder),
                ) {
                  Text(
                    text = "${lang.flag} ${lang.nativeName}",
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp,
                    color = if (isSelected) Color.White else DarkForestInk,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                  )
                }
              }
            }
          }
        }
      }

      // Madhab Selection - first, because it is the choice that moves times the most.
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmBeigeBackground),
          border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = LocalizationManager.getSelectMadhabTitle(language),
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = DarkForestInk
            )
            Text(
              text = LocalizationManager.getSelectMadhabSubtitle(language),
              fontSize = 12.sp,
              color = DarkForestInk.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              IslamicMadhab.entries.forEach { m ->
                val isSelected = madhab == m
                Surface(
                  onClick = { onMadhabSelect(m) },
                  shape = RoundedCornerShape(20.dp),
                  color = if (isSelected) Color(0xFF0F3E2F) else Color.White,
                  border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF0F3E2F) else OutlineDarkBorder),
                ) {
                  Text(
                    text = m.getLocalizedName(language),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp,
                    color = if (isSelected) Color.White else DarkForestInk,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                  )
                }
              }
            }
          }
        }
      }

      // Prayer calculation method
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmBeigeBackground),
          border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = LocalizationManager.getSelectMethodTitle(language),
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = DarkForestInk
            )
            Text(
              text = LocalizationManager.getSelectMethodSubtitle(language),
              fontSize = 12.sp,
              color = DarkForestInk.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              CalculationMethod.entries.forEach { m ->
                val isSelected = method == m
                Surface(
                  onClick = { onMethodSelect(m) },
                  shape = RoundedCornerShape(20.dp),
                  color = if (isSelected) Color(0xFF0F3E2F) else Color.White,
                  border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF0F3E2F) else OutlineDarkBorder),
                ) {
                  Text(
                    text = m.getLocalizedShortName(language),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp,
                    color = if (isSelected) Color.White else DarkForestInk,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                  )
                }
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = method.getLocalizedName(language),
              fontSize = 11.sp,
              color = DarkForestInk.copy(alpha = 0.6f)
            )
          }
        }
      }

      // Hijri date adjustment: moon sighting starts the month a day later in much of the world,
      // and Ramadan - with Sehri and Iftar at the top of Today - has to start on the right day.
      if (com.example.FeatureFlags.SHOW_FASTING_TIMES) {
        item {
          val hijriPrefs = remember { com.example.data.preferences.UserPreferences(context) }
          // null = automatic, from the country.
          var hijriOverride by remember { mutableStateOf(hijriPrefs.hijriAdjustmentOverride) }
          val automaticDays = com.example.data.prayer.HijriDateHelper
            .automaticAdjustmentFor(hijriPrefs.selectedCity?.country)
          fun signed(days: Int) = LocalizationManager.localizeDigits(
            if (days > 0) "+$days" else days.toString(),
            language
          )
          fun chooseHijri(value: Int?) {
            hijriOverride = value
            hijriPrefs.hijriAdjustmentOverride = value
            // Ramadan moves Umm al-Qura's Isha interval, so the alarms re-arm.
            onSettingsChanged()
          }
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = WarmBeigeBackground),
            border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = LocalizationManager.getHijriAdjustmentLabel(language),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = DarkForestInk
              )
              Text(
                text = LocalizationManager.getHijriAdjustmentDesc(language),
                fontSize = 12.sp,
                color = DarkForestInk.copy(alpha = 0.75f)
              )
              Spacer(modifier = Modifier.height(10.dp))
              FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                // Automatic first, and the default: most people should never need the rest.
                Surface(
                  onClick = { chooseHijri(null) },
                  shape = RoundedCornerShape(20.dp),
                  color = if (hijriOverride == null) Color(0xFF0F3E2F) else Color.White,
                  border = androidx.compose.foundation.BorderStroke(1.dp, if (hijriOverride == null) Color(0xFF0F3E2F) else OutlineDarkBorder),
                  modifier = Modifier.testTag("hijri_adjust_auto")
                ) {
                  Text(
                    text = "${LocalizationManager.getAutomaticLabel(language)} (${signed(automaticDays)})",
                    fontWeight = if (hijriOverride == null) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp,
                    color = if (hijriOverride == null) Color.White else DarkForestInk,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                  )
                }
                (-2..2).forEach { days ->
                  val isSelected = hijriOverride == days
                  Surface(
                    onClick = { chooseHijri(days) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Color(0xFF0F3E2F) else Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF0F3E2F) else OutlineDarkBorder),
                    modifier = Modifier.testTag("hijri_adjust_$days")
                  ) {
                    Text(
                      text = signed(days),
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      fontSize = 13.sp,
                      color = if (isSelected) Color.White else DarkForestInk,
                      modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                  }
                }
              }
              Spacer(modifier = Modifier.height(8.dp))
              // Read after the chips, so it reflects the value just chosen.
              key(hijriOverride) {
                Text(
                  text = com.example.data.prayer.HijriDateHelper.getTodayHijriFormatted(
                    java.time.LocalDate.now(),
                    language
                  ),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = DarkForestInk.copy(alpha = 0.8f)
                )
              }
            }
          }
        }
      }

      // Notification reliability - the first thing to check when alerts do not arrive.
      item {
        val notificationsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
        val exactAlarmsAllowed = PrayerAlarmScheduler.canScheduleExactAlarms(context)
        val fullScreenAlarmAllowed = PrayerNotificationManager.canUseFullScreenAlarm(context)
        val batteryUnrestricted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          (context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager)
            .isIgnoringBatteryOptimizations(context.packageName)
        } else {
          true
        }
        val aggressiveOem = Build.MANUFACTURER.lowercase() in
          setOf("oppo", "realme", "oneplus", "xiaomi", "redmi", "poco", "vivo", "huawei", "honor")

        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmBeigeBackground),
          border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = LocalizationManager.getReliabilityTitle(language),
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = DarkForestInk
            )
            Spacer(modifier = Modifier.height(10.dp))

            ReliabilityRow(
              label = LocalizationManager.getReliabilityNotificationsRow(language),
              ok = notificationsAllowed,
              fixLabel = LocalizationManager.getReliabilityFixAction(language),
              onFix = {
                context.startActivity(
                  Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
              }
            )
            ReliabilityRow(
              label = LocalizationManager.getReliabilityExactAlarmsRow(language),
              ok = exactAlarmsAllowed,
              fixLabel = LocalizationManager.getReliabilityFixAction(language),
              onFix = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                  runCatching {
                    context.startActivity(
                      Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        .setData(android.net.Uri.fromParts("package", context.packageName, null))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                  }
                }
              }
            )
            // Whether the alert can take over the screen and ring, or is only a shade row.
            // Android 14 made this a user-granted permission, and when it is off the alarm
            // silently degrades to a notification - so the panel says so instead of leaving the
            // user to wonder why the alarm screen never appears.
            ReliabilityRow(
              label = LocalizationManager.getReliabilityFullScreenRow(language),
              ok = fullScreenAlarmAllowed,
              fixLabel = LocalizationManager.getReliabilityFixAction(language),
              onFix = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                  runCatching {
                    context.startActivity(
                      Intent(android.provider.Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                        .setData(android.net.Uri.fromParts("package", context.packageName, null))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                  }
                }
              }
            )
            ReliabilityRow(
              label = LocalizationManager.getReliabilityBatteryRow(language),
              ok = batteryUnrestricted,
              fixLabel = LocalizationManager.getReliabilityFixAction(language),
              onFix = {
                // The direct request, which is a single "Allow" instead of finding this app in a
                // list of every installed app. Play restricts ACTION_REQUEST_IGNORE_BATTERY_-
                // OPTIMIZATIONS to a short list of permitted use cases, of which "alarms and
                // reminders as core functionality" is one - and that is literally what this app
                // is; it holds USE_EXACT_ALARM for the same reason.
                //
                // Falls back to the full list if the direct dialog is refused by the OEM, which
                // some heavily skinned ROMs do.
                val direct = runCatching {
                  context.startActivity(
                    Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                      .setData(android.net.Uri.fromParts("package", context.packageName, null))
                      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                  )
                }
                if (direct.isFailure) {
                  runCatching {
                    context.startActivity(
                      Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                  }
                }
              }
            )

            if (aggressiveOem) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = LocalizationManager.getOemBatteryHint(language),
                fontSize = 11.sp,
                color = DarkForestInk.copy(alpha = 0.75f)
              )
            }

            // The satellite readout sits with the alarm test for the same reason: both turn a
            // vague "it did not work" into something specific enough to act on.
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
              onClick = { showSatelliteSignal = true },
              shape = RoundedCornerShape(14.dp),
              color = Color.White,
              border = androidx.compose.foundation.BorderStroke(1.dp, OutlineDarkBorder),
              modifier = Modifier.fillMaxWidth().testTag("btn_satellite_signal")
            ) {
              Text(
                text = LocalizationManager.getSatelliteSignalAction(language),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = DarkForestInk,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
              )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
              onClick = {
                PrayerAlarmScheduler.scheduleTestAlarm(context)
                testSecondsLeft = 60
                testAlarmDueAt = System.currentTimeMillis() + 60_000L
              },
              shape = RoundedCornerShape(14.dp),
              color = Color(0xFF0F3E2F),
              modifier = Modifier.fillMaxWidth().testTag("btn_test_alarm")
            ) {
              Text(
                text = LocalizationManager.getTestAlarmAction(language),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
              )
            }

            // Right under the button: that it worked, and when to expect the ring.
            testAlarmDueAt?.let { dueAt ->
              LaunchedEffect(dueAt) {
                while (true) {
                  testSecondsLeft = ((dueAt - System.currentTimeMillis() + 999) / 1000).toInt()
                    .coerceAtLeast(0)
                  if (testSecondsLeft == 0) break
                  kotlinx.coroutines.delay(1_000L)
                }
              }
              Spacer(modifier = Modifier.height(10.dp))
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0xFFE8F5E9))
                  .padding(12.dp)
                  .testTag("test_alarm_status"),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = Color(0xFF2E7D32),
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (testSecondsLeft > 0) {
                    LocalizationManager.getTestAlarmCountdown(testSecondsLeft, language)
                  } else {
                    LocalizationManager.getTestAlarmDue(language)
                  },
                  color = Color(0xFF1B5E20),
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 13.sp
                )
              }
            }
          }
        }
      }

      // Section 1: Prayer Start Notifications Toggle
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmBeigeBackground),
          border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = LocalizationManager.getPrayerStartAlertsLabel(language),
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = DarkForestInk
                )
                Text(
                  text = LocalizationManager.getPrayerStartAlertsDesc(language),
                  fontSize = 12.sp,
                  color = DarkForestInk.copy(alpha = 0.75f)
                )
              }
              Switch(
                checked = prayerNotifsEnabled,
                onCheckedChange = { checked ->
                  prayerNotifsEnabled = checked
                  PrayerNotificationManager.setPrayerNotificationsEnabled(context, checked)
                  onSettingsChanged()
                  if (checked) {
                    checkAndRequestPermission {}
                  }
                },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = Color(0xFF0F3E2F)
                ),
                modifier = Modifier.testTag("switch_prayer_notifs")
              )
            }
          }
        }
      }

      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmBeigeBackground),
          border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = LocalizationManager.getFollowUpAlertsLabel(language),
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = DarkForestInk
                )
                Text(
                  text = LocalizationManager.getFollowUpAlertsDesc(language),
                  fontSize = 12.sp,
                  color = DarkForestInk.copy(alpha = 0.75f)
                )
              }
              Switch(
                checked = followUpEnabled,
                onCheckedChange = { checked ->
                  followUpEnabled = checked
                  PrayerNotificationManager.setFollowUpNotificationsEnabled(context, checked)
                  onSettingsChanged()
                },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = Color(0xFF0F3E2F)
                ),
                modifier = Modifier.testTag("switch_follow_up")
              )
            }
          }
        }
      }

      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmBeigeBackground),
          border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = LocalizationManager.getTahajjudAlertsLabel(language),
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = DarkForestInk
                )
                Text(
                  text = LocalizationManager.getTahajjudAlertsDesc(language),
                  fontSize = 12.sp,
                  color = DarkForestInk.copy(alpha = 0.75f)
                )
              }
              Switch(
                checked = tahajjudEnabled,
                onCheckedChange = { checked ->
                  tahajjudEnabled = checked
                  PrayerNotificationManager.setTahajjudNotificationsEnabled(context, checked)
                  onSettingsChanged()
                },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = Color(0xFF0F3E2F)
                ),
                modifier = Modifier.testTag("switch_tahajjud")
              )
            }
          }
        }
      }

      // Section 2: Forbidden Prayer Times Notifications Toggle & Schedule
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmBeigeBackground),
          border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = LocalizationManager.getForbiddenAlertsLabel(language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = DarkForestInk
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFFF3E0),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFFFB74D))
                  ) {
                    Text(
                      text = LocalizationManager.getSunnahNaflLabel(language),
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFFE65100),
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                  }
                }
                Text(
                  text = LocalizationManager.getForbiddenAlertsDesc(language),
                  fontSize = 12.sp,
                  color = DarkForestInk.copy(alpha = 0.75f)
                )
              }
              Switch(
                checked = forbiddenNotifsEnabled,
                onCheckedChange = { checked ->
                  forbiddenNotifsEnabled = checked
                  PrayerNotificationManager.setForbiddenNotificationsEnabled(context, checked)
                  onSettingsChanged()
                  if (checked) {
                    checkAndRequestPermission {}
                  }
                },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = Color(0xFF0F3E2F)
                ),
                modifier = Modifier.testTag("switch_forbidden_notifs")
              )
            }
          }
        }
      }

      // Section 3: Today's 3 Forbidden Windows
      item {
        Text(
          text = LocalizationManager.getTodaysProhibitedWindows(language) + " (أوقات النهي)",
          fontWeight = FontWeight.ExtraBold,
          fontSize = 16.sp,
          color = DarkForestInk
        )
      }

      val forbiddenWindows = schedule?.forbiddenTimes ?: emptyList()
      val currentTime = schedule?.currentTime ?: LocalTime.now()

      if (forbiddenWindows.isEmpty()) {
        item {
          Text(
            text = LocalizationManager.getLoadingCelestialTimes(language),
            fontSize = 13.sp,
            color = DarkForestInk.copy(alpha = 0.6f)
          )
        }
      } else {
        items(forbiddenWindows.size) { index ->
          val window = forbiddenWindows[index]
          val isActive = window.isActive(currentTime)

          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isActive) Color(0xFFFFF8E1) else Color.White,
            border = androidx.compose.foundation.BorderStroke(
              width = if (isActive) 1.5.dp else 1.dp,
              color = if (isActive) Color(0xFFFFA000) else LinenBorderLight
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(CircleShape)
                      .background(if (isActive) Color(0xFFFFE082) else Color(0xFFECEFF1)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = if (isActive) Icons.Default.Warning else Icons.Default.Alarm,
                      contentDescription = null,
                      tint = if (isActive) Color(0xFFE65100) else DarkForestInk.copy(alpha = 0.6f),
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = window.getLocalizedTitle(language),
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp,
                      color = DarkForestInk
                    )
                    Text(
                      text = LocalizationManager.formatTimeRange(window.startTime, window.endTime, language),
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 12.sp,
                      color = if (isActive) Color(0xFFD84315) else DarkForestInk.copy(alpha = 0.7f)
                    )
                  }
                }

                if (isActive) {
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE65100)
                  ) {
                    Text(
                      text = LocalizationManager.getActiveNowBadge(language),
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 10.sp,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = window.getLocalizedDescription(language),
                fontSize = 11.sp,
                color = DarkForestInk.copy(alpha = 0.8f),
                lineHeight = 15.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = window.hadithReference,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = DarkForestInk.copy(alpha = 0.55f)
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }
}

@Composable
private fun ReliabilityRow(
  label: String,
  ok: Boolean,
  fixLabel: String,
  onFix: () -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = (if (ok) "✓  " else "⚠  ") + label,
      fontSize = 13.sp,
      color = if (ok) DarkForestInk else Color(0xFFB06A00),
      modifier = Modifier.weight(1f)
    )
    if (!ok) {
      TextButton(onClick = onFix) {
        Text(text = fixLabel, fontSize = 12.sp, color = Color(0xFF0F3E2F))
      }
    }
  }
}
