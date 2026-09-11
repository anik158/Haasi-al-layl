import re
with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "r") as f:
    text = f.read()

# Delete duplicate getWindSpeedLabel at the end
text = re.sub(r'  fun getWindSpeedLabel.*?\}\n', '', text, flags=re.DOTALL)

# Because there is a missing '}' at the end for the object LocalizationManager, append it
text = text.rstrip() + "\n}\n"

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "w") as f:
    f.write(text)
