with open("app/src/main/java/com/example/ui/components/CelestialHorizonCard.kt", "r") as f:
    text = f.read()

text = text.replace(
    'text = if (isDaytime) "Daytime Solar Tracking Active" else "Night Sky & Lunar Tracking Active",',
    'text = if (isDaytime) LocalizationManager.getDaytimeActiveLabel(language) else LocalizationManager.getNightSkyActiveLabel(language),'
)

text = text.replace(
    'text = "Time: ${currentTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}",',
    'text = "${LocalizationManager.getTimeLabel(language)} ${currentTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}",'
)

text = text.replace(
    'text = "Solar Markers:",',
    'text = LocalizationManager.getSolarMarkersLabel(language),'
)

text = text.replace(
    'text = "• Sunrise: ${sunriseTime.format(timeFormatter)}",',
    'text = "• ${LocalizationManager.getNextSunriseLabel(language).replace("Next ", "")}: ${sunriseTime.format(timeFormatter)}",'
)

text = text.replace(
    'text = "• Solar Noon (Dhuhr): ${dhuhrTime.format(timeFormatter)}",',
    'text = "• ${LocalizationManager.getPrayerDisplayName(PrayerName.DHUHR, language)}: ${dhuhrTime.format(timeFormatter)}",'
)

text = text.replace(
    'text = "• Sunset (Maghrib): ${sunsetTime.format(timeFormatter)}",',
    'text = "• ${LocalizationManager.getPrayerDisplayName(PrayerName.MAGHRIB, language)}: ${sunsetTime.format(timeFormatter)}",'
)

text = text.replace(
    'text = "Lunar Details:",',
    'text = LocalizationManager.getLunarDetailsLabel(language),'
)

text = text.replace(
    'text = "• Hijri Day: $hijriDay (${LocalizationManager.getMoonPhaseName(moonPhaseName, language)})",',
    'text = "• ${LocalizationManager.getHijriDayLabel(language)} $hijriDay (${LocalizationManager.getMoonPhaseName(moonPhaseName, language)})",'
)

text = text.replace(
    'text = "• Lunar Illumination: ${illumination.toInt()}%",',
    'text = "• ${LocalizationManager.getIlluminationLabel(illumination.toInt(), language)}",'
)

text = text.replace(
    'Text("Got it")',
    'Text(LocalizationManager.getGotItLabel(language))'
)

with open("app/src/main/java/com/example/ui/components/CelestialHorizonCard.kt", "w") as f:
    f.write(text)
