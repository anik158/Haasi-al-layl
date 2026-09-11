import re

with open("app/src/main/java/com/example/ui/screens/WeatherScreen.kt", "r") as f:
    text = f.read()

# 1. Modify HourlyWeatherItem signature and usages
text = text.replace(
    'fun HourlyWeatherItem(\n  hour: HourlyForecast,\n  formatTemperature: (Double) -> String,\n  language: AppLanguage\n)',
    'fun HourlyWeatherItem(\n  hour: HourlyForecast,\n  formatTemperature: (Double) -> String,\n  language: AppLanguage,\n  isFahrenheit: Boolean\n)'
)
text = text.replace(
    'HourlyWeatherItem(hour = hour, formatTemperature = formatTemperature, language = language)',
    'HourlyWeatherItem(hour = hour, formatTemperature = formatTemperature, language = language, isFahrenheit = isFahrenheit)'
)

# 2. Modify MetricItem signature and usages
text = text.replace(
    'private fun MetricItem(\n  modifier: Modifier = Modifier,\n  title: String,\n  value: String,\n  icon: ImageVector,\n  iconTint: Color\n)',
    'private fun MetricItem(\n  modifier: Modifier = Modifier,\n  title: String,\n  value: String,\n  icon: ImageVector,\n  iconTint: Color,\n  isFahrenheit: Boolean\n)'
)
text = re.sub(r'MetricItem\(([\s\S]*?)iconTint = ([^\n]*)\n\s*\)', r'MetricItem(\1iconTint = \2,\nisFahrenheit = isFahrenheit\n)', text)

# 3. Modify DailyForecastRow signature and usages
text = text.replace(
    'private fun DailyForecastRow(\n  day: DailyForecast,\n  formatTemperature: (Double) -> String\n)',
    'private fun DailyForecastRow(\n  day: DailyForecast,\n  formatTemperature: (Double) -> String,\n  isFahrenheit: Boolean\n)'
)
text = text.replace(
    'DailyForecastRow(day = day, formatTemperature = formatTemperature)',
    'DailyForecastRow(day = day, formatTemperature = formatTemperature, isFahrenheit = isFahrenheit)'
)

with open("app/src/main/java/com/example/ui/screens/WeatherScreen.kt", "w") as f:
    f.write(text)

