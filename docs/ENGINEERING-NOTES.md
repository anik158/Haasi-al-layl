# Engineering notes

Working context for this codebase: decisions that are non-obvious from the code, bugs that were
subtle enough to be worth remembering, and the traps that are still set. The README describes what
the app *does*; this describes what you need to know before changing it.

Last updated: 17 September 2026.

---

## Environment

- **There is no Gradle wrapper in this repo.** `gradlew` and `gradle-wrapper.jar` are absent; only
  `gradle-wrapper.properties` is checked in. CI provisions Gradle 9.3.1 explicitly. Locally you
  need Gradle 9.3.1 installed, or you must generate a wrapper yourself.
- **There is no local Gradle on the dev machine used for most of this work**, so CI has been the
  only compile check. Treat every push as unverified until the Actions run is green.
- `debug.keystore` is gitignored but the debug build type is configured to sign with it. CI
  generates one with `keytool`; locally you must too, or `assembleDebug` fails.
- `google-services.json` is absent and that is fine — `missingGoogleServicesStrategy = WARN`.
- **`assets/hisn_al_muslim.json` is referenced but not in the repo.** `loadHisnAlMuslimData()`
  swallows the resulting `FileNotFoundException`, so a clean checkout silently ships the 14
  built-in duas from `DuaData.kt` rather than the full Hisn al-Muslim set.

### CI traps already hit

| Symptom | Cause |
| --- | --- |
| Thousands of lines of Google TV / Glass / XR licence text | `setup-android` echoes every licence while accepting them. `log-accepted-android-sdk-licenses: false` |
| `Failed to find package 'tools'` | `setup-android` installs the legacy `tools` package, removed from the SDK repo years ago |
| `Wrong version in preinstalled sdkmanager`, then an eight-minute retry loop | `setup-android` rejected the runner's own cmdline-tools and re-downloaded its own. It did eventually succeed, but the same step takes ~37s when it does not. **The action is no longer used** - the runner ships a complete SDK with licences accepted, so the workflow just uses it |
| `KSP ... ApplicationManager.getApplication() is null` | **Not a failure.** Thrown on a background AWT thread inside KSP's IntelliJ embedding. Present even on successful builds. Ignore it |
| Node 20 deprecation warning | Informational, every run |

---

## Architecture decisions worth not re-litigating

### Prayer times

- `PrayerCalculator` is **pure and synchronous** — no Room, no network, no coroutines. That is what
  lets the alarm receiver call it directly without `goAsync()` gymnastics for the common path.
  Keep it that way.
- `completedPrayerIds` only populates a display flag. Notifications pass `emptySet()`.
- **Default method is Umm al-Qura**, which sets Isha as Maghrib + 90 min (120 in Ramadan), not an
  angle. There is no universal convention; this was a deliberate user choice.
- **High-latitude rule.** `sunAngleTime` used to clamp `cos(hourAngle)` into `[-1, 1]`, which
  silently collapsed Fajr and Isha onto solar midnight wherever the sun never reaches the twilight
  angle — London manages only −15° at midsummer, so both prayers showed ~01:00 for months. Fajr and
  Isha now use `sunAngleTimeOrNull` and fall back to one-seventh of the night. Sunrise, sunset and
  Asr keep the clamped path because they always occur.

### Default madhab is Maliki

Derived from `IslamicMadhab.DEFAULT`, which reads the `isDefault` flag, so the flag and the real
default cannot drift. **Maliki, Shafi'i and Hanbali all share `shadowFactor = 1.0`** — only Hanafi
(2.0) changes any time. Switching among the other three changes the badge only.

### Notifications

- **`AlarmManager`, never WorkManager.** WorkManager's contract is best-effort and batched; a Fajr
  alert twenty minutes late is a correctness bug.
- **Prayer alerts use `setAlarmClock`.** `setExactAndAllowWhileIdle` is quota-limited to roughly one
  alarm per app per nine minutes in deep Doze, which would push every five-minute follow-up out to
  nine, precisely for Fajr and Isha. The cost is a permanent status-bar alarm icon; `discreetAlarmMode`
  opts out and says so honestly.
- **Never hang rescheduling off `prayerSchedule`.** It is driven by a one-second ticker and would
  re-arm ~30 alarms every second the app is open. Hook the setters: `applyCity`,
  `setCalculationMethod`, `setMadhab`, and the Settings toggles.
