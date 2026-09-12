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
 * `onReceive` stays fully synchronous: PrayerCalculator is pure, and the one parameter that would
 * pull in Room (`completedPrayerIds`) only feeds a display flag notifications never read. No
 * coroutine, no `goAsync()`.
 */
class PrayerAlarmReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent?) {
    val action = intent?.action
    try {
      if (action != ACTION_DAILY_REFRESH) {
        dispatch(context, action, intent)
      }
    } catch (e: Exception) {
      Log.e(TAG, "failed to post alert for $action", e)
    } finally {
      // The self-extending chain: as long as one alarm fires, the window keeps rolling forward
      // with no app launch required.
      PrayerAlarmScheduler.rescheduleAll(context)
    }
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
          windowTitle = type.id,
          rangeFormatted = range,
          isStart = isStart,
          language = language
        )
      }

      ACTION_TAHAJJUD_ALERT ->
        PrayerNotificationManager.showTahajjudNotification(context, language)

      else -> Log.w(TAG, "unrecognised action: $action")
    }
  }

  /**
   * Reads the prayer log directly. This is a blocking query on the broadcast thread, which is
   * acceptable for a single indexed row read inside the ten-second `onReceive` budget, and avoids
   * `goAsync()` for the one case that needs the database.
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

    const val EXTRA_PRAYER = "prayer"
    const val EXTRA_KIND = "kind"
    const val EXTRA_WINDOW = "window"
    const val EXTRA_IS_START = "is_start"
    const val EXTRA_EPOCH_DAY = "epoch_day"
  }
}
