import re

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "r") as f:
    text = f.read()

# Fix Isha
text = re.sub(
    r'AppLanguage.ENGLISH -> "Isha".*?(?=\n\s*// Celestial Horizon)',
    'AppLanguage.ENGLISH -> "Isha"\n    }\n  }',
    text,
    flags=re.DOTALL
)

# Fix Dark Moon
text = re.sub(
    r'AppLanguage.ENGLISH -> "Dark Moon \(Mahq\)".*?(?=\n\s*fun getIlluminationLabel)',
    'AppLanguage.ENGLISH -> "Dark Moon (Mahq)"\n    }\n  }',
    text,
    flags=re.DOTALL
)

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "w") as f:
    f.write(text)
