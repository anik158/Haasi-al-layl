with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "r") as f:
    lines = f.readlines()

while len(lines) > 0 and lines[-1].strip() == "}":
    lines.pop()

text = "".join(lines)

wind_speed_text = """
  fun getWindSpeedLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "سرعة الرياح"
    AppLanguage.BENGALI -> "বাতাসের গতি"
    AppLanguage.SPANISH -> "Velocidad del Viento"
    AppLanguage.PORTUGUESE -> "Velocidade do Vento"
    AppLanguage.ENGLISH -> "Wind Speed"
  }
}
"""
text = text.rstrip() + "\n\n" + wind_speed_text

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "w") as f:
    f.write(text)
