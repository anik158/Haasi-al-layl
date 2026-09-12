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
   * Wall-clock millis for a local date and time in [zone].
   *
   * `ZonedDateTime.of` shifts forward out of a DST spring-forward gap rather than throwing, which
   * is the behaviour we want for a prayer time that falls in a nonexistent hour.
   */
  fun toEpochMillis(date: LocalDate, time: LocalTime, zone: ZoneId): Long =
    ZonedDateTime.of(date, time, zone).toInstant().toEpochMilli()
}
