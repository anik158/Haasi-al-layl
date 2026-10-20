package com.example.data.hijri

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Aladhan's HJCoSA month starts, 1447-1449, as fetched on 21 Oct 2026 - announced for the months
 * already begun, Umm al-Qura's prediction after - and a fake server that answers with them.
 */
object SaudiFixture {
  private val dmy = DateTimeFormatter.ofPattern("dd-MM-yyyy")

  val STARTS: Map<HijriMonthId, LocalDate> = """
    1447-01 26-06-2025 1447-02 26-07-2025 1447-03 24-08-2025 1447-04 23-09-2025 1447-05 23-10-2025
    1447-06 22-11-2025 1447-07 21-12-2025 1447-08 20-01-2026 1447-09 18-02-2026 1447-10 20-03-2026
    1447-11 18-04-2026 1447-12 18-05-2026 1448-01 16-06-2026 1448-02 15-07-2026 1448-03 14-08-2026
    1448-04 12-09-2026 1448-05 12-10-2026 1448-06 11-11-2026 1448-07 10-12-2026 1448-08 09-01-2027
    1448-09 08-02-2027 1448-10 09-03-2027 1448-11 08-04-2027 1448-12 07-05-2027 1449-01 06-06-2027
    1449-02 05-07-2027 1449-03 03-08-2027 1449-04 02-09-2027 1449-05 01-10-2027 1449-06 31-10-2027
    1449-07 29-11-2027 1449-08 29-12-2027 1449-09 28-01-2028 1449-10 26-02-2028 1449-11 27-03-2028
    1449-12 26-04-2028
  """.trim().split(Regex("\\s+")).chunked(2).associate { (k, v) ->
    HijriMonthId.parse(k)!! to LocalDate.parse(v, dmy)
  }

  /** The JSON Aladhan returns for the 1st of [id], trimmed to the fields the client reads. */
  fun body(id: HijriMonthId, date: LocalDate, method: String = "HJCoSA") =
    """{"code":200,"status":"OK","data":{"hijri":{"date":"01-%02d-%d","day":"1","month":{"number":%d},"year":"%d","method":"%s"},"gregorian":{"date":"%s"}}}"""
      .format(id.month, id.year, id.month, id.year, method, date.format(dmy))

  /** A fake Aladhan: answers from [starts], records every URL asked, null when [online] is false. */
  class Server(var starts: Map<HijriMonthId, LocalDate> = STARTS, var online: Boolean = true) : AladhanClient.Http {
    val requests = mutableListOf<String>()
    override fun get(url: String): String? {
      requests += url
      if (!online) return null
      val m = Regex("hToG/01-(\\d\\d)-(\\d+)\\?calendarMethod=(\\w+)").find(url) ?: return null
      val id = HijriMonthId(m.groupValues[2].toInt(), m.groupValues[1].toInt())
      return starts[id]?.let { body(id, it, m.groupValues[3]) }
    }
  }
}
