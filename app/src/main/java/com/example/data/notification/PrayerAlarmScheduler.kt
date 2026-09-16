package com.example.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.AlarmManagerCompat
import com.example.MainActivity
import com.example.data.prayer.PrayerCalculator
import com.example.data.preferences.UserPreferences
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * The one place alarms are armed.
 *
 * AlarmManager rather than WorkManager: WorkManager's contract is explicitly best-effort and
 * batched, and a Fajr alert twenty minutes late is a correctness bug, not a polish issue.
 *
 * Prayer alerts use `setAlarmClock`, which is exempt from Doze and App Standby. The obvious
 * alternative, `setExactAndAllowWhileIdle`, is quota-limited to roughly one alarm per app per
 * nine minutes in deep Doze - which would silently push every five-minute follow-up out to nine,
 * exactly for Fajr and Isha. The cost is a permanent alarm icon in the status bar, so
 * [UserPreferences.discreetAlarmMode] offers the quieter, less reliable path.
 */
object PrayerAlarmScheduler {

  private const val TAG = "PrayerAlarmScheduler"

  /**
   * How far ahead to arm. Long enough that a device which misses a whole day still has the next
   * one armed; short enough that settings changes do not leave a long tail of stale alarms.
   */
  private const val HORIZON_HOURS = 48L

  /** Cheap enough to call on every process start: a no-op unless the day or settings changed. */
  fun ensureScheduled(context: Context) {
    val prefs = UserPreferences(context)
    val city = prefs.selectedCity ?: PrayerCalculator.PRESET_CITIES[0]
    val today = LocalDate.now(zoneOf(city.timeZoneId)).toEpochDay()
    if (prefs.lastScheduledEpochDay == today &&
      prefs.lastScheduleSignature == signature(context, prefs)
    ) {
      return
    }
    rescheduleAll(context)
  }

  /**
   * Cancels every alarm this app can own, then re-arms the next [HORIZON_HOURS]. Idempotent.
   *
   * Cancelling the whole id space rather than tracking what was armed makes this immune to drift
   * after a crash, an upgrade, or a settings change that shortens the list.
   */
  fun rescheduleAll(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val prefs = UserPreferences(context)
    val city = prefs.selectedCity ?: PrayerCalculator.PRESET_CITIES[0]
    val zone = zoneOf(city.timeZoneId)

    cancelAll(context, alarmManager)

    val prayerEnabled = PrayerNotificationManager.isPrayerNotificationsEnabled(context)
    val followUpEnabled = PrayerNotificationManager.isFollowUpNotificationsEnabled(context)
    val forbiddenEnabled = PrayerNotificationManager.isForbiddenNotificationsEnabled(context)
    val tahajjudEnabled = PrayerNotificationManager.isTahajjudNotificationsEnabled(context)

    val now = System.currentTimeMillis()
    val horizonEnd = now + HORIZON_HOURS * 60 * 60 * 1000L
    val today = LocalDate.now(zone)

    val alerts = (0L..2L).flatMap { offset ->
      val date = today.plusDays(offset)
      val schedule = PrayerCalculator.calculateSchedule(
        date = date,
        location = city,
        // Only feeds isNext/progress, which the planner ignores; noon keeps it deterministic.
        now = LocalDateTime.of(date, LocalTime.NOON),
        completedPrayerIds = emptySet(),
        method = prefs.calculationMethod,
        madhab = prefs.madhab
      )
      PrayerAlertPlanner.planForDay(
        schedule = schedule,
        date = date,
        zone = zone,
        nowMillis = now,
        prayerAlertsEnabled = prayerEnabled,
        followUpEnabled = followUpEnabled,
        forbiddenAlertsEnabled = forbiddenEnabled,
        tahajjudAlertsEnabled = tahajjudEnabled
      )
    }.filter { it.triggerAtMillis <= horizonEnd }

    alerts.forEach { arm(context, alarmManager, it, prefs.discreetAlarmMode) }

    // The day-rollover trigger, and the recovery path if a whole day's alarms were suppressed.
    val refreshAt = PrayerAlertPlanner.toEpochMillis(
      today.plusDays(1),
      LocalTime.of(0, 5),
      zone
    )
    arm(
      context,
      alarmManager,
      ScheduledAlert.DailyRefresh(refreshAt, AlertIds.DAILY_REFRESH),
      discreet = true
    )

    prefs.lastScheduledEpochDay = today.toEpochDay()
    prefs.lastScheduleSignature = signature(context, prefs)
    Log.i(TAG, "armed ${alerts.size} alerts for ${city.name} through +${HORIZON_HOURS}h")
  }

  /**
   * Arms a real alarm one minute out, through the same receiver, PendingIntent and channel the
   * daily alerts use.
   *
   * A notification posted directly only proves the notification path. This proves the *alarm*
   * path, which is the part an aggressive power manager breaks - so if this does not arrive, the
   * problem is the device holding the alarm, not the app building it.
   */
  fun scheduleTestAlarm(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val prefs = UserPreferences(context)
    val city = prefs.selectedCity ?: PrayerCalculator.PRESET_CITIES[0]
    val zone = zoneOf(city.timeZoneId)
    val schedule = PrayerCalculator.calculateSchedule(
      date = LocalDate.now(zone),
      location = city,
      now = LocalDateTime.now(zone),
      completedPrayerIds = emptySet(),
      method = prefs.calculationMethod,
      madhab = prefs.madhab
    )
    val nextPrayer = schedule.items
      .firstOrNull { it.prayer in PrayerAlertPlanner.ALERTED_PRAYERS }
      ?.prayer ?: com.example.data.model.PrayerName.FAJR

    arm(
      context,
      alarmManager,
      ScheduledAlert.Prayer(
        prayer = nextPrayer,
        kind = PrayerAlertKind.AT_TIME,
        prayerTime = LocalTime.now(zone),
        triggerAtMillis = System.currentTimeMillis() + 60_000L,
        requestCode = AlertIds.prayer(nextPrayer, PrayerAlertKind.AT_TIME)
      ),
      discreet = prefs.discreetAlarmMode
    )
    Log.i(TAG, "test alarm armed for +60s")
  }

