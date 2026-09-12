package com.example

import android.app.Application
import com.example.data.notification.PrayerAlarmScheduler
import com.example.data.notification.PrayerNotificationManager

/**
 * Runs on every process start, including ones woken by a broadcast rather than by the user.
 *
 * Creating channels here is what fixes alerts being posted to a channel that was never created -
 * previously only two of the three `show*` functions created channels lazily, and the Tahajjud
 * one did not, so on API 26+ the system dropped it silently.
 *
 * [PrayerAlarmScheduler.ensureScheduled] is a no-op unless the day or the settings signature
 * changed, so this stays cheap even though it runs on every cold start.
 */
class HaasiApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    PrayerNotificationManager.ensureChannels(this)
    PrayerAlarmScheduler.ensureScheduled(this)
  }
}
