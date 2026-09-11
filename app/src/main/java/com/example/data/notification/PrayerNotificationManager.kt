package com.example.data.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
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

  const val CHANNEL_PRAYER_ID = "haasi_prayer_alerts"
  const val CHANNEL_FORBIDDEN_ID = "haasi_forbidden_alerts"

  private const val PREFS_NAME = "haasi_notifications_prefs"
  private const val KEY_PRAYER_NOTIFS = "key_prayer_notifs"
  private const val KEY_FORBIDDEN_NOTIFS = "key_forbidden_notifs"

  private const val NOTIF_ID_PRAYER_BASE = 1000
  private const val NOTIF_ID_FORBIDDEN_START = 2000
  private const val NOTIF_ID_FORBIDDEN_END = 2100

  fun createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      val prayerChannel = NotificationChannel(
        CHANNEL_PRAYER_ID,
        "Prayer Time Notifications",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Alerts when each Islamic prayer time begins"
        enableVibration(true)
        enableLights(true)
      }

      val forbiddenChannel = NotificationChannel(
        CHANNEL_FORBIDDEN_ID,
        "Forbidden Prayer Time Alerts",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Alerts when prohibited prayer windows start and end"
        enableVibration(true)
        enableLights(true)
      }

      notificationManager.createNotificationChannel(prayerChannel)
      notificationManager.createNotificationChannel(forbiddenChannel)
    }
  }

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


  fun showTahajjudNotification(context: Context, language: AppLanguage) {
    if (!isPrayerNotificationsEnabled(context)) return

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

    val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    val builder = NotificationCompat.Builder(context, CHANNEL_PRAYER_ID)
      .setSmallIcon(R.drawable.ic_launcher_foreground) // using default icon for now
      .setContentTitle(title)
      .setContentText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .setSound(alarmSound)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)

    val notificationManager = NotificationManagerCompat.from(context)
    try {
      notificationManager.notify(3000, builder.build())
    } catch (_: SecurityException) {
      // Permission not granted
    }
  }

  fun showPrayerNotification(
    context: Context,
    prayerName: String,
    arabicName: String,
    timeFormatted: String,
    cityName: String,
    language: AppLanguage
  ) {
    createNotificationChannels(context)

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      0,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val title = when (language) {
      AppLanguage.ARABIC -> "حان الآن وقت صلاة $arabicName ($timeFormatted)"
      AppLanguage.BENGALI -> "ওয়াক্ত শুরু হয়েছে: $prayerName ($timeFormatted)"
      AppLanguage.SPANISH -> "Ha comenzado la oración de $prayerName ($timeFormatted)"
      AppLanguage.PORTUGUESE -> "Começou o horário da oração de $prayerName ($timeFormatted)"
      AppLanguage.ENGLISH -> "Prayer Time: $prayerName has begun ($timeFormatted)"
    }

    val contentText = when (language) {
      AppLanguage.ARABIC -> "في مدينة $cityName. حيّ على الصلاة، حيّ على الفلاح."
      AppLanguage.BENGALI -> "$cityName শহরে নামাজের সময় হয়েছে। হাইয়া আলাস সালাহ।"
      AppLanguage.SPANISH -> "En $cityName. Acude a la oración, acude al éxito."
      AppLanguage.PORTUGUESE -> "Em $cityName. Vinde para a oração, vinde para o sucesso."
      AppLanguage.ENGLISH -> "In $cityName. Come to prayer, come to success."
    }

    val builder = NotificationCompat.Builder(context, CHANNEL_PRAYER_ID)
      .setSmallIcon(android.R.drawable.ic_popup_reminder)
      .setContentTitle(title)
      .setContentText(contentText)
      .setStyle(NotificationCompat.BigTextStyle().bigText("$contentText\nḤaasi al-Layl • The Night Sipper"))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

    try {
      NotificationManagerCompat.from(context).notify(NOTIF_ID_PRAYER_BASE + prayerName.hashCode().let { kotlin.math.abs(it) % 100 }, builder.build())
    } catch (_: SecurityException) {
      // Permission not yet granted
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
      .setSmallIcon(android.R.drawable.ic_dialog_alert)
      .setContentTitle(title)
      .setContentText(contentText)
      .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

    try {
      NotificationManagerCompat.from(context).notify(notifId, builder.build())
    } catch (_: SecurityException) {
      // Permission not granted
    }
  }

  fun sendTestPrayerNotification(context: Context, language: AppLanguage) {
    showPrayerNotification(
      context = context,
      prayerName = "Asr",
      arabicName = "العصر",
      timeFormatted = LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")),
      cityName = "Dhaka",
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
