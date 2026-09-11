package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.ui.theme.DarkForestInk
import com.example.ui.theme.LinenBorderLight
import com.example.ui.theme.SunGoldenGlow
import com.example.ui.theme.WarmBeigeBackground

/**
 * HaasiBrandLogo
 * Renders the custom Haasi al-Layl logo (golden crescent moon, steaming ornate cup, celestial stardust,
 * calligraphy and graceful hand) in a luxurious emerald and gold medal format.
 */
@Composable
fun HaasiBrandLogo(
  modifier: Modifier = Modifier,
  size: Dp = 40.dp,
  onClick: (() -> Unit)? = null
) {
  val clickModifier = if (onClick != null) {
    Modifier.clickable(onClick = onClick)
  } else Modifier

  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(
        brush = Brush.radialGradient(
          colors = listOf(
            Color(0xFF0F3E2F),
            Color(0xFF09251C),
            Color(0xFF04150F)
          )
        )
      )
      .border(
        width = 1.2.dp,
        brush = Brush.linearGradient(
          colors = listOf(
            Color(0xFFFFEE99),
            Color(0xFFD4AF37),
            Color(0xFF996515)
          )
        ),
        shape = CircleShape
      )
      .then(clickModifier)
      .testTag("haasi_brand_logo"),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      painter = painterResource(id = R.drawable.ic_haasi_logo),
      contentDescription = "Ḥaasi al-Layl Logo",
      tint = Color.Unspecified,
      modifier = Modifier
        .size(size * 0.78f)
    )
  }
}

/**
 * AboutHaasiDialog
 * Displays the authentic origin, meaning, and poetic depth of Ḥaasi al-Layl (حَاسِي اللَّيْل),
 * showcasing the brand artwork and giving users an intimate appreciation of the app.
 */
@Composable
fun AboutHaasiDialog(
  language: AppLanguage,
  onDismiss: () -> Unit,
  onOpenNotifications: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("about_haasi_dialog"),
    title = null,
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Spacer(modifier = Modifier.weight(1f))
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = DarkForestInk)
          }
        }

        HaasiBrandLogo(size = 72.dp)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "حَاسِي اللَّيْل",
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFB38012),
          fontFamily = FontFamily.Serif
        )

        Text(
          text = "Ḥaasi al-Layl",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.ExtraBold,
          color = DarkForestInk
        )

        Text(
          text = "“The One Who Sips the Night”",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = DarkForestInk.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = WarmBeigeBackground),
          border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Poetic & Spiritual Context:",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = DarkForestInk
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Ḥaasi (حَاسِي) comes from the Arabic verb ḥasā (حَسَا), meaning to sip a liquid slowly with contemplation and reverence. Al-Layl (اللَّيْل) is the night.",
              fontSize = 12.sp,
              color = DarkForestInk.copy(alpha = 0.85f),
              lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "It describes the devout worshipper who drinks in the spiritual nectar of the late hours — Tahajjud, quiet reflection, and the serenity before dawn — with gradual, deliberate mindfulness.",
              fontSize = 12.sp,
              color = DarkForestInk.copy(alpha = 0.85f),
              lineHeight = 17.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
          onClick = {
            onDismiss()
            onOpenNotifications()
          },
          shape = RoundedCornerShape(14.dp),
          color = Color(0xFF0F3E2F),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = SunGoldenGlow, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Configure Prayer & Forbidden Time Alerts",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
              )
              Text(
                text = "Receive notifications when prayers and prohibited windows start/end",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.8f)
              )
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Close", color = DarkForestInk, fontWeight = FontWeight.Bold)
      }
    }
  )
}
