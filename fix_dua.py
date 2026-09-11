with open("app/src/main/java/com/example/ui/screens/DuaScreen.kt", "r") as f:
    lines = f.readlines()

out = []
skip = False
for i, line in enumerate(lines):
    if "import androidx.compose.material3.Text" in line:
        out.append(line)
        out.append("import androidx.compose.material3.DropdownMenu\n")
        out.append("import androidx.compose.material3.DropdownMenuItem\n")
        out.append("import androidx.compose.material.icons.filled.FilterList\n")
        out.append("import androidx.compose.foundation.layout.Row\n")
        out.append("import androidx.compose.foundation.layout.Box\n")
        out.append("import androidx.compose.foundation.layout.Arrangement\n")
        continue

    if "var showBookmarksOnly by remember { mutableStateOf(false) }" in line:
        out.append(line)
        out.append("    var expanded by remember { mutableStateOf(false) }\n")
        continue

    if "    // Category Filter Chips" in line:
        skip = True
        out.append("""    // Category Filter Chips and Dropdown
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
    }
""")
        continue
    
    if skip and "Spacer(modifier = Modifier.height(10.dp))" in line:
        skip = False
        
    if not skip:
        out.append(line)

with open("app/src/main/java/com/example/ui/screens/DuaScreen.kt", "w") as f:
    f.writelines(out)

