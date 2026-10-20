package com.example.data.hijri

import java.time.LocalDate

/**
 * Bangladesh's announced month starts (National Moon Sighting Committee) next to Saudi Arabia's
 * (Aladhan, calendarMethod=HJCoSA), 1440-1448 AH. Each Bangladesh date is from a news report of the
 * committee's decision - Dhaka Tribune, Prothom Alo, Financial Express, TBS, UNB, Daily Star,
 * risingbd - with the weekday checked against the calendar. Dhul-Hijjah 1440 has no report of
 * the final decision and is marked unverified.
 */
data class HistoricalStart(
  val label: String,
  val hijriYear: Int,
  val hijriMonth: Int,
  val bangladesh: LocalDate,
  val saudi: LocalDate,
  val verified: Boolean = true
)

object BangladeshHistory {
  private fun d(s: String) = LocalDate.parse(s)

  val STARTS = listOf(
    HistoricalStart("Ramadan 1440", 1440, 9, d("2019-05-07"), d("2019-05-06")),
    HistoricalStart("Shawwal 1440", 1440, 10, d("2019-06-05"), d("2019-06-04")),
    HistoricalStart("Dhul-Hijjah 1440", 1440, 12, d("2019-08-03"), d("2019-08-02"), verified = false),
    HistoricalStart("Ramadan 1441", 1441, 9, d("2020-04-25"), d("2020-04-24")),
    HistoricalStart("Shawwal 1441", 1441, 10, d("2020-05-25"), d("2020-05-24")),
    HistoricalStart("Dhul-Hijjah 1441", 1441, 12, d("2020-07-23"), d("2020-07-22")),
    HistoricalStart("Ramadan 1442", 1442, 9, d("2021-04-14"), d("2021-04-13")),
    HistoricalStart("Shawwal 1442", 1442, 10, d("2021-05-14"), d("2021-05-13")),
    HistoricalStart("Dhul-Hijjah 1442", 1442, 12, d("2021-07-12"), d("2021-07-11")),
    HistoricalStart("Ramadan 1443", 1443, 9, d("2022-04-03"), d("2022-04-02")),
    HistoricalStart("Shawwal 1443", 1443, 10, d("2022-05-03"), d("2022-05-02")),
    HistoricalStart("Dhul-Hijjah 1443", 1443, 12, d("2022-07-01"), d("2022-06-30")),
    HistoricalStart("Ramadan 1444", 1444, 9, d("2023-03-24"), d("2023-03-23")),
    HistoricalStart("Shawwal 1444", 1444, 10, d("2023-04-22"), d("2023-04-21")),
    HistoricalStart("Dhul-Hijjah 1444", 1444, 12, d("2023-06-20"), d("2023-06-19")),
    HistoricalStart("Ramadan 1445", 1445, 9, d("2024-03-12"), d("2024-03-11")),
    HistoricalStart("Shawwal 1445", 1445, 10, d("2024-04-11"), d("2024-04-10")),
    HistoricalStart("Dhul-Hijjah 1445", 1445, 12, d("2024-06-08"), d("2024-06-07")),
    HistoricalStart("Ramadan 1446", 1446, 9, d("2025-03-02"), d("2025-03-01")),
    HistoricalStart("Shawwal 1446", 1446, 10, d("2025-03-31"), d("2025-03-30")),
    HistoricalStart("Dhul-Hijjah 1446", 1446, 12, d("2025-05-29"), d("2025-05-28")),
    HistoricalStart("Ramadan 1447", 1447, 9, d("2026-02-19"), d("2026-02-18")),
    HistoricalStart("Shawwal 1447", 1447, 10, d("2026-03-21"), d("2026-03-20")),
    HistoricalStart("Dhul-Hijjah 1447", 1447, 12, d("2026-05-19"), d("2026-05-18")),
    HistoricalStart("Muharram 1444", 1444, 1, d("2022-07-31"), d("2022-07-30")),
    HistoricalStart("Rabi al-Awwal 1444", 1444, 3, d("2022-09-28"), d("2022-09-27")),
    HistoricalStart("Rabi al-Awwal 1445", 1445, 3, d("2023-09-17"), d("2023-09-16")),
    HistoricalStart("Sha'ban 1445", 1445, 8, d("2024-02-12"), d("2024-02-11")),
    HistoricalStart("Muharram 1446", 1446, 1, d("2024-07-08"), d("2024-07-07")),
    HistoricalStart("Rabi al-Awwal 1446", 1446, 3, d("2024-09-05"), d("2024-09-04")),
    HistoricalStart("Sha'ban 1446", 1446, 8, d("2025-02-01"), d("2025-01-31")),
    HistoricalStart("Muharram 1447", 1447, 1, d("2025-06-27"), d("2025-06-26")),
    HistoricalStart("Rabi al-Awwal 1447", 1447, 3, d("2025-08-26"), d("2025-08-24")),
    HistoricalStart("Muharram 1448", 1448, 1, d("2026-06-17"), d("2026-06-16")),
    HistoricalStart("Rabi al-Awwal 1448", 1448, 3, d("2026-08-15"), d("2026-08-14"))
  )
}
