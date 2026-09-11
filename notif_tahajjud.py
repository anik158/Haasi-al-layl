with open("app/src/main/java/com/example/data/notification/PrayerNotificationManager.kt", "r") as f:
    text = f.read()

tahajjud_method = """
  fun showTahajjudNotification(context: Context, language: AppLanguage) {
    if (!isPrayerNotificationsEnabled(context)) return

    val title = when (language) {
      AppLanguage.BENGALI -> "তাহাজ্জুদের সময় হয়েছে"
      AppLanguage.ARABIC -> "حان وقت التهجد"
      AppLanguage.ENGLISH -> "Tahajjud Time"
    }

    val message = when (language) {
      AppLanguage.BENGALI -> "আল্লাহ সুবহানাহু ওয়া তায়ালা প্রথম আসমানে নেমে এসেছেন, আপনার যা খুশি চাইতে পারেন।"
      AppLanguage.ARABIC -> "لقد نزل الله سبحانه وتعالى إلى السماء الدنيا، فاسألوا ما شئتم."
      AppLanguage.ENGLISH -> "Allah swt has come down to 1st heaven, ask whatever you want."
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
"""

text = text.replace("  fun showPrayerNotification(", tahajjud_method + "\n  fun showPrayerNotification(")

with open("app/src/main/java/com/example/data/notification/PrayerNotificationManager.kt", "w") as f:
    f.write(text)
