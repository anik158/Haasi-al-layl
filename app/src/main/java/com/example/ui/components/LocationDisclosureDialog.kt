package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.ui.theme.DarkForestInk

/**
 * The prominent disclosure Google Play requires before the location permission prompt: what is
 * collected, why, who receives it, and that it never leaves the foreground. Shown in front of the
 * system dialog, never instead of it, and with a real "Not now".
 */
@Composable
fun LocationDisclosureDialog(
  language: AppLanguage,
  onContinue: () -> Unit,
  onNotNow: () -> Unit
) {
  val context = LocalContext.current
  AlertDialog(
    onDismissRequest = onNotNow,
    modifier = Modifier.testTag("location_disclosure_dialog"),
    icon = { Icon(Icons.Filled.LocationOn, contentDescription = null, tint = DarkForestInk) },
    title = {
      Text(
        text = LocalizationManager.getLocationDisclosureTitle(language),
        fontWeight = FontWeight.Bold,
        color = DarkForestInk
      )
    },
    text = {
      Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        LocalizationManager.getLocationDisclosurePoints(language).forEach { point ->
          Row(modifier = Modifier.padding(vertical = 3.dp)) {
            Text("•", color = DarkForestInk, modifier = Modifier.width(14.dp))
            Text(point, style = MaterialTheme.typography.bodyMedium, color = DarkForestInk)
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = LocalizationManager.getPrivacyPolicyLabel(language),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = DarkForestInk,
          textDecoration = TextDecoration.Underline,
          modifier = Modifier
            .clickable { DeveloperContact.openPrivacyPolicy(context) }
            .padding(vertical = 4.dp)
            .testTag("disclosure_privacy_link")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onContinue,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3E2F)),
        modifier = Modifier.testTag("disclosure_continue")
      ) {
        Text(LocalizationManager.getContinueLabel(language), color = Color.White)
      }
    },
    dismissButton = {
      TextButton(onClick = onNotNow, modifier = Modifier.testTag("disclosure_not_now")) {
        Text(LocalizationManager.getNotNowLabel(language), color = DarkForestInk)
      }
    }
  )
}
