package com.example.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.data.model.PrayerName
import com.example.data.prayer.ForbiddenWindowType
import com.example.data.prayer.PrayerCalculator
import com.example.data.preferences.UserPreferences
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Fires one alert, then re-arms the schedule.
 *
 * The intent carries **identity keys only** - enum names and an epoch day. Everything shown is
 * recomputed here from preferences, so an alert stays correct after a language change, a
 * calculation-method change, or a reboot between arming and firing.
 *
 * The work runs on a background thread under `goAsync()`. PrayerCalculator is pure and needs no
 * database - the one parameter that would pull in Room (`completedPrayerIds`) only feeds a display
 * flag notifications never read - but the follow-up still has to check the prayer log, and
 * re-arming the schedule is not free either.
 */
class PrayerAlarmReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent?) {
    val action = intent?.action
    // Posting the alert reads the prayer log and recomputes three days of times, and re-arming
    // builds roughly thirty PendingIntents. goAsync keeps that off the main thread while holding
    // the broadcast alive, instead of blocking it inside onReceive's ten-second budget.
    val pendingResult = goAsync()
    val appContext = context.applicationContext
    Thread {
      try {
        if (action != ACTION_DAILY_REFRESH) {
          dispatch(appContext, action, intent)
        } else {
          // A new day: move the month table's window, and fetch the announced starts if due.
          com.example.data.hijri.HijriCalendar.rebuild(appContext)
          com.example.data.hijri.HijriCalendar.refreshAnnouncements(appContext)
        }
      } catch (e: Exception) {
        Log.e(TAG, "failed to post alert for $action", e)
      } finally {
        try {
          // The self-extending chain: as long as one alarm fires, the window keeps rolling
          // forward with no app launch required.
          PrayerAlarmScheduler.rescheduleAll(appContext)
        } catch (e: Exception) {
          Log.e(TAG, "failed to reschedule after $action", e)
        }
        pendingResult.finish()
      }
    }.start()
  }

  private fun dispatch(context: Context, action: String?, intent: Intent?) {
    intent ?: return
    val prefs = UserPreferences(context)
    val city = prefs.selectedCity ?: PrayerCalculator.PRESET_CITIES[0]
    val zone = try {
      ZoneId.of(city.timeZoneId)
    } catch (_: Exception) {
      ZoneId.systemDefault()
    }
    val today = LocalDate.now(zone)

    // An alarm that fired hours late - the device was off - should not announce a prayer that
    // passed this morning.
    val armedFor = intent.getLongExtra(EXTRA_EPOCH_DAY, today.toEpochDay())
    if (today.toEpochDay() - armedFor > 1) {
      Log.i(TAG, "skipping stale alert from epoch day $armedFor")
      return
    }

    val language = prefs.manualLanguage
      ?: LocalizationManager.detectLanguageForCountry(city.country)

    val schedule = PrayerCalculator.calculateSchedule(
      date = today,
      location = city,
      now = LocalDateTime.now(zone),
      completedPrayerIds = emptySet(),
      method = prefs.calculationMethod,
      madhab = prefs.madhab
    )

    when (action) {
      ACTION_PRAYER_ALERT -> {
        val prayer = intent.getStringExtra(EXTRA_PRAYER)
          ?.let { name -> PrayerName.entries.find { it.name == name } } ?: return
        val kind = intent.getStringExtra(EXTRA_KIND)
          ?.let { name -> PrayerAlertKind.entries.find { it.name == name } } ?: return
        val item = schedule.items.find { it.prayer == prayer } ?: return

        // The follow-up is a nudge, so it has nothing to say once the prayer is marked done.
        if (kind == PrayerAlertKind.FOLLOW_UP && isAlreadyPrayed(context, today, prayer)) {
          Log.i(TAG, "skipping ${prayer.name} follow-up: already marked prayed")
          return
        }

        PrayerNotificationManager.showPrayerAlert(
          context = context,
          prayer = prayer,
          kind = kind,
          timeFormatted = item.time.format(TIME_FORMAT),
          language = language
        )
      }

      ACTION_FORBIDDEN_ALERT -> {
        val type = intent.getStringExtra(EXTRA_WINDOW)
          ?.let { name -> ForbiddenWindowType.entries.find { it.name == name } } ?: return
        val isStart = intent.getBooleanExtra(EXTRA_IS_START, true)
        val window = schedule.forbiddenTimes.find { it.type == type } ?: return
        val range = "${window.startTime.format(TIME_FORMAT)} - " +
          window.endTime.format(TIME_FORMAT)

        PrayerNotificationManager.showForbiddenTimeNotification(
          context = context,
          windowTitle = window.getLocalizedTitle(language),
          rangeFormatted = range,
          isStart = isStart,
          language = language
        )
      }

      ACTION_TAHAJJUD_ALERT ->
        PrayerNotificationManager.showTahajjudNotification(context, language)

      ACTION_SEHRI_ALERT -> {
        // Recomputed for the fast's own day: the alert can fire before midnight for an early dawn.
        val fastDay = LocalDate.ofEpochDay(intent.getLongExtra(EXTRA_FAST_DAY, today.toEpochDay()))
        val sehriEnd = PrayerCalculator.calculateSchedule(
          date = fastDay,
          location = city,
          now = LocalDateTime.now(zone),
          completedPrayerIds = emptySet(),
          method = prefs.calculationMethod,
          madhab = prefs.madhab
        ).items.firstOrNull { it.prayer == PrayerName.FAJR }?.time ?: return
        val minutesLeft = java.time.Duration.between(
          LocalDateTime.now(zone),
          LocalDateTime.of(fastDay, sehriEnd)
        ).toMinutes().toInt()
        if (minutesLeft <= 0) return
        PrayerNotificationManager.showSehriAlert(
          context = context,
          sehriEndFormatted = LocalizationManager.formatTime(sehriEnd, language),
          minutesLeft = minutesLeft,
          language = language
        )
      }

      else -> Log.w(TAG, "unrecognised action: $action")
    }
  }

  /**
   * Reads the prayer log directly. Blocking is fine here: the caller already moved off the main
   * thread under `goAsync()`, and this is a single indexed row read.
   */
  private fun isAlreadyPrayed(context: Context, date: LocalDate, prayer: PrayerName): Boolean =
    try {
      val dao = com.example.data.local.HaasiAlLaylDatabase
        .getDatabase(context.applicationContext)
        .haasiAlLaylDao()
      val log = kotlinx.coroutines.runBlocking { dao.getPrayerLogOnce(date.toString()) }
      when (prayer) {
        PrayerName.FAJR -> log?.fajrDone
        PrayerName.DHUHR -> log?.dhuhrDone
        PrayerName.ASR -> log?.asrDone
        PrayerName.MAGHRIB -> log?.maghribDone
        PrayerName.ISHA -> log?.ishaDone
        else -> false
      } ?: false
    } catch (e: Exception) {
      Log.w(TAG, "could not read the prayer log; sending the follow-up anyway", e)
      false
    }

  companion object {
    private const val TAG = "PrayerAlarmReceiver"
    private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    const val ACTION_PRAYER_ALERT = "com.example.haasi.alarm.PRAYER_ALERT"
    const val ACTION_FORBIDDEN_ALERT = "com.example.haasi.alarm.FORBIDDEN_ALERT"
    const val ACTION_TAHAJJUD_ALERT = "com.example.haasi.alarm.TAHAJJUD_ALERT"
    const val ACTION_DAILY_REFRESH = "com.example.haasi.alarm.DAILY_REFRESH"
    const val ACTION_SEHRI_ALERT = "com.example.haasi.alarm.SEHRI_ALERT"

    const val EXTRA_PRAYER = "prayer"
    const val EXTRA_KIND = "kind"
    const val EXTRA_WINDOW = "window"
    const val EXTRA_IS_START = "is_start"
    const val EXTRA_EPOCH_DAY = "epoch_day"
    const val EXTRA_FAST_DAY = "fast_day"
  }
}
