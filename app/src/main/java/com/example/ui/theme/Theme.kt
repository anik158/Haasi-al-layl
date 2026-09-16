package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Haasi al-Layl signature brand colour scheme
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

/**
 * The app is deliberately light-only: the warm parchment palette is the brand, so there is no
 * dark scheme and no dynamic colour. The theme took `darkTheme` and `dynamicColor` parameters and
 * silently ignored both, which read as a bug; they are gone rather than honoured.
 */
@Composable
fun HaasiTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = HaasiBrandColorScheme,
    typography = Typography,
    content = content
  )
}



