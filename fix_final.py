with open("app/src/main/java/com/example/ui/components/CitySelectionSheet.kt", "r") as f:
    text = f.read()

text = text.replace(
    'fun CitySelectionSheet(\n  onAutoDetect: () -> Unit,\n  currentCity: CityLocation,',
    'fun CitySelectionSheet(\n  onAutoDetect: () -> Unit,\n  language: com.example.data.localization.AppLanguage,\n  currentCity: CityLocation,'
)

with open("app/src/main/java/com/example/ui/components/CitySelectionSheet.kt", "w") as f:
    f.write(text)

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    text = f.read()

text = text.replace("viewModel.updateLanguage(it)", "viewModel.setLanguage(it)")
text = text.replace(
    '''      CitySelectionSheet(
        currentCity = currentCity,
        onCitySelected = { city -> viewModel.selectCity(city) },
        onDismiss = { showCitySheet = false }
      )''',
    '''      CitySelectionSheet(
        onAutoDetect = { viewModel.autoDetectLocation(); showCitySheet = false },
        language = currentLanguage,
        currentCity = currentCity,
        onCitySelected = { city -> viewModel.updateCity(city) },
        onDismiss = { showCitySheet = false }
      )'''
)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(text)

