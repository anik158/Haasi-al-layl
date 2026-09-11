package com.example.data.weather

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface WeatherApiService {
  @GET("v1/forecast")
  suspend fun getForecast(
    @Query("latitude") latitude: Double,
    @Query("longitude") longitude: Double,
    @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,wind_speed_10m",
    @Query("hourly") hourly: String = "temperature_2m,weather_code,precipitation_probability",
    @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,uv_index_max",
    @Query("timezone") timezone: String = "auto"
  ): OpenMeteoResponse

  companion object {
    private const val BASE_URL = "https://api.open-meteo.com/"

    fun create(): WeatherApiService {
      val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

      val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

      return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(WeatherApiService::class.java)
    }
  }
}
