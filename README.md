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

### 2. ⚖️ Four Sunni Madhabs Jurisprudence (Default: Imam Malik)
The juristic school (*Madhab*) selects the Asr shadow factor:
- **Imam Malik (الإمام مالك) — [Default]**: Asr begins when an object's shadow equals its length (1× shadow factor).
- **Imam Shafi'i (الإمام الشافعي)**: 1× shadow factor.
- **Imam Abu Hanifa (الإمام أبو حنيفة)**: Asr begins when the shadow equals twice the object's length (2× shadow factor).
- **Imam Ahmad ibn Hanbal (الإمام أحمد بن حنبل)**: 1× shadow factor.

Only Hanafi differs in practice — the other three share a 1× shadow factor, so switching among
them changes the badge but not the times. The choice is persisted, as are the calculation method,
language, city and the dhikr you were last counting.

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

- **Location** is resolved down a ladder, best first: the device's own GPS fix, then IP
  geolocation, then whatever city was chosen by hand.
- **Location permission is requested on the first launch**, before anything else, and the first run
  waits on the answer behind a screen that shows what it is doing. This is not politeness deferred
  — without the permission the cached-fix read returns null *before it consults the receiver*, so
  an app that waits for you to find a "Detect" button has not tried the GPS rung at all.
- **Language** is derived from the resolved country — and until a location is actually resolved, it
  follows your **device's** language rather than a default city's. No prompts are shown at all when
  detection succeeds; the language prompt and city picker appear only when the whole ladder fails.
- Every choice — language, city, calculation method, madhab — is written to `SharedPreferences`, so
  it survives process death, restarts and reboots until the app is uninstalled.

**Coordinates and the city name are separate things, and only one of them needs a network.**

**Why GPS works with no signal.** Around thirty satellites continuously broadcast timestamped
signals. Your phone listens to four or more and solves for its own position from the tiny
differences in when each one arrives — the receiver transmits nothing and contacts no server. It is
the same reason a hiking GPS works in a forest with no phone coverage. Turning on the system
Location toggle is a *permission* gate, not a network one: Location on with mobile data off still
gives coordinates.

So with no connectivity at all the app still has exact coordinates, and the prayer arithmetic is
exact with them. What does need a network is turning `60.1699°N` into `"Helsinki"` — so offline the
location simply reads **`60.17°N, 24.94°E`**, and the name is filled in on the next launch that has
connectivity, without touching the coordinates.

Two honest limits. Normally the phone downloads satellite orbit data over the internet (A-GPS) to
get a fix in seconds; without that it must read the same data off the satellites themselves at 50
bits per second, which takes 30 to 60 seconds with a view of the sky. And indoors a fix often never
arrives at all.

Neither limit can be engineered away, so the app is built around them rather than pretending
otherwise:

- **The wait is shown, not hidden.** A cached fix is preferred and instant. When there is none, a
  screen with three orbiting satellites and a **second counter** holds the first run for up to 90
  seconds — enough for a genuine cold fix, where the usual 20-second timeout gives up mid-download.
  There is always a way out of it.
- **Giving up looking is not giving up.** Past 90 seconds the app stops *blocking* on the fix and
  keeps a listener running instead, for as long as it has no location of its own. A first fix
  indoors arrives when you next step outside, not on a timer — so stepping outside is enough, and
  the app sets your location by itself when it happens. A city you pick by hand stands that down,
  so a deliberate choice is never silently overwritten.
- **A failure says which rung failed.** Location switched off, permission refused, and a genuine
  indoor no-fix each get their own message and the action that fixes it — rather than all three
  quietly becoming the same city picker.

**Testing it offline:** get one fix outdoors first (tap Detect with Location on), then turn data
off entirely. The app keeps using those coordinates, prayer times stay exact, and the location
shows as coordinates rather than a name until connectivity returns. A fresh install with no network
*and* no cached fix is the hard case: the counter will run out, and the app will then wait — take
the phone outside and it resolves itself without you doing anything.

