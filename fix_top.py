with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "r") as f:
    content = f.read()

# Fix the enum class AppLanguage part
import re
content = re.sub(
    r"enum class AppLanguage.*?object LocalizationManager {",
    """enum class AppLanguage(val code: String, val displayName: String, val nativeName: String, val flag: String) {
  ENGLISH("en", "English", "English", "🌐"),
  ARABIC("ar", "Arabic", "العربية", "🇸🇦"),
  BENGALI("bn", "Bengali", "বাংলা", "🇧🇩"),
  SPANISH("es", "Spanish", "Español", "🇪🇸"),
  PORTUGUESE("pt", "Portuguese", "Português", "🇵🇹");

  val englishName: String get() = displayName

  companion object {
    fun fromCode(code: String): AppLanguage {
      return entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
    }
  }
}

object LocalizationManager {""",
    content,
    flags=re.DOTALL
)

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "w") as f:
    f.write(content)
