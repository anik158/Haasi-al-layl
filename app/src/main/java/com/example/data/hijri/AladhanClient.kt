package com.example.data.hijri

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Announced month starts from Aladhan (api.aladhan.com, free, no key).
 *
 * `calendarMethod=HJCoSA` is Umm al-Qura corrected to the Saudi High Judiciary Council's sighting
 * announcements for Muharram, Ramadan, Shawwal and Dhul-Hijjah; `DIYANET` is Turkey's calendar.
 * One request per month: the Gregorian date of the 1st.
 */
object AladhanClient {

  private const val BASE = "https://api.aladhan.com/v1/hToG/"
  private val DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy")

  fun firstOfMonthUrl(id: HijriMonthId, authority: Authority): String =
    "${BASE}01-%02d-%d?calendarMethod=%s".format(java.util.Locale.US, id.month, id.year, authority.aladhanMethod)

  /** Blocking GET; null on any failure. */
  fun interface Http {
    fun get(url: String): String?
  }

  private val client by lazy {
    OkHttpClient.Builder()
      .connectTimeout(8, TimeUnit.SECONDS)
      .readTimeout(8, TimeUnit.SECONDS)
      .build()
  }

  val okHttp = Http { url ->
    try {
      client.newCall(Request.Builder().url(url).build()).execute().use { response ->
        if (response.isSuccessful) response.body?.string() else null
      }
    } catch (_: Exception) {
      null
    }
  }

  /** The months that answered; a failed or malformed month is simply missing. */
  fun fetchStarts(months: List<HijriMonthId>, authority: Authority, http: Http = okHttp): Map<HijriMonthId, LocalDate> =
    months.mapNotNull { id ->
      val body = http.get(firstOfMonthUrl(id, authority)) ?: return@mapNotNull null
      parse(body, id)?.let { id to it }
    }.toMap()

  internal data class Response(val code: Int? = null, val data: Data? = null)
  internal data class Data(val gregorian: Gregorian? = null, val hijri: Hijri? = null)
  internal data class Gregorian(val date: String? = null)
  internal data class Hijri(val day: String? = null, val month: HijriMonth? = null, val year: String? = null)
  internal data class HijriMonth(val number: Int? = null)

  private val adapter by lazy {
    Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(Response::class.java)
  }

  /**
   * The Gregorian date in [body], if it really is the 1st of [id] - an answer about another month
   * (a server-side change, a wrong year) is dropped rather than trusted.
   */
  fun parse(body: String, id: HijriMonthId): LocalDate? = try {
    val response = adapter.fromJson(body)
    val data = response?.data
    val hijri = data?.hijri
    val matches = response?.code == 200 &&
      hijri?.day?.toIntOrNull() == 1 &&
      hijri.month?.number == id.month &&
      hijri.year?.toIntOrNull() == id.year
    if (matches) data?.gregorian?.date?.let { LocalDate.parse(it, DATE) } else null
  } catch (_: Exception) {
    null
  }
}
