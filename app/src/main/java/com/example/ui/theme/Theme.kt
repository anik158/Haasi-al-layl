package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ForestSlatePrimaryDark,
    onPrimary = ForestSlateOnPrimaryDark,
    primaryContainer = ForestSlateContainerDark,
    onPrimaryContainer = ForestSlateOnContainerDark,
    secondary = WarmSandSecondaryDark,
    onSecondary = WarmSandOnSecondaryDark,
    secondaryContainer = WarmSandContainerDark,
    onSecondaryContainer = WarmSandOnContainerDark,
    tertiary = SageTertiaryDark,
    onTertiary = SageOnTertiaryDark,
    tertiaryContainer = SageContainerDark,
    onTertiaryContainer = SageOnContainerDark,
    background = MidnightPineBackgroundDark,
    onBackground = OnMidnightPineBackgroundDark,
    surface = DarkForestSurfaceDark,
    onSurface = OnDarkForestSurfaceDark,
    surfaceVariant = DarkForestVariantDark,
    onSurfaceVariant = OnDarkForestVariantDark,
    outline = DarkForestBorderDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ForestSlatePrimaryLight,
    onPrimary = ForestSlateOnPrimaryLight,
    primaryContainer = ForestSlateContainerLight,
    onPrimaryContainer = ForestSlateOnContainerLight,
    secondary = WarmSandSecondaryLight,
    onSecondary = WarmSandOnSecondaryLight,
    secondaryContainer = WarmSandContainerLight,
    onSecondaryContainer = WarmSandOnContainerLight,
    tertiary = SageTertiaryLight,
    onTertiary = SageOnTertiaryLight,
    tertiaryContainer = SageContainerLight,
    onTertiaryContainer = SageOnContainerLight,
    background = LinenBackgroundLight,
    onBackground = OnLinenBackgroundLight,
    surface = EggshellSurfaceLight,
    onSurface = OnEggshellSurfaceLight,
    surfaceVariant = ParchmentSurfaceVariantLight,
    onSurfaceVariant = OnParchmentSurfaceVariantLight,
    outline = LinenBorderLight
  )

@Composable
fun SakeenahTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Preserve bespoke organic brand palette by default
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

// Backward compatibility alias
@Composable
fun MuslimDayTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) = SakeenahTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)

