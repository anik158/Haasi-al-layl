package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
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
/**
 * The largest system font scale this layout is built to survive.
 *
 * Android lets the user go to 2.0 and beyond, and at those sizes compact chrome here stops
 * working: the header wraps to two lines and eats its own tagline, and the badges in the prayer
 * rows get squeezed until they wrap their letters vertically. Those specific rows have been made
 * to yield properly, but a ceiling is the honest general defence - every future row cannot be
 * relied on to be perfect.
 *
 * 1.3 rather than 1.0, deliberately. Clamping to 1.0 would throw away accessibility entirely;
 * 1.3 keeps most of the benefit for people who need larger text while staying inside what these
 * layouts can absorb. Body content is unaffected in practice because it wraps freely.
 */
private const val MAX_FONT_SCALE = 1.3f

@Composable
fun HaasiTheme(
  content: @Composable () -> Unit,
) {
  val density = LocalDensity.current
  val cappedDensity = remember(density.density, density.fontScale) {
    if (density.fontScale <= MAX_FONT_SCALE) {
      density
    } else {
      Density(density = density.density, fontScale = MAX_FONT_SCALE)
    }
  }

  CompositionLocalProvider(LocalDensity provides cappedDensity) {
    MaterialTheme(
      colorScheme = HaasiBrandColorScheme,
      typography = Typography,
      content = content
    )
  }
}