- **Receiver extras carry identity keys only** — enum names plus an epoch day. Copy, times and
  language are recomputed at fire time, so an alert armed yesterday is still correct after a
  language or method change.
- **Channel ids are versioned** (`haasi_prayer_alerts_v2`). A channel's sound, importance and
  vibration are immutable after creation on API 26+, so changing any of them needs a new id, and
  `ensureChannels` deletes superseded ones. Bump only for those attributes, never for copy — each
  bump costs users their per-channel customisations.
- Four independent mechanisms keep the schedule alive: 48-hour horizon, reschedule at the tail of
  every fired alarm, a daily refresh at 00:05 local, and `Application.onCreate`.

#### The prayer time is an alarm, not a notice

A notification could not do this job: its sound is a system-played one-shot, and a row in the shade
that goes quiet after four seconds cannot wake anyone for Fajr.

- **`PrayerAlarmActivity` is reached through a notification's full-screen intent**, never
  `startActivity`. Background activity launches have been blocked since Android 10; the full-screen
  intent is the sanctioned path for an alarm that must appear over the lock screen. The notification
  is the vehicle, not the alert.
- **Two channels, on purpose.** `haasi_prayer_alarm_v1` is silent and vibration-free, because
  `AlarmSoundPlayer` owns the tone on that path; `haasi_prayer_alerts_v2` stays audible and is the
  fallback whenever full-screen intents are refused, plus what the five-minute nudge uses. Adding a
  channel rather than bumping the audible one's version was deliberate - a bump costs every user
  their per-channel settings.
- **Android 14 narrowed `USE_FULL_SCREEN_INTENT`** to calling and alarm apps.
  `canUseFullScreenAlarm` asks rather than assumes, and Settings' reliability panel has a row for it
  with a deep link, so a refused permission is visible instead of silently degrading the alarm.
- **The three-minute auto-stop lives in `AlarmSoundPlayer`, not the activity.** The system downgrades
  a full-screen intent to a heads-up banner whenever the device is already unlocked and in use, so an
  activity-owned timer would never run and an untapped banner would ring until the process died.
- **Only `AT_TIME` and Tahajjud take over the screen.** The five-minute follow-up stays an ordinary
  notification; taking over the display twice per prayer makes a reminder something to switch off.
- `AlarmStopReceiver` backs both the notification's Stop action and its delete intent, so clearing
  the notification by any route - including Clear all - silences the ring.

### Location

- **Coordinates and the city name are separate problems.** GPS gives coordinates with no network;
  only the *name* needs a lookup. Offline the location renders as `23.81°N, 90.41°E` and the times
  are still exact. `applyCity` re-attempts the naming for any coordinates-only city, so a fix taken
  offline gets a real name as soon as there is a network, without touching the coordinates.
- **Location permission is requested on the first launch**, before anything else, and the first run
  blocks on the result. This is not negotiable: without the permission `lastKnownFix` returns null
  *before it consults the receiver*, so deferring the request to the "Detect" button meant the GPS
  rung was never tried at all. See the bug table.
- **A cold fix gets 90 seconds, not 20.** A receiver with no almanac reads the ephemeris off the
  satellites at 50 bits per second; 30 to 60 seconds is normal and 20 gives up mid-download.
  `DeviceLocationProvider.COLD_FIX_TIMEOUT_MS` is used by the first run and by the Detect button,
  both of which show the wait. The 20-second `LIVE_FIX_TIMEOUT_MS` remains the default for anything
  that does not.
- **The wait is shown, not hidden.** `SatelliteAcquisitionScreen` holds the screen with an orbital
  animation, a second counter and a "enter my location instead" escape. A minute behind a bare
  spinner reads as a hang, and the acquisition genuinely cannot be made faster.
- **Platform `LocationManager`, not the fused provider.** Play Services' fused provider answers from
  Wi-Fi and cell towers, which needs connectivity and would defeat the entire point. GPS provider
  first, network only as a fallback.
- IP geolocation (`ipinfo.io`) is the *rough* fallback — it reports the ISP's registered hub. It
  placed a Helsinki user in Turku, 150 km away, shifting prayer times by 8–11 minutes.
- Place search uses **Open-Meteo's geocoding API** — same family as the weather API, free, no key,
  and it returns an IANA timezone, so a result maps straight onto `CityLocation`.

### The app name is deliberately spelled two ways

