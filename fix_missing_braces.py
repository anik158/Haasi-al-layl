with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "r") as f:
    text = f.read()

text = text.replace(
    '      AppLanguage.ENGLISH -> "Isha"\n  }',
    '      AppLanguage.ENGLISH -> "Isha"\n    }\n  }'
)

text = text.replace(
    '      AppLanguage.ENGLISH -> "Dark Moon (Mahq)"\n  }',
    '      AppLanguage.ENGLISH -> "Dark Moon (Mahq)"\n    }\n  }'
)

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "w") as f:
    f.write(text)
