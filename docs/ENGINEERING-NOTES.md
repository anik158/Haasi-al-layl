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

### Location

- **Coordinates and the city name are separate problems.** GPS gives coordinates with no network;
  only the *name* needs a lookup. Offline the location renders as `23.81°N, 90.41°E` and the times
  are still exact.
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
- **Location permission is only requested when the user taps Detect**, so a fresh install still
  starts from IP geolocation.
- **Onboarding language rules**: Bangladesh → Bengali, Arab states → Arabic, Spanish/Portuguese
  worlds → their own, everything else → English. `detectLanguageForCountry` accepts both display
  names and ISO alpha-2 codes — it used to accept only names, so every IP-detected location
  (which returns `"BD"`, not `"Bangladesh"`) fell through to English.

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
