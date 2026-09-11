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
import androidx.compose.runtime.getValue
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

  var testSentMessage by remember { mutableStateOf<String?>(null) }

  // Permission launcher for Android 13+ (Tiramisu)
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      testSentMessage = "Notification permission granted!"
    } else {
      testSentMessage = "Please enable notifications in system settings."
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
            Icon(Icons.Default.Close, contentDescription = "Close", tint = DarkForestInk)
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
                    text = "${lang.flag} ${lang.displayName}",
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
                    text = m.shortTitle,
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
              text = method.title,
              fontSize = 11.sp,
              color = DarkForestInk.copy(alpha = 0.6f)
            )
          }
        }
      }

      // Madhab Selection
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
                      text = "Sunnah/Nafl",
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
            text = "Loading today's celestial times...",
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
                      text = window.formattedRange,
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
                      text = "ACTIVE NOW",
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
