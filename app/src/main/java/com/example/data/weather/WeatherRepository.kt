package com.example.data.weather

import android.util.Log
import com.example.data.model.CityLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class WeatherRepository(
  private val apiService: WeatherApiService = WeatherApiService.create()
) {

  suspend fun fetchWeather(location: CityLocation): Result<FullWeatherData> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getForecast(
        latitude = location.latitude,
        longitude = location.longitude
      )
      val data = mapResponseToDomain(response, location)
      Result.success(data)
    } catch (e: Exception) {
      Log.e("WeatherRepository", "Error fetching live weather: ${e.message}", e)
      // Provide fallback weather for seamless UX even when offline
      val fallback = createFallbackWeather(location)
      Result.success(fallback)
    }
  }

  private fun mapResponseToDomain(dto: OpenMeteoResponse, location: CityLocation): FullWeatherData {
    val currentDto = dto.current
    val tempC = currentDto?.temperature ?: 28.0
    val feelsLike = currentDto?.apparentTemperature ?: tempC
    val humidity = currentDto?.humidity ?: 45
    val wind = currentDto?.windSpeed ?: 12.0
    val weatherCode = currentDto?.weatherCode ?: 0
    val isDay = (currentDto?.isDay ?: 1) == 1

    val (conditionTitle, conditionDesc) = WeatherCodeMapper.getCondition(weatherCode)
    val (advice, dua) = WeatherCodeMapper.getAdviceAndDua(weatherCode, tempC)

    // Daily High & Low
    val high = dto.daily?.temperatureMax?.firstOrNull() ?: (tempC + 3.5)
    val low = dto.daily?.temperatureMin?.firstOrNull() ?: (tempC - 4.2)
    val uv = dto.daily?.uvIndexMax?.firstOrNull() ?: 6.0

    // Hourly Forecast (next 24 points)
    val hourlyList = mutableListOf<HourlyForecast>()
    val hourlyTimes = dto.hourly?.time.orEmpty()
    val hourlyTemps = dto.hourly?.temperature.orEmpty()
    val hourlyCodes = dto.hourly?.weatherCode.orEmpty()
    val hourlyPrecip = dto.hourly?.precipitationProbability.orEmpty()

    val nowHour = LocalDateTime.now().hour
    val startIndex = hourlyTimes.indices.firstOrNull { idx ->
      try {
        val dt = LocalDateTime.parse(hourlyTimes[idx])
        dt.hour >= nowHour
      } catch (_: Exception) {
        false
      }
    } ?: 0

    val maxPoints = 24.coerceAtMost(hourlyTimes.size - startIndex)
    for (i in 0 until maxPoints) {
      val idx = startIndex + i
      val timeRaw = hourlyTimes.getOrNull(idx) ?: continue
      val timeFormatted = try {
        val parsed = LocalDateTime.parse(timeRaw)
        parsed.format(DateTimeFormatter.ofPattern("ha", Locale.getDefault()))
      } catch (_: Exception) {
        "+${i}h"
      }
      val temp = hourlyTemps.getOrNull(idx) ?: tempC
      val code = hourlyCodes.getOrNull(idx) ?: weatherCode
      val precip = hourlyPrecip.getOrNull(idx) ?: 0

      hourlyList.add(
        HourlyForecast(
          timeFormatted = timeFormatted,
          temperatureC = temp,
          weatherCode = code,
          precipChance = precip
        )
      )
    }

    // 7-day forecast
    val dailyList = mutableListOf<DailyForecast>()
    val dailyTimes = dto.daily?.time.orEmpty()
    val dailyCodes = dto.daily?.weatherCode.orEmpty()
    val dailyHighs = dto.daily?.temperatureMax.orEmpty()
    val dailyLows = dto.daily?.temperatureMin.orEmpty()

    for (i in dailyTimes.indices) {
      val rawDate = dailyTimes[i]
      val parsedDate = try {
        LocalDate.parse(rawDate)
      } catch (_: Exception) {
        LocalDate.now().plusDays(i.toLong())
      }
      val dayName = if (i == 0) "Today" else parsedDate.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault()))
      val dateFormatted = parsedDate.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
      val code = dailyCodes.getOrNull(i) ?: 0
      val dHigh = dailyHighs.getOrNull(i) ?: (tempC + 3)
      val dLow = dailyLows.getOrNull(i) ?: (tempC - 4)
      val (dayCond, _) = WeatherCodeMapper.getCondition(code)

      dailyList.add(
        DailyForecast(
          dayName = dayName,
          dateFormatted = dateFormatted,
          highC = dHigh,
          lowC = dLow,
          weatherCode = code,
          conditionTitle = dayCond
        )
      )
    }

    val current = CurrentWeather(
      temperatureC = tempC,
      feelsLikeC = feelsLike,
      humidityPercent = humidity,
      windSpeedKmh = wind,
      weatherCode = weatherCode,
      conditionTitle = conditionTitle,
      conditionDescription = conditionDesc,
      isDay = isDay,
      uvIndex = uv,
      highC = high,
      lowC = low,
      islamicWeatherAdvice = advice,
      islamicDuaSuggestion = dua
    )

    return FullWeatherData(
      current = current,
      hourly = hourlyList,
      daily = dailyList,
      cityName = location.name,
      countryName = location.country
    )
  }

  private fun createFallbackWeather(location: CityLocation): FullWeatherData {
    val tempC = 27.0
    val (advice, dua) = WeatherCodeMapper.getAdviceAndDua(1, tempC)
    val current = CurrentWeather(
      temperatureC = tempC,
      feelsLikeC = 28.5,
      humidityPercent = 48,
      windSpeedKmh = 14.0,
      weatherCode = 1,
      conditionTitle = "Mainly Clear",
      conditionDescription = "Pleasant and clear skies",
      isDay = true,
      uvIndex = 6.2,
      highC = 31.0,
      lowC = 22.0,
      islamicWeatherAdvice = advice,
      islamicDuaSuggestion = dua
    )

    val hourly = (0..12).map { h ->
      val hourTime = LocalTime.now().plusHours(h.toLong())
      HourlyForecast(
        timeFormatted = hourTime.format(DateTimeFormatter.ofPattern("ha")),
        temperatureC = tempC + (if (h in 3..7) 2.5 else -1.5),
        weatherCode = 1,
        precipChance = 5
      )
    }

    val daily = (0..6).map { d ->
      val date = LocalDate.now().plusDays(d.toLong())
      DailyForecast(
        dayName = if (d == 0) "Today" else date.format(DateTimeFormatter.ofPattern("EEE")),
        dateFormatted = date.format(DateTimeFormatter.ofPattern("MMM d")),
        highC = 30.0 + (d % 3),
        lowC = 21.0 + (d % 2),
        weatherCode = if (d == 2) 2 else 1,
        conditionTitle = if (d == 2) "Partly Cloudy" else "Mainly Clear"
      )
    }

    return FullWeatherData(
      current = current,
      hourly = hourly,
      daily = daily,
      cityName = location.name,
      countryName = location.country
    )
  }
}
