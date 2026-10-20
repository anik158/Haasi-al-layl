package com.example.data.hijri

import android.content.Context
import android.util.Log
import com.example.data.astronomy.CrescentCalculator
import com.example.data.model.CityLocation
import com.example.data.prayer.HijriDateHelper
import com.example.data.preferences.UserPreferences
import java.time.LocalDate
import java.time.ZoneId

/**
 * Builds the month table from what is stored and installs it in [HijriDateHelper], which every
 * Hijri read in the app goes through - the date on Today, Ramadan, Eid, the fasting rules, the
 * Ramadan Isha rule and the alarm planner alike.
 *
 * Cheap enough for a process start: sixteen months, two crescent evaluations each.
 */
object HijriCalendar {

  private const val TAG = "HijriCalendar"

  /** Three months back (Undo, history), twelve ahead (the next Ramadan and Eid al-Adha). */
  private const val MONTHS_BACK = 3
  private const val MONTH_COUNT = 16

  private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

  /** The network, swapped out by tests so nothing on the JVM reaches the real Aladhan. */
  @Volatile
  var http: AladhanClient.Http = AladhanClient.okHttp

  /** Back from a debug build's test date: forget what was saved under it, and rebuild. */
  fun leaveTestDate(context: Context) {
    com.example.data.AppClock.backToRealTime()
    HijriStore(context).dropTestRecords(System.currentTimeMillis())
    rebuild(context)
  }

  /** Rebuilds from storage and installs it. Call after any input changes. */
  fun rebuild(context: Context): HijriMonthTable? = try {
    val prefs = UserPreferences(context)
    val city = prefs.selectedCity
    val table = city?.let {
      build(
        city = it,
        // The app's clock, so a debug test date gets a table around it.
        today = com.example.data.AppClock.today(zoneOf(it)),
        store = HijriStore(context)
      )
    }
    HijriDateHelper.table = table
    table
  } catch (e: Exception) {
    // Never take the app down over the calendar: the Umm al-Qura fallback still answers.
    Log.e(TAG, "could not build the month table", e)
    HijriDateHelper.table = null
    null
  }

  fun build(
    city: CityLocation,
    today: LocalDate,
    store: HijriStore,
    userStartsOverride: Map<HijriMonthId, LocalDate>? = null
  ): HijriMonthTable {
    val policy = HijriCountryRules.policyFor(city.country)
    return build(
      city = city,
      today = today,
      policy = policy,
      announced = store.announced(policy.authority),
      userStarts = userStartsOverride ?: store.userStarts().mapValues { it.value.startsOn }
    )
  }

  /** The pure core - also what the correction flow uses to preview a change. */
  fun build(
    city: CityLocation,
    today: LocalDate,
    policy: SightingPolicy,
    announced: Map<HijriMonthId, LocalDate>,
    userStarts: Map<HijriMonthId, LocalDate>
  ): HijriMonthTable {
    val zone = zoneOf(city)
    val cache = HashMap<LocalDate, Double>()
    val resolver = MonthStartResolver(
      policy = policy,
      announced = announced,
      userStarts = userStarts,
      crescentV = { date ->
        cache.getOrPut(date) {
          CrescentCalculator.evaluate(date, city.latitude, city.longitude, zone).odehV
        }
      }
    )
    val first = MonthStartResolver.ummAlQuraMonth(today) + (-MONTHS_BACK)
    return resolver.resolve(first, MONTH_COUNT)
  }

  /**
   * Fetches the authority's announced starts when due: once a day, and hourly on the evenings a
   * month can start (the 29th and 30th) and the day after. Blocking - call off the main thread.
   * Returns true when the table changed, so the caller re-arms the alarms.
   */
  fun refreshAnnouncements(
    context: Context,
    nowMillis: Long = System.currentTimeMillis(),
    force: Boolean = false,
    http: AladhanClient.Http = this.http
  ): Boolean {
    val city = UserPreferences(context).selectedCity ?: return false
    val policy = HijriCountryRules.policyFor(city.country)
    val store = HijriStore(context)
    val today = com.example.data.AppClock.today(zoneOf(city))
    val age = nowMillis - store.fetchedAtMillis(policy.authority)
    if (!force && !isDue(age, HijriDateHelper.dayInfo(today)?.day)) return false

    val centre = HijriDateHelper.dayInfo(today)?.id ?: MonthStartResolver.ummAlQuraMonth(today)
    // The daily fetch covers the table's whole year ahead (the next Eids, Arafah); the hourly
    // ones near a month start only the months around it, where an announcement can land.
    val months = if (force || age >= DAY_MILLIS || age < 0) {
      (-1..MONTH_COUNT - MONTHS_BACK - 1).map { centre + it }
    } else {
      (-1..1).map { centre + it }
    }
    val fetched = AladhanClient.fetchStarts(months, policy.authority, http)
    if (fetched.isEmpty()) return false

    val before = HijriDateHelper.table?.signature
    store.saveAnnounced(policy.authority, fetched, nowMillis)
    val after = rebuild(context)?.signature
    Log.i(TAG, "fetched ${fetched.size} ${policy.authority} month starts; changed=${before != after}")
    return before != after
  }

  /**
   * Whether a fetch [ageMillis] after the last one is due on Hijri day [hijriDay]: daily, hourly
   * from the 28th to the 2nd (an announcement can land any evening then), and always when the
   * clock went backwards or the day is unknown.
   */
  internal fun isDue(ageMillis: Long, hijriDay: Int?): Boolean {
    val nearMonthStart = hijriDay == null || hijriDay >= 28 || hijriDay <= 2
    return ageMillis < 0 || ageMillis >= DAY_MILLIS || (nearMonthStart && ageMillis >= DAY_MILLIS / 24)
  }

  /**
   * Saves the user's confirmed answer and installs the new table. The caller re-arms the alarms.
   * [undoUntilMillis] is the next Maghrib, until which Today offers Undo.
   */
  fun applyCorrection(
    context: Context,
    decision: SightingFlow.Decision,
    nowMillis: Long,
    undoUntilMillis: Long
  ): HijriMonthTable? {
    val store = HijriStore(context)
    if (decision.answer == SightingAnswer.USE_ESTIMATE) {
      store.clear(decision.month, nowMillis)
    } else {
      store.setUserStart(
        UserMonthStart(
          id = decision.month,
          startsOn = decision.startsOn,
          heardFrom = decision.heardFrom ?: HeardFrom.OTHER,
          recordedAtMillis = nowMillis,
          undoUntilMillis = undoUntilMillis,
          previous = store.userStarts()[decision.month]?.startsOn
        )
      )
    }
    return rebuild(context)
  }

  /** Undo on Today: the month's earlier state comes back. The caller re-arms the alarms. */
  fun undoCorrection(context: Context, id: HijriMonthId, nowMillis: Long): HijriMonthTable? {
    HijriStore(context).undo(id, nowMillis)
    return rebuild(context)
  }

  /** The correction Today should offer to undo at [nowMillis], if any. */
  fun undoable(context: Context, nowMillis: Long): UserMonthStart? =
    HijriStore(context).userStarts().values
      .filter { nowMillis >= it.recordedAtMillis && nowMillis < it.undoUntilMillis }
      .maxByOrNull { it.recordedAtMillis }

  private fun zoneOf(city: CityLocation): ZoneId = try {
    ZoneId.of(city.timeZoneId)
  } catch (_: Exception) {
    ZoneId.systemDefault()
  }
}