  /** Whether exact alarms are currently permitted, for the diagnostics card. */
  fun canScheduleExactAlarms(context: Context): Boolean {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
      ?: return false
    return canScheduleExact(alarmManager)
  }

  fun cancelAll(
    context: Context,
    alarmManager: AlarmManager? =
      context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
  ) {
    val manager = alarmManager ?: return
    AlertIds.all().forEach { requestCode ->
      val intent = baseIntent(context, requestCode)
      val pending = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
      )
      if (pending != null) {
        manager.cancel(pending)
        pending.cancel()
      }
    }
  }

  private fun arm(
    context: Context,
    alarmManager: AlarmManager,
    alert: ScheduledAlert,
    discreet: Boolean
  ) {
    val pending = PendingIntent.getBroadcast(
      context,
      alert.requestCode,
      intentFor(context, alert),
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    if (!canScheduleExact(alarmManager)) {
      // A one-minute window is far better than nothing while the user has not granted the
      // exact-alarm permission.
      alarmManager.setWindow(
        AlarmManager.RTC_WAKEUP,
        alert.triggerAtMillis,
        60_000L,
        pending
      )
      return
    }

    val usesAlarmClock = alert is ScheduledAlert.Prayer && !discreet
    if (usesAlarmClock) {
      val show = PendingIntent.getActivity(
        context,
        alert.requestCode,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      alarmManager.setAlarmClock(
        AlarmManager.AlarmClockInfo(alert.triggerAtMillis, show),
        pending
      )
    } else {
      AlarmManagerCompat.setExactAndAllowWhileIdle(
        alarmManager,
        AlarmManager.RTC_WAKEUP,
        alert.triggerAtMillis,
        pending
      )
    }
  }

  /**
   * The shell of an alert intent: explicit component, unique data, and the action.
   *
   * All three matter. Extras are *not* part of PendingIntent identity but action and data are, so
   * a cancel has to rebuild the same action the arm used - otherwise FLAG_NO_CREATE finds nothing
   * and the old alarm survives, which is how duplicates accumulate.
   */
  private fun baseIntent(context: Context, requestCode: Int) =
    Intent(context, PrayerAlarmReceiver::class.java).apply {
      action = actionFor(requestCode)
      data = Uri.parse("haasi://alert/$requestCode")
    }

  private fun actionFor(requestCode: Int): String = when {
    requestCode >= AlertIds.BASE_MAINTENANCE -> PrayerAlarmReceiver.ACTION_DAILY_REFRESH
    requestCode >= AlertIds.BASE_TAHAJJUD -> PrayerAlarmReceiver.ACTION_TAHAJJUD_ALERT
    requestCode >= AlertIds.BASE_FORBIDDEN -> PrayerAlarmReceiver.ACTION_FORBIDDEN_ALERT
    else -> PrayerAlarmReceiver.ACTION_PRAYER_ALERT
  }

  private fun intentFor(context: Context, alert: ScheduledAlert): Intent {
    val intent = baseIntent(context, alert.requestCode)
    when (alert) {
      is ScheduledAlert.Prayer -> {
        intent.putExtra(PrayerAlarmReceiver.EXTRA_PRAYER, alert.prayer.name)
        intent.putExtra(PrayerAlarmReceiver.EXTRA_KIND, alert.kind.name)
      }

      is ScheduledAlert.Forbidden -> {
        intent.putExtra(PrayerAlarmReceiver.EXTRA_WINDOW, alert.type.name)
        intent.putExtra(PrayerAlarmReceiver.EXTRA_IS_START, alert.isStart)
      }

      is ScheduledAlert.Tahajjud, is ScheduledAlert.DailyRefresh -> Unit
    }
    // Lets a late-firing alarm tell that it is stale rather than announcing a passed prayer.
    intent.putExtra(
      PrayerAlarmReceiver.EXTRA_EPOCH_DAY,
      alert.triggerAtMillis / (24 * 60 * 60 * 1000L)
    )
    return intent
  }

  private fun canScheduleExact(alarmManager: AlarmManager): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

  /** Everything that changes computed times or gating. Language is absent: copy is resolved at
   *  fire time, so a language change needs no re-arm. */
  private fun signature(context: Context, prefs: UserPreferences): String {
    val city = prefs.selectedCity ?: PrayerCalculator.PRESET_CITIES[0]
    return listOf(
      city.name,
      city.latitude,
      city.longitude,
      city.timeZoneId,
      prefs.calculationMethod.name,
      prefs.madhab.name,
      PrayerNotificationManager.isPrayerNotificationsEnabled(context),
      PrayerNotificationManager.isFollowUpNotificationsEnabled(context),
      PrayerNotificationManager.isForbiddenNotificationsEnabled(context),
      PrayerNotificationManager.isTahajjudNotificationsEnabled(context),
      prefs.discreetAlarmMode
    ).joinToString("|")
  }

  private fun zoneOf(id: String): ZoneId = try {
    ZoneId.of(id)
  } catch (_: Exception) {
    ZoneId.systemDefault()
  }
}
