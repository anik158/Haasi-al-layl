package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.data.model.DailyPrayerSchedule
import com.example.data.prayer.HijriDateHelper
import com.example.ui.theme.CoolMintGradientBase
import com.example.ui.theme.DarkForestInk
import com.example.ui.theme.LinenBorderLight
import com.example.ui.theme.OutlineDarkBorder
import com.example.ui.theme.SunGoldenGlow
import com.example.ui.theme.SunRaysAura
import com.example.ui.theme.WarmBeigeBackground
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * CelestialHorizonCard
 * Displays a live celestial horizon where users can observe real-time Sun and Moon tracking,
 * day/night solar elevations, lunar phases, and sunrise/sunset trajectories.
 * Artistically styled with Ahsanul Houqe's warm beige (#F5EEDD) & cool mint (#D8EAE3) palette
 * and delicate botanical reeds.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CelestialHorizonCard(
  schedule: DailyPrayerSchedule?,
  modifier: Modifier = Modifier,
  language: AppLanguage = AppLanguage.ENGLISH,
  currentDate: LocalDate = schedule?.date ?: LocalDate.now(),
  currentTime: LocalTime = schedule?.currentTime ?: LocalTime.now()
) {
  var showDetailsDialog by remember { mutableStateOf(false) }

  // Lunar calculations
  val hijriDay = HijriDateHelper.getHijriDayOfMonth(currentDate).coerceIn(1, 30)
  val moonInfo = calculateMoonPhase(hijriDay)

  // Solar calculations
  val sunrise = schedule?.sunrise ?: LocalTime.of(6, 0)
  val sunset = schedule?.maghrib ?: LocalTime.of(18, 15)
  val noon = schedule?.dhuhr ?: LocalTime.of(12, 5)

  val isDaytime = currentTime.isAfter(sunrise) && currentTime.isBefore(sunset)
  val sunProgress = calculateSunProgress(currentTime, sunrise, sunset)
  val moonProgress = calculateMoonProgress(currentTime, sunset, sunrise)

  val infiniteTransition = rememberInfiniteTransition(label = "celestial_glow")
  val pulseAnim by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(2800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "sun_pulse"
  )

  // Animated lunar glow and halo
  val moonPulseAnim by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.22f,
    animationSpec = infiniteRepeatable(
      animation = tween(3200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "moon_pulse"
  )

  // Animated twinkling stars in the night sky
  val starTwinkleAnim by infiniteTransition.animateFloat(
    initialValue = 0.25f,
    targetValue = 0.95f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "star_twinkle"
  )

  Card(
    onClick = { showDetailsDialog = true },
    modifier = modifier
      .fillMaxWidth(0.94f)
      .testTag("celestial_horizon_card"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Header Section with Title and Badges
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(CoolMintGradientBase)
              .border(1.dp, OutlineDarkBorder, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isDaytime) Icons.Default.Brightness5 else Icons.Default.DarkMode,
              contentDescription = null,
              tint = if (isDaytime) SunGoldenGlow else DarkForestInk,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = LocalizationManager.getCelestialHorizonTitle(language),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = DarkForestInk
            )
            Text(
              text = LocalizationManager.getCelestialHorizonSubtitle(language),
              style = MaterialTheme.typography.labelSmall,
              color = DarkForestInk.copy(alpha = 0.7f)
            )
          }
        }

        // Live status pill with crisp white/dark border (clickable for celestial details)
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = Color.White,
          border = androidx.compose.foundation.BorderStroke(1.dp, OutlineDarkBorder),
          modifier = Modifier.testTag("celestial_sky_status_chip")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isDaytime) SunGoldenGlow else Color(0xFF6B8BA4))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isDaytime) LocalizationManager.getDaySkyLabel(language) else LocalizationManager.getNightSkyLabel(language),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = DarkForestInk
            )
          }
        }
      }

      // Horizon Sky Canvas
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(175.dp)
          .padding(horizontal = 12.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(
            brush = Brush.verticalGradient(
              colors = if (isDaytime) {
                listOf(
                  Color(0xFFF7FBF9),
                  Color(0xFFEAF5F0),
                  CoolMintGradientBase.copy(alpha = 0.75f)
                )
              } else {
                listOf(
                  Color(0xFF0C1920),
                  Color(0xFF132732),
                  Color(0xFF1B3743)
                )
              }
            )
          )
          .border(1.dp, LinenBorderLight, RoundedCornerShape(18.dp))
      ) {
        Canvas(modifier = Modifier.matchParentSize()) {
          val width = size.width
          val height = size.height
          val horizonY = height * 0.75f
          val arcPeakY = height * 0.18f

          val startX = 28.dp.toPx()
          val endX = width - 28.dp.toPx()
          val centerX = width / 2f

          // Twinkling Stars in the Night Sky
          if (!isDaytime) {
            val starPositions = listOf(
              Offset(width * 0.15f, height * 0.22f) to 2.2f,
              Offset(width * 0.28f, height * 0.14f) to 1.8f,
              Offset(width * 0.42f, height * 0.26f) to 2.5f,
              Offset(width * 0.58f, height * 0.12f) to 1.7f,
              Offset(width * 0.72f, height * 0.28f) to 2.3f,
              Offset(width * 0.85f, height * 0.18f) to 1.9f,
              Offset(width * 0.35f, height * 0.38f) to 1.5f,
              Offset(width * 0.65f, height * 0.35f) to 2.0f
            )
            starPositions.forEachIndexed { idx, (starPos, starRadius) ->
              val factor = if (idx % 2 == 0) starTwinkleAnim else (1.2f - starTwinkleAnim).coerceIn(0.2f, 1f)
              drawCircle(
                color = Color(0xFFF0FDF4).copy(alpha = factor * 0.85f),
                radius = starRadius.dp.toPx(),
                center = starPos
              )
              // Star cross shimmer
              if (idx % 3 == 0) {
                drawLine(
                  color = Color.White.copy(alpha = factor * 0.5f),
                  start = Offset(starPos.x - 3.dp.toPx(), starPos.y),
                  end = Offset(starPos.x + 3.dp.toPx(), starPos.y),
                  strokeWidth = 0.8.dp.toPx()
                )
                drawLine(
                  color = Color.White.copy(alpha = factor * 0.5f),
                  start = Offset(starPos.x, starPos.y - 3.dp.toPx()),
                  end = Offset(starPos.x, starPos.y + 3.dp.toPx()),
                  strokeWidth = 0.8.dp.toPx()
                )
              }
            }
          }

          // 1. Draw Celestial Sky Dome Arc (East to West)
          val arcPath = Path().apply {
            moveTo(startX, horizonY)
            quadraticTo(centerX, arcPeakY, endX, horizonY)
          }

          // Dashed sky trajectory guide
          drawPath(
            path = arcPath,
            color = if (isDaytime) DarkForestInk.copy(alpha = 0.22f) else Color(0xFFCBD5E1).copy(alpha = 0.35f),
            style = Stroke(
              width = 2.dp.toPx(),
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
              cap = StrokeCap.Round
            )
          )

          // 2. Solar Position & Sun Rendering
          if (isDaytime) {
            val t = sunProgress.coerceIn(0f, 1f)
            val sunPos = getQuadraticBezierPoint(startX, horizonY, centerX, arcPeakY, endX, horizonY, t)

            // Sun radiant aura
            drawCircle(
              color = SunRaysAura.copy(alpha = 0.25f * pulseAnim),
              radius = 24.dp.toPx() * pulseAnim,
              center = sunPos
            )
            drawCircle(
              color = SunRaysAura.copy(alpha = 0.5f),
              radius = 16.dp.toPx(),
              center = sunPos
            )
            // Sun core
            drawCircle(
              color = SunGoldenGlow,
              radius = 10.dp.toPx(),
              center = sunPos
            )
            drawCircle(
              color = Color.White,
              radius = 4.dp.toPx(),
              center = sunPos
            )
          }

          // 3. Moon Position & Animated Lunar Rendering
          val moonT = if (!isDaytime) moonProgress.coerceIn(0f, 1f) else ((sunProgress + 0.5f) % 1f)
          val moonPos = getQuadraticBezierPoint(startX, horizonY, centerX, arcPeakY, endX, horizonY, moonT)

          // Draw animated glowing moon (prominent at night, subtle during day)
          val moonAlpha = if (!isDaytime) 1f else 0.45f

          // Outer animated lunar pulsating aura
          drawCircle(
            color = if (!isDaytime) Color(0xFF81E6D9).copy(alpha = 0.22f * moonPulseAnim) else Color(0xFFE2E8F0).copy(alpha = 0.2f),
            radius = 24.dp.toPx() * moonPulseAnim,
            center = moonPos
          )
          drawCircle(
            color = if (!isDaytime) Color(0xFFE2E8F0).copy(alpha = 0.35f * moonAlpha) else Color(0xFFEDF2F7).copy(alpha = 0.25f),
            radius = 16.dp.toPx(),
            center = moonPos
          )

          // Moon body (Ethereal soft pearl with lunar gradient)
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF1F5F9),
                Color(0xFFCBD5E1)
              ),
              center = Offset(moonPos.x - 2.dp.toPx(), moonPos.y - 2.dp.toPx()),
              radius = 10.dp.toPx()
            ),
            radius = 9.dp.toPx(),
            center = moonPos
          )

          // Moon crater / lunar maria texture
          drawCircle(
            color = Color(0xFF94A3B8).copy(alpha = 0.35f * moonAlpha),
            radius = 2.4.dp.toPx(),
            center = Offset(moonPos.x - 3.dp.toPx(), moonPos.y - 1.5.dp.toPx())
          )
          drawCircle(
            color = Color(0xFF94A3B8).copy(alpha = 0.25f * moonAlpha),
            radius = 1.6.dp.toPx(),
            center = Offset(moonPos.x + 2.dp.toPx(), moonPos.y + 2.dp.toPx())
          )

          // 4. Ground Horizon Plane
          val groundPath = Path().apply {
            moveTo(0f, horizonY)
            lineTo(width, horizonY)
            lineTo(width, height)
            lineTo(0f, height)
            close()
          }
          drawPath(
            path = groundPath,
            brush = Brush.verticalGradient(
              colors = if (isDaytime) {
                listOf(
                  CoolMintGradientBase.copy(alpha = 0.85f),
                  Color(0xFFC7DFD6)
                )
              } else {
                listOf(
                  Color(0xFF152A34),
                  Color(0xFF0F1E26)
                )
              },
              startY = horizonY,
              endY = height
            )
          )

          // Horizon division stroke
          drawLine(
            color = if (isDaytime) DarkForestInk.copy(alpha = 0.35f) else Color(0xFF64748B).copy(alpha = 0.5f),
            start = Offset(0f, horizonY),
            end = Offset(width, horizonY),
            strokeWidth = 1.5.dp.toPx()
          )

          // 5. Botanical Reeds & Grass Silhouettes along the Horizon (Inspired by ahsanulhouqe.com)
          drawBotanicalReeds(width = width, horizonY = horizonY, height = height)
        }

        // East and West labels
        Text(
          text = LocalizationManager.getEastLabel(language),
          style = MaterialTheme.typography.labelSmall,
          fontSize = 10.sp,
          color = if (isDaytime) DarkForestInk.copy(alpha = 0.7f) else Color(0xFFE2E8F0),
          fontWeight = FontWeight.Bold,
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 14.dp, bottom = 4.dp)
        )

        Text(
          text = LocalizationManager.getWestLabel(language),
          style = MaterialTheme.typography.labelSmall,
          fontSize = 10.sp,
          color = if (isDaytime) DarkForestInk.copy(alpha = 0.7f) else Color(0xFFE2E8F0),
          fontWeight = FontWeight.Bold,
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 14.dp, bottom = 4.dp)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Bottom Glance Bar with Sun & Moon details
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp)
          .padding(bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Solar Info Pill
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp),
          color = Color.White,
          border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              Icons.Default.WbTwilight,
              contentDescription = null,
              tint = SunGoldenGlow,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              val solarTitle = if (isDaytime) {
                "${LocalizationManager.getNextSunsetLabel(language)} ${LocalizationManager.formatTime(sunset, language)}"
              } else {
                "${LocalizationManager.getNextSunriseLabel(language)} ${LocalizationManager.formatTime(sunrise, language)}"
              }
              val solarSub = if (isDaytime) {
                LocalizationManager.getSunsetMaghribSub(language)
              } else {
                LocalizationManager.getSunriseIshraqSub(language)
              }
              Text(
                text = solarTitle,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = DarkForestInk
              )
              Text(
                text = solarSub,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = DarkForestInk.copy(alpha = 0.7f)
              )
            }
          }
        }

        // Lunar Info Pill
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp),
          color = Color.White,
          border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              Icons.Default.DarkMode,
              contentDescription = null,
              tint = DarkForestInk,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = LocalizationManager.getMoonPhaseName(moonInfo.phaseKey, language),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = DarkForestInk
              )
              Text(
                text = LocalizationManager.getIlluminationLabel(moonInfo.illuminationPercent, language),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = DarkForestInk.copy(alpha = 0.7f)
              )
            }
          }
        }
      }
    }
  }

  if (showDetailsDialog) {
    AlertDialog(
      onDismissRequest = { showDetailsDialog = false },
      title = {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = LocalizationManager.getCelestialHorizonTitle(language) + " " + LocalizationManager.getLunarDetailsLabel(language).replace(":", ""),
            fontWeight = FontWeight.Bold,
            color = DarkForestInk
          )
          IconButton(onClick = { showDetailsDialog = false }) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = DarkForestInk)
          }
        }
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isDaytime) Color(0xFFFFF9C4) else Color(0xFFECEFF1),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (isDaytime) Icons.Default.Brightness5 else Icons.Default.DarkMode,
                contentDescription = null,
                tint = if (isDaytime) SunGoldenGlow else DarkForestInk
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = if (isDaytime) LocalizationManager.getDaytimeActiveLabel(language) else LocalizationManager.getNightSkyActiveLabel(language),
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = DarkForestInk
                )
                Text(
                  text = "${LocalizationManager.getTimeLabel(language)} ${LocalizationManager.formatTime(currentTime, language)}",
                  fontSize = 12.sp,
                  color = DarkForestInk.copy(alpha = 0.7f)
                )
              }
            }
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = WarmBeigeBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("☀️ ${LocalizationManager.getSolarMarkersLabel(language)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkForestInk)
              Text("• ${LocalizationManager.getPrayerDisplayName(com.example.data.model.PrayerName.SUNRISE, language)}: ${LocalizationManager.formatTime(sunrise, language)}", fontSize = 11.sp, color = DarkForestInk.copy(alpha = 0.8f))
              Text("• ${LocalizationManager.getPrayerDisplayName(com.example.data.model.PrayerName.DHUHR, language)}: ${LocalizationManager.formatTime(noon, language)}", fontSize = 11.sp, color = DarkForestInk.copy(alpha = 0.8f))
              Text("• ${LocalizationManager.getPrayerDisplayName(com.example.data.model.PrayerName.MAGHRIB, language)}: ${LocalizationManager.formatTime(sunset, language)}", fontSize = 11.sp, color = DarkForestInk.copy(alpha = 0.8f))
            }
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = WarmBeigeBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("🌙 ${LocalizationManager.getLunarDetailsLabel(language)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkForestInk)
              Text("• ${LocalizationManager.getHijriDayLabel(language)} $hijriDay (${LocalizationManager.getMoonPhaseName(moonInfo.phaseKey, language)})", fontSize = 11.sp, color = DarkForestInk.copy(alpha = 0.8f))
              Text("• ${LocalizationManager.getIlluminationLabel(moonInfo.illuminationPercent, language)}", fontSize = 11.sp, color = DarkForestInk.copy(alpha = 0.8f))
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showDetailsDialog = false }) {
          Text(LocalizationManager.getGotItLabel(language), color = DarkForestInk, fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}

/**
 * Draws artistic botanical reeds and grasses across the horizon
 */
private fun DrawScope.drawBotanicalReeds(width: Float, horizonY: Float, height: Float) {
  val grassColorDark = DarkForestInk.copy(alpha = 0.28f)
  val grassColorSage = Color(0xFF4A725D).copy(alpha = 0.35f)
  val stemStep = width / 18f

  for (i in 0..18) {
    val x = i * stemStep + (sin(i.toDouble() * 1.7).toFloat() * 6f)
    val reedHeight = 22.dp.toPx() + (cos(i.toDouble() * 2.3).toFloat() * 10.dp.toPx()).coerceAtLeast(8f)
    val tipX = x + sin(i.toDouble() * 0.8).toFloat() * 8.dp.toPx()
    val tipY = horizonY - reedHeight

    val reedPath = Path().apply {
      moveTo(x, horizonY + 8.dp.toPx())
      quadraticTo(x, horizonY - (reedHeight * 0.5f), tipX, tipY)
    }

    drawPath(
      path = reedPath,
      color = if (i % 2 == 0) grassColorSage else grassColorDark,
      style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
    )

    // Small reed leaf blade
    if (i % 3 != 0) {
      val leafLength = 10.dp.toPx()
      val leafDirection = if (i % 2 == 0) 1f else -1f
      val leafPath = Path().apply {
        moveTo(x, horizonY - (reedHeight * 0.4f))
        quadraticTo(
          x + (leafDirection * leafLength * 0.8f),
          horizonY - (reedHeight * 0.55f),
          x + (leafDirection * leafLength),
          horizonY - (reedHeight * 0.35f)
        )
      }
      drawPath(
        path = leafPath,
        color = grassColorSage,
        style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
      )
    }
  }
}

private fun getQuadraticBezierPoint(
  x0: Float, y0: Float,
  x1: Float, y1: Float,
  x2: Float, y2: Float,
  t: Float
): Offset {
  val oneMinusT = 1f - t
  val x = oneMinusT * oneMinusT * x0 + 2f * oneMinusT * t * x1 + t * t * x2
  val y = oneMinusT * oneMinusT * y0 + 2f * oneMinusT * t * y1 + t * t * y2
  return Offset(x, y)
}

private fun calculateSunProgress(now: LocalTime, sunrise: LocalTime, sunset: LocalTime): Float {
  val nowSec = now.toSecondOfDay()
  val riseSec = sunrise.toSecondOfDay()
  val setSec = sunset.toSecondOfDay()

  return if (nowSec in riseSec..setSec && setSec > riseSec) {
    (nowSec - riseSec).toFloat() / (setSec - riseSec)
  } else {
    0.5f
  }
}

private fun calculateMoonProgress(now: LocalTime, sunset: LocalTime, nextSunrise: LocalTime): Float {
  val nowSec = now.toSecondOfDay()
  val setSec = sunset.toSecondOfDay()
  val riseSec = nextSunrise.toSecondOfDay()

  val totalNightSec = (86400 - setSec) + riseSec
  val elapsed = if (nowSec >= setSec) {
    nowSec - setSec
  } else {
    (86400 - setSec) + nowSec
  }

  return (elapsed.toFloat() / totalNightSec.toFloat()).coerceIn(0f, 1f)
}

data class MoonPhaseData(
  val phaseKey: String,
  val phaseNameEn: String,
  val phaseNameBn: String,
  val illuminationPercent: Int
)

private fun calculateMoonPhase(hijriDay: Int): MoonPhaseData {
  return when (hijriDay) {
    1 -> MoonPhaseData("NEW_MOON", "Hilal (New Crescent)", "নতুন চাঁদ / হেলাল", 3)
    in 2..6 -> {
      val illum = (5 + (hijriDay - 2) * 8)
      MoonPhaseData("WAXING_CRESCENT", "Waxing Crescent", "শুক্ল কাস্তে চাঁদ", illum)
    }
    in 7..9 -> MoonPhaseData("FIRST_QUARTER", "First Quarter", "প্রথম চতুর্থাংশ", 50)
    in 10..13 -> {
      val illum = (60 + (hijriDay - 10) * 10).coerceAtMost(94)
      MoonPhaseData("WAXING_GIBBOUS", "Waxing Gibbous", "উজ্জ্বল কুঁজো চাঁদ", illum)
    }
    in 14..16 -> MoonPhaseData("FULL_MOON", "Full Moon (Badr)", "পূর্ণিমা / বদর", 100)
    in 17..21 -> {
      val illum = (92 - (hijriDay - 17) * 8).coerceAtLeast(55)
      MoonPhaseData("WANING_GIBBOUS", "Waning Gibbous", "কৃষ্ণ কুঁজো চাঁদ", illum)
    }
    in 22..24 -> MoonPhaseData("LAST_QUARTER", "Last Quarter", "শেষ চতুর্থাংশ", 50)
    in 25..28 -> {
      val illum = (40 - (hijriDay - 25) * 8).coerceAtLeast(6)
      MoonPhaseData("WANING_CRESCENT", "Waning Crescent", "কৃষ্ণ কাস্তে চাঁদ", illum)
    }
    else -> MoonPhaseData("DARK_MOON", "Dark Moon (Mahq)", "অমাবস্যা / মাহক", 1)
  }
}
