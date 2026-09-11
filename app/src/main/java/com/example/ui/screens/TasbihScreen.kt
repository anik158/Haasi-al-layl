package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TasbihEntity
import com.example.ui.theme.WarmGoldAccent

@Composable
fun TasbihScreen(
  tasbihList: List<TasbihEntity>,
  selectedTasbih: TasbihEntity?,
  onSelectTasbih: (String) -> Unit,
  onIncrement: () -> Unit,
  onReset: () -> Unit
) {
  val currentItem = selectedTasbih ?: tasbihList.firstOrNull() ?: TasbihEntity(
    id = "default",
    name = "SubhanAllah",
    arabic = "سُبْحَانَ اللَّهِ",
    transliteration = "Glory be to Allah",
    currentCount = 0,
    targetCount = 33
  )

  val target = currentItem.targetCount.coerceAtLeast(1)
  val count = currentItem.currentCount
  val progress = (count % target).toFloat() / target.toFloat()

  val animatedProgress by animateFloatAsState(
    targetValue = progress,
    label = "tasbih_progress"
  )

  val completedRounds = count / target

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
          text = "Digital Tasbih",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Dhikr & Remembrance of Allah",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      IconButton(
        onClick = onReset,
        modifier = Modifier.testTag("btn_reset_tasbih")
      ) {
        Icon(
          Icons.Default.Refresh,
          contentDescription = "Reset counter",
          tint = MaterialTheme.colorScheme.primary
        )
      }
    }

    // Presets Row
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
        .testTag("tasbih_presets_row")
    ) {
      items(tasbihList) { item ->
        val isSelected = item.id == currentItem.id
        FilterChip(
          selected = isSelected,
          onClick = { onSelectTasbih(item.id) },
          label = {
            Text(
              text = item.name,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
          ),
          modifier = Modifier.testTag("preset_${item.id}")
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Active Dhikr Title Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = currentItem.arabic,
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = currentItem.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = currentItem.transliteration,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    // Giant Interactive Counter Button
    Box(
      modifier = Modifier
        .size(240.dp)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            colors = listOf(
              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
              MaterialTheme.colorScheme.surface
            )
          )
        )
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = androidx.compose.material3.ripple(bounded = true, radius = 120.dp)
        ) {
          onIncrement()
        }
        .testTag("tasbih_counter_button"),
      contentAlignment = Alignment.Center
    ) {
      // Circular progress bar track
      CircularProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier.size(230.dp),
        strokeWidth = 10.dp,
        color = WarmGoldAccent,
        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
      )

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "$count",
          style = MaterialTheme.typography.displayLarge,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.primary,
          fontSize = 60.sp
        )
        Text(
          text = "Target: $target",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Outlined.TouchApp,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "TAP HERE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Stats Row (Laps completed, Lifetime Total)
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
            text = "Rounds Completed",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "$completedRounds",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
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
            text = "Lifetime Total",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "${currentItem.totalLifetimeCount}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Reset Button
    OutlinedButton(
      onClick = onReset,
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("tasbih_reset_action")
    ) {
      Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text("Reset Current Counter to 0")
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}
