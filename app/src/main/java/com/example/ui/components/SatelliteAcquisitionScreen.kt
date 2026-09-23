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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.ui.LocatingPhase
import com.example.ui.theme.GoldAccent
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
  onEnablePositioning: () -> Unit,
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
      // The animation keeps running through the failure phases too. The app really is still
      // listening in the background at that point, so a frozen illustration would be a lie.
      SignalSearchAnimation(
        modifier = Modifier
          .fillMaxWidth()
          .height(190.dp)
      )

      // STARTING is the animation and nothing else - a continuation of the launch screen. An
      // online first run resolves by IP in well under a second, and flashing "turn on location"
      // at someone whose location the network has already answered is how the previous version
      // earned the complaint.
      if (phase == LocatingPhase.STARTING) return@Column

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
            PrimaryAction.ENABLE_POSITIONING -> onEnablePositioning
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

private enum class PrimaryAction { RETRY, ENABLE_POSITIONING, APP_SETTINGS }

private fun titleFor(phase: LocatingPhase, language: AppLanguage): String = when (phase) {
  LocatingPhase.STARTING,
  LocatingPhase.REQUESTING_PERMISSION,
  LocatingPhase.SEARCHING -> LocalizationManager.getLocatingTitle(language)
  LocatingPhase.LOCATION_SERVICES_OFF -> LocalizationManager.getLocationOffTitle(language)
  LocatingPhase.GPS_PROVIDER_OFF -> LocalizationManager.getGpsOffTitle(language)
  LocatingPhase.PERMISSION_DENIED ->
    LocalizationManager.getLocationPermissionDeniedTitle(language)
  LocatingPhase.NO_FIX -> LocalizationManager.getNoFixTitle(language)
}

private fun bodyFor(phase: LocatingPhase, language: AppLanguage): String = when (phase) {
  LocatingPhase.STARTING -> ""
  LocatingPhase.REQUESTING_PERMISSION -> LocalizationManager.getLocationRationale(language)
  LocatingPhase.SEARCHING -> LocalizationManager.getLocatingBody(language)
  LocatingPhase.LOCATION_SERVICES_OFF -> LocalizationManager.getLocationOffBody(language)
  LocatingPhase.GPS_PROVIDER_OFF -> LocalizationManager.getGpsOffBody(language)
  LocatingPhase.PERMISSION_DENIED ->
    LocalizationManager.getLocationPermissionDeniedBody(language)
  LocatingPhase.NO_FIX -> LocalizationManager.getNoFixBody(language)
}

private fun primaryActionFor(
  phase: LocatingPhase,
  language: AppLanguage
): Pair<String, PrimaryAction>? = when (phase) {
  // Nothing to offer: still deciding, the system dialog is up, or the app is already waiting.
  LocatingPhase.STARTING,
  LocatingPhase.REQUESTING_PERMISSION,
  LocatingPhase.SEARCHING -> null
  // Both of these go through the same one-tap positioning dialog. The master toggle and the GPS
  // provider are different switches, but the dialog turns on whatever is needed for high
  // accuracy, so there is no reason to make the user work out which one they are missing.
  LocatingPhase.LOCATION_SERVICES_OFF, LocatingPhase.GPS_PROVIDER_OFF ->
    LocalizationManager.getEnablePositioningAction(language) to PrimaryAction.ENABLE_POSITIONING
  LocatingPhase.PERMISSION_DENIED ->
    LocalizationManager.getOpenSettingsAction(language) to PrimaryAction.APP_SETTINGS
  LocatingPhase.NO_FIX ->
    LocalizationManager.getTryAgainAction(language) to PrimaryAction.RETRY
}

