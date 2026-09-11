package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Haasi al-layl signature brand color scheme
// Main background: #F5EEDD (Warm light beige)
// Gradient base: #D8EAE3 (Cool mint-green tint)
// Text & Borders: #1C2A24 (Deep forest charcoal) & #FFFFFF
private val HaasiBrandColorScheme =
  lightColorScheme(
    primary = DarkForestInk,
    onPrimary = Color.White,
    primaryContainer = MintBadgeContainer,
    onPrimaryContainer = DarkForestInk,
    secondary = WarmSandSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = WarmSandContainerLight,
    onSecondaryContainer = DarkForestInk,
    tertiary = SageTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = MintBadgeContainer,
    onTertiaryContainer = DarkForestInk,
    background = WarmBeigeBackground,
    onBackground = DarkForestInk,
    surface = PureWhiteSurface,
    onSurface = DarkForestInk,
    surfaceVariant = SoftBeigeSurface,
    onSurfaceVariant = DarkForestInk,
    outline = OutlineDarkBorder
  )

@Composable
fun HaasiTheme(
  darkTheme: Boolean = false, // Strictly preserve signature warm beige brand palette
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = HaasiBrandColorScheme,
    typography = Typography,
    content = content
  )
}

// Aliases for compatibility
@Composable
fun SakeenahTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) = HaasiTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)

@Composable
fun MuslimDayTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) = HaasiTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)



