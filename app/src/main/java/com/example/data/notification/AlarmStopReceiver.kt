package com.example.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

/**
 * Silences a ringing alarm from its notification, without opening anything.
 *
 * The full-screen alarm screen is the primary way to stop the ring, but it is not always the one
 * on screen: when the device is already unlocked and in use, the system downgrades a full-screen
 * intent to a heads-up banner. The banner's "Stop" has to work on its own terms, and sending the
 * user through a full-screen alarm just to dismiss an alert they have already seen would be
 * absurd.
 *
 * Not exported and with no intent-filter: it is only ever reached through this app's own
 * PendingIntent.
 */
class AlarmStopReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent?) {
    if (intent?.action != ACTION_STOP_ALARM) return
    AlarmSoundPlayer.stop()
    val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
    if (notificationId != 0) {
      NotificationManagerCompat.from(context).cancel(notificationId)
    }
  }

  companion object {
    const val ACTION_STOP_ALARM = "com.example.haasi.alarm.STOP_ALARM"
    const val EXTRA_NOTIFICATION_ID = "notification_id"

    fun intent(context: Context, notificationId: Int): Intent =
      Intent(context, AlarmStopReceiver::class.java).apply {
        action = ACTION_STOP_ALARM
        putExtra(EXTRA_NOTIFICATION_ID, notificationId)
      }
  }
}