/**
 * A satellite passing overhead, beaming down to a house on the ground.
 *
 * This replaced three abstract orbits around a glowing sphere. That picture was pleasant and told
 * the user nothing: on a device it read as a loading spinner with extra steps, or as an atom. The
 * thing being waited for is a *signal reaching where you are*, so the picture is a satellite, a
 * beam, and a house - which needs no caption to be understood.
 *
 * The beam pulses from satellite to roof rather than glowing steadily, because the wait is an
 * attempt being repeated, not a process filling up. Stars drift at a different rate again so the
 * whole frame never looks like one rotating object.
 *
 * All one [Canvas]: there is no layout work here, only drawing, and a single draw pass is what
 * makes a 60fps animation cost nothing.
 */
@Composable
private fun SignalSearchAnimation(modifier: Modifier = Modifier) {
  val transition = rememberInfiniteTransition(label = "signal")

  // The satellite tracks across the sky and starts again, the way a real pass does.
  val pass = transition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(9000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pass"
  )

  // The beam: one pulse travelling down, repeatedly. Faster than the pass, and not a divisor of
  // it, so the two never settle into a visible rhythm.
  val beam = transition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1700, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "beam"
  )

  val twinkle = transition.animateFloat(
    initialValue = 0.25f,
    targetValue = 0.9f,
    animationSpec = infiniteRepeatable(
      animation = tween(2600, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "twinkle"
  )

  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val groundY = h * 0.84f

    drawStars(w, h, twinkle.value)

    // The satellite arcs from left to right, dipping at the edges so the path reads as an orbit
    // seen from below rather than a straight line.
    val t = pass.value
    val satX = w * (0.08f + 0.84f * t)
    val arc = sin(Math.PI * t).toFloat()
    val satY = h * (0.34f - 0.16f * arc)

    val houseX = w * 0.5f
    val roofPeakY = groundY - h * 0.17f

    drawBeam(
      from = Offset(satX, satY),
      to = Offset(houseX, roofPeakY),
      progress = beam.value
    )

    drawGround(w, h, groundY)
    drawHouse(houseX, groundY, h)
    drawSatellite(Offset(satX, satY), min(w, h))
  }
}

/** A faint starfield, so the sky is a sky and not an empty rectangle. */
private fun DrawScope.drawStars(w: Float, h: Float, alpha: Float) {
  STAR_POSITIONS.forEachIndexed { index, (fx, fy) ->
    // Alternating the alpha means half the stars brighten while the other half dim, which reads
    // as twinkling rather than as the whole sky pulsing.
    val a = if (index % 2 == 0) alpha else (1.15f - alpha)
    drawCircle(
      color = StarlightText.copy(alpha = (a * 0.5f).coerceIn(0f, 1f)),
      radius = if (index % 3 == 0) 1.6f else 1.1f,
      center = Offset(w * fx, h * fy)
    )
  }
}

/**
 * The signal itself: a bright head travelling from satellite to roof, with a fading tail.
 *
 * Drawn as a short run of dots rather than a gradient line because a gradient reads as a static
 * beam, and the point is that something is being *sent* and may or may not arrive.
 */
private fun DrawScope.drawBeam(from: Offset, to: Offset, progress: Float) {
  // The faint guide line the pulse travels along.
  drawLine(
    color = GoldAccent.copy(alpha = 0.16f),
    start = from,
    end = to,
    strokeWidth = 1.5f
  )

  val head = progress
  repeat(BEAM_TAIL) { i ->
    val p = head - i * 0.055f
    if (p < 0f || p > 1f) return@repeat
    val point = Offset(
      from.x + (to.x - from.x) * p,
      from.y + (to.y - from.y) * p
    )
    val fade = (1f - i / BEAM_TAIL.toFloat()).coerceIn(0f, 1f)
    drawCircle(
      color = GoldAccent.copy(alpha = 0.75f * fade),
      radius = (3.2f * fade).coerceAtLeast(1f),
      center = point
    )
  }
}

