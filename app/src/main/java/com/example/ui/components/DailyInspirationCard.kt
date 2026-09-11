package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dua.DailyInspiration
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.ui.theme.CoolMintGradientBase
import com.example.ui.theme.DarkForestInk
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LinenBorderLight
import com.example.ui.theme.OutlineDarkBorder

@Composable
fun DailyInspirationCard(
  inspiration: DailyInspiration,
  modifier: Modifier = Modifier,
  language: AppLanguage = AppLanguage.ENGLISH
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("inspiration_card"),
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
        .padding(18.dp)
    ) {
      // Ayah Section
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(GoldAccent.copy(alpha = 0.25f))
            .border(0.5.dp, OutlineDarkBorder, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            Icons.Outlined.AutoStories,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = DarkForestInk
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = LocalizationManager.getAyahOfTheDayTitle(language),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = DarkForestInk
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = inspiration.ayahArabic,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.End,
        lineHeight = 32.sp,
        color = DarkForestInk,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "“${inspiration.ayahTranslation}”",
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = 22.sp,
        color = DarkForestInk
      )

      Text(
        text = "— ${inspiration.ayahSurah}",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = DarkForestInk.copy(alpha = 0.7f),
        modifier = Modifier.padding(top = 4.dp)
      )

      Spacer(modifier = Modifier.height(16.dp))
      HorizontalDivider(color = LinenBorderLight)
      Spacer(modifier = Modifier.height(14.dp))

      // Hadith Section
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(CoolMintGradientBase)
            .border(0.5.dp, OutlineDarkBorder, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            Icons.Outlined.FormatQuote,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = DarkForestInk
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = LocalizationManager.getHadithOfTheDayTitle(language),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = DarkForestInk
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "“${inspiration.hadithTranslation}”",
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = 22.sp,
        color = DarkForestInk
      )

      Text(
        text = "— Narrated by ${inspiration.hadithNarrator} (${inspiration.hadithSource})",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = DarkForestInk.copy(alpha = 0.7f),
        modifier = Modifier.padding(top = 4.dp)
      )
    }
  }
}
