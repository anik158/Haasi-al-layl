package com.example.data.weather

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
  val latitude: Double? = null,
  val longitude: Double? = null,
  val elevation: Double? = null,
  val timezone: String? = null,
  val current: CurrentWeatherDto? = null,
  val hourly: HourlyWeatherDto? = null,
  val daily: DailyWeatherDto? = null
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
  val time: String? = null,
  @Json(name = "temperature_2m") val temperature: Double? = null,
  @Json(name = "relative_humidity_2m") val humidity: Int? = null,
  @Json(name = "apparent_temperature") val apparentTemperature: Double? = null,
  @Json(name = "is_day") val isDay: Int? = null,
  val precipitation: Double? = null,
  @Json(name = "weather_code") val weatherCode: Int? = null,
  @Json(name = "wind_speed_10m") val windSpeed: Double? = null
)

@JsonClass(generateAdapter = true)
data class HourlyWeatherDto(
  val time: List<String>? = null,
  @Json(name = "temperature_2m") val temperature: List<Double>? = null,
  @Json(name = "weather_code") val weatherCode: List<Int>? = null,
  @Json(name = "precipitation_probability") val precipitationProbability: List<Int>? = null
)

@JsonClass(generateAdapter = true)
data class DailyWeatherDto(
  val time: List<String>? = null,
  @Json(name = "weather_code") val weatherCode: List<Int>? = null,
  @Json(name = "temperature_2m_max") val temperatureMax: List<Double>? = null,
  @Json(name = "temperature_2m_min") val temperatureMin: List<Double>? = null,
  @Json(name = "uv_index_max") val uvIndexMax: List<Double>? = null
)
