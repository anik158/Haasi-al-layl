# Haasi Al-Layl

A beautiful, offline-first Android application designed to track the quiet rhythm of night and day, providing accurate Islamic prayer times, celestial tracking, and localized weather insights.

## Features
- **Offline Astronomical Engine:** Haasi Al-Layl contains a powerful offline mathematical engine based on standard astronomical algorithms used globally (such as the US Naval Observatory algorithms). It calculates the precise position of the sun and moon using your latitude, longitude, timezone, and the current date. This means that **seasons, daylight saving changes, and timezones are naturally accounted for**, allowing the app to compute accurate prayer times (Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha) completely offline without needing a continuous internet connection.
- **Auto-Sync:** If internet is available, you can auto-sync your location and timezone.
- **Multilingual Support:** Fully localized in English, Arabic, Bengali, Spanish, and Portuguese.
- **Celestial Tracking:** Track moon phases, solar noon, and twilight horizons.

## How to update the App Icon
If you have a custom foreground logo (e.g. the crescent and tea cup):
1. In the AI Studio left sidebar, expand the `app/src/main/res/drawable/` folder.
2. Click the three dots next to `drawable` and select "Upload File".
3. Upload your image and name it exactly `ic_launcher_foreground.png`.
4. Overwrite the existing file if prompted.
5. Compile the app to see your custom logo on the home screen and splash screen!
