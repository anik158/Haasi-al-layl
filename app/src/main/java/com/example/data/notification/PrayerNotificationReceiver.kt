package com.example.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.localization.AppLanguage

class PrayerNotificationReceiver : BroadcastReceiver() {

  companion object {
    const val ACTION_PRAYER = "com.example.haasi.ACTION_PRAYER_NOTIFICATION"
    const val ACTION_FORBIDDEN = "com.example.haasi.ACTION_FORBIDDEN_TIME_NOTIFICATION"

    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_ARABIC_NAME = "extra_arabic_name"
    const val EXTRA_TIME_FORMATTED = "extra_time_formatted"
    const val EXTRA_CITY_NAME = "extra_city_name"
    const val EXTRA_WINDOW_TITLE = "extra_window_title"
    const val EXTRA_RANGE_FORMATTED = "extra_range_formatted"
    const val EXTRA_IS_START = "extra_is_start"
    const val EXTRA_LANGUAGE_CODE = "extra_language_code"
  }

  override fun onReceive(context: Context, intent: Intent?) {
    if (intent == null) return

    val langCode = intent.getStringExtra(EXTRA_LANGUAGE_CODE) ?: "en"
    val language = AppLanguage.fromCode(langCode)

    when (intent.action) {
      ACTION_PRAYER -> {
        if (!PrayerNotificationManager.isPrayerNotificationsEnabled(context)) return
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Prayer"
        val arabicName = intent.getStringExtra(EXTRA_ARABIC_NAME) ?: ""
        val timeFormatted = intent.getStringExtra(EXTRA_TIME_FORMATTED) ?: ""
        val cityName = intent.getStringExtra(EXTRA_CITY_NAME) ?: ""

        PrayerNotificationManager.showPrayerNotification(
          context = context,
          prayerName = prayerName,
          arabicName = arabicName,
          timeFormatted = timeFormatted,
          cityName = cityName,
          language = language
        )
      }

      ACTION_FORBIDDEN -> {
        if (!PrayerNotificationManager.isForbiddenNotificationsEnabled(context)) return
        val windowTitle = intent.getStringExtra(EXTRA_WINDOW_TITLE) ?: "Forbidden Window"
        val rangeFormatted = intent.getStringExtra(EXTRA_RANGE_FORMATTED) ?: ""
        val isStart = intent.getBooleanExtra(EXTRA_IS_START, true)

        PrayerNotificationManager.showForbiddenTimeNotification(
          context = context,
          windowTitle = windowTitle,
          rangeFormatted = rangeFormatted,
          isStart = isStart,
          language = language
        )
      }
    }
  }
}
