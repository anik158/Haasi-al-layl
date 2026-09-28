package com.example.data.prayer

import com.example.data.model.CityLocation
import com.example.data.model.PrayerName
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * One day's fast: when Sehri ends and when Iftar is.
 *
 * - **Sehri ends at Fajr, true dawn.** Qur'an 2:187 - eat and drink "until the white thread of dawn
 *   becomes distinct" - and Sahih al-Bukhari 1918-1919 / Muslim 1092: "eat and drink until Ibn Umm
 *   Maktum calls the adhan", which he called at dawn. The ten-minute Imsak some timetables print is
 *   a later precaution, not the sunnah, so it is not used as the end.
 * - **Iftar is at sunset.** Sahih al-Bukhari 1954 / Muslim 1100, and hastening it, Bukhari 1957 /
 *   Muslim 1098. This uses Maghrib, which is the computed sunset plus a three-minute margin for
 *   calculation error, so Iftar and the Maghrib alert always agree.
 *
 * Offline: everything comes from [PrayerCalculator], which needs no network.
 */
data class FastingDay(
  val date: LocalDate,
  val sehriEnd: LocalTime,
  val iftar: LocalTime,
  val isRamadan: Boolean,
  /** 1..30 during Ramadan, else 0. */
  val ramadanDay: Int
)

/** What the card shows at a given moment: the fast in question, and what comes next. */
data class FastingStatus(
  val day: FastingDay,
  /** True between Sehri end and Iftar on [FastingDay.date]: the fast is under way. */
  val isFasting: Boolean,
  /** HH:MM:SS until Sehri ends (before dawn) or until Iftar (during the fast). */
  val countdownFormatted: String,
  /** True after today's Iftar, when [day] is tomorrow's fast. */
  val isTomorrow: Boolean
)

object FastingTimes {

  /**
   * The fast that matters at [now]: today's until today's Iftar, then tomorrow's.
   *
   * Switching at Iftar rather than at midnight is what puts the first Sehri of Ramadan on screen on
   * the evening before 1 Ramadan, when people need it.
   */
  fun statusAt(
    now: LocalDateTime,
    location: CityLocation,
    method: CalculationMethod,
    madhab: IslamicMadhab
  ): FastingStatus {
    val today = dayFor(now.toLocalDate(), location, method, madhab)
    val day = if (now.toLocalTime().isBefore(today.iftar)) {
      today
    } else {
      dayFor(now.toLocalDate().plusDays(1), location, method, madhab)
    }

    val sehriEnd = LocalDateTime.of(day.date, day.sehriEnd)
    val iftar = LocalDateTime.of(day.date, day.iftar)
    val isFasting = !now.isBefore(sehriEnd) && now.isBefore(iftar)
    val target = if (isFasting) iftar else sehriEnd
    return FastingStatus(
      day = day,
      isFasting = isFasting,
      countdownFormatted = hms(Duration.between(now, target)),
      isTomorrow = day.date.isAfter(now.toLocalDate())
    )
  }

  fun dayFor(
    date: LocalDate,
    location: CityLocation,
    method: CalculationMethod,
    madhab: IslamicMadhab
  ): FastingDay {
    val schedule = PrayerCalculator.calculateSchedule(
      date = date,
      location = location,
      // Only feeds isNext/progress, which nothing here reads; noon keeps it deterministic.
      now = LocalDateTime.of(date, LocalTime.NOON),
      method = method,
      madhab = madhab
    )
    fun at(prayer: PrayerName) = schedule.items.first { it.prayer == prayer }.time
    val ramadan = HijriDateHelper.isRamadan(date)
    return FastingDay(
      date = date,
      sehriEnd = at(PrayerName.FAJR),
      iftar = at(PrayerName.MAGHRIB),
      isRamadan = ramadan,
      ramadanDay = if (ramadan) HijriDateHelper.getHijriDayOfMonth(date) else 0
    )
  }

  private fun hms(duration: Duration): String {
    val s = duration.seconds.coerceAtLeast(0)
    return String.format(java.util.Locale.US, "%02d:%02d:%02d", s / 3600, (s / 60) % 60, s % 60)
  }
}
