package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.ui.LocatingPhase
import com.example.ui.theme.GoldAccent
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * The screen the app holds while it works out where the user is - and, when that fails, the
 * screen that says which rung failed and what would fix it.
 *
 * Two things this has to get right, both learned the hard way on a device.
 *
 * **The wait is shown, not hidden.** A receiver with no almanac reads the ephemeris off the
 * satellites at 50 bits per second, so 30 to 60 seconds under open sky is normal and indoors it
 * may never complete. That cannot be engineered away, so the choice is between hiding it behind a
 * spinner - where it reads as a hang - and showing it. This is the second.
 *
 * **A failure is a diagnosis, not a dead end.** Every terminal [LocatingPhase] names the rung
 * that failed and offers the action that fixes it. The first version collapsed a refused
 * permission, a switched-off Location toggle and a genuine indoor no-fix into one manual city
 * picker, which made all three look identical to the app never having tried at all.
 *
 * Drawn on the same night navy as the launch window, so it continues the splash screen rather
 * than flashing parchment in between.
 *
 * @param elapsedSeconds counts up from zero; the honest measure of how long this has taken.
 * @param timeoutSeconds when the attempt gives up looking and starts listening in the background.
 */
@Composable
fun SatelliteAcquisitionScreen(
  phase: LocatingPhase,
  elapsedSeconds: Int,
  timeoutSeconds: Int,
  language: AppLanguage,
  onEnterManually: () -> Unit,
  onRetry: () -> Unit,
  onOpenLocationSettings: () -> Unit,
  onOpenAppSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(NightSky)
      .testTag("satellite_acquisition"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // The orbits keep turning through the failure phases too. The app really is still
      // listening in the background at that point, so a frozen illustration would be a lie.
      OrbitAnimation(modifier = Modifier.size(180.dp))

      Spacer(modifier = Modifier.height(32.dp))

      Text(
        text = titleFor(phase, language),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        color = StarlightText,
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("acquisition_title")
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = bodyFor(phase, language),
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = 21.sp,
        color = StarlightText.copy(alpha = 0.78f),
        textAlign = TextAlign.Center
      )

      // The counter belongs only to the phase that is actually measuring something. Showing
      // "0s / 90s" while the permission dialog was up was the first thing to look broken.
      if (phase == LocatingPhase.SEARCHING) {
        Spacer(modifier = Modifier.height(28.dp))

        Text(
          text = "${elapsedSeconds}s / ${timeoutSeconds}s",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = GoldAccent,
          modifier = Modifier.testTag("acquisition_counter")
        )

        Spacer(modifier = Modifier.height(10.dp))

        ElapsedTrack(
          fraction = if (timeoutSeconds <= 0) 0f else {
            (elapsedSeconds.toFloat() / timeoutSeconds).coerceIn(0f, 1f)
          },
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
          text = LocalizationManager.getLocatingHint(language),
          style = MaterialTheme.typography.bodySmall,
          lineHeight = 18.sp,
          color = StarlightText.copy(alpha = 0.6f),
          textAlign = TextAlign.Center
        )
      }

      // The action that would actually fix this phase, where there is one.
      primaryActionFor(phase, language)?.let { (label, action) ->
        Spacer(modifier = Modifier.height(28.dp))
        Button(
          onClick = when (action) {
            PrimaryAction.RETRY -> onRetry
            PrimaryAction.LOCATION_SETTINGS -> onOpenLocationSettings
            PrimaryAction.APP_SETTINGS -> onOpenAppSettings
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("acquisition_primary_action"),
          shape = RoundedCornerShape(26.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = GoldAccent,
            contentColor = NightSky
          )
        ) {
          Text(text = label, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Always present, in every phase: the wait is never a trap.
      TextButton(
        onClick = onEnterManually,
        modifier = Modifier.testTag("acquisition_skip")
      ) {
        Text(
          text = LocalizationManager.getEnterLocationManuallyAction(language),
          color = GoldAccent,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}

private enum class PrimaryAction { RETRY, LOCATION_SETTINGS, APP_SETTINGS }

private fun titleFor(phase: LocatingPhase, language: AppLanguage): String = when (phase) {
  LocatingPhase.REQUESTING_PERMISSION,
  LocatingPhase.SEARCHING -> LocalizationManager.getLocatingTitle(language)
  LocatingPhase.LOCATION_SERVICES_OFF -> LocalizationManager.getLocationOffTitle(language)
  LocatingPhase.PERMISSION_DENIED ->
    LocalizationManager.getLocationPermissionDeniedTitle(language)
  LocatingPhase.NO_FIX -> LocalizationManager.getNoFixTitle(language)
}

private fun bodyFor(phase: LocatingPhase, language: AppLanguage): String = when (phase) {
  LocatingPhase.REQUESTING_PERMISSION -> LocalizationManager.getLocationRationale(language)
  LocatingPhase.SEARCHING -> LocalizationManager.getLocatingBody(language)
  LocatingPhase.LOCATION_SERVICES_OFF -> LocalizationManager.getLocationOffBody(language)
  LocatingPhase.PERMISSION_DENIED ->
    LocalizationManager.getLocationPermissionDeniedBody(language)
  LocatingPhase.NO_FIX -> LocalizationManager.getNoFixBody(language)
}

private fun primaryActionFor(
  phase: LocatingPhase,
  language: AppLanguage
): Pair<String, PrimaryAction>? = when (phase) {
  // Nothing to offer: the system dialog is up, or the app is already waiting.
  LocatingPhase.REQUESTING_PERMISSION, LocatingPhase.SEARCHING -> null
  LocatingPhase.LOCATION_SERVICES_OFF ->
    LocalizationManager.getOpenSettingsAction(language) to PrimaryAction.LOCATION_SETTINGS
  LocatingPhase.PERMISSION_DENIED ->
    LocalizationManager.getOpenSettingsAction(language) to PrimaryAction.APP_SETTINGS
  LocatingPhase.NO_FIX ->
    LocalizationManager.getTryAgainAction(language) to PrimaryAction.RETRY
}

/**
 * Three satellites on inclined orbits around a glowing earth.
 *
 * Each orbit is an ellipse rotated into its own plane and swept at its own rate, which is what
 * keeps the whole thing from reading as one spinning wheel. Everything is one [Canvas] rather
 * than nested rotating layouts - there is no layout work to do here, only drawing, and a single
 * draw pass is what makes a 60fps animation free.
 */
@Composable
private fun OrbitAnimation(modifier: Modifier = Modifier) {
  val transition = rememberInfiniteTransition(label = "orbits")

  val sweeps = ORBITS.mapIndexed { index, orbit ->
    transition.animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      animationSpec = infiniteRepeatable(
        animation = tween(orbit.periodMillis, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
      ),
      label = "sweep$index"
    )
  }

  // Not a rotation: the globe breathes, so the centre is alive even at the instant every
  // satellite happens to be behind it.
  val pulse = transition.animateFloat(
    initialValue = 0.94f,
    targetValue = 1.06f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  Canvas(modifier = modifier) {
    val centre = Offset(size.width / 2f, size.height / 2f)
    val unit = min(size.width, size.height) / 2f

    // Orbits first, so the globe and its glow sit on top of the far side of each ring.
    ORBITS.forEachIndexed { index, orbit ->
      drawOrbit(centre, unit, orbit, sweeps[index].value)
    }

    val globeRadius = unit * 0.22f * pulse.value
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(GlobeGlow.copy(alpha = 0.55f), Color.Transparent),
        center = centre,
        radius = globeRadius * 2.6f
      ),
      radius = globeRadius * 2.6f,
      center = centre
    )
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(GlobeHighlight, GlobeCore),
        center = Offset(centre.x - globeRadius * 0.35f, centre.y - globeRadius * 0.35f),
        radius = globeRadius * 1.6f
      ),
      radius = globeRadius,
      center = centre
    )
  }
}

/** One inclined ring plus the satellite riding it. */
private fun DrawScope.drawOrbit(
  centre: Offset,
  unit: Float,
  orbit: Orbit,
  sweepDegrees: Float
) {
  val semiMajor = unit * orbit.radiusFraction
  val semiMinor = semiMajor * orbit.flattening

  rotate(degrees = orbit.inclinationDegrees, pivot = centre) {
    drawOval(
      color = OrbitLine,
      topLeft = Offset(centre.x - semiMajor, centre.y - semiMinor),
      size = Size(semiMajor * 2f, semiMinor * 2f),
      style = Stroke(width = 1.2f)
    )

    // The satellite's own position on the un-rotated ellipse; the enclosing rotate() carries it
    // into the orbital plane, so the dot always sits exactly on the line drawn above.
    val radians = Math.toRadians(sweepDegrees.toDouble())
    val position = Offset(
      centre.x + semiMajor * cos(radians).toFloat(),
      centre.y + semiMinor * sin(radians).toFloat()
    )

    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(GoldAccent.copy(alpha = 0.45f), Color.Transparent),
        center = position,
        radius = unit * 0.1f
      ),
      radius = unit * 0.1f,
      center = position
    )
    drawCircle(color = SatelliteBody, radius = unit * 0.028f, center = position)
  }
}

private data class Orbit(
  /** Semi-major axis as a fraction of the canvas radius. */
  val radiusFraction: Float,
  /** Semi-minor over semi-major: how edge-on the ring is seen. */
  val flattening: Float,
  /** How far the orbital plane is tilted in the picture. */
  val inclinationDegrees: Float,
  /** One full revolution. Deliberately coprime-ish, so the three never lock into step. */
  val periodMillis: Int
)

private val ORBITS = listOf(
  Orbit(radiusFraction = 0.94f, flattening = 0.34f, inclinationDegrees = -18f, periodMillis = 5200),
  Orbit(radiusFraction = 0.74f, flattening = 0.28f, inclinationDegrees = 62f, periodMillis = 3900),
  Orbit(radiusFraction = 0.56f, flattening = 0.42f, inclinationDegrees = 128f, periodMillis = 6700)
)

/** The elapsed bar. Not a progress bar: it measures patience spent, not work completed. */
@Composable
private fun ElapsedTrack(fraction: Float, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .height(3.dp)
      .background(StarlightText.copy(alpha = 0.18f))
  ) {
    // fillMaxWidth documents its fraction as (0, 1]; at zero seconds there is simply no bar.
    if (fraction > 0f) {
      Box(
        modifier = Modifier
          .fillMaxWidth(fraction)
          .height(3.dp)
          .background(GoldAccent.copy(alpha = 0.85f))
      )
    }
  }
}

// Matches @color/haasi_night_navy, so this screen continues the launch window rather than
// cutting to a different background.
private val NightSky = Color(0xFF090F28)
private val StarlightText = Color(0xFFEAEFF7)
private val OrbitLine = Color(0x33A8C3E8)
private val SatelliteBody = Color(0xFFF6E7C1)
private val GlobeCore = Color(0xFF1B3A5C)
private val GlobeHighlight = Color(0xFF4E86B8)
private val GlobeGlow = Color(0xFF2E6FA8)
