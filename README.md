# Ḥaasi al-Layl (حَاسِي اللَّيْل)

> *"The one who sips the night" — A serene, mindful Islamic daily companion featuring astronomical horizon celestial tracking, multi-madhab prayer schedules, offline prophetic supplications, and tranquil weather guidance.*

---

## 🌙 Name Details & Poetic Origin

### **Ḥaasi al-Layl (حَاسِي اللَّيْل)**
- **Meaning:** "The one who sips the night" or "The night-sipper."
- **Language Breakdown:**
  - **Ḥaasi (حَاسِي)**: Derived from the Arabic root and verb *ḥasā* (حَسَا), which means to sip, sup, or drink a liquid slowly and mindfully.
  - **Al-Layl (اللَّيْل)**: Means "the night."
- **Poetic Context:**
  This is a highly metaphorical and poetic Arabic expression. It traditionally depicts a traveler, a seeker of night prayers (*Qiyam al-Layl* / *Tahajjud*), a sleepless contemplator, or a poet who "absorbs" or slowly endures the long hours of darkness, as if savoring every drop of a soothing beverage under the celestial canopy.

---

## ✨ Key Features & Architecture

### 1. 🕌 Prayer Calculation Methods (Default: Umm al-Qura)

Prayer times are computed entirely on-device. Because there is **no single universally agreed
convention** for Fajr and Isha — they depend on how far below the horizon the sun must sit, and
that angle was never settled by unanimous consensus — the method is selectable in Settings and
persisted:

| Method | Fajr | Isha |
| --- | --- | --- |
| **Umm al-Qura, Makkah — [Default]** | 18.5° | Maghrib + 90 min (120 min in Ramadan) |
| Muslim World League | 18.0° | 17.0° |
| Islamic Society of North America (ISNA) | 15.0° | 15.0° |
| Egyptian General Authority | 19.5° | 17.5° |
| University of Islamic Sciences, Karachi | 18.0° | 18.0° |
| Dubai Religious Affairs | 18.2° | 18.2° |

Umm al-Qura is the standard published by Saudi Arabia and used for Makkah and Madinah. It is the
default here because it is the closest thing to a reference clock for the Muslim world; it sets
Isha by a fixed interval after Maghrib rather than a twilight angle, and widens that interval to
120 minutes for Ramadan.

**High-latitude handling.** Above roughly 48° of latitude the sun stops dipping far enough below
the horizon in midsummer for the Fajr and Isha angles to occur at all — London reaches only about
−15° at solar midnight. Where the angle is unreachable, Fajr and Isha fall back to the
**one-seventh-of-the-night** rule rather than silently collapsing onto solar midnight. Sunrise,
sunset and Asr always occur at supported latitudes and use the direct solar calculation.

### 2. ⚖️ Four Sunni Madhabs Jurisprudence (Default: Imam Shafi'i)
The juristic school (*Madhab*) selects the Asr shadow factor:
- **Imam Shafi'i (الإمام الشافعي) — [Default]**: Asr begins when an object's shadow equals its length (1× shadow factor).
- **Imam Abu Hanifa (الإمام أبو حنيفة)**: Asr begins when the shadow equals twice the object's length (2× shadow factor).
- **Imam Malik (الإمام مالك)**: 1× shadow factor.
- **Imam Ahmad ibn Hanbal (الإمام أحمد بن حنبل)**: 1× shadow factor.

**How the Astronomical Algorithm Works:**
To avoid any dependency on the internet or external servers, the engine employs spherical trigonometry and precise solar positioning algorithms (similar to the standard Jean Meeus astronomical algorithms):
1. **Julian Date & Equation of Time**: From the Gregorian date and UTC time, the algorithm computes the Julian Date, which yields the Sun's *Declination* (its tilt relative to the Earth's equator) and the *Equation of Time* (the discrepancy between solar time and clock time).
2. **Transit & Zenith Calculation**: From the selected coordinates (latitude, longitude) and timezone offset, the math predicts the exact moment of Solar Noon (Dhuhr) when the sun reaches its highest transit point.
3. **Altitude & Azimuth Offsets**: Fajr and Isha are found by determining when the sun's centre drops to the method's angle below the horizon. Asr uses the inverse cotangent of the sun's altitude combined with the shadow multiple. Sunrise and sunset are found where the sun crosses the horizon, allowing for atmospheric refraction.

The day is reported as seven markers — Imsak, Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha — alongside
**forbidden-time windows** (sunrise, zenith and sunset) and a **Tahajjud** window in the last third
of the night, each with its own opt-in notification channel.

---

### 3. 🌍 First-Run Setup & Automatic Localization

The app asks as little as possible, and asks it **once per installation**:

- **When the device is online**, the location is resolved from the IP address (via `ipinfo.io`) and
  the language is derived from its country. No prompts are shown at all.
- **When it is not**, and only then, the language prompt appears, followed by the city picker.
- Every choice — language, city, calculation method, madhab — is written to `SharedPreferences`, so
  it survives process death, restarts and reboots until the app is uninstalled.

