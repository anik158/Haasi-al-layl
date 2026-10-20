package com.example

import android.app.Application
import com.example.data.notification.PrayerAlarmScheduler
import com.example.data.notification.PrayerNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Runs on every process start, including ones woken by a broadcast rather than by the user.
 *
 * Creating channels here is what fixes alerts being posted to a channel that was never created -
 * previously only two of the three `show*` functions created channels lazily, and the Tahajjud
 * one did not, so on API 26+ the system dropped it silently.
 *
 * Scheduling is pushed off the main thread: arming a day of alarms means three prayer-time
 * computations, roughly thirty PendingIntents and a preferences commit, none of which should sit
 * between the user tapping the icon and the first frame.
 */
class HaasiApplication : Application() {

  private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  override fun onCreate() {
    super.onCreate()
    // Before anything computes a Hijri date, including an alarm receiver's Ramadan Isha rule:
    // builds the month table from the cached announcements and the user's corrections.
    // A restart is always real time: drop anything a debug test date saved, first.
    com.example.data.hijri.HijriStore(this).dropTestRecords(System.currentTimeMillis())
    com.example.data.preferences.UserPreferences(this).applyHijriAdjustment()
    PrayerNotificationManager.ensureChannels(this)
    appScope.launch {
      // A no-op unless the day or the settings signature changed.
      PrayerAlarmScheduler.ensureScheduled(this@HaasiApplication)
      // Saudi Arabia's (or Turkey's) announced month starts, when due and online. A changed
      // table can move Ramadan, so the alarms follow.
      if (com.example.data.hijri.HijriCalendar.refreshAnnouncements(this@HaasiApplication)) {
        PrayerAlarmScheduler.rescheduleAll(this@HaasiApplication)
      }
    }
  }
}
