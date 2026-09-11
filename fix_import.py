with open("app/src/main/java/com/example/ui/screens/WeatherScreen.kt", "r") as f:
    text = f.read()

text = text.replace(
    "import androidx.compose.foundation.background",
    "import androidx.compose.foundation.background\nimport androidx.compose.foundation.clickable"
)

with open("app/src/main/java/com/example/ui/screens/WeatherScreen.kt", "w") as f:
    f.write(text)

