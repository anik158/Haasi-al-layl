@@ -115,7 +115,7 @@
               )
             }
             Text(
-              text = if (isError) "OFFLINE" else "LIVE",
+              text = if (isError) LocalizationManager.getOfflineStatus(language) else LocalizationManager.getLiveStatus(language),
               fontWeight = FontWeight.Bold,
               fontSize = 10.sp,
               color = if (isError) Color(0xFFC62828) else CoolMintGradientBase,
@@ -155,7 +155,7 @@
               Spacer(modifier = Modifier.width(6.dp))
               Text(
-                text = "H: $highFormatted  •  L: $lowFormatted",
+                text = "${LocalizationManager.getHighShort(language)}: $highFormatted  •  ${LocalizationManager.getLowShort(language)}: $lowFormatted",
                 style = MaterialTheme.typography.bodySmall,
                 color = Color.White.copy(alpha = 0.9f)
               )
@@ -272,7 +272,7 @@
           Icon(Icons.Default.CloudOff, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(32.dp))
           Spacer(modifier = Modifier.height(12.dp))
           Text(
-            text = "Weather data is currently unavailable. Please check your internet connection.",
+            text = LocalizationManager.getWeatherErrorDesc(language),
             color = Color.White.copy(alpha = 0.9f),
             fontSize = 12.sp,
             textAlign = TextAlign.Center
@@ -280,7 +280,7 @@
         }
       } else if (isLoading) {
         Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
           CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
           Spacer(modifier = Modifier.height(12.dp))
           Text(
-            text = "Loading weather information...",
+            text = LocalizationManager.getWeatherLoadingDesc(language),
             color = Color.White.copy(alpha = 0.9f),
             fontSize = 12.sp
           )
