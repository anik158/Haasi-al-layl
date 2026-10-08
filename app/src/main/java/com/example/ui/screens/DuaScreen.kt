package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import com.example.data.dua.HisnChapter
import com.example.data.dua.DuaCategory
import com.example.data.dua.DuaItem
import com.example.data.dua.IslamicContentProvider
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.ui.theme.WarmGoldAccent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DuaScreen(
  allDuas: List<com.example.data.dua.DuaItem>,
  bookmarkedDuaIds: Set<String>,
  onToggleBookmark: (String) -> Unit,
  language: AppLanguage = AppLanguage.ENGLISH,
  /** Hisn al-Muslim's chapters; empty until its data files are installed. */
  hisnChapters: List<HisnChapter> = emptyList()
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf<DuaCategory?>(null) }
  var showBookmarksOnly by remember { mutableStateOf(false) }
  var expanded by remember { mutableStateOf(false) }
  var selectedHisnChapter by remember { mutableStateOf<Int?>(null) }

  // Filtering waits for typing to pause instead of running on every keystroke - it matters once
  // Hisn al-Muslim's few hundred entries are in the list.
  var debouncedQuery by remember { mutableStateOf("") }
  LaunchedEffect(searchQuery) {
    if (searchQuery.isNotBlank()) delay(250)
    debouncedQuery = searchQuery
  }

  val searchIndex = remember(allDuas) { allDuas.associate { it.id to searchableText(it) } }

  val filteredDuas = remember(debouncedQuery, selectedCategory, showBookmarksOnly, bookmarkedDuaIds, allDuas, selectedHisnChapter) {
    val query = normalizeForSearch(debouncedQuery)
    allDuas.filter { dua ->
      val matchesSearch = query.isBlank() || searchIndex[dua.id].orEmpty().contains(query)
      // No category = Everyday Duas: the built-in ones only. Hisn al-Muslim has its own entry,
      // and dumping its 302 duas here as one flat list made "All" read as wrong. Search and
      // Bookmarks still reach everything.
      val matchesCategory = when {
        selectedCategory != null -> dua.category == selectedCategory
        query.isNotBlank() || showBookmarksOnly -> true
        else -> dua.category != DuaCategory.HISN
      }
      val matchesChapter = selectedHisnChapter == null || dua.hisnChapter == selectedHisnChapter
      val matchesBookmarks = !showBookmarksOnly || bookmarkedDuaIds.contains(dua.id)
      matchesSearch && matchesCategory && matchesChapter && matchesBookmarks
    }
  }

  // Hisn al-Muslim is browsed by chapter: picking it shows the chapter index, and a chapter shows
  // its supplications. A search skips the index and searches the whole collection.
  val showChapterIndex = selectedCategory == DuaCategory.HISN &&
    selectedHisnChapter == null && debouncedQuery.isBlank() && !showBookmarksOnly

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 16.dp)
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
          text = LocalizationManager.getDuaTitle(language),
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = LocalizationManager.getDuaSubtitle(language),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Search bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text(LocalizationManager.getSearchDuaPlaceholder(language)) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = LocalizationManager.getSearchLabel(language)) },
      singleLine = true,
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("dua_search_input")
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Category Filter Chips and Dropdown
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Bookmarks chip
      FilterChip(
        selected = showBookmarksOnly,
        onClick = {
          showBookmarksOnly = !showBookmarksOnly
          if (showBookmarksOnly) selectedCategory = null
        },
        label = { Text("★ ${LocalizationManager.getBookmarksOnlyLabel(language)} (${bookmarkedDuaIds.size})") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = WarmGoldAccent.copy(alpha = 0.25f),
          selectedLabelColor = MaterialTheme.colorScheme.primary
        )
      )

      Box {
        IconButton(onClick = { expanded = true }) {
          Icon(Icons.Default.FilterList, contentDescription = LocalizationManager.getFilterLabel(language))
        }
        DropdownMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false }
        ) {
          // Hisn al-Muslim first: it is the main collection, so it heads the list, above All.
          if (hisnChapters.isNotEmpty()) {
            DropdownMenuItem(
              text = { Text(DuaCategory.HISN.getLocalizedName(language), fontWeight = if (selectedCategory == DuaCategory.HISN) FontWeight.Bold else FontWeight.Normal) },
              onClick = {
                selectedCategory = DuaCategory.HISN
                selectedHisnChapter = null
                showBookmarksOnly = false
                expanded = false
              },
              modifier = Modifier.testTag("dua_category_hisn")
            )
          }
          DropdownMenuItem(
            text = { Text(DuaCategory.ALL.getLocalizedName(language), fontWeight = if (selectedCategory == null && !showBookmarksOnly) FontWeight.Bold else FontWeight.Normal) },
            onClick = {
              selectedCategory = null
              selectedHisnChapter = null
              showBookmarksOnly = false
              expanded = false
            }
          )
          DuaCategory.entries
            .filter { it != DuaCategory.ALL && it != DuaCategory.HISN }
            .forEach { category ->
              DropdownMenuItem(
                text = { Text(category.getLocalizedName(language), fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal) },
                onClick = {
                  selectedCategory = category
                  selectedHisnChapter = null
                  showBookmarksOnly = false
                  expanded = false
                }
              )
            }
        }
      }
    }
    Spacer(modifier = Modifier.height(10.dp))

    if (selectedHisnChapter != null) {
      val chapter = hisnChapters.firstOrNull { it.number == selectedHisnChapter }
      Surface(
        onClick = { selectedHisnChapter = null },
        shape = RoundedCornerShape(12.dp),
        color = WarmGoldAccent.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).testTag("hisn_chapter_back")
      ) {
        Text(
          text = "←  ${DuaCategory.HISN.getLocalizedName(language)} · " +
            LocalizationManager.localizeDigits("${chapter?.number ?: ""}", language) +
            ". ${chapter?.title(language).orEmpty()}",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        )
      }
    }

    // Dua Cards List
    if (showChapterIndex) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("hisn_chapter_index"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(hisnChapters, key = { "hisn_chapter_${it.number}" }) { chapter ->
          val count = allDuas.count { it.hisnChapter == chapter.number }
          Surface(
            onClick = { selectedHisnChapter = chapter.number },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = LocalizationManager.localizeDigits(chapter.number.toString(), language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = WarmGoldAccent,
                modifier = Modifier.width(40.dp)
              )
              Text(
                text = chapter.title(language),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
              )
              Text(
                text = LocalizationManager.localizeDigits(count.toString(), language),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
      }
    } else if (filteredDuas.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .testTag("no_duas_found"),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("📖", fontSize = 40.sp)
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = LocalizationManager.getNoDuasFoundTitle(language, showBookmarksOnly),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = LocalizationManager.getNoDuasFoundSubtitle(language, showBookmarksOnly),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      val groupedDuas = remember(filteredDuas, language, hisnChapters) {
        filteredDuas.groupBy { dua ->
          val chapter = dua.hisnChapter?.let { n -> hisnChapters.firstOrNull { it.number == n } }
          if (chapter != null) {
            LocalizationManager.localizeDigits("${chapter.number}", language) + ". " + chapter.title(language)
          } else {
            dua.category.getLocalizedName(language)
          }
        }
      }

      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("duas_list"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        groupedDuas.forEach { (groupTitle, duas) ->
          stickyHeader(key = "header_$groupTitle") {
            Surface(
              color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = groupTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = WarmGoldAccent,
                modifier = Modifier.padding(vertical = 8.dp)
              )
            }
          }
          items(duas, key = { it.id }) { dua ->
            val isBookmarked = bookmarkedDuaIds.contains(dua.id)
            DuaCardItem(
              dua = dua,
              isBookmarked = isBookmarked,
              language = language,
              onToggleBookmark = { onToggleBookmark(dua.id) }
            )
          }
        }
        item {
          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    }
  }
}

@Composable
private fun DuaCardItem(
  dua: DuaItem,
  isBookmarked: Boolean,
  language: AppLanguage,
  onToggleBookmark: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("dua_card_${dua.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = dua.getLocalizedTitle(language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.padding(top = 4.dp)
          ) {
            Text(
              text = "${dua.category.getLocalizedName(language)} • ${dua.repetition}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        IconButton(
          onClick = onToggleBookmark,
          modifier = Modifier.testTag("bookmark_${dua.id}")
        ) {
          Icon(
            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
            contentDescription = if (isBookmarked) LocalizationManager.getRemoveBookmarkLabel(language) else LocalizationManager.getAddBookmarkLabel(language),
            tint = if (isBookmarked) WarmGoldAccent else MaterialTheme.colorScheme.outline
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Arabic Text (Right-aligned, large legible authentic font with Tashkeel)
      Text(
        text = dua.arabic,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.End,
        lineHeight = 34.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Transliteration
      Text(
        text = dua.transliteration,
        style = MaterialTheme.typography.bodyMedium,
        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      // The translation, where there is one. In Arabic the Arabic above is the text, and an
      // unconditional pair of quotes rendered empty.
      val translation = dua.getLocalizedTranslation(language)
      if (translation.isNotBlank()) {
        Text(
          text = "“$translation”",
          style = MaterialTheme.typography.bodyMedium,
          lineHeight = 22.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      // The virtue or the narrator's words, boxed and labelled so it is never read as part of
      // what to recite.
      val note = dua.getLocalizedNote(language)
      if (note.isNotBlank()) {
        Spacer(modifier = Modifier.height(10.dp))
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WarmGoldAccent.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("dua_note_${dua.id}")
        ) {
          Text(
            text = LocalizationManager.getDuaVirtueLabel(language),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = note,
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 19.sp,
            textAlign = if (language == AppLanguage.ARABIC) TextAlign.End else TextAlign.Start,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
      Spacer(modifier = Modifier.height(6.dp))

      // Reference
      Text(
        text = "${LocalizationManager.getSourceLabel(language)}: ${dua.reference}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline
      )
    }
  }
}

/** Every field worth searching, normalized once per supplication rather than on every keystroke. */
private fun searchableText(dua: DuaItem): String = normalizeForSearch(
  listOf(
    dua.title, dua.titleBn, dua.titleAr, dua.titleEs, dua.titlePt,
    dua.translation, dua.translationBn, dua.translationEs, dua.translationPt,
    dua.transliteration, dua.arabic, dua.reference
  ).joinToString(" ")
)

/**
 * Lower-case, with Arabic vowel marks and tatweel removed and the alef variants folded together,
 * so "سبحان الله" typed without tashkeel finds the fully vowelled text.
 */
internal fun normalizeForSearch(text: String): String = buildString(text.length) {
  for (c in text.lowercase()) {
    when (c) {
      in '\u064B'..'\u065F', '\u0670', '\u0640' -> Unit          // harakat, dagger alef, tatweel
      '\u0622', '\u0623', '\u0625', '\u0671' -> append('\u0627')  // آ أ إ ٱ -> ا
      '\u0649' -> append('\u064A')                                  // ى -> ي
      else -> append(c)
    }
  }
}.trim()
