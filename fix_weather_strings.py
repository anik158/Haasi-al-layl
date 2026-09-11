with open("app/src/main/java/com/example/ui/components/WeatherHomeScreenCard.kt", "r") as f:
    text = f.read()

text = text.replace(
    'text = weather.conditionTitle,',
    'text = LocalizationManager.getWeatherConditionTitle(weather.weatherCode, language),'
)

text = text.replace(
    'text = weather.islamicWeatherAdvice,',
    'text = LocalizationManager.getIslamicWeatherAdvice(weather.weatherCode, weather.temperatureC, language),'
)

with open("app/src/main/java/com/example/ui/components/WeatherHomeScreenCard.kt", "w") as f:
    f.write(text)

with open("app/src/main/java/com/example/ui/screens/WeatherScreen.kt", "r") as f:
    text2 = f.read()

text2 = text2.replace(
    'text = cur.conditionTitle,',
    'text = LocalizationManager.getWeatherConditionTitle(cur.weatherCode, language),'
)
text2 = text2.replace(
    'text = cur.islamicWeatherAdvice,',
    'text = LocalizationManager.getIslamicWeatherAdvice(cur.weatherCode, cur.temperatureC, language),'
)
with open("app/src/main/java/com/example/ui/screens/WeatherScreen.kt", "w") as f:
    f.write(text2)
