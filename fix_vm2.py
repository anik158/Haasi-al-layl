with open("app/src/main/java/com/example/ui/HaasiAlLaylViewModel.kt", "r") as f:
    text = f.read()

text = text.replace(
    'com.example.data.notification.PrayerNotificationManager.showTahajjudNotification(getApplication<android.app.Application>(), _currentLanguage.value)',
    'com.example.data.notification.PrayerNotificationManager.showTahajjudNotification(getApplication<android.app.Application>(), currentLanguage.value)'
)

with open("app/src/main/java/com/example/ui/HaasiAlLaylViewModel.kt", "w") as f:
    f.write(text)
