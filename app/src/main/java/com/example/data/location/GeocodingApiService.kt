package com.example.data.location

import com.example.data.model.CityLocation
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Place search, so the city picker is not limited to the handful of presets.
 *
 * Open-Meteo's geocoding service is the same family as the weather API already in use: free, no
 * key, and it returns latitude, longitude *and* an IANA timezone - exactly what [CityLocation]
 * needs, with no second lookup.
 */
interface GeocodingApiService {

  @GET("v1/search")
  suspend fun search(
    @Query("name") name: String,
    @Query("count") count: Int = 20,
    @Query("language") language: String = "en",
    @Query("format") format: String = "json"
  ): GeocodingResponse

  companion object {
    private const val BASE_URL = "https://geocoding-api.open-meteo.com/"

    fun create(): GeocodingApiService {
      val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
      val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
      return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(GeocodingApiService::class.java)
    }
  }
}

data class GeocodingResponse(
  val results: List<GeocodingResult>? = null
)

data class GeocodingResult(
  val name: String,
  val latitude: Double,
  val longitude: Double,
  val timezone: String? = null,
  val country: String? = null,
  @Json(name = "country_code") val countryCode: String? = null,
  /** First-level administrative division - the state or division a city sits in. */
  val admin1: String? = null,
  val population: Long? = null
) {
  fun toCityLocation(): CityLocation = CityLocation(
    name = name,
    country = country ?: countryCode.orEmpty(),
    latitude = latitude,
    longitude = longitude,
    // Falling back to the device zone would be wrong for a city the user is not standing in.
    timeZoneId = timezone ?: "UTC"
  )

  /** "Faridpur, Dhaka Division" - disambiguates the several places sharing a name. */
  val subtitle: String
    get() = listOfNotNull(admin1, country).distinct().joinToString(", ")
}
