with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    text = f.read()

if "language = currentLanguage" not in text.split("CitySelectionSheet")[1]:
    text = text.replace(
        'CitySelectionSheet(\n                onAutoDetect = { viewModel.autoDetectLocation(); showCitySheet = false },\n                schedule = schedule,',
        'CitySelectionSheet(\n                onAutoDetect = { viewModel.autoDetectLocation(); showCitySheet = false },\n                language = currentLanguage,\n                schedule = schedule,'
    )
    
if "WelcomeDialog" not in text:
    text = text.replace("import com.example.ui.components.CitySelectionSheet", "import com.example.ui.components.CitySelectionSheet\nimport com.example.ui.components.WelcomeDialog")
    
    welcome_block = """
      if (showWelcome) {
        WelcomeDialog(
          language = currentLanguage,
          onLanguageSelect = { viewModel.updateLanguage(it) },
          onOpenCitySelection = { showCitySheet = true },
          onDismiss = { showWelcome = false }
        )
      }
      
      if (showCitySheet) {"""
      
    text = text.replace("if (showCitySheet) {", welcome_block)
    
    # Add showWelcome flag
    text = text.replace("var showCitySheet by remember { mutableStateOf(false) }", "var showCitySheet by remember { mutableStateOf(false) }\n      var showWelcome by remember { mutableStateOf(true) }")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(text)

