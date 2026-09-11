import re

with open("app/src/main/java/com/example/ui/screens/DuaScreen.kt", "r") as f:
    text = f.read()

# Make sure DropdownMenu, DropdownMenuItem are imported
if "androidx.compose.material3.DropdownMenu" not in text:
    text = text.replace("import androidx.compose.material3.Text\n", "import androidx.compose.material3.Text\nimport androidx.compose.material3.DropdownMenu\nimport androidx.compose.material3.DropdownMenuItem\nimport androidx.compose.material.icons.filled.FilterList\n")

if "var expanded by remember { mutableStateOf(false) }" not in text:
    text = text.replace("var showBookmarksOnly by remember { mutableStateOf(false) }", "var showBookmarksOnly by remember { mutableStateOf(false) }\n    var expanded by remember { mutableStateOf(false) }")

lazy_row_pattern = r"    // Category Filter Chips\n    LazyRow\([\s\S]*?    \} // End of LazyRow"

replacement = """    // Category Filter Chips and Dropdown
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
        androidx.compose.material3.IconButton(onClick = { expanded = true }) {
          Icon(Icons.Default.FilterList, contentDescription = "Filter")
        }
        DropdownMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false }
        ) {
          DropdownMenuItem(
            text = { Text(DuaCategory.ALL.getLocalizedName(language), fontWeight = if (selectedCategory == null && !showBookmarksOnly) FontWeight.Bold else FontWeight.Normal) },
            onClick = {
              selectedCategory = null
              showBookmarksOnly = false
              expanded = false
            }
          )
          DuaCategory.entries.filter { it != DuaCategory.ALL }.forEach { category ->
            DropdownMenuItem(
              text = { Text(category.getLocalizedName(language), fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal) },
              onClick = {
                selectedCategory = category
                showBookmarksOnly = false
                expanded = false
              }
            )
          }
        }
      }
    }"""

text = re.sub(lazy_row_pattern, replacement, text, flags=re.MULTILINE)

# Also fix the trailing brace issue in LazyRow if my regex doesn't match perfectly. Wait, I should match exactly.
