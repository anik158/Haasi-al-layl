package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager

@Composable
fun WelcomeDialog(
    language: AppLanguage,
    onLanguageSelect: (AppLanguage) -> Unit,
    onOpenCitySelection: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        // The app's parchment, not Material's default dialog tint.
        containerColor = com.example.ui.theme.WarmBeigeBackground,
        title = { Text(LocalizationManager.getWelcomeSetupTitle(language)) },
        text = {
            Column {
                Text(LocalizationManager.getWelcomeSetupSubtitle(language))
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(LocalizationManager.getChooseLanguageLabel(language), style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                
                AppLanguage.entries.forEach { lang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { onLanguageSelect(lang) }) {
                            Text("${lang.flag} ${lang.nativeName} (${lang.displayName})")
                        }
                        if (language == lang) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onDismiss()
                onOpenCitySelection()
            }) {
                Text(LocalizationManager.getGotItLabel(language))
            }
        }
    )
}
