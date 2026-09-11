with open("app/src/main/java/com/example/ui/components/CelestialHorizonCard.kt", "r") as f:
    text = f.read()

text = text.replace(
    'text = "Celestial Horizon Details",',
    'text = LocalizationManager.getCelestialHorizonTitle(language) + " " + LocalizationManager.getLunarDetailsLabel(language).replace(":", ""),')

text = text.replace(
    'text = "Time: ${currentTime.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))}",',
    'text = "${LocalizationManager.getTimeLabel(language)} ${currentTime.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))}",')

text = text.replace(
    'Text("☀️ Solar Markers:",',
    'Text("☀️ ${LocalizationManager.getSolarMarkersLabel(language)}",')

text = text.replace(
    'Text("• Sunrise: ${sunrise.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))}",',
    'Text("• ${LocalizationManager.getNextSunriseLabel(language).replace("Next ", "")}: ${sunrise.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))}",')

text = text.replace(
    'Text("• Solar Noon (Dhuhr): ${noon.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))}",',
    'Text("• ${LocalizationManager.getPrayerDisplayName(com.example.data.model.PrayerName.DHUHR, language)}: ${noon.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))}",')

text = text.replace(
    'Text("• Sunset (Maghrib): ${sunset.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))}",',
    'Text("• ${LocalizationManager.getPrayerDisplayName(com.example.data.model.PrayerName.MAGHRIB, language)}: ${sunset.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))}",')

text = text.replace(
    'Text("🌙 Lunar Details:",',
    'Text("🌙 ${LocalizationManager.getLunarDetailsLabel(language)}",')

text = text.replace(
    'Text("• Hijri Day: $hijriDay (${moonInfo.phaseNameEn})",',
    'Text("• ${LocalizationManager.getHijriDayLabel(language)} $hijriDay (${LocalizationManager.getMoonPhaseName(moonInfo.phaseKey, language)})",')

text = text.replace(
    'Text("• Lunar Illumination: ${moonInfo.illuminationPercent}%",',
    'Text("• ${LocalizationManager.getIlluminationLabel(moonInfo.illuminationPercent, language)}",')

text = text.replace(
    'Text("Got it")',
    'Text(LocalizationManager.getGotItLabel(language))'
)

with open("app/src/main/java/com/example/ui/components/CelestialHorizonCard.kt", "w") as f:
    f.write(text)
