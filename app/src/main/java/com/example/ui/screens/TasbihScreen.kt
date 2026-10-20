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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.ui.theme.WarmGoldAccent

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(
  tasbihList: List<TasbihEntity>,
  selectedTasbih: TasbihEntity?,
  onSelectTasbih: (String) -> Unit,
  onIncrement: () -> Unit,
  onReset: () -> Unit,
  language: AppLanguage = AppLanguage.ENGLISH
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

  var showPicker by remember { mutableStateOf(false) }
  if (showPicker) {
    DhikrPickerSheet(
      tasbihList = tasbihList,
      selectedId = currentItem.id,
      language = language,
      onSelect = { id ->
        onSelectTasbih(id)
        showPicker = false
      },
      onDismiss = { showPicker = false }
    )
  }

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
          text = LocalizationManager.getTasbihTitle(language),
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = LocalizationManager.getTasbihSubtitle(language),
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
          contentDescription = LocalizationManager.getResetCounterDescription(language),
          tint = MaterialTheme.colorScheme.primary
        )
      }
    }

    // The current dhikr, which is also the way to pick another: the whole card opens the list,
    // and says so. A sideways-scrolling row of chips hid most of the choices off screen.
    Card(
      onClick = { showPicker = true },
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp)
        .testTag("tasbih_current_card"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
        LocalizationManager.getTasbihPronunciation(currentItem.id, language)?.let { pronunciation ->
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = pronunciation,
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = LocalizationManager.getTasbihName(currentItem.id, language, currentItem.name),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center
        )
        Text(
          text = LocalizationManager.getTasbihMeaning(currentItem.id, language, currentItem.transliteration),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )
        LocalizationManager.getTasbihSource(currentItem.id, language)?.let { source ->
          Text(
            text = source,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 4.dp)
          )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.primaryContainer
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = LocalizationManager.getChangeDhikrLabel(language),
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              Icons.Default.KeyboardArrowDown,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(18.dp)
            )
          }
        }
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
          text = "${LocalizationManager.getTargetLabel(language)}: $target",
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
            text = LocalizationManager.getTapToCountHint(language),
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
            text = LocalizationManager.getCompletedRoundsLabel(language),
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
            text = LocalizationManager.getLifetimeTotalLabel(language),
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
      Text(LocalizationManager.getResetLabel(language))
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}

/** Every dhikr as a full row - Arabic, name, meaning and progress - so the choice is visible. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun DhikrPickerSheet(
  tasbihList: List<TasbihEntity>,
  selectedId: String,
  language: AppLanguage,
  onSelect: (String) -> Unit,
  onDismiss: () -> Unit
) {
  androidx.compose.material3.ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
    // The app's parchment, not Material's default sheet tint, which read as another app's colour.
    containerColor = com.example.ui.theme.WarmBeigeBackground,
    modifier = Modifier.testTag("dhikr_picker_sheet")
  ) {
    Text(
      text = LocalizationManager.getChooseDhikrTitle(language),
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
    androidx.compose.foundation.lazy.LazyColumn(
      contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(tasbihList, key = { it.id }) { item ->
        val selected = item.id == selectedId
        Surface(
          onClick = { onSelect(item.id) },
          shape = RoundedCornerShape(16.dp),
          color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
          ),
          modifier = Modifier.fillMaxWidth().testTag("dhikr_option_${item.id}")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = LocalizationManager.getTasbihName(item.id, language, item.name),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = LocalizationManager.getTasbihMeaning(item.id, language, item.transliteration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
              )
              Text(
                text = "${item.currentCount} / ${item.targetCount.coerceAtLeast(1)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = item.arabic,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              textAlign = TextAlign.End,
              maxLines = 2,
              modifier = Modifier.weight(0.9f)
            )
            if (selected) {
              Spacer(modifier = Modifier.width(8.dp))
              Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }
        }
      }
    }
  }
}

