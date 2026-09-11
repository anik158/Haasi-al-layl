with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    text = f.read()

text = text.replace(
    'import com.example.ui.components.CitySelectionSheet',
    'import com.example.ui.components.CitySelectionSheet\nimport com.example.ui.components.WelcomeDialog'
)

text = text.replace(
    'var showCitySheet by remember { mutableStateOf(false) }',
    'var showCitySheet by remember { mutableStateOf(false) }\n  var showWelcome by remember { mutableStateOf(true) }'
)

welcome_dialog = """
    if (showWelcome) {
      WelcomeDialog(
        language = language,
        onLanguageSelect = onLanguageSelect,
        onOpenCitySelection = { showCitySheet = true },
        onDismiss = { showWelcome = false }
      )
    }
    
    if (showCitySheet) {"""

text = text.replace('if (showCitySheet) {', welcome_dialog)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(text)
