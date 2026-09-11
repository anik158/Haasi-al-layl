with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "r") as f:
    text = f.read()

text = text.replace(
    '  fun getPrecipitationLabel(language: AppLanguage): String = when (language) {\n    AppLanguage.ARABIC -> "فرصة هطول أمطار"\n    AppLanguage.BENGALI -> "বৃষ্টির সম্ভাবনা"\n    AppLanguage.SPANISH -> "Probabilidad de lluvia"\n    AppLanguage.PORTUGUESE -> "Chance de chuva"\n    AppLanguage.ENGLISH -> "Chance of Rain"\n  }',
    '  fun getPrecipitationLabel(language: AppLanguage): String = when (language) {\n    AppLanguage.ARABIC -> "فرصة هطول أمطار"\n    AppLanguage.BENGALI -> "বৃষ্টির সম্ভাবনা"\n    AppLanguage.SPANISH -> "Probabilidad de lluvia"\n    AppLanguage.PORTUGUESE -> "Chance de chuva"\n    AppLanguage.ENGLISH -> "Chance of Rain"\n  }\n\n  fun getWindSpeedLabel(language: AppLanguage): String = when (language) {\n    AppLanguage.ARABIC -> "سرعة الرياح"\n    AppLanguage.BENGALI -> "বাতাসের গতি"\n    AppLanguage.SPANISH -> "Velocidad del Viento"\n    AppLanguage.PORTUGUESE -> "Velocidade do Vento"\n    AppLanguage.ENGLISH -> "Wind Speed"\n  }'
)

while "\n}\n}\n" in text:
    text = text.replace("\n}\n}\n", "\n}\n")

text = text.rstrip() + "\n"

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "w") as f:
    f.write(text)
