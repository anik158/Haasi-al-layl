with open("app/src/main/java/com/example/data/prayer/PrayerCalculator.kt", "r") as f:
    text = f.read()

replacement = """
    val sunriseTime = timesMap[PrayerName.SUNRISE] ?: LocalTime.of(6, 0)
    val dhuhrTime = timesMap[PrayerName.DHUHR] ?: LocalTime.of(12, 10)
    val maghribTime = timesMap[PrayerName.MAGHRIB] ?: LocalTime.of(18, 15)
    val fajrTime = timesMap[PrayerName.FAJR] ?: LocalTime.of(4, 30)
    
    val forbiddenWindows = ForbiddenPrayerTimesHelper.calculateWindows(sunriseTime, dhuhrTime, maghribTime)

    // Tahajjud calculation (last third of the night)
    // Approximate night duration = 24 - Maghrib + Fajr
    // Night is from Maghrib to Fajr of next day.
    val maghribMinutes = maghribTime.hour * 60 + maghribTime.minute
    val fajrMinutes = fajrTime.hour * 60 + fajrTime.minute
    val nightLengthMinutes = (24 * 60 - maghribMinutes) + fajrMinutes
    val lastThirdDuration = nightLengthMinutes / 3
    
    // Tahajjud start time is Fajr - lastThirdDuration
    val tahajjudStartMinutes = fajrMinutes - lastThirdDuration
    val currentMinutes = now.hour * 60 + now.minute
    
    val isTahajjudTime = if (tahajjudStartMinutes < 0) {
        // Starts before midnight
        currentMinutes >= (24*60 + tahajjudStartMinutes) || currentMinutes < fajrMinutes
    } else {
        // Starts after midnight
        currentMinutes >= tahajjudStartMinutes && currentMinutes < fajrMinutes
    }

    return DailyPrayerSchedule(
"""

text = text.replace(
    '''    val sunriseTime = timesMap[PrayerName.SUNRISE] ?: LocalTime.of(6, 0)
    val dhuhrTime = timesMap[PrayerName.DHUHR] ?: LocalTime.of(12, 10)
    val maghribTime = timesMap[PrayerName.MAGHRIB] ?: LocalTime.of(18, 15)

    val forbiddenWindows = ForbiddenPrayerTimesHelper.calculateWindows(sunriseTime, dhuhrTime, maghribTime)

    return DailyPrayerSchedule(''',
    replacement
)
text = text.replace(
    'forbiddenTimes = forbiddenWindows\n    )',
    'forbiddenTimes = forbiddenWindows,\n      isTahajjudTime = isTahajjudTime\n    )'
)

with open("app/src/main/java/com/example/data/prayer/PrayerCalculator.kt", "w") as f:
    f.write(text)
