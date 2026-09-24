package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.MenuBook
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
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.data.prayer.IslamicMadhab
import com.example.ui.theme.CoolMintGradientBase
import com.example.ui.theme.DarkForestInk
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LinenBorderLight
import com.example.ui.theme.OutlineDarkBorder
import com.example.ui.theme.WarmGoldAccent

@Composable
fun MadhabSelectionDialog(
  currentMadhab: IslamicMadhab,
  language: AppLanguage,
  onMadhabSelected: (IslamicMadhab) -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("madhab_selection_dialog"),
    containerColor = Color.White,
    shape = RoundedCornerShape(24.dp),
    icon = {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(CoolMintGradientBase)
          .border(1.dp, OutlineDarkBorder, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Outlined.MenuBook,
          contentDescription = null,
          tint = DarkForestInk,
          modifier = Modifier.size(24.dp)
        )
      }
    },
    title = {
      Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
          text = LocalizationManager.getSelectMadhabTitle(language),
          fontWeight = FontWeight.ExtraBold,
          style = MaterialTheme.typography.titleLarge,
          color = DarkForestInk
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = LocalizationManager.getSelectMadhabSubtitle(language),
          style = MaterialTheme.typography.bodySmall,
          color = DarkForestInk.copy(alpha = 0.7f),
          lineHeight = 16.sp
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        IslamicMadhab.entries.forEach { madhab ->
          val isSelected = currentMadhab == madhab
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isSelected) CoolMintGradientBase.copy(alpha = 0.85f) else Color(0xFFFAF7EE),
            border = androidx.compose.foundation.BorderStroke(
              width = if (isSelected) 1.5.dp else 1.dp,
              color = if (isSelected) OutlineDarkBorder else LinenBorderLight
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("madhab_item_${madhab.id}")
              .clickable {
                onMadhabSelected(madhab)
                onDismiss()
              }
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Text(
                    text = madhab.getLocalizedName(language),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = DarkForestInk
                  )

                  if (madhab.isDefault) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = GoldAccent.copy(alpha = 0.35f),
                      border = androidx.compose.foundation.BorderStroke(0.5.dp, OutlineDarkBorder)
                    ) {
                      Text(
                        text = LocalizationManager.getDefaultBadge(language),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkForestInk,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }
                }

                if (isSelected) {
                  Box(
                    modifier = Modifier
                      .size(24.dp)
                      .clip(CircleShape)
                      .background(DarkForestInk),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = LocalizationManager.getSelectedLabel(language),
                      tint = Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = madhab.arabicName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                color = DarkForestInk.copy(alpha = 0.8f)
              )

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = madhab.getLocalizedDescription(language),
                style = MaterialTheme.typography.labelSmall,
                color = DarkForestInk.copy(alpha = 0.65f),
                lineHeight = 15.sp
              )

              Spacer(modifier = Modifier.height(6.dp))

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) Color.White.copy(alpha = 0.7f) else Color.White,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, LinenBorderLight)
              ) {
                Text(
                  text = LocalizationManager.getAsrShadowRatioLabel(language, madhab.shadowFactor),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  color = DarkForestInk,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("madhab_dialog_close")
      ) {
        Text(LocalizationManager.getCloseLabel(language), color = DarkForestInk, fontWeight = FontWeight.Bold)
      }
    }
  )
}
