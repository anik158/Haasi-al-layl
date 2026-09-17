package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.ui.theme.GoldAccent

/**
 * What a firing prayer alarm looks like: the prayer, the time, the words, and one thing to do.
 *
 * A notification was the wrong shape for this. It is a row in a list, dismissible by a swipe,
 * silent a few seconds after it arrives - fine for "the forbidden window has ended", useless for
 * an alert meant to wake someone. This is a screen that takes over the display, over the lock
 * screen, and rings until it is stopped.
 *
 * Deliberately one action. A "snooze" on a prayer alert would be the app inviting the user to
 * delay a prayer, and a "dismiss" next to a "stop" is two words for the same thing.
 */
@Composable
fun PrayerAlarmScreen(
  title: String,
  body: String,
  arabicName: String?,
  language: AppLanguage,
  onStop: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(NightSkyTop, NightSkyBottom)
        )
      )
      .testTag("prayer_alarm_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      RingingBell()

      Spacer(modifier = Modifier.height(32.dp))

      if (!arabicName.isNullOrBlank()) {
        Text(
          text = arabicName,
          fontSize = 40.sp,
          fontWeight = FontWeight.SemiBold,
          color = GoldAccent,
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
      }

      Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = StarlightText,
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("alarm_title")
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = body,
        style = MaterialTheme.typography.bodyLarge,
        lineHeight = 26.sp,
        color = StarlightText.copy(alpha = 0.82f),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(48.dp))

      Button(
        onClick = onStop,
        modifier = Modifier
          .fillMaxWidth()
          .height(64.dp)
          .testTag("alarm_stop_button"),
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = GoldAccent,
          contentColor = NightSkyBottom
        )
      ) {
        Text(
          text = LocalizationManager.getStopAlarmAction(language),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = LocalizationManager.getAlarmAutoStopHint(language),
        style = MaterialTheme.typography.bodySmall,
        color = StarlightText.copy(alpha = 0.5f),
        textAlign = TextAlign.Center
      )
    }
  }
}

/** A bell that pulses in time with the ring, so the screen reads as sounding, not as a still. */
@Composable
private fun RingingBell() {
  val transition = rememberInfiniteTransition(label = "ring")
  val pulse = transition.animateFloat(
    initialValue = 1f,
    targetValue = 1.12f,
    animationSpec = infiniteRepeatable(
      animation = tween(700, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )
  val halo = transition.animateFloat(
    initialValue = 0.18f,
    targetValue = 0.42f,
    animationSpec = infiniteRepeatable(
      animation = tween(700, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "halo"
  )

  Box(
    modifier = Modifier.size(132.dp),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .size(132.dp)
        .scale(pulse.value)
        .clip(CircleShape)
        .background(GoldAccent.copy(alpha = halo.value * 0.35f))
    )
    Box(
      modifier = Modifier
        .size(92.dp)
        .clip(CircleShape)
        .background(GoldAccent.copy(alpha = 0.22f))
        .border(1.dp, GoldAccent.copy(alpha = 0.6f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Filled.NotificationsActive,
        contentDescription = null,
        tint = GoldAccent,
        modifier = Modifier
          .size(44.dp)
          .scale(pulse.value)
      )
    }
  }
}

private val NightSkyTop = Color(0xFF111A3D)
private val NightSkyBottom = Color(0xFF070B1C)
private val StarlightText = Color(0xFFEAEFF7)
