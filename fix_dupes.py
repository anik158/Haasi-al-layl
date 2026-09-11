import re

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "r") as f:
    text = f.read()

# The duplicates are at the very end of the file.
# They are: getHumidityLabel, getWindLabel, getUvIndexLabel, getPrecipitationLabel
text = re.sub(r'  fun getHumidityLabel.*?\}', '', text, flags=re.DOTALL)
text = re.sub(r'  fun getWindLabel.*?\}', '', text, flags=re.DOTALL)
text = re.sub(r'  fun getUvIndexLabel.*?\}', '', text, flags=re.DOTALL)
text = re.sub(r'  fun getPrecipitationLabel.*?\}', '', text, flags=re.DOTALL)

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "w") as f:
    f.write(text)
