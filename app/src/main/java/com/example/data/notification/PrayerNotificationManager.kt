package com.example.data.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.data.model.DailyPrayerSchedule
import com.example.data.model.PrayerName
import com.example.data.prayer.ForbiddenTimeWindow
import com.example.data.prayer.ForbiddenWindowType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object PrayerNotificationManager {

  private const val TAG = "HaasiNotifications"

  // A channel's sound, importance and vibration are frozen at creation on API 26+, so changing
  // any of them means a new id. Bump ONLY for such a change, never for copy - each bump costs
  // the user their per-channel customisations.
  private const val PREFIX_PRAYER = "haasi_prayer_alerts"
  private const val PREFIX_ALARM = "haasi_prayer_alarm"
  private const val PREFIX_FORBIDDEN = "haasi_forbidden_alerts"
  private const val PREFIX_TAHAJJUD = "haasi_tahajjud_alerts"
  // v3/v3/v2: the tone changed to the forest birdsong recording, and the stored URI moved from
  // the numeric resource id to the resource name - both frozen channel attributes.
  private const val PRAYER_CHANNEL_VERSION = 3
  private const val FORBIDDEN_CHANNEL_VERSION = 3
  private const val TAHAJJUD_CHANNEL_VERSION = 2

  const val CHANNEL_PRAYER_ID = "${PREFIX_PRAYER}_v$PRAYER_CHANNEL_VERSION"
  const val CHANNEL_FORBIDDEN_ID = "${PREFIX_FORBIDDEN}_v$FORBIDDEN_CHANNEL_VERSION"
  const val CHANNEL_TAHAJJUD_ID = "${PREFIX_TAHAJJUD}_v$TAHAJJUD_CHANNEL_VERSION"

  // PREFIX_ALARM is retained with no live channel so that ensureChannels' sweep *deletes* the
  // silent "Prayer Alarm" channel shipped briefly alongside the full-screen alarm. It was a bad
  // idea: it relied on the alarm screen for all audio, and when Android downgraded the
  // full-screen intent to a heads-up banner the result was an alert with no sound at all.
  private val CHANNEL_PREFIXES =
    listOf(PREFIX_PRAYER, PREFIX_ALARM, PREFIX_FORBIDDEN, PREFIX_TAHAJJUD)

  private const val PREFS_NAME = "haasi_notifications_prefs"
  private const val KEY_PRAYER_NOTIFS = "key_prayer_notifs"
  private const val KEY_FORBIDDEN_NOTIFS = "key_forbidden_notifs"
  private const val KEY_TAHAJJUD_NOTIFS = "key_tahajjud_notifs"
  private const val KEY_FOLLOW_UP_NOTIFS = "key_follow_up_notifs"

  private const val NOTIF_ID_PRAYER_BASE = 1000
  private const val NOTIF_ID_FORBIDDEN_START = 2000
  private const val NOTIF_ID_FORBIDDEN_END = 2100

  /**
   * The alert tone. USAGE_ALARM routes it to the alarm stream, which survives silent mode.
   *
   * Addressed by resource *name*, not `R.raw.haasi_alert`. A channel stores this URI permanently,
   * and numeric resource ids are reassigned when resources are added or renamed - so the numeric
   * form would one day point a live channel at the wrong file, or at nothing, with no error.
   */
  fun alertSoundUri(context: Context): android.net.Uri =
    android.net.Uri.parse("android.resource://${context.packageName}/raw/haasi_alert")

  /**
   * Creates the current channels and deletes superseded ones. Idempotent, and safe to call on
   * every process start.
   *
   * Without the sweep, the user's notification settings accumulate one dead "Prayer Time
   * Notifications" row per version shipped.
   */
  fun ensureChannels(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val audioAttributes = android.media.AudioAttributes.Builder()
      .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
      .setUsage(android.media.AudioAttributes.USAGE_ALARM)
      .build()
    val sound = alertSoundUri(context)

    val current = listOf(
      NotificationChannel(
        CHANNEL_PRAYER_ID,
        "Prayer Time Notifications",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Alerts when each Islamic prayer time begins"
        enableVibration(true)
        enableLights(true)
        setSound(sound, audioAttributes)
      },
      NotificationChannel(
        CHANNEL_FORBIDDEN_ID,
        "Forbidden Prayer Time Alerts",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Alerts when prohibited prayer windows start and end"
        enableVibration(true)
        enableLights(true)
        setSound(sound, audioAttributes)
      },
      // Its own channel so a 3am alert can be silenced without losing Fajr.
      NotificationChannel(
        CHANNEL_TAHAJJUD_ID,
        "Tahajjud Reminders",
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = "A reminder when the last third of the night begins"
        enableVibration(true)
        setSound(sound, audioAttributes)
      }
    )
    current.forEach { manager.createNotificationChannel(it) }

    val liveIds = current.map { it.id }.toSet()
    manager.notificationChannels
      .filter { channel -> CHANNEL_PREFIXES.any { channel.id.startsWith(it) } && channel.id !in liveIds }
      .forEach { manager.deleteNotificationChannel(it.id) }
  }

  @Deprecated("Use ensureChannels", ReplaceWith("ensureChannels(context)"))
  fun createNotificationChannels(context: Context) = ensureChannels(context)

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  fun isPrayerNotificationsEnabled(context: Context): Boolean {
    return getPrefs(context).getBoolean(KEY_PRAYER_NOTIFS, true)
  }

  fun setPrayerNotificationsEnabled(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_PRAYER_NOTIFS, enabled).apply()
  }

  fun isForbiddenNotificationsEnabled(context: Context): Boolean {
    return getPrefs(context).getBoolean(KEY_FORBIDDEN_NOTIFS, true)
  }

  fun setForbiddenNotificationsEnabled(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_FORBIDDEN_NOTIFS, enabled).apply()
  }

  fun isTahajjudNotificationsEnabled(context: Context): Boolean =
    getPrefs(context).getBoolean(KEY_TAHAJJUD_NOTIFS, true)

  fun setTahajjudNotificationsEnabled(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_TAHAJJUD_NOTIFS, enabled).apply()
  }

  /** The five-minute nudge after each prayer, independent of the start alert. */
  fun isFollowUpNotificationsEnabled(context: Context): Boolean =
    getPrefs(context).getBoolean(KEY_FOLLOW_UP_NOTIFS, true)

  fun setFollowUpNotificationsEnabled(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_FOLLOW_UP_NOTIFS, enabled).apply()
  }


  /**
   * Whether a full-screen intent will actually be honoured.
   *
   * Android 14 narrowed `USE_FULL_SCREEN_INTENT` to apps that provide calling or alarm
   * functionality; everything else has to be granted it by the user. A prayer alarm qualifies on
   * its face, but "qualifies" is the system's call, not ours, so this asks rather than assumes -
   * and when the answer is no the caller falls back to the ordinary audible notification instead
   * of posting a silent one nobody hears.
   */
  fun canUseFullScreenAlarm(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return true
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      ?: return false
    return manager.canUseFullScreenIntent()
  }

  /**
   * Posts an alert that is audible on its own **and** carries a full-screen intent.
   *
   * The ordering of responsibilities here was got wrong once and is worth stating plainly. The
   * first attempt put the alert on a silent channel and left all the audio to
   * [AlarmSoundPlayer], started from the receiver. That works only if the alarm screen actually
   * opens - and Android downgrades a full-screen intent to a heads-up banner whenever the device
   * is unlocked and in use, while a MediaPlayer started from a `BroadcastReceiver` has no live
   * component keeping its process alive once `onReceive` returns. Both failed together, and the
   * alert made no sound at all.
   *
   * So the notification carries the sound, exactly as it did before the alarm screen existed -
   * the system plays it, which needs nothing of this process. The full-screen intent is a
   * *addition*: when it is honoured, [com.example.PrayerAlarmActivity] opens, cancels this
   * notification and takes over with a looping tone that rings until it is stopped. When it is
   * not honoured, the behaviour is exactly the previously working notification.
   */
  private fun showFullScreenAlarm(
    context: Context,
    channelId: String,
    notificationId: Int,
    title: String,
    body: String,
    prayer: PrayerName?,
    language: AppLanguage
  ) {
    ensureChannels(context)

    val alarmIntent = com.example.PrayerAlarmActivity.intent(
      context = context,
      title = title,
      body = body,
      prayer = prayer,
      language = language,
      notificationId = notificationId
    )
    val fullScreen = PendingIntent.getActivity(
      context,
      notificationId,
      alarmIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, channelId)
      .setSmallIcon(R.drawable.ic_notification)
      .setContentTitle(title)
      .setContentText(body)
      .setStyle(NotificationCompat.BigTextStyle().bigText(body))
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .setAutoCancel(true)
      // Channels own the sound from API 26+; below that the builder is the only path, and
      // minSdk is 24. Either way the *system* plays it, so it does not depend on this process
      // surviving the broadcast.
      .setSound(alertSoundUri(context))
      .setContentIntent(fullScreen)
      .setFullScreenIntent(fullScreen, /* highPriority = */ true)
      // A broadcast, not the activity: when the system downgrades the full-screen intent to a
      // heads-up banner, "Stop" has to silence the ring where the user is, not take over the
      // display to do it.
      .addAction(
        R.drawable.ic_notification,
        LocalizationManager.getStopAlarmAction(language),
        PendingIntent.getBroadcast(
          context,
          notificationId,
          AlarmStopReceiver.intent(context, notificationId),
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
      )
      // The ring stops when the notification is cleared by any route, including Clear all.
      .setDeleteIntent(
        PendingIntent.getBroadcast(
          context,
          notificationId,
          AlarmStopReceiver.intent(context, notificationId),
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
      )

    try {
      NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    } catch (e: SecurityException) {
      android.util.Log.w(TAG, "notify denied - POST_NOTIFICATIONS not granted", e)
    }
  }

  fun showTahajjudNotification(context: Context, language: AppLanguage) {
    if (!isTahajjudNotificationsEnabled(context)) return
    // This was missing, so on API 26+ the notification was posted to a channel that had never
    // been created and the system dropped it silently.
    ensureChannels(context)

    val title = when (language) {
      AppLanguage.BENGALI -> "তাহাজ্জুদের সময় হয়েছে"
      AppLanguage.ARABIC -> "حان وقت التهجد"
      AppLanguage.ENGLISH -> "Tahajjud Time"
      else -> "Tahajjud Time"
    }

    val message = when (language) {
      AppLanguage.BENGALI -> "আল্লাহ সুবহানাহু ওয়া তায়ালা প্রথম আসমানে নেমে এসেছেন, আপনার যা খুশি চাইতে পারেন।"
      AppLanguage.ARABIC -> "لقد نزل الله سبحانه وتعالى إلى السماء الدنيا، فاسألوا ما شئتم."
      AppLanguage.ENGLISH -> "Allah swt has come down to 1st heaven, ask whatever you want."
      else -> "Allah swt has come down to 1st heaven, ask whatever you want."
    }

    // The whole point of a Tahajjud reminder is to wake someone in the last third of the night,
    // which a row in the notification shade cannot do. Same treatment as a prayer time, on its
    // own channel so a 3am alert can still be silenced without losing Fajr.
    showFullScreenAlarm(
      context = context,
      channelId = CHANNEL_TAHAJJUD_ID,
      notificationId = AlertIds.TAHAJJUD,
      title = title,
      body = message,
      prayer = null,
      language = language
    )
  }

  /**
   * Posts one prayer alert.
   *
   * Keyed on [PrayerName] rather than a display string, so the notification id is stable across
   * languages - the old scheme hashed the localized name, giving the same prayer a different id
   * in each language, with collisions unguarded inside a 100-slot space.
   */
  fun showPrayerAlert(
    context: Context,
    prayer: PrayerName,
    kind: PrayerAlertKind,
    timeFormatted: String,
    language: AppLanguage
  ) {
    if (!isPrayerNotificationsEnabled(context)) return
    if (kind == PrayerAlertKind.FOLLOW_UP && !isFollowUpNotificationsEnabled(context)) return
    ensureChannels(context)

    val title = PrayerAlertMessages.title(prayer, kind, language, timeFormatted)
    val body = PrayerAlertMessages.body(prayer, kind, language)

    // The prayer time itself gets the alarm screen; the five-minute nudge stays an ordinary
    // notification, because taking over the display twice for one prayer would make the reminder
    // something to switch off rather than something to welcome.
    //
    // Note there is no canUseFullScreenAlarm() gate here. Attaching a full-screen intent that
    // the system declines to honour costs nothing - it simply shows as a heads-up notification,
    // which is what this is anyway. Gating on it meant a different, quieter code path on exactly
    // the devices where the alarm most needed to be loud.
    if (kind == PrayerAlertKind.AT_TIME) {
      showFullScreenAlarm(
        context = context,
        channelId = CHANNEL_PRAYER_ID,
        notificationId = AlertIds.prayer(prayer, kind),
        title = title,
        body = body,
        prayer = prayer,
        language = language
      )
      return
    }

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      0,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, CHANNEL_PRAYER_ID)
      .setSmallIcon(R.drawable.ic_notification)
      .setContentTitle(title)
      .setContentText(body)
      .setStyle(NotificationCompat.BigTextStyle().bigText(body))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .setSound(alertSoundUri(context))

    try {
      NotificationManagerCompat.from(context)
        .notify(AlertIds.prayer(prayer, kind), builder.build())
    } catch (e: SecurityException) {
      android.util.Log.w(TAG, "notify denied - POST_NOTIFICATIONS not granted", e)
    }
  }

  fun showForbiddenTimeNotification(
    context: Context,
    windowTitle: String,
    rangeFormatted: String,
    isStart: Boolean,
    language: AppLanguage
  ) {
    createNotificationChannels(context)

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      1,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val title = if (isStart) {
      when (language) {
        AppLanguage.ARABIC -> "⚠️ بدء وقت النهي عن الصلاة ($rangeFormatted)"
        AppLanguage.BENGALI -> "⚠️ নিষিদ্ধ নামাজের সময় শুরু হয়েছে ($rangeFormatted)"
        AppLanguage.SPANISH -> "⚠️ Inicio de Tiempo Prohibido para la Oración ($rangeFormatted)"
        AppLanguage.PORTUGUESE -> "⚠️ Início do Período Proibido para Oração ($rangeFormatted)"
        AppLanguage.ENGLISH -> "⚠️ Forbidden Prayer Time Started ($rangeFormatted)"
      }
    } else {
      when (language) {
        AppLanguage.ARABIC -> "✅ انتهاء وقت النهي عن الصلاة"
        AppLanguage.BENGALI -> "✅ নিষিদ্ধ নামাজের সময় শেষ হয়েছে"
        AppLanguage.SPANISH -> "✅ Fin del Tiempo Prohibido para la Oración"
        AppLanguage.PORTUGUESE -> "✅ Fim do Período Proibido para Oração"
        AppLanguage.ENGLISH -> "✅ Forbidden Prayer Time Ended"
      }
    }

    val contentText = if (isStart) {
      when (language) {
        AppLanguage.ARABIC -> "$windowTitle. يُنهى عن صلاة النوافل والتطوع حتى انتهاء هذا الوقت."
        AppLanguage.BENGALI -> "$windowTitle। এ নির্দিষ্ট সময়সীমার মধ্যে সকল নফল সালাত আদায় নিষিদ্ধ।"
        AppLanguage.SPANISH -> "$windowTitle. Las oraciones voluntarias (nafl) quedan prohibidas hasta finalizar este período."
        AppLanguage.PORTUGUESE -> "$windowTitle. As orações voluntárias (nafl) são proibidas até o fim deste período."
        AppLanguage.ENGLISH -> "$windowTitle. Voluntary (nafl) prayers are prohibited during this window."
      }
    } else {
      when (language) {
        AppLanguage.ARABIC -> "انتهى وقت الكراهة ($windowTitle). أصبحت صلاة النوافل مباحة الآن بحمد الله."
        AppLanguage.BENGALI -> "নিষিদ্ধ সময় অতিক্রান্ত হয়েছে। এখন নফল বা অন্যান্য সালাত আদায় করা বৈধ।"
        AppLanguage.SPANISH -> "El período prohibido ($windowTitle) ha concluido. La oración voluntaria está permitida."
        AppLanguage.PORTUGUESE -> "O período proibido ($windowTitle) terminou. A oração voluntária é permitida."
        AppLanguage.ENGLISH -> "The prohibited window ($windowTitle) has ended. Voluntary prayer is now permitted."
      }
    }

    val notifId = if (isStart) NOTIF_ID_FORBIDDEN_START + (windowTitle.hashCode().let { kotlin.math.abs(it) % 100 })
                  else NOTIF_ID_FORBIDDEN_END + (windowTitle.hashCode().let { kotlin.math.abs(it) % 100 })

    val builder = NotificationCompat.Builder(context, CHANNEL_FORBIDDEN_ID)
      .setSmallIcon(R.drawable.ic_notification)
      .setContentTitle(title)
      .setContentText(contentText)
      .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .setSound(alertSoundUri(context))

    try {
      NotificationManagerCompat.from(context).notify(notifId, builder.build())
    } catch (e: SecurityException) {
      android.util.Log.w(TAG, "notify denied - POST_NOTIFICATIONS not granted", e)
    }
  }

  fun sendTestPrayerNotification(context: Context, language: AppLanguage) {
    showPrayerAlert(
      context = context,
      prayer = PrayerName.ASR,
      kind = PrayerAlertKind.AT_TIME,
      timeFormatted = LocalTime.now()
        .format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")),
      language = language
    )
  }

  fun sendTestForbiddenNotification(context: Context, language: AppLanguage) {
    val sampleWindow = ForbiddenTimeWindow(
      type = ForbiddenWindowType.ZENITH,
      startTime = LocalTime.now().minusMinutes(2),
      endTime = LocalTime.now().plusMinutes(8),
      hadithReference = "Sahih Muslim 832"
    )
    showForbiddenTimeNotification(
      context = context,
      windowTitle = sampleWindow.getLocalizedTitle(language),
      rangeFormatted = sampleWindow.formattedRange,
      isStart = true,
      language = language
    )
  }
}
