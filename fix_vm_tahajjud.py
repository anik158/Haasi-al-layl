with open("app/src/main/java/com/example/ui/HaasiAlLaylViewModel.kt", "r") as f:
    text = f.read()

text = text.replace(
    'com.example.data.notification.PrayerNotificationManager.showPrayerNotification(\n              context = getApplication<android.app.Application>(),\n              prayerName = "Tahajjud",\n              arabicName = "تهجد",\n              timeFormatted = "Now",\n              cityName = schedule.city,\n              language = _currentLanguage.value\n            )',
    'com.example.data.notification.PrayerNotificationManager.showTahajjudNotification(getApplication<android.app.Application>(), _currentLanguage.value)'
)

with open("app/src/main/java/com/example/ui/HaasiAlLaylViewModel.kt", "w") as f:
    f.write(text)
