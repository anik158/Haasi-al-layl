package com.example

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat
import com.example.data.localization.AppLanguage
import com.example.data.model.PrayerName
import com.example.data.notification.AlarmSoundPlayer
import com.example.ui.screens.PrayerAlarmScreen
import com.example.ui.theme.HaasiTheme

/**
 * The screen a prayer alarm puts in front of you, and the only thing that stops it ringing.
 *
 * Reached through a notification's full-screen intent rather than `startActivity` from the
 * receiver: background activity launches have been blocked since Android 10, and a full-screen
 * intent is the sanctioned path for exactly this case - an alarm that must show itself over the
 * lock screen. The notification is the vehicle, not the alert.
 *
 * `singleInstance` with `excludeFromRecents`: one alarm screen at a time, and a spent alarm has no
 * business sitting in the recents list afterwards. A second alarm firing while this one rings
 * arrives at [onNewIntent] and replaces the content rather than stacking a second copy.
 */
class PrayerAlarmActivity : ComponentActivity() {

  private var content by mutableStateOf(AlarmContent.EMPTY)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    showOverLockScreen()
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
    )

    content = AlarmContent.from(intent)
    beginRinging()

    setContent {
      HaasiTheme {
        val current = content
        PrayerAlarmScreen(
          title = current.title,
          body = current.body,
          arabicName = current.arabicName,
          language = current.language,
          onStop = { stopAndFinish() }
        )
      }
    }
  }

  /**
   * A second alarm arriving mid-ring. The tone is already going - [AlarmSoundPlayer.start] is
   * idempotent - so only the words on screen and the notification to clear change.
   */
  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    content = AlarmContent.from(intent)
    beginRinging()
  }

  private fun beginRinging() {
    // The notification did its job by launching this; leaving it in the shade would give the
    // user a second, silent copy of an alert they are already looking at.
    if (content.notificationId != 0) {
      NotificationManagerCompat.from(this).cancel(content.notificationId)
    }
    // Ordinarily already ringing - PrayerNotificationManager starts the tone as it posts, so it
    // sounds even when the system downgrades the full-screen intent to a banner. This call is
    // the idempotent safety net for the path where that did not happen.
    AlarmSoundPlayer.start(this)
  }

  private fun stopAndFinish() {
    AlarmSoundPlayer.stop()
    finish()
  }

  /**
   * Stops the tone whenever this screen goes away, however it goes away - Back, Home, or the
   * system killing it. Without this a swipe away from the alarm would leave it ringing with
   * nothing on screen to stop it.
   */
  override fun onDestroy() {
    super.onDestroy()
    if (isFinishing) AlarmSoundPlayer.stop()
  }

  private fun showOverLockScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
      setShowWhenLocked(true)
      setTurnScreenOn(true)
      // Only dismisses an *insecure* keyguard; a PIN or fingerprint stays in the way, which is
      // correct - the alarm is visible over it either way.
      (getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager)
        ?.requestDismissKeyguard(this, null)
    } else {
      @Suppress("DEPRECATION")
      window.addFlags(
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
          WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
          WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
      )
    }
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
  }

  /**
   * What this screen says, carried on the intent.
   *
   * The copy is passed in rather than recomputed here: unlike the alarm's *extras*, which are
   * identity keys because they are written when the alarm is armed and read up to 48 hours later,
   * this intent is built at fire time by the receiver that has just recomputed everything. There
   * is no staleness to guard against, and one copy of the wording is better than two.
   */
  private data class AlarmContent(
    val title: String,
    val body: String,
    val arabicName: String?,
    val language: AppLanguage,
    val notificationId: Int
  ) {
    companion object {
      val EMPTY = AlarmContent("", "", null, AppLanguage.ENGLISH, 0)

      fun from(intent: Intent?): AlarmContent {
        intent ?: return EMPTY
        return AlarmContent(
          title = intent.getStringExtra(EXTRA_TITLE).orEmpty(),
          body = intent.getStringExtra(EXTRA_BODY).orEmpty(),
          arabicName = intent.getStringExtra(EXTRA_PRAYER)
            ?.let { name -> PrayerName.entries.find { it.name == name } }
            ?.arabicName,
          language = intent.getStringExtra(EXTRA_LANGUAGE)
            ?.let { AppLanguage.fromCode(it) }
            ?: AppLanguage.ENGLISH,
          notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        )
      }
    }
  }

  companion object {
    const val EXTRA_TITLE = "alarm_title"
    const val EXTRA_BODY = "alarm_body"
    const val EXTRA_PRAYER = "alarm_prayer"
    const val EXTRA_LANGUAGE = "alarm_language"
    const val EXTRA_NOTIFICATION_ID = "alarm_notification_id"

    /**
     * The intent a notification's full-screen intent wraps.
     *
     * `CLEAR_TOP` alongside `singleInstance` so a repeat alert reuses the one instance instead of
     * building a second on top of it.
     */
    fun intent(
      context: Context,
      title: String,
      body: String,
      prayer: PrayerName?,
      language: AppLanguage,
      notificationId: Int
    ): Intent = Intent(context, PrayerAlarmActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or
        Intent.FLAG_ACTIVITY_CLEAR_TOP or
        Intent.FLAG_ACTIVITY_NO_USER_ACTION
      putExtra(EXTRA_TITLE, title)
      putExtra(EXTRA_BODY, body)
      putExtra(EXTRA_PRAYER, prayer?.name)
      putExtra(EXTRA_LANGUAGE, language.code)
      putExtra(EXTRA_NOTIFICATION_ID, notificationId)
    }
  }
}