| Where | Spelling | Why |
| --- | --- | --- |
| `strings.xml` → launcher label, Play Store title | `Haasi al-Layl` | A store title is a search key. The overwhelmingly common query is the unaccented spelling, and Play's diacritic folding is not something to bet the listing on |
| `LocalizationManager.getAppName` → in-app header, welcome, About | `Ḥaasi al-Layl` | The scholarly transliteration, where `ḥ` marks the Arabic ḥāʾ (ح) as distinct from hāʾ (ه) |

The article stays lowercase (`al-Layl`, not `Al-Layl`) because that is the transliteration
convention for a medial definite article, and it matches the logo artwork and the About dialog.

`Ḥ` is U+1E24, in Latin Extended Additional. Typography uses `FontFamily.Default`, and every
modern Android system font covers that block — but if a custom font is ever introduced, check the
glyph before using it in the header, or it renders as tofu.

### Localization

`LocalizationManager` is a ~1400-line object of `fun getX(language): String = when (language)`.
There is no `strings.xml` for these. Two conventions:

- Names belonging to an entity live **on the enum** (`IslamicMadhab.getLocalizedName`,
  `CalculationMethod.getLocalizedName`), not in `LocalizationManager`.
- Anything stored in Room is **storage, not display**. Tasbih names are keyed by row id through
  `LocalizationManager.getTasbihName(id, language, fallback)`; the English in the seed list is only
  a fallback, because a stored string would pin the user to whatever language was current at seed
  time.

**"Of the day" content is keyed on the epoch day**, not randomised and not stored:
`DailyInspirationData.forDate`. Everyone sees the same verse on the same day, it survives a process
restart without repeating or skipping, and the two pools are coprime in length (12 verses, 11
narrations) so the *pairing* takes 132 days to come round rather than 12. It is driven from
`currentCityDate`, so it turns over at the same local midnight the prayer log does, and turns over
at all while the app is open.

The app is **deliberately light-only**. `HaasiTheme` takes no `darkTheme` parameter — it used to
accept one and silently ignore it, which read as a bug. A dark palette exists unused in `Color.kt`.

---

## Bugs fixed, and what made them hard to see

Worth reading before assuming a report is a device problem.

| Bug | Why it hid |
| --- | --- |
| **Notifications never fired at all** | `HaasiApplication` was never registered: the manifest tag was `<application ` *with a trailing space*, so a string replacement matched nothing and no assertion caught it. Nothing called `ensureScheduled`, so no alarm was ever armed. Looked exactly like OEM battery-killing |
| **Tahajjud silently dropped** | `showTahajjudNotification` never called `createNotificationChannels`. On API 26+ posting to a non-existent channel is dropped with no error |
| **POST_NOTIFICATIONS never requested** | The only launcher sat behind toggles that default to `true`, so the switches rendered already-on and the off→on transition never happened. Every `notify` was then a no-op wrapped in `catch (_: SecurityException) {}` |
| **Tasbih counters reset every launch** | `seedDefaultTasbihIfEmpty()` had **no emptiness check** despite the name, and its `@Insert(REPLACE)` deleted and recreated all six rows |
| **Prayer circles looked inert** | The query's date was captured once at construction while the writer recomputed it per tap, so they diverged across local midnight. Separately, a tap before Room's first emission built an all-false entity and REPLACE-overwrote the day |
| **Qibla off by the declination** | A true-north bearing minus a magnetic-north heading. `GeomagneticField` appeared nowhere. ~0.3° in Dhaka so it looked fine there; 5–20° in the UK and US |
| **Dark launch screen** | `themes.xml` was one empty style inheriting `android:Theme.DeviceDefault.NoActionBar`, which follows system dark mode. Nothing to do with the Compose theme |
| **Madhab never persisted** | No preference key existed at all, and `SHAFI` was hardcoded in five places |
| **Calculation-method chips unlocalized** | The enum lived in `PrayerCalculator`, a pure-astronomy file, with English-only strings |
| **Salawat untranslated in all five languages** | The private helpers in `TasbihScreen` early-returned English for everything but Bengali, *and* both `when` blocks omitted the `"salawat"` id entirely |
| **`PendingIntent` cancels were no-ops** | `cancelAll` built intents without an action while arming set one. Action is part of PendingIntent identity, so `FLAG_NO_CREATE` never matched |
| **Offline first run ignored GPS entirely and asked for a language and a city** | The whole GPS ladder was in place and correct, but `ACCESS_FINE_LOCATION` was only requested when the user tapped Detect. `lastKnownFix` opens with `if (!hasPermission(context)) return null`, so on a fresh install the cached read returned null *without consulting the receiver*, and with no network there was no IP rung either — straight to the manual dialog. Read as "GPS does not work"; it had never been asked to |
| **Weather stayed on Makkah until a manual refresh** | `refreshWeather()` was called once in the ViewModel's `init`, against the city restored from disk — Makkah on a fresh install — and `applyCity` never re-ran it. Detection landed a moment later and moved the city, the header, and the prayer times, but not the forecast. A city collector owns it now |
| **"Ayah of the day" never changed** | `IslamicContentProvider.DAILY_INSPIRATION` was a single constant. One value cannot be "of the day"; it is now a pool selected by epoch day |

