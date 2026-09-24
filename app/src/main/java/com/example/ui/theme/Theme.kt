package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalConfiguration
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

/** The width the type scale was designed at - a typical modern phone. */
private const val REFERENCE_WIDTH_DP = 393f

/** How far text may shrink on the narrowest phones; below this it stops reading comfortably. */
private const val MIN_WIDTH_FACTOR = 0.85f

/**
 * Scales text with the screen's width, so compact chrome fits the phone it is on.
 *
 * At or above [REFERENCE_WIDTH_DP] this is 1.0 - it never enlarges, because the layouts are
 * phone-first and a tablet does not need bigger chrome. Below it text shrinks in proportion, down
 * to [MIN_WIDTH_FACTOR]: a 360dp phone gets about 0.92, a 320dp phone the 0.85 floor.
 *
 * This is the general answer to text that is fine at 393dp and truncates at 360dp. It is
 * applied as a font scale, so only `sp` sizes change; `dp` spacing and touch targets do not.
 */
fun responsiveFontFactor(screenWidthDp: Int): Float =
  (screenWidthDp / REFERENCE_WIDTH_DP).coerceIn(MIN_WIDTH_FACTOR, 1f)

@Composable
fun HaasiTheme(
  content: @Composable () -> Unit,
) {
  val density = LocalDensity.current
  val screenWidthDp = LocalConfiguration.current.screenWidthDp
  val cappedDensity = remember(density.density, density.fontScale, screenWidthDp) {
    // The user's own scale, capped, then fitted to the screen. Order matters: the cap bounds
    // what the layouts must absorb, and the width factor then fits that to this phone.
    val scale = minOf(density.fontScale, MAX_FONT_SCALE) * responsiveFontFactor(screenWidthDp)
    if (scale == density.fontScale) {
      density
    } else {
      Density(density = density.density, fontScale = scale)
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



