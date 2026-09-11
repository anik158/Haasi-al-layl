import re

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "r") as f:
    text = f.read()

text = re.sub(
    r'  fun getAppName\(language: AppLanguage\): String \{\n    return when \(language\) \{',
    r'  fun getAppName(language: AppLanguage): String = when (language) {',
    text
)

text = re.sub(
    r'      else -> "Haasi al-layl"\n    \}\n  \}',
    r'      else -> "Haasi al-layl"\n    }',
    text
)

text = re.sub(
    r'  fun getAppSubtitle\(language: AppLanguage\): String \{\n    return when \(language\) \{',
    r'  fun getAppSubtitle(language: AppLanguage): String = when (language) {',
    text
)

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "w") as f:
    f.write(text)
