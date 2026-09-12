package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.data.model.CityLocation
import com.example.ui.theme.WarmGoldAccent
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(
  currentCity: CityLocation,
  compassHeading: Float,
  qiblaBearing: Float,
  distanceToKaabaKm: Int,
  isFacingQibla: Boolean,
  magneticDeclination: Float,
  needsCalibration: Boolean,
  onLocationClick: () -> Unit,
  language: AppLanguage = AppLanguage.ENGLISH
) {
  // Needle angle relative to device top:
  // When device faces Qibla, needle points straight up (0 degrees).
  val needleAngle = (qiblaBearing - compassHeading + 360f) % 360f

  // Animating a wrapped angle directly makes a 359 -> 1 step spin the dial 358 degrees backwards.
  // Accumulate the shortest signed delta instead, so the spring always takes the short way round.
  var headingTarget by remember { mutableFloatStateOf(-compassHeading) }
  LaunchedEffect(compassHeading) {
    headingTarget += shortestDelta(headingTarget, -compassHeading)
  }
  val animatedHeading by animateFloatAsState(
    targetValue = headingTarget,
    animationSpec = spring(stiffness = 50f),
    label = "compass_rotation"
  )

  var needleTarget by remember { mutableFloatStateOf(needleAngle) }
  LaunchedEffect(needleAngle) {
    needleTarget += shortestDelta(needleTarget, needleAngle)
  }
  val animatedNeedleAngle by animateFloatAsState(
    targetValue = needleTarget,
    animationSpec = spring(stiffness = 50f),
    label = "needle_rotation"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = LocalizationManager.getQiblaTitle(language),
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Surface(
          modifier = Modifier.padding(top = 2.dp),
          color = Color.Transparent
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              Icons.Default.LocationOn,
              contentDescription = null,
              modifier = Modifier.size(14.dp),
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${currentCity.name}, ${currentCity.country}",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }

      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
      ) {
        Text(
          text = "%.0f° ${LocalizationManager.getTowardsKaabaLabel(language)}".format(qiblaBearing),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Facing Qibla Alert Banner
    AnimatedVisibility(visible = isFacingQibla) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp)
          .testTag("facing_qibla_banner")
      ) {
        Row(
          modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = WarmGoldAccent,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = LocalizationManager.getFacingQiblaText(language),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }

    // Interactive Compass Dial Box
    Box(
      modifier = Modifier
        .size(290.dp)
        .testTag("compass_dial_container"),
      contentAlignment = Alignment.Center
    ) {
      // Background Outer Ring with Card Glow
      Box(
        modifier = Modifier
          .size(280.dp)
          .clip(CircleShape)
          .background(
            Brush.radialGradient(
              colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
              )
            )
          )
      )

      // Rotating Compass Rose (Draws N, E, S, W and degree ticks)
      Canvas(
        modifier = Modifier
          .size(260.dp)
          .rotate(animatedHeading)
      ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2

        // Outer circular stroke
        drawCircle(
          color = Color(0x3322382C),
          radius = radius - 4f,
          style = Stroke(width = 2.dp.toPx())
        )

        // Degree ticks every 15 degrees
        for (deg in 0 until 360 step 15) {
          val angleRad = Math.toRadians(deg.toDouble() - 90)
          val isCardinal = deg % 90 == 0
          val tickLen = if (isCardinal) 14.dp.toPx() else 6.dp.toPx()
          val strokeWidth = if (isCardinal) 2.dp.toPx() else 1.dp.toPx()
          val tickColor = if (deg == 0) Color(0xFFD32F2F) else Color(0x66000000)

          val startX = (center.x + (radius - 8f - tickLen) * cos(angleRad)).toFloat()
          val startY = (center.y + (radius - 8f - tickLen) * sin(angleRad)).toFloat()
          val endX = (center.x + (radius - 8f) * cos(angleRad)).toFloat()
          val endY = (center.y + (radius - 8f) * sin(angleRad)).toFloat()

          drawLine(
            color = tickColor,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = strokeWidth
          )
        }
      }

      // North Label
      Box(
        modifier = Modifier
          .size(260.dp)
          .rotate(animatedHeading),
        contentAlignment = Alignment.TopCenter
      ) {
        Text(
          text = "N",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFFD32F2F),
          modifier = Modifier.padding(top = 10.dp)
        )
      }

      // Qibla Indicator Needle
      // Rotates towards Qibla relative to top of device
      Canvas(
        modifier = Modifier
          .size(220.dp)
          .rotate(animatedNeedleAngle)
      ) {
        val center = Offset(size.width / 2, size.height / 2)

        // Forest/Sage needle pointing upward towards Kaaba
        val needlePath = Path().apply {
          moveTo(center.x, center.y - 85.dp.toPx())
          lineTo(center.x - 12.dp.toPx(), center.y)
          lineTo(center.x, center.y - 10.dp.toPx())
          close()
        }
        drawPath(
          path = needlePath,
          color = Color(0xFF22382C)
        )

        val needlePathRight = Path().apply {
          moveTo(center.x, center.y - 85.dp.toPx())
          lineTo(center.x + 12.dp.toPx(), center.y)
          lineTo(center.x, center.y - 10.dp.toPx())
          close()
        }
        drawPath(
          path = needlePathRight,
          color = Color(0xFF86B69C)
        )

        // Tail pointing downward
        val tailPath = Path().apply {
          moveTo(center.x, center.y + 60.dp.toPx())
          lineTo(center.x - 8.dp.toPx(), center.y)
          lineTo(center.x + 8.dp.toPx(), center.y)
          close()
        }
        drawPath(
          path = tailPath,
          color = Color(0x44000000)
        )

        // Center hub
        drawCircle(
          color = Color(0xFFD4A853),
          radius = 14.dp.toPx()
        )
        drawCircle(
          color = Color.White,
          radius = 5.dp.toPx()
        )
      }

      // Holy Kaaba Icon badge at top of the needle
      Box(
        modifier = Modifier
          .size(240.dp)
          .rotate(animatedNeedleAngle),
        contentAlignment = Alignment.TopCenter
      ) {
        Surface(
          shape = CircleShape,
          color = if (isFacingQibla) MaterialTheme.colorScheme.primary else Color(0xFF1B1B1B),
          modifier = Modifier.size(32.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text("🕋", fontSize = 16.sp)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Info Cards: Bearing & Distance
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = LocalizationManager.getQiblaBearingLabel(language),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "%.1f°".format(qiblaBearing),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = LocalizationManager.getTrueNorthLabel(language),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            fontSize = 10.sp
          )
          Text(
            text = LocalizationManager.getDeclinationNote(magneticDeclination, language),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            fontSize = 9.sp
          )
        }
      }

      Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = LocalizationManager.getDistanceToKaabaLabel(language),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "%,d km".format(distanceToKaabaKm),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
          )
          Text(
            text = LocalizationManager.getGreatCircleDistanceLabel(language),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            fontSize = 10.sp
          )
        }
      }
    }

    // Calibration & Guidance Tip - only while the magnetometer actually reports low accuracy.
    // Showing it unconditionally trained users to ignore it.
    if (needsCalibration) {
      Spacer(modifier = Modifier.height(14.dp))

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            Icons.Default.CompassCalibration,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = LocalizationManager.getCompassCalibrationHint(language),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}

/** Signed shortest rotation from [from] to [to], in the range (-180, 180]. */
private fun shortestDelta(from: Float, to: Float): Float =
  ((to - from + 540f) % 360f) - 180f
