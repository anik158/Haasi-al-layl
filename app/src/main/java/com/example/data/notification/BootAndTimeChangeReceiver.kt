package com.example.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Re-arms alarms after anything that invalidates them.
 *
 * Alarms do not survive a reboot, an app upgrade, or a clock change, so each of those has to
 * rebuild the schedule. A timezone change matters more than it looks: a traveller's whole day is
 * re-derived from it.
 *
 * Exported, because BOOT_COMPLETED is only delivered reliably to exported receivers. That carries
 * no injection risk here: every action handled is a protected broadcast only the system can send,
 * and anything unrecognised is ignored.
 *
 * LOCKED_BOOT_COMPLETED is deliberately absent. Preferences live in credential-encrypted storage
 * and are unreadable before first unlock, so a direct-boot receiver would read an empty
 * UserPreferences and schedule Makkah times for everyone. The consequence is real but narrow: a
 * device rebooted at 3am and left locked until 8am misses that day's Fajr.
 */
class BootAndTimeChangeReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent?) {
    when (intent?.action) {
      Intent.ACTION_BOOT_COMPLETED,
      Intent.ACTION_MY_PACKAGE_REPLACED,
      Intent.ACTION_TIME_CHANGED,
      Intent.ACTION_TIMEZONE_CHANGED,
      ACTION_EXACT_ALARM_PERMISSION_STATE_CHANGED -> {
        Log.i(TAG, "re-arming alarms after ${intent.action}")
        PrayerNotificationManager.ensureChannels(context)
        PrayerAlarmScheduler.rescheduleAll(context)
      }

      else -> Unit
    }
  }

  private companion object {
    const val TAG = "BootAndTimeChange"

    /** `AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED`, which is API 31+. */
    const val ACTION_EXACT_ALARM_PERMISSION_STATE_CHANGED =
      "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
  }
}
