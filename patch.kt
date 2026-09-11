@@ -10,12 +10,17 @@
 import androidx.compose.foundation.layout.Box
 import androidx.compose.foundation.layout.Column
+import androidx.compose.foundation.layout.ExperimentalLayoutApi
+import androidx.compose.foundation.layout.FlowRow
 import androidx.compose.foundation.layout.Row
 import androidx.compose.foundation.layout.Spacer
 import androidx.compose.foundation.layout.fillMaxWidth
 import androidx.compose.foundation.layout.height
 import androidx.compose.foundation.layout.padding
 import androidx.compose.foundation.layout.size
 import androidx.compose.foundation.layout.width
+import androidx.compose.foundation.rememberScrollState
+import androidx.compose.foundation.horizontalScroll
 import androidx.compose.foundation.lazy.LazyColumn
 import androidx.compose.foundation.shape.CircleShape
@@ -73,7 +78,7 @@
 import java.time.LocalTime
 
-@OptIn(ExperimentalMaterial3Api::class)
+@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
 @Composable
 fun SettingsSheet(
@@ -156,11 +161,11 @@
             Column {
               Text(
-                text = "Settings",
+                text = LocalizationManager.getSettingsTitle(language),
                 style = MaterialTheme.typography.titleLarge,
                 fontWeight = FontWeight.ExtraBold,
                 color = DarkForestInk
               )
               Text(
-                text = "Language, Madhab & Notifications",
+                text = LocalizationManager.getSettingsSubtitle(language),
                 style = MaterialTheme.typography.bodySmall,
                 color = DarkForestInk.copy(alpha = 0.7f)
               )
             }
           }
@@ -210,7 +215,7 @@
           Column(modifier = Modifier.padding(16.dp)) {
             Text(
-              text = "Language",
+              text = LocalizationManager.getLanguageLabel(language),
               fontWeight = FontWeight.Bold,
               fontSize = 15.sp,
               color = DarkForestInk
             )
             Spacer(modifier = Modifier.height(10.dp))
-            Row(
-              modifier = Modifier.fillMaxWidth(),
+            FlowRow(
+              modifier = Modifier.fillMaxWidth(),
               horizontalArrangement = Arrangement.spacedBy(8.dp),
+              verticalArrangement = Arrangement.spacedBy(8.dp)
             ) {
               AppLanguage.entries.forEach { lang ->
                 val isSelected = language == lang
                 Surface(
                   onClick = { onLanguageSelect(lang) },
                   shape = RoundedCornerShape(20.dp),
                   color = if (isSelected) Color(0xFF0F3E2F) else Color.White,
                   border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF0F3E2F) else OutlineDarkBorder),
-                  modifier = Modifier.weight(1f)
                 ) {
                   Text(
                     text = "${lang.flag} ${lang.displayName}",
                     fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                     fontSize = 12.sp,
                     color = if (isSelected) Color.White else DarkForestInk,
-                    modifier = Modifier.padding(vertical = 8.dp),
+                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                     textAlign = androidx.compose.ui.text.style.TextAlign.Center
                   )
                 }
               }
             }
@@ -254,12 +259,12 @@
           Column(modifier = Modifier.padding(16.dp)) {
             Text(
-              text = "Islamic Madhab",
+              text = LocalizationManager.getSelectMadhabTitle(language),
               fontWeight = FontWeight.Bold,
               fontSize = 15.sp,
               color = DarkForestInk
             )
             Text(
-              text = "Affects Asr prayer time calculation",
+              text = LocalizationManager.getSelectMadhabSubtitle(language),
               fontSize = 12.sp,
               color = DarkForestInk.copy(alpha = 0.75f)
             )
             Spacer(modifier = Modifier.height(10.dp))
-            Row(
-              modifier = Modifier.fillMaxWidth(),
+            FlowRow(
+              modifier = Modifier.fillMaxWidth(),
               horizontalArrangement = Arrangement.spacedBy(8.dp),
+              verticalArrangement = Arrangement.spacedBy(8.dp)
             ) {
               IslamicMadhab.entries.forEach { m ->
                 val isSelected = madhab == m
                 Surface(
                   onClick = { onMadhabSelect(m) },
                   shape = RoundedCornerShape(20.dp),
                   color = if (isSelected) Color(0xFF0F3E2F) else Color.White,
                   border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF0F3E2F) else OutlineDarkBorder),
-                  modifier = Modifier.weight(1f)
                 ) {
                   Text(
                     text = m.getLocalizedName(language),
                     fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                     fontSize = 12.sp,
                     color = if (isSelected) Color.White else DarkForestInk,
-                    modifier = Modifier.padding(vertical = 8.dp),
+                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                     textAlign = androidx.compose.ui.text.style.TextAlign.Center
                   )
                 }
