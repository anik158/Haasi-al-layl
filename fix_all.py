import re

# 1. MainActivity.kt
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    text = f.read()

text = text.replace(
    'CitySelectionSheet(\n                schedule = schedule,',
    'CitySelectionSheet(\n                onAutoDetect = { viewModel.autoDetectLocation(); showCitySheet = false },\n                schedule = schedule,'
)
with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(text)

# 2. HaasiAlLaylViewModel.kt
with open("app/src/main/java/com/example/ui/HaasiAlLaylViewModel.kt", "r") as f:
    text = f.read()
# Removing duplicate Dispatchers import
lines = text.splitlines()
out = []
seen_dispatchers = False
for line in lines:
    if "import kotlinx.coroutines.Dispatchers" in line:
        if not seen_dispatchers:
            seen_dispatchers = True
            out.append(line)
    else:
        out.append(line)
with open("app/src/main/java/com/example/ui/HaasiAlLaylViewModel.kt", "w") as f:
    f.write("\n".join(out))


# 3, 4, 5. CitySelectionSheet.kt
with open("app/src/main/java/com/example/ui/components/CitySelectionSheet.kt", "r") as f:
    text = f.read()

import_lines = "import com.example.ui.theme.DarkForestInk\nimport com.example.data.localization.LocalizationManager\n"
if "import com.example.ui.theme.DarkForestInk" not in text:
    text = text.replace("import androidx.compose.material3.ExperimentalMaterial3Api", import_lines + "import androidx.compose.material3.ExperimentalMaterial3Api")

# Fix `language` unresolved
# Need to pass language to CitySelectionSheet
text = text.replace(
    "fun CitySelectionSheet(\n  onAutoDetect: () -> Unit,\n  schedule: DailyPrayerSchedule?",
    "fun CitySelectionSheet(\n  onAutoDetect: () -> Unit,\n  language: com.example.data.localization.AppLanguage,\n  schedule: DailyPrayerSchedule?"
)

with open("app/src/main/java/com/example/ui/components/CitySelectionSheet.kt", "w") as f:
    f.write(text)

# Also fix HomeScreen and MainActivity to pass language to CitySelectionSheet
with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    text = f.read()
if "language = language," not in text.split("CitySelectionSheet")[1]:
    text = text.replace(
        'CitySelectionSheet(\n        onAutoDetect = { viewModel.autoDetectLocation(); showCitySheet = false },',
        'CitySelectionSheet(\n        onAutoDetect = { viewModel.autoDetectLocation(); showCitySheet = false },\n        language = language,'
    )
with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(text)

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    text = f.read()
if "language = currentLanguage," not in text.split("CitySelectionSheet")[1]:
    text = text.replace(
        'CitySelectionSheet(\n                onAutoDetect = { viewModel.autoDetectLocation(); showCitySheet = false },',
        'CitySelectionSheet(\n                onAutoDetect = { viewModel.autoDetectLocation(); showCitySheet = false },\n                language = currentLanguage,'
    )
with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(text)

# 6. DuaScreen.kt
with open("app/src/main/java/com/example/ui/screens/DuaScreen.kt", "r") as f:
    text = f.read()

lines = text.splitlines()
out = []
seen_arrangement = False
for line in lines:
    if "import androidx.compose.foundation.layout.Arrangement" in line:
        if not seen_arrangement:
            seen_arrangement = True
            out.append(line)
    else:
        out.append(line)
with open("app/src/main/java/com/example/ui/screens/DuaScreen.kt", "w") as f:
    f.write("\n".join(out))

