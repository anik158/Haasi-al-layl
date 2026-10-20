package com.example.data.notification

import com.example.data.model.DailyPrayerSchedule
import com.example.data.model.PrayerName
import com.example.data.prayer.ForbiddenWindowType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/** One alarm to arm: what it is, and the wall-clock instant it fires at. */
sealed interface ScheduledAlert {
  val triggerAtMillis: Long
  val requestCode: Int

  data class Prayer(
    val prayer: PrayerName,
    val kind: PrayerAlertKind,
    val prayerTime: LocalTime,
    override val triggerAtMillis: Long,
    override val requestCode: Int
  ) : ScheduledAlert

  data class Forbidden(
    val type: ForbiddenWindowType,
    val isStart: Boolean,
    override val triggerAtMillis: Long,
    override val requestCode: Int
  ) : ScheduledAlert

  data class Tahajjud(
    override val triggerAtMillis: Long,
    override val requestCode: Int
  ) : ScheduledAlert

  /** The Sehri wake-up for the fast on [fastDate]. */
  data class Sehri(
    val fastDate: LocalDate,
    val sehriEnd: LocalTime,
    override val triggerAtMillis: Long,
    override val requestCode: Int
  ) : ScheduledAlert

  data class DailyRefresh(
    override val triggerAtMillis: Long,
    override val requestCode: Int
  ) : ScheduledAlert
}

/**
 * Turns a day's prayer schedule into the alarms that should be armed for it.
 *
 * Deliberately free of Android imports so the whole policy - which prayers get alerts, how far
 * apart, what is filtered out as already past - is unit-testable on the JVM.
 */
object PrayerAlertPlanner {

  const val FOLLOW_UP_DELAY_MINUTES = 5L

  /**
   * The prayers that get alerts. Imsak and Sunrise are markers rather than prayers, so they are
   * deliberately absent.
   */
  val ALERTED_PRAYERS = listOf(
    PrayerName.FAJR,
    PrayerName.DHUHR,
    PrayerName.ASR,
    PrayerName.MAGHRIB,
    PrayerName.ISHA
  )

  /**
   * Alerts for [date] in [zone], filtered to those strictly after [nowMillis].
   *
   * Going through the city's [zone] rather than the device default matters: the selected city can
   * be in a different timezone from the phone, and alarms take device wall-clock millis.
   */
  fun planForDay(
    schedule: DailyPrayerSchedule,
    date: LocalDate,
    zone: ZoneId,
    nowMillis: Long,
    prayerAlertsEnabled: Boolean,
    followUpEnabled: Boolean,
    forbiddenAlertsEnabled: Boolean,
    tahajjudAlertsEnabled: Boolean
  ): List<ScheduledAlert> {
    val alerts = mutableListOf<ScheduledAlert>()

    if (prayerAlertsEnabled) {
      schedule.items
        .filter { it.prayer in ALERTED_PRAYERS }
        .forEach { item ->
          alerts += ScheduledAlert.Prayer(
            prayer = item.prayer,
            kind = PrayerAlertKind.AT_TIME,
            prayerTime = item.time,
            triggerAtMillis = toEpochMillis(date, item.time, zone),
            requestCode = AlertIds.prayer(item.prayer, PrayerAlertKind.AT_TIME)
          )
          if (followUpEnabled) {
            alerts += ScheduledAlert.Prayer(
              prayer = item.prayer,
              kind = PrayerAlertKind.FOLLOW_UP,
              prayerTime = item.time,
              triggerAtMillis = toEpochMillis(date, item.time, zone) +
                FOLLOW_UP_DELAY_MINUTES * 60_000L,
              requestCode = AlertIds.prayer(item.prayer, PrayerAlertKind.FOLLOW_UP)
            )
          }
        }
    }

    if (forbiddenAlertsEnabled) {
      schedule.forbiddenTimes.forEach { window ->
        alerts += ScheduledAlert.Forbidden(
          type = window.type,
          isStart = true,
          triggerAtMillis = toEpochMillis(date, window.startTime, zone),
          requestCode = AlertIds.forbidden(window.type, isStart = true)
        )
        alerts += ScheduledAlert.Forbidden(
          type = window.type,
          isStart = false,
          triggerAtMillis = toEpochMillis(date, window.endTime, zone),
          requestCode = AlertIds.forbidden(window.type, isStart = false)
        )
      }
    }

    if (tahajjudAlertsEnabled) {
      schedule.tahajjudStart?.let { start ->
        alerts += ScheduledAlert.Tahajjud(
          triggerAtMillis = toEpochMillis(date, start, zone),
          requestCode = AlertIds.TAHAJJUD
        )
      }
    }

    return alerts.filter { it.triggerAtMillis > nowMillis }
  }

  /**
   * The Sehri wake-up for the fast on [date], [leadMinutes] before Sehri ends, or null.
   *
   * Only on a day the user fasts - Ramadan, or one they marked - and never on a day fasting is
   * forbidden (the Eids, Tashreeq), even if marked: a reminder would read as an invitation.
   */
  fun planSehri(
    date: LocalDate,
    sehriEnd: LocalTime,
    zone: ZoneId,
    nowMillis: Long,
    enabled: Boolean,
    leadMinutes: Int,
    isFastingDay: Boolean,
    isForbiddenDay: Boolean
  ): ScheduledAlert.Sehri? {
    if (!enabled || !isFastingDay || isForbiddenDay) return null
    val trigger = ZonedDateTime.of(date, sehriEnd, zone).minusMinutes(leadMinutes.toLong())
      .toInstant().toEpochMilli()
    if (trigger <= nowMillis) return null
    return ScheduledAlert.Sehri(date, sehriEnd, trigger, AlertIds.SEHRI)
  }

  /**
   * Keeps only the earliest alert for each request code.
   *
   * Slots are reused day after day (see [AlertIds]), so arming three days of alerts means three
   * alerts per slot - and each arm replaces the previous PendingIntent. Without this the *last*
   * day armed won, so today's alarms were always overwritten by tomorrow's and never fired. The
   * next one is armed when this one fires, or at the 00:05 refresh.
   */
  fun nextPerSlot(alerts: List<ScheduledAlert>): List<ScheduledAlert> =
    alerts.groupBy { it.requestCode }.values.map { slot -> slot.minBy { it.triggerAtMillis } }
      .sortedBy { it.triggerAtMillis }

  /**
   * Wall-clock millis for a local date and time in [zone].
   *
   * `ZonedDateTime.of` shifts forward out of a DST spring-forward gap rather than throwing, which
   * is the behaviour we want for a prayer time that falls in a nonexistent hour.
   */
  fun toEpochMillis(date: LocalDate, time: LocalTime, zone: ZoneId): Long =
    ZonedDateTime.of(date, time, zone).toInstant().toEpochMilli()
}
