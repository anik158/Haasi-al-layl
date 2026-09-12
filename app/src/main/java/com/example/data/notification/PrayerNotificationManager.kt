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
  private const val PREFIX_FORBIDDEN = "haasi_forbidden_alerts"
  private const val PREFIX_TAHAJJUD = "haasi_tahajjud_alerts"
  private const val PRAYER_CHANNEL_VERSION = 2
  private const val FORBIDDEN_CHANNEL_VERSION = 2
  private const val TAHAJJUD_CHANNEL_VERSION = 1

  const val CHANNEL_PRAYER_ID = "${PREFIX_PRAYER}_v$PRAYER_CHANNEL_VERSION"
  const val CHANNEL_FORBIDDEN_ID = "${PREFIX_FORBIDDEN}_v$FORBIDDEN_CHANNEL_VERSION"
  const val CHANNEL_TAHAJJUD_ID = "${PREFIX_TAHAJJUD}_v$TAHAJJUD_CHANNEL_VERSION"

  private val CHANNEL_PREFIXES = listOf(PREFIX_PRAYER, PREFIX_FORBIDDEN, PREFIX_TAHAJJUD)

  private const val PREFS_NAME = "haasi_notifications_prefs"
  private const val KEY_PRAYER_NOTIFS = "key_prayer_notifs"
  private const val KEY_FORBIDDEN_NOTIFS = "key_forbidden_notifs"
  private const val KEY_TAHAJJUD_NOTIFS = "key_tahajjud_notifs"
  private const val KEY_FOLLOW_UP_NOTIFS = "key_follow_up_notifs"

  private const val NOTIF_ID_PRAYER_BASE = 1000
  private const val NOTIF_ID_FORBIDDEN_START = 2000
  private const val NOTIF_ID_FORBIDDEN_END = 2100

  /** The alert tone. USAGE_ALARM routes it to the alarm stream, which survives silent mode. */
  private fun alertSoundUri(context: Context) =
    android.net.Uri.parse("android.resource://${context.packageName}/${R.raw.haasi_alert}")

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

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
      context, 0, intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, CHANNEL_TAHAJJUD_ID)
      .setSmallIcon(R.drawable.ic_notification)
      .setContentTitle(title)
      .setContentText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      // Channels own the sound on API 26+; below that the builder is the only path, and
      // minSdk is 24.
      .setSound(alertSoundUri(context))
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)

    val notificationManager = NotificationManagerCompat.from(context)
    try {
      notificationManager.notify(3000, builder.build())
    } catch (e: SecurityException) {
      android.util.Log.w(TAG, "notify denied - POST_NOTIFICATIONS not granted", e)
    }
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

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      0,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val title = PrayerAlertMessages.title(prayer, kind, language, timeFormatted)
    val body = PrayerAlertMessages.body(prayer, kind, language)

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
