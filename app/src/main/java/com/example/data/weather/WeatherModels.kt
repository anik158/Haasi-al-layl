package com.example.data.weather

data class CurrentWeather(
  val temperatureC: Double,
  val feelsLikeC: Double,
  val humidityPercent: Int,
  val windSpeedKmh: Double,
  val weatherCode: Int,
  val conditionTitle: String,
  val conditionDescription: String,
  val isDay: Boolean,
  val uvIndex: Double,
  val highC: Double,
  val lowC: Double,
  val islamicWeatherAdvice: String,
  val islamicDuaSuggestion: String?
)

data class HourlyForecast(
  val timeFormatted: String,
  val temperatureC: Double,
  val weatherCode: Int,
  val precipChance: Int
)

data class DailyForecast(
  val dayName: String,
  val dateFormatted: String,
  val highC: Double,
  val lowC: Double,
  val weatherCode: Int,
  val conditionTitle: String
)

data class FullWeatherData(
  val current: CurrentWeather,
  val hourly: List<HourlyForecast>,
  val daily: List<DailyForecast>,
  val cityName: String,
  val countryName: String,
  val lastUpdated: Long = System.currentTimeMillis()
)

object WeatherCodeMapper {
  fun getCondition(code: Int): Pair<String, String> {
    return when (code) {
      0 -> Pair("Clear Sky", "Sunny and serene")
      1 -> Pair("Mainly Clear", "Pleasant and clear")
      2 -> Pair("Partly Cloudy", "Scattered clouds")
      3 -> Pair("Overcast", "Cloudy skies")
      45, 48 -> Pair("Foggy", "Foggy with limited visibility")
      51, 53, 55 -> Pair("Drizzle", "Light gentle rain")
      61, 63, 65 -> Pair("Rain", "Rainfall across the area")
      71, 73, 75 -> Pair("Snow", "Snowfall")
      77 -> Pair("Snow Grains", "Freezing precipitation")
      80, 81, 82 -> Pair("Rain Showers", "Passing rain showers")
      85, 86 -> Pair("Snow Showers", "Passing snow showers")
      95 -> Pair("Thunderstorm", "Thunder and lightning")
      96, 99 -> Pair("Severe Storm", "Thunderstorm with hail")
      else -> Pair("Fair", "Moderate weather")
    }
  }

  fun getAdviceAndDua(code: Int, tempC: Double): Pair<String, String?> {
    return when {
      code in listOf(95, 96, 99) -> Pair(
        "Thunderstorm in the area. Take shelter indoors and recite the Du'a for lightning and thunder.",
        "Subhana alladhi yusabbihu ar-ra'du bihamdihi wal-mala'ikatu min kheefatih (Glory to Him whom thunder and angels glorify in awe)."
      )
      code in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82) -> Pair(
        "Rain is descending — the Prophet (ﷺ) noted this is a blessed time for prayers to be answered!",
        "Allahumma sayyiban nafi'an (O Allah, may it be a beneficial rain)."
      )
      tempC > 36 -> Pair(
        "High temperatures today. Keep well-hydrated and seek shaded routes to the mosque.",
        "Recite Istighfar and seek refuge from the heat of Hellfire."
      )
      tempC < 5 -> Pair(
        "Cold conditions today. Perform wudhu thoroughly with warmth and dress warmly for Fajr/Isha.",
        "The Prophet (ﷺ) praised perfecting wudhu during hardships and cold."
      )
      else -> Pair(
        "Comfortable and serene weather — an excellent opportunity to walk to the Masjid for congregational prayer.",
        null
      )
    }
  }
}
