with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    text = f.read()

text = text.replace(
    'CitySelectionSheet(',
    'CitySelectionSheet(\n        onAutoDetect = { viewModel.autoDetectLocation(); showCitySheet = false },'
)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(text)

