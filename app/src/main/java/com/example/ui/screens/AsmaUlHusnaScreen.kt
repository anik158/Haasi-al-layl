package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dua.AllahName
import com.example.data.dua.AsmaUlHusna
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.ui.theme.DarkForestInk
import com.example.ui.theme.LinenBorderLight
import com.example.ui.theme.WarmGoldAccent

/**
 * The 99 Names of Allah, its own tab ("ﷲ99").
 *
 * Opens with Sahih al-Bukhari 2736, which establishes the ninety-nine, and closes with the honest
 * note that the list itself is Tirmidhi 3507, graded da'if. Search runs over the Arabic (vowel
 * marks ignored, via the Duas screen's normalizeForSearch), the transliteration and the meaning.
 */
@Composable
fun AsmaUlHusnaScreen(language: AppLanguage = AppLanguage.ENGLISH) {
  var query by remember { mutableStateOf("") }
  // The name whose explanation is open; null when the sheet is closed.
  var selected by remember { mutableStateOf<AllahName?>(null) }
  selected?.let { name -> NameDetailSheet(name, language, onDismiss = { selected = null }) }
  val index = remember(language) {
    AsmaUlHusna.NAMES.associate { name ->
      name.number to normalizeForSearch(
        "${name.arabic} ${name.transliteration} ${name.transliterationBn} ${name.transliterationEs} " +
          "${name.explanation(language)} " +
          "${name.meaning(language)} ${name.meaning(AppLanguage.ENGLISH)}"
      )
    }
  }
  val needle = normalizeForSearch(query)
  val shown = if (needle.isBlank()) {
    AsmaUlHusna.NAMES
  } else {
    AsmaUlHusna.NAMES.filter { index[it.number].orEmpty().contains(needle) }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 16.dp)
  ) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
      Text(
        text = LocalizationManager.getAsmaUlHusnaTitle(language),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Text(
        text = LocalizationManager.getAsmaUlHusnaSubtitle(language),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    OutlinedTextField(
      value = query,
      onValueChange = { query = it },
      placeholder = { Text(LocalizationManager.getAsmaUlHusnaSearchPlaceholder(language)) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = LocalizationManager.getSearchLabel(language)) },
      singleLine = true,
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("asma_search_input")
    )

    Spacer(modifier = Modifier.height(10.dp))

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("asma_list"),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      contentPadding = PaddingValues(bottom = 24.dp)
    ) {
      items(shown, key = { it.number }) { name ->
        NameCard(name, language, onClick = { selected = name })
      }
      item(key = "footnote") {
        Text(
          text = LocalizationManager.getAsmaUlHusnaFootnote(language),
          style = MaterialTheme.typography.bodySmall,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 6.dp, start = 4.dp, end = 4.dp)
        )
      }
    }
  }
}

@Composable
private fun NameCard(name: AllahName, language: AppLanguage, onClick: () -> Unit) {
  val shape = RoundedCornerShape(18.dp)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .clickable(onClick = onClick)
      .background(Color.White)
      .border(1.dp, LinenBorderLight, shape)
      .padding(horizontal = 14.dp, vertical = 12.dp)
      .testTag("asma_name_${name.number}"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(CircleShape)
        .background(DarkForestInk),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = LocalizationManager.localizeDigits(name.number.toString(), language),
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
      )
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      // In the reader's own script - Bengali readers were left reading Latin letters.
      val pronunciation = name.pronunciation(language)
      if (pronunciation != null) {
        Text(
          text = pronunciation,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = DarkForestInk
        )
      }
      Text(
        text = name.meaning(language),
        style = if (pronunciation == null) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
        fontWeight = if (pronunciation == null) FontWeight.SemiBold else FontWeight.Normal,
        color = DarkForestInk.copy(alpha = if (pronunciation == null) 0.9f else 0.75f)
      )
    }
    Spacer(modifier = Modifier.width(10.dp))
    Text(
      text = name.arabic,
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = WarmGoldAccent,
      textAlign = TextAlign.End
    )
  }
}

/** One name in full: Arabic, pronunciation, meaning, a short explanation, and its verse. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun NameDetailSheet(name: AllahName, language: AppLanguage, onDismiss: () -> Unit) {
  androidx.compose.material3.ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
    modifier = Modifier.testTag("asma_detail_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(androidx.compose.foundation.rememberScrollState())
        .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(DarkForestInk),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = LocalizationManager.localizeDigits(name.number.toString(), language),
          color = Color.White,
          fontWeight = FontWeight.Bold
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = name.arabic,
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        color = WarmGoldAccent,
        textAlign = TextAlign.Center
      )
      name.pronunciation(language)?.let { pronunciation ->
        Text(
          text = pronunciation,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = DarkForestInk,
          textAlign = TextAlign.Center
        )
      }
      Text(
        text = name.meaning(language),
        style = MaterialTheme.typography.bodyLarge,
        color = DarkForestInk.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = name.explanation(language),
        style = MaterialTheme.typography.bodyLarge,
        lineHeight = 24.sp,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = if (language == AppLanguage.ARABIC) TextAlign.End else TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = name.quranRef?.let { LocalizationManager.getQuranRefLabel(it, language) }
          ?: LocalizationManager.getNameFromListNote(language),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = if (name.quranRef != null) FontWeight.Bold else FontWeight.Normal,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth(),
        textAlign = if (language == AppLanguage.ARABIC) TextAlign.End else TextAlign.Start
      )
    }
  }
}

