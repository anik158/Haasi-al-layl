with open("app/src/main/java/com/example/data/prayer/PrayerCalculator.kt", "r") as f:
    text = f.read()

replacement = """
    val sunriseTime = timesMap[PrayerName.SUNRISE] ?: LocalTime.of(6, 0)
    val dhuhrTime = timesMap[PrayerName.DHUHR] ?: LocalTime.of(12, 10)
    val maghribTime = timesMap[PrayerName.MAGHRIB] ?: LocalTime.of(18, 15)
    val fajrTime = timesMap[PrayerName.FAJR] ?: LocalTime.of(4, 30)
    val forbiddenWindows = ForbiddenPrayerTimesHelper.calculateWindows(sunriseTime, dhuhrTime, maghribTime)

    val maghribMinutes = maghribTime.hour * 60 + maghribTime.minute
    val fajrMinutes = fajrTime.hour * 60 + fajrTime.minute
    val nightLengthMinutes = (24 * 60 - maghribMinutes) + fajrMinutes
    val lastThirdDuration = nightLengthMinutes / 3
    
    val tahajjudStartMinutes = fajrMinutes - lastThirdDuration
    val currentMinutes = now.hour * 60 + now.minute
    
    val isTahajjudTime = if (tahajjudStartMinutes < 0) {
        currentMinutes >= (24*60 + tahajjudStartMinutes) || currentMinutes < fajrMinutes
    } else {
        currentMinutes >= tahajjudStartMinutes && currentMinutes < fajrMinutes
    }

    return DailyPrayerSchedule("""

import re
text = re.sub(
    r'\s*val sunriseTime = timesMap\[PrayerName\.SUNRISE\] \?: LocalTime\.of\(6, 0\)\s*val dhuhrTime = timesMap\[PrayerName\.DHUHR\] \?: LocalTime\.of\(12, 10\)\s*val maghribTime = timesMap\[PrayerName\.MAGHRIB\] \?: LocalTime\.of\(18, 15\)\s*val forbiddenWindows = ForbiddenPrayerTimesHelper\.calculateWindows\(sunriseTime, dhuhrTime, maghribTime\)\s*return DailyPrayerSchedule\(',
    replacement,
    text
)

with open("app/src/main/java/com/example/data/prayer/PrayerCalculator.kt", "w") as f:
    f.write(text)
