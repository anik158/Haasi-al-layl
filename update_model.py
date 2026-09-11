with open("app/src/main/java/com/example/data/model/PrayerModels.kt", "r") as f:
    text = f.read()

text = text.replace(
    'val forbiddenTimes: List<com.example.data.prayer.ForbiddenTimeWindow> = emptyList()\n)',
    'val forbiddenTimes: List<com.example.data.prayer.ForbiddenTimeWindow> = emptyList(),\n  val isTahajjudTime: Boolean = false\n)'
)

with open("app/src/main/java/com/example/data/model/PrayerModels.kt", "w") as f:
    f.write(text)
