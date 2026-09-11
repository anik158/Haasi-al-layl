with open("app/src/main/java/com/example/ui/components/CitySelectionSheet.kt", "r") as f:
    text = f.read()

import_button = "import androidx.compose.material3.Button\nimport androidx.compose.material3.ButtonDefaults\n"
text = text.replace("import androidx.compose.material3.ExperimentalMaterial3Api", import_button + "import androidx.compose.material3.ExperimentalMaterial3Api")

auto_detect_button = """
      Button(
        onClick = { onAutoDetect() },
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DarkForestInk)
      ) {
        Icon(Icons.Default.LocationOn, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(LocalizationManager.getAutoDetectLocationLabel(language))
      }
      
      LazyColumn("""

text = text.replace("LazyColumn(", auto_detect_button)

# Also need to add onAutoDetect to parameters
text = text.replace(
    "fun CitySelectionSheet(",
    "fun CitySelectionSheet(\n  onAutoDetect: () -> Unit,"
)

with open("app/src/main/java/com/example/ui/components/CitySelectionSheet.kt", "w") as f:
    f.write(text)

