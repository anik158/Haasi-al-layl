with open("app/src/main/java/com/example/ui/screens/WeatherScreen.kt", "r") as f:
    text = f.read()

text = text.replace(
    '''WeatherSuccessContent(
          data = weatherState.data,
          formatTemperature = formatTemperature,
          language = language,
          onLocationClick = onLocationClick
        )''',
    '''WeatherSuccessContent(
          data = weatherState.data,
          formatTemperature = formatTemperature,
          language = language,
          onLocationClick = onLocationClick,
          isFahrenheit = isFahrenheit
        )'''
)

text = text.replace(
    '''@Composable
private fun WeatherSuccessContent(
  data: FullWeatherData,
  formatTemperature: (Double) -> String,
  language: AppLanguage,
  onLocationClick: () -> Unit
) {''',
    '''@Composable
private fun WeatherSuccessContent(
  data: FullWeatherData,
  formatTemperature: (Double) -> String,
  language: AppLanguage,
  onLocationClick: () -> Unit,
  isFahrenheit: Boolean
) {'''
)

with open("app/src/main/java/com/example/ui/screens/WeatherScreen.kt", "w") as f:
    f.write(text)

