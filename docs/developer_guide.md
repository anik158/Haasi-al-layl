# Developer Guide: Ḥaasi al-Layl

## Architecture Overview

The app follows standard Android architectural principles with MVVM and unidirectional data flow:

```
├── data/
│   ├── dua/               # Embedded offline Islamic content & translations (EN, BN, ES, PT, AR)
│   ├── local/             # Room Database (PrayerLog, TasbihEntity, DuaBookmarkEntity)
│   ├── localization/      # AppLanguage enum & LocalizationManager for all localized UI strings
│   ├── model/             # Domain entities (CityLocation, PrayerModels, CalendarDates)
│   ├── prayer/            # Astronomical calculation algorithms & IslamicMadhab definitions
│   ├── qibla/             # Spherical geometry Kaaba bearing & compass sensor fusion
│   ├── repository/        # Data synchronization & Room abstraction layer
│   └── weather/           # Open-Meteo public free weather API client & repository
└── ui/
    ├── components/        # Reusable Jetpack Compose cards & dialogs (CelestialHorizon, MadhabDialog, etc.)
    ├── screens/           # Primary destinations (HomeScreen, WeatherScreen, QiblaScreen, TasbihScreen, DuaScreen)
    └── theme/             # Material 3 Color palette, Typography, and Theme system
```

## Adding New Madhabs or Astronomical Adjustments
The `IslamicMadhab` enum is defined in `com.example.data.prayer.IslamicMadhab`.
- `shadowFactor`: 1.0 for Shafi'i, Maliki, Hanbali; 2.0 for Hanafi.
- The `PrayerCalculator` dynamically recalculates Asr when `selectedMadhab` is mutated.
- The default madhab is set to `IslamicMadhab.SHAFI` in `HaasiAlLaylViewModel`.

## Localization
All user-facing strings are funneled through `LocalizationManager`.
Supported languages:
- English (`en`)
- Bengali (`bn`)
- Arabic (`ar`)
- Spanish (`es`)
- Portuguese (`pt`)

When changing language via `setLanguage(language)`, all screens and embedded Dua data react instantly without needing an app restart.