> IP geolocation remains as a fallback, and it is the *rough* one — it reports where your ISP
> registered the address block, which is frequently a regional hub. It placed a Helsinki user in
> Turku, 150 km away, which shifts prayer times by 8 to 11 minutes. GPS exists precisely to avoid
> that; if both are unavailable, set the city by hand.
>
> It is never used to point the **Qibla compass**. Prayer times may reasonably follow a city you
> chose — checking Makkah's times from Dhaka is a fair thing to want — but a bearing is a property
> of where the phone physically is, so the compass uses the device's own coordinates and says so.
> When it has to fall back to a selected city, it tells you that instead of implying a precision it
> does not have.

**Finding a city by hand.** The picker is not limited to the built-in presets: typing searches
Open-Meteo's geocoding service — the same family as the weather API — so Faridpur, or any town in
Mexico, resolves to a full location with coordinates and an IANA timezone. Results show their
administrative region, to separate the several places that share a name.

Language follows the detected country, and can always be overridden in Settings:

| Country | Language |
| --- | --- |
| Bangladesh | Bengali (বাংলা) |
| Arab states (SA, AE, EG, QA, KW, OM, BH, JO, LB, IQ, PS, YE, MA, DZ, TN, LY, SD, SY, MR, SO, DJ, KM) | Arabic (العربية) |
| Spanish-speaking countries | Spanish (Español) |
| Portuguese-speaking countries | Portuguese (Português) |
| Everywhere else | English |

---

### 4. 🔔 Prayer Notifications

Alerts are scheduled with `AlarmManager`, so they fire with the app closed and after a reboot.
WorkManager is deliberately not used: its contract is explicitly best-effort and batched, and a
Fajr alert twenty minutes late is a correctness bug rather than a polish issue.

- **Two alerts per prayer** — one at the time, and a nudge five minutes later. The follow-up is
  **skipped once you mark the prayer as prayed** on the Today screen, and can be switched off on
  its own.
- **A different message for each prayer**, in all five languages. Fajr carries the *tathwīb*
  (*"Prayer is better than sleep"*); the others carry the Qur'anic or hadith text for that prayer.
- **Forbidden-time alerts** at the start and end of the three prohibited windows — sunrise
  (+15 min), the solar zenith (−10 min to Dhuhr) and sunset (−20 min to Maghrib).
- **Tahajjud reminder** when the last third of the night begins, on its own channel and toggle so a
  3 a.m. alert can be silenced without losing Fajr.
- **A bundled alert tone**, routed at `USAGE_ALARM` so it is heard in silent mode.

Prayer alerts use `setAlarmClock`, which is exempt from Doze and App Standby. The obvious
alternative, `setExactAndAllowWhileIdle`, is quota-limited to roughly one alarm per app per nine
minutes in deep Doze — which would silently push every five-minute follow-up out to nine, precisely
for Fajr and Isha. The cost is a permanent alarm icon in the status bar, so *Hide the alarm icon*
in Settings offers the quieter, less reliable path.

Four independent mechanisms keep the schedule alive without the app ever being opened: a rolling
48-hour horizon, a reschedule at the tail of every alarm that fires, a daily refresh at 00:05 local
time, and `Application.onCreate`.

> **Aggressive power management.** On OPPO/ColorOS, Xiaomi/MIUI and Vivo, background alarms are
> dropped by default. Enable auto-launch in Startup Manager, turn off *Sleep standby optimisation*,
> and lock the app in Recents. The Settings reliability panel reports what is currently blocked.

---

### 5. 📖 Prophetic Supplications (Duas)

The **Duas** tab ships a curated offline set of supplications, each with Arabic text, phonetic
transliteration, and translations in English, Bengali, Spanish, Portuguese and Arabic, plus
bookmarks and instant search across titles and translations.

> **Note:** the app also contains a loader for a fuller *Hisn al-Muslim* (Fortress of the Muslim)
> dataset, read from `assets/hisn_al_muslim.json` and appended to the built-in set. **That asset is
> not currently present in this repository**, so builds from a clean checkout show the built-in
> supplications only. The loader fails silently by design and does not affect the rest of the app.

---