### The meta-lesson

Two of the worst bugs above were **silent failures of my own edits** — a string replacement that
matched nothing because of a trailing space, and a conditional import block that added three of five
imports. Both would have been caught by asserting on every replacement. Every scripted edit in this
repo should assert its match count.

---

## Still outstanding

- **Tests.** None of the planned ones are written: `UserPreferencesTest`, `TasbihSeedTest` (the
  highest-value regression — seed, increment, seed again, assert counts survive),
  `PrayerLogToggleTest`, `PrayerAlertPlannerTest`, `AlertIdsTest` with pinned literals,
  `QiblaCalculatorTest`, and a DST test. Robolectric and Roborazzi are already configured at
  `sdk = [36]`. **CI runs only `assembleDebug`, so test code is not even compiled there.**
- **The Hisn al-Muslim asset** is missing (see above).
- **Prayer log has no city dimension** — `PrayerLogEntity` is keyed on date alone, so completions
  carry across cities. Deliberate: "did I pray Fajr" is a fact about the worshipper. Adding it needs
  a schema bump.
- **`fallbackToDestructiveMigration()` has been removed**, so the next schema change needs a real
  `Migration`. That was intentional — it was pointed at exactly the tasbih counts that were being
  protected.
- **Onboarding language rules**: Bangladesh → Bengali, Arab states → Arabic, Spanish/Portuguese
  worlds → their own, everything else → English. `detectLanguageForCountry` accepts both display
  names and ISO alpha-2 codes — it used to accept only names, so every IP-detected location
  (which returns `"BD"`, not `"Bangladesh"`) fell through to English.

### The first-run state machine

`OnboardingState` is the contract between the ViewModel's ladder and what MainActivity puts on
screen. Read them together or neither makes sense.

| State | What is on screen | Leaves when |
| --- | --- | --- |
| `RESOLVING` | the acquisition screen | `init` decides, within a frame — except when location is off, where it covers the IP lookup too |
| `NEEDS_LOCATION_PERMISSION` | the acquisition screen, under the system dialog | the user answers, via `onLocationPermissionResult` |
| `ACQUIRING_FIX` | the acquisition screen, counter running | a fix lands, the 90s budget expires, or the user taps the escape |
| `REQUIRED` | `WelcomeDialog` — language, then the city picker | the user dismisses it |
| `NOT_NEEDED` | the app | never; also the initial state for a returning user |

Two things are easy to break here. The **notification** prompt is gated on `NOT_NEEDED`, so it
cannot land on top of the acquisition screen or the welcome dialog — two system dialogs racing into
one frame means one is dropped. And the cached-fix attempt lives *inside* `ACQUIRING_FIX` rather
than before it, because it reverse-geocodes and can spend eight seconds on an HTTP timeout.

## Device reality (OPPO / ColorOS, the main test device)

Not fixable in code, and worth ruling out before debugging:

- `BOOT_COMPLETED` is blocked unless auto-launch is enabled in Startup Manager.
- **Sleep standby optimisation** is on by default and delays even exact alarms overnight — the most
  likely cause of a missed Fajr.
- Locking the app in Recents is the most effective user-side mitigation.
- A foreground service is **not** an escape hatch; ColorOS kills those too.

Settings has a reliability panel reporting what is blocked, and a **Test alert in 1 minute** button
that arms a real alarm through the production receiver, PendingIntent and channel. If that arrives
with the app closed, the pipeline works and the problem is elsewhere.