Language follows the detected country, and can always be overridden in Settings:

| Country | Language |
| --- | --- |
| Bangladesh | Bengali (বাংলা) |
| Arab states (SA, AE, EG, QA, KW, OM, BH, JO, LB, IQ, PS, YE, MA, DZ, TN, LY, SD, SY, MR, SO, DJ, KM) | Arabic (العربية) |
| Spanish-speaking countries | Spanish (Español) |
| Portuguese-speaking countries | Portuguese (Português) |
| Everywhere else | English |

---

### 4. 📖 Prophetic Supplications (Duas)

The **Duas** tab ships a curated offline set of supplications, each with Arabic text, phonetic
transliteration, and translations in English, Bengali, Spanish, Portuguese and Arabic, plus
bookmarks and instant search across titles and translations.

> **Note:** the app also contains a loader for a fuller *Hisn al-Muslim* (Fortress of the Muslim)
> dataset, read from `assets/hisn_al_muslim.json` and appended to the built-in set. **That asset is
> not currently present in this repository**, so builds from a clean checkout show the built-in
> supplications only. The loader fails silently by design and does not affect the rest of the app.

---

### 5. ⚙️ Centralized Settings & Customization
- **Unified Settings Bottom Sheet**: Prayer start alerts, forbidden-time warnings, app language, calculation method, and madhab, all from one menu in the top bar.
- **Offline Weather Graceful Degradation**: Weather and temperature widgets handle network loss with an offline banner instead of endless loading.
- **Dynamic Top Bar**: Brand mark, app title, an actionable city changer, and the settings hub.

---

### 6. 🎨 Color Depth & Visual Aesthetic
- **Warm Parchment Canvas (`#F4EFE6`)**: Natural unbleached manuscript backdrop that reduces eye strain and honors Islamic scholarly manuscripts.
- **Deep Forest Emerald (`#1C3D2B`)**: Rich, saturated ink tones for typography, iconography, and active highlights.
- **Pale Water-Wash Celadon (`#D1E2D3`) & Warm Gold (`#D4A373`)**: Soft accents and badges for next prayer status, celestial horizons, and madhab indicators.
- **Night-Sky Brand Mark**: The launcher icon and the in-app logo share one deep navy ground, so the identity reads the same inside and outside the app. Source artwork lives in `docs/brand/`.

---

### 7. 🌅 Celestial Horizon & Real-Time Tracking
- **Proportional Curved Horizon**: Tracks the real-time position of the sun and moon across the sky dome for the selected coordinates and timezone.
- **Animated Moon Phase Indicator**: Lunar phase (New, Waxing Crescent, First Quarter, Waxing Gibbous, Full, Waning Gibbous, Last Quarter, Waning Crescent) with live illumination percentage.
- **Triple Calendar Integration**: Hijri lunar date, Bengali solar calendar (বঙ্গাব্দ), and Gregorian calendar shown concurrently.

---

### 8. 🧭 Precision Qibla Compass & Digital Tasbih
- **True Direction to the Kaaba**: Real-time sensor-fused compass heading with alignment haptics.
- **Digital Counter (Tasbih)**: Offline Room-persisted counts with haptic feedback at milestone targets (33, 99, 100).

---

## 🛠️ Tech Stack & Engineering
- **Platform:** Android 7.0+ (`minSdk 24`), compiled and targeting API 36
- **UI:** Jetpack Compose with Material 3
- **Language:** Kotlin 2.2.10 (AGP 9.1.1, Gradle 9.3.1)
- **Architecture:** MVVM with `StateFlow`, Coroutines, and unidirectional data flow
- **Local Persistence:** Room with KSP (prayer logs, Tasbih counters, Dua bookmarks); `SharedPreferences` for user settings
- **Hardware Integration:** Android `SensorManager` (magnetometer and accelerometer)
- **Network Layer:** Retrofit + OkHttp + Moshi. Weather from the Open-Meteo public API (no key required); location from `ipinfo.io`
- **Screenshot Tests:** Roborazzi over Robolectric

---

## 🔨 Building

The Gradle wrapper is **not** checked in, so either install Gradle 9.3.1 and run `gradle`, or use a
local wrapper you generate yourself.

```bash
gradle assembleDebug
```

The debug build type signs with `debug.keystore` at the repository root. That file is gitignored, so
generate one before the first build:

```bash
keytool -genkeypair -v -keystore debug.keystore -storepass android -keypass android -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
```

`google-services.json` is optional — the Google Services plugin is configured with
`missingGoogleServicesStrategy = WARN`, so the build succeeds without it and Firebase features
simply stay inactive.

### Continuous Integration

Pushing to the `build-apk-release` branch (or running the workflow manually) builds an APK via
[`.github/workflows/build-apk-release.yml`](.github/workflows/build-apk-release.yml). The workflow
provisions Gradle 9.3.1, generates the debug keystore, and publishes `haasi-al-layl-debug.apk` both
as a run artifact and as a GitHub Release.