/** The horizon, as a soft band rather than a hard line. */
private fun DrawScope.drawGround(w: Float, h: Float, groundY: Float) {
  drawRect(
    brush = Brush.verticalGradient(
      colors = listOf(GroundNear.copy(alpha = 0f), GroundNear),
      startY = groundY - h * 0.06f,
      endY = h
    ),
    topLeft = Offset(0f, groundY - h * 0.06f),
    size = Size(w, h - groundY + h * 0.06f)
  )
  drawLine(
    color = StarlightText.copy(alpha = 0.22f),
    start = Offset(0f, groundY),
    end = Offset(w, groundY),
    strokeWidth = 1.2f
  )
}

/** A house: the "you are here" of the picture, and why the beam has somewhere to land. */
private fun DrawScope.drawHouse(centreX: Float, groundY: Float, h: Float) {
  val bodyW = h * 0.20f
  val bodyH = h * 0.12f
  val roofH = h * 0.075f
  val left = centreX - bodyW / 2f
  val bodyTop = groundY - bodyH

  drawRect(
    color = HouseWall,
    topLeft = Offset(left, bodyTop),
    size = Size(bodyW, bodyH)
  )

  val roof = Path().apply {
    moveTo(left - bodyW * 0.12f, bodyTop)
    lineTo(centreX, bodyTop - roofH)
    lineTo(left + bodyW + bodyW * 0.12f, bodyTop)
    close()
  }
  drawPath(roof, color = HouseRoof)

  // A lit window, so the house reads as somebody's home rather than a shape.
  val winSize = bodyW * 0.26f
  drawRect(
    color = GoldAccent.copy(alpha = 0.85f),
    topLeft = Offset(centreX - winSize / 2f, bodyTop + bodyH * 0.22f),
    size = Size(winSize, winSize)
  )
}

/** The satellite: a body, two panels, and a dish facing down. */
private fun DrawScope.drawSatellite(centre: Offset, unit: Float) {
  val body = unit * 0.055f
  val panel = unit * 0.075f
  val panelH = unit * 0.032f

  drawRect(
    color = SatellitePanel,
    topLeft = Offset(centre.x - body / 2f - panel, centre.y - panelH / 2f),
    size = Size(panel, panelH)
  )
  drawRect(
    color = SatellitePanel,
    topLeft = Offset(centre.x + body / 2f, centre.y - panelH / 2f),
    size = Size(panel, panelH)
  )
  drawRect(
    color = SatelliteBody,
    topLeft = Offset(centre.x - body / 2f, centre.y - body / 2f),
    size = Size(body, body)
  )
  // The dish, pointing at the house below.
  drawCircle(
    color = SatelliteBody,
    radius = body * 0.34f,
    center = Offset(centre.x, centre.y + body * 0.62f)
  )
  // A halo, so the satellite stays legible against the starfield.
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(GoldAccent.copy(alpha = 0.30f), Color.Transparent),
      center = centre,
      radius = unit * 0.11f
    ),
    radius = unit * 0.11f,
    center = centre
  )
}

/** Fixed positions, so the sky does not reshuffle itself on every recomposition. */
private val STAR_POSITIONS = listOf(
  0.08f to 0.12f, 0.19f to 0.28f, 0.31f to 0.08f, 0.44f to 0.20f,
  0.57f to 0.10f, 0.68f to 0.26f, 0.79f to 0.14f, 0.91f to 0.30f,
  0.13f to 0.42f, 0.86f to 0.46f, 0.26f to 0.52f, 0.73f to 0.55f
)

/** How many trailing dots follow the head of the beam. */
private const val BEAM_TAIL = 7

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
private val SatelliteBody = Color(0xFFF6E7C1)
private val SatellitePanel = Color(0xFF4E86B8)
// The ground reads as a dark silhouette against the night sky rather than as lit terrain: it is
// scenery, and anything brighter competes with the beam, which is the thing to watch.
private val GroundNear = Color(0xFF0B1330)
private val HouseWall = Color(0xFF1B2A52)
private val HouseRoof = Color(0xFF2C4172)