### 6. ⚙️ Centralized Settings & Customization
- **Unified Settings Bottom Sheet**: Prayer start alerts, the 5-minute reminder, Tahajjud alerts, forbidden-time warnings, app language, calculation method, and madhab, all from one menu in the top bar.
- **Notification reliability panel**: shows at a glance whether notifications, exact alarms and unrestricted battery are actually granted, each with a deep link to the right system screen, plus a *Test alert in 1 minute* button that arms a real alarm through the production path.
- **Offline Weather Graceful Degradation**: Weather and temperature widgets handle network loss with an offline banner instead of endless loading.
- **Dynamic Top Bar**: Brand mark, app title, an actionable city changer, and the settings hub.

---

### 7. 🎨 Color Depth & Visual Aesthetic
- **Warm Parchment Canvas (`#F4EFE6`)**: Natural unbleached manuscript backdrop that reduces eye strain and honors Islamic scholarly manuscripts.
- **Deep Forest Emerald (`#1C3D2B`)**: Rich, saturated ink tones for typography, iconography, and active highlights.
- **Pale Water-Wash Celadon (`#D1E2D3`) & Warm Gold (`#D4A373`)**: Soft accents and badges for next prayer status, celestial horizons, and madhab indicators.
- **Night-Sky Brand Mark**: The launcher icon and the in-app logo share one deep navy ground, so the identity reads the same inside and outside the app. Source artwork lives in `docs/brand/`.
- **Branded Launch**: a splash screen on the same night navy hands over to the parchment UI. The app is deliberately light-only, and the window background is pinned explicitly so a device in system dark mode never flashes a black frame before the first frame is drawn.

---

### 8. 🌅 Celestial Horizon & Real-Time Tracking
- **Proportional Curved Horizon**: Tracks the real-time position of the sun and moon across the sky dome for the selected coordinates and timezone.
- **Animated Moon Phase Indicator**: Lunar phase (New, Waxing Crescent, First Quarter, Waxing Gibbous, Full, Waning Gibbous, Last Quarter, Waning Crescent) with live illumination percentage.
- **Triple Calendar Integration**: Hijri lunar date, Bengali solar calendar (বঙ্গাব্দ), and Gregorian calendar shown concurrently.

---

### 9. 🧭 Precision Qibla Compass & Digital Tasbih
- **True Direction to the Kaaba**: A great-circle bearing, with the compass heading corrected from magnetic to true north using `GeomagneticField`, so both sides of the calculation share one reference frame. The correction applied is shown under the bearing. Negligible in South Asia, 5–20° across the UK and North America.
- **Held any way up**: the sensor frame is remapped past 45° of pitch, so the reading survives holding the phone upright to sight a direction, and a circular low-pass filter keeps the needle steady across the 0°/360° seam.
- **Calibration hint on demand**: shown only when the magnetometer actually reports low accuracy, rather than permanently.
- **Digital Counter (Tasbih)**: Offline Room-persisted counts, with lap and lifetime totals that survive a restart, and haptic feedback at milestone targets (33, 99, 100).

---

## 🛠️ Tech Stack & Engineering
- **Platform:** Android 7.0+ (`minSdk 24`), compiled and targeting API 36
- **UI:** Jetpack Compose with Material 3
- **Language:** Kotlin 2.2.10 (AGP 9.1.1, Gradle 9.3.1)
- **Architecture:** MVVM with `StateFlow`, Coroutines, and unidirectional data flow
- **Local Persistence:** Room with KSP (prayer logs, Tasbih counters, Dua bookmarks); `SharedPreferences` for user settings
- **Hardware Integration:** Android `SensorManager` (rotation vector, magnetometer and accelerometer) and `GeomagneticField` for magnetic declination
- **Background Work:** `AlarmManager` (`setAlarmClock` for prayer alerts, exempt from Doze) with a boot receiver; no WorkManager, whose best-effort scheduling is unsuitable for prayer times
- **Startup:** `androidx.core:core-splashscreen`
- **Location:** platform `LocationManager` (GPS provider first, offline-capable); place search and reverse geocoding over HTTP, with the coordinates never depending on either
- **Network Layer:** Retrofit + OkHttp + Moshi. Weather and geocoding from the Open-Meteo public APIs (no key required); IP fallback via `ipinfo.io`
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
