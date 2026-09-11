with open("app/src/main/java/com/example/ui/HaasiAlLaylViewModel.kt", "r") as f:
    text = f.read()

replacement = """  // State to prevent spamming the Tahajjud notification
  private var lastTahajjudNotifiedDate: java.time.LocalDate? = null

  init {
    viewModelScope.launch {
      prayerSchedule.collect { schedule ->
        if (schedule.isTahajjudTime) {
          val today = schedule.date
          if (lastTahajjudNotifiedDate != today) {
            lastTahajjudNotifiedDate = today
            com.example.data.notification.PrayerNotificationManager.showPrayerNotification(
              context = getApplication<android.app.Application>(),
              prayerName = "Tahajjud",
              arabicName = "تهجد",
              timeFormatted = "Now",
              cityName = schedule.city,
              language = _currentLanguage.value
            )
            // Show emotional message via notification (since we are reusing the Prayer Notification, 
            // maybe we can send a custom one instead, but let's build a custom notification method for Tahajjud)
          }
        }
      }
    }
"""

text = text.replace("  init {", replacement)

with open("app/src/main/java/com/example/ui/HaasiAlLaylViewModel.kt", "w") as f:
    f.write(text)
