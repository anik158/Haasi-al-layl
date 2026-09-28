# Engineering notes

Working context for this codebase: decisions that are non-obvious from the code, bugs that were
subtle enough to be worth remembering, and the traps that are still set. The README describes what
the app *does*; this describes what you need to know before changing it.

Last updated: 25 September 2026.

---

## Environment

- **There is no Gradle wrapper in this repo.** `gradlew` and `gradle-wrapper.jar` are absent; only
  `gradle-wrapper.properties` is checked in. CI provisions Gradle 9.3.1 explicitly. Locally you
  need Gradle 9.3.1 installed, or you must generate a wrapper yourself.
- **There is no local Gradle, Android SDK or Kotlin compiler on the dev machine**, so nothing
  can be compiled locally: CI is the *only* compiler this project has. Treat every push as
  unverified until the Actions run is green, and note that a green run is also the only way an
  APK exists to install.
- **`gh` is installed** (`C:\Program Files\GitHub CLI\gh.exe`, not on PATH in every shell). The
  fast loop after a push is `gh run watch <id> --exit-status`, then
  `gh run view <id> --log-failed` on a failure. The repository is private, so the browser cannot
  read the Actions page without auth; use `gh`.
- **CI only builds on pushes to `build-apk-release`.** `main` alone produces no APK. Push both.
- `debug.keystore` is gitignored but the debug build type is configured to sign with it. CI
  generates one with `keytool`; locally you must too, or `assembleDebug` fails.
- `google-services.json` is absent and that is fine — `missingGoogleServicesStrategy = WARN`.
- **Hisn al-Muslim's assets are generated - edit `tools/hisn/`, then run `python3 tools/hisn/build.py`.**
  The Arabic, counts and references are vendored from asellam/HisnElMuslim (MIT, notice shipped as
  `assets/hisn/LICENSE-HisnElMuslim.md`) in `tools/hisn/source/`; never write them from memory. The
  four translations in `tools/hisn/translations/` are our own, made from that Arabic - never copy
  sunnah.com (terms forbid whole collections) or hisnulmuslim.org (all rights reserved). They are
  machine-assisted and unreviewed. Ids are assigned 1..302 in the upstream file's order and become
  `hisn_<ID>` bookmark keys: never reorder the source or insert into its middle.

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

### Sehri and Iftar follow the sahih texts, not timetable conventions

- **Sehri ends at Fajr** (Qur'an 2:187; Bukhari 1918-1919). The calculator's `IMSAK` (Fajr − 10)
  stays in the schedule list but is never the end of Sehri: it is a precaution with no basis in the
  sunnah, and the user asked for sahih guidance over convention.
- **Iftar is Maghrib**, i.e. sunset + `MAGHRIB_MARGIN_MINUTES` (3). The margin is computational
  (refraction, elevation), not fiqh; the forbidden window uses the true sunset (`trueSunset`).
- **The fast switches at Iftar, not midnight** (`FastingTimes.statusAt`), so the first Sehri is on
  screen the evening before 1 Ramadan.
- **`HijriDateHelper.adjustmentDays`** is loaded in `HaasiApplication.onCreate`, before any receiver
  can compute a Ramadan Isha interval, and is part of the alarm schedule signature.
- **The adjustment is automatic unless overridden.** `hijriAdjustmentOverride == null` means "use
  `automaticAdjustmentFor(country)`" (−1 for BD/IN/PK/NP/LK/MA, else 0). The `selectedCity` setter
  re-applies it, so a new country moves Ramadan before `applyCity` re-arms the alarms. A manual
  setting was the first version; users should not have to know what an Umm al-Qura offset is.
- References were checked against sunnah.com: Bukhari 586, 1918-1919, 1921, 1923, 1954, 1957;
  Muslim 831, 832.

### Default madhab is Maliki

Derived from `IslamicMadhab.DEFAULT`, which reads the `isDefault` flag, so the flag and the real
default cannot drift. **Maliki, Shafi'i and Hanbali all share `shadowFactor = 1.0`** — only Hanafi
(2.0) changes any time. Switching among the other three changes the badge only.

### Notifications

- **`AlarmManager`, never WorkManager.** WorkManager's contract is best-effort and batched; a Fajr
  alert twenty minutes late is a correctness bug.
- **Prayer alerts use `setAlarmClock`.** `setExactAndAllowWhileIdle` is quota-limited to roughly one
  alarm per app per nine minutes in deep Doze, which would push every five-minute follow-up out to
  nine, precisely for Fajr and Isha. The cost is a status-bar alarm icon, and there is no opt-out:
  the old "Hide the alarm icon" setting was removed as confusing.
- **Arm one alert per slot, the next one.** `AlertIds` reuses a slot per prayer and kind every day,
  and arming a slot replaces its PendingIntent. The scheduler plans three days, so it must keep only
  the earliest per slot (`PrayerAlertPlanner.nextPerSlot`) - before that, tomorrow's alarm always
  overwrote today's and no real prayer alert ever fired, while the one-minute test alarm (armed
  last) did. Each alarm that fires re-arms the next.
- **Never hang rescheduling off `prayerSchedule`.** It is driven by a one-second ticker and would
  re-arm ~30 alarms every second the app is open. Hook the setters: `applyCity`,
  `setCalculationMethod`, `setMadhab`, and the Settings toggles.
- **Receiver extras carry identity keys only** — enum names plus an epoch day. Copy, times and
  language are recomputed at fire time, so an alert armed yesterday is still correct after a
  language or method change.
- **Channel ids are versioned** (`haasi_prayer_alerts_v3`, `haasi_forbidden_alerts_v3`,
  `haasi_tahajjud_alerts_v2`). A channel's sound, importance and vibration are immutable after
  creation on API 26+, so changing any of them needs a new id, and `ensureChannels` deletes
  superseded ones. Bump only for those attributes, never for copy — each bump costs users their
  per-channel customisations. The last bump was the forest-birdsong tone.
- **The tone is addressed by resource name**, `android.resource://<pkg>/raw/haasi_alert`, never
  `R.raw.haasi_alert`. A channel stores its sound URI permanently, and numeric resource ids are
  reassigned when resources are added - so the numeric form would one day point a live channel at
  the wrong file, or at nothing, with no error. `PrayerNotificationManager.alertSoundUri` is the
  one place it is built; `AlarmSoundPlayer` uses it too, so the two cannot drift apart.
- Four independent mechanisms keep the schedule alive: 48-hour horizon, reschedule at the tail of
  every fired alarm, a daily refresh at 00:05 local, and `Application.onCreate`.

#### The prayer time is an alarm, not a notice

A notification could not do this job: its sound is a system-played one-shot, and a row in the shade
that goes quiet after four seconds cannot wake anyone for Fajr.

- **`PrayerAlarmActivity` is reached through a notification's full-screen intent**, never
  `startActivity`. Background activity launches have been blocked since Android 10; the full-screen
  intent is the sanctioned path for an alarm that must appear over the lock screen. The notification
  is the vehicle, not the alert.
- **The notification carries the sound; the full-screen alarm only adds to it.** A silent
  `haasi_prayer_alarm_v1` channel once left all audio to `AlarmSoundPlayer` and produced alerts
  with no sound at all (see the bugs table); it is swept on startup and must not come back. The
  channel plays the tone once as the alert posts. `PrayerAlarmActivity` starts the *looping* ring
  when it opens - so if Android downgrades the full-screen intent to a banner, the one-shot channel
  tone is the only sound. Both are five seconds: `haasi_alert.mp3` is trimmed to 5 s, so the
  channel's one-shot and the looping ring end together.
- **Android 14 narrowed `USE_FULL_SCREEN_INTENT`** to calling and alarm apps.
  `canUseFullScreenAlarm` asks rather than assumes, and Settings' reliability panel has a row for it
  with a deep link, so a refused permission is visible instead of silently degrading the alarm.
- **The five-second auto-stop lives in `AlarmSoundPlayer`, not the activity.** The system downgrades
  a full-screen intent to a heads-up banner whenever the device is already unlocked and in use, so an
  activity-owned timer would never run and an untapped banner would ring until the process died.
  It was three minutes; a prayer alert is a reminder, not a wake-up alarm that must be dismissed.
- **The tap action never rings.** It used to share the full-screen PendingIntent, and the activity
  rings on open - so a banner left in the shade rang its old prayer's alarm when tapped an hour
  later. Tapping now opens the screen with `EXTRA_SILENT` under its own request code (extras do not
  distinguish PendingIntents), the activity only rings for an alert under a minute old
  (`EXTRA_FIRED_AT`), and alarm notifications time out after ten minutes.
- **Only `AT_TIME` and Tahajjud take over the screen.** The five-minute follow-up stays an ordinary
  notification; taking over the display twice per prayer makes a reminder something to switch off.
- `AlarmStopReceiver` backs both the notification's Stop action and its delete intent, so clearing
  the notification by any route - including Clear all - silences the ring.

### Location

#### The placeholder city is not an answer, and three bugs came from forgetting it

`UserPreferences.selectedCity` is **null** when the app has no location, and the ViewModel falls
back to `PRESET_CITIES[0]` - Makkah - so there is something to render. Nothing could tell that
placeholder from a resolved location, and it was consequently believed three times over: the
location chip announced "Makkah, Saudi Arabia" to a user in Helsinki, the interface language was
derived from that country so every first-run screen came up in Arabic, and the Qibla compass
measured its bearing *from* it.

- **Read `hasResolvedLocation`, never infer from the rendered city.** It is the only thing that
  distinguishes the two.
- **Language falls back to the device locale** until a location is genuinely known.
  `AppLanguage.fromCode` already yields English for anything unrecognised, so
  `fromCode(Locale.getDefault().language)` is the whole rule - and a far better guess than a
  placeholder's country.
- **"Has been onboarded" and "has a location" are different flags.** They were one, and dismissing
  the welcome dialog set it, so an installation that never resolved a location believed it had
  one: permanently on Makkah, with the ladder never re-run on any later launch.
  `isOnboardingComplete` now records only that the user has been *asked*; the ladder runs whenever
  `selectedCity == null`, on every launch.

#### The Qibla compass measures from the device, not from the selected city

`qiblaOrigin` is `_deviceLocation ?: _selectedCity`. Prayer times legitimately follow a chosen
city - checking Makkah's times from Dhaka is a reasonable thing to want - but a bearing is a
property of where the needle physically is, so a chosen city points the arrow from somewhere the
user is not standing. `_deviceLocation` is populated **only** from real receiver coordinates,
never from IP geolocation, which names the ISP's hub and can be 150 km out. When the fallback is
in use, `qiblaUsesDeviceLocation` is false and the screen says so.

#### The ladder is ordered by what answers fastest, not by what is most accurate

This reversed once and the reversal was wrong. **When there is a network, IP geolocation goes
first** - it answers in under a second, needs no permission, and puts the app in a usable state
immediately. Only with no network (or a failed IP lookup) does the app block on the receiver
behind the acquisition screen. Putting GPS first unconditionally meant an online user was shown a
"turn on location" screen to solve a problem the network had already solved.

Accuracy is then recovered rather than traded away: `UserPreferences.locationFromDevice` records
which rung answered, and while it is false the background watch keeps running and upgrades the IP
estimate to real coordinates when a fix lands. So the *order* is by latency and the *outcome* is
still the top of the ladder.

Consequences worth not undoing:

- `startBackgroundLocationWatch` is **not** gated on `hasResolvedLocation`. Having *a* location is
  not the same as having a good one; the gate is `locationFromDevice`.
- **Location permission is requested independently of the ladder.** It was briefly tied to the
  `REQUESTING_PERMISSION` phase, which meant an online run - which shows no phases at all - never
  asked, and so had nothing for the Qibla compass or the background upgrade.
- `LocatingPhase.STARTING` renders the orbit animation and nothing else, so the sub-second pass
  through `RESOLVING` reads as a continuation of the splash rather than a flash of prompts.

#### "Turn on location" is wrong advice to someone who already has

`isLocationEnabled` reports the **master** toggle. On ColorOS that can be on while the GPS
provider is off, so the phone positions from Wi-Fi and masts alone: online merely imprecise,
offline nothing at all - and indistinguishable, from the user's side, from location being enabled.
The app spent ninety seconds on a switched-off receiver and reported "no signal", which is true,
useless, and reasonably read as the app being broken.

- `isGpsProviderEnabled` is a **separate check** from `isLocationEnabled`. Both are needed.
- `LocatingPhase.GPS_PROVIDER_OFF` says exactly that, opening by acknowledging location *is* on.
- The fix is one tap. `LocationSettingsResolver` wraps
  `SettingsClient.checkLocationSettings(PRIORITY_HIGH_ACCURACY)`, which returns a `PendingIntent`
  for the familiar system dialog that switches on whatever is missing - no per-OEM settings hunt.
  An `IntentSender` can only be launched from an Activity, so the ViewModel emits the
  `PendingIntent` and `MainActivity` consumes it through `StartIntentSenderForResult`.
- **`play-services-location` is used for that dialog and nothing else.** The fused provider stays
  unused on purpose: it answers from Wi-Fi and cell towers, needs connectivity, and would defeat
  the premise. Play Services is also absent on some devices, so every caller must handle a null
  resolution by falling back to `ACTION_LOCATION_SOURCE_SETTINGS`.
- The dialog's result code is ignored; `onReturnedFromLocationSettings` re-reads the switches,
  because the user may have changed them by hand or backed out.

#### A stored location is a claim with an expiry date

The first location an installation resolved used to be the last one it ever had: `resolveOnboarding`
returns immediately when a city is stored, and the background watch only runs while one is
missing. Someone who set the app up in Dhaka and then moved kept Dhaka's prayer times indefinitely,
with nothing on screen suggesting anything was wrong.

`refreshLocationIfMoved` runs on every resume and is deliberately cheap: `lastKnownFix` reads the
fix the system already holds, waking no radio and needing no network, so it works offline exactly
as well as online.

- **`MOVED_THRESHOLD_KM = 25`.** Prayer times move with longitude at roughly four minutes per
  degree, so 25 km is about a minute of drift — the point where a different answer justifies
  re-arming ~30 alarms. Anything much smaller re-arms the schedule for someone crossing their own
  city.
- **A timezone change is its own move, and a bigger one.** `timeZoneId` is captured from
  `ZoneId.systemDefault()` at fix time and then frozen, so flying to another country computed every
  prayer time in the zone left behind. Checked separately from distance, because the phone updates
  its own zone and the stored city does not.
- **`locationUpdatedAt` is stamped in `applyCity`**, the one place a location becomes real, so
  staleness cannot drift out of sync with the value it describes.
- A city the user picked by hand is never overridden — they may be checking another city's times
  on purpose.
- **It runs on every resume (`onAppResumed`, via `LifecycleResumeEffect`), not only a cold start.**
  A phone that kept the process alive for days otherwise never re-checked.
- **An IP estimate is replaced by any real fix, however close.** The 25 km threshold is for
  comparing two real fixes; an IP location's coordinates are the ISP's hub, so distance from it
  means nothing.

#### An IP estimate must never be the last word

A user in Panchagarh was shown Rangpur, 80 km off, with Rangpur's prayer times. ipinfo named the
ISP's hub; that part is expected. The bug was that nothing ever corrected it:
`startBackgroundLocationWatch` returned for want of permission while the permission dialog was
still up, and `onLocationPermissionResult` then returned early because a city was already stored.
On later launches the watch only restarted after seven days of staleness, since `applyCity` had
just stamped the estimate as fresh.

- Granting permission after an IP estimate now starts the watch and the re-check.
- The watch runs whenever `!locationFromDevice`, not only while no city is stored.
- **It is bounded to two minutes** (`WATCH_TIMEOUT_MILLIS`) and retried on resume, so indoors it
  cannot hold the GPS on for a whole session. It already stops at the first fix.
- Reverse geocoding never falls back to `adminArea` / `principalSubdivision`: in Bangladesh that is
  the *division*, which would have printed "Rangpur Division" for Panchagarh. Town, else district,
  else coordinates.

#### "No signal" was three different failures wearing one label

A receiver that is switched off, one that hears nothing, and one that hears six satellites but is
still reading their orbit data are indistinguishable from outside. That is why the earliest offline
failures were reported as the app being broken: it had nothing else it could say.

`GnssDiagnostics` wraps `GnssStatus` and `SatelliteSignalDialog` renders it — satellites in view,
satellites *used in the fix*, and C/N0 per satellite, with a plain-language verdict for each of the
three cases. Two things about it that are easy to undo by accident:

- **It must drive the GPS engine itself.** `GnssStatus` only reports while something is asking for
  a position, so the dialog also calls `requestLocationUpdates`. Without that it would sit at zero
  satellites beside a perfectly healthy receiver — precisely the confusion it exists to end.
- **The bars scale against a fixed 45 dB-Hz**, not against the strongest signal present. A display
  that renormalises to its own maximum draws a full-height bar for a hopeless signal.

Being purely observational, it is safe to run only while its dialog is open, and it deliberately
influences nothing.

#### A first fix indoors is not on a timer

Ninety seconds is still a timeout, and a cold fix indoors with no network does not arrive on one -
it arrives when the user next steps outside. So the app stops *blocking* on the fix without giving
up on it: `LocationRepository.observeDeviceLocation` runs for as long as there is no location, and
the first fix to land is adopted. Two things keep that honest:

- `userPrefs.cityChosenManually` stands the watch down, so a deliberate choice is never silently
  overwritten by a fix arriving later.
- `WATCH_MIN_TIME_MS` is **zero**. A non-zero `minTime` lets the provider duty-cycle the receiver,
  which is the opposite of what acquiring a first fix needs. The battery cost is bounded by
  cancelling on the first fix, not by asking for fewer.

The watch only runs while the ViewModel is alive. Acquiring location with the app closed would
need background location permission, which is not worth the Play review burden here.

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
| **First-run screens rendered in Arabic** | Language was derived from `_selectedCity.country`, and on a fresh install that is the Makkah placeholder. The screens explaining the location permission were therefore in Arabic for a user whose location was not yet known |
| **A user who dismissed the welcome dialog was stuck on Makkah forever** | Dismissing it set `isOnboardingComplete`, which also gated the GPS ladder - so no later launch retried, anywhere, ever. The ladder is gated on `selectedCity == null` now |
| **The Qibla needle pointed from Makkah to Makkah** | Bearing and distance were derived from `_selectedCity`, i.e. the placeholder: a bearing of nothing, 0 km away. They come from `qiblaOrigin` now |
| **The full-screen alarm made no sound at all** | Self-inflicted, and worth the detail. The alert went on a *silent* channel with all audio left to a `MediaPlayer` started from the receiver. Android downgrades a full-screen intent to a heads-up banner whenever the device is unlocked and in use, so the screen often never opened - and a `MediaPlayer` started from a `BroadcastReceiver` has no live component keeping its process alive once `onReceive` returns. Both failed together. The notification carries the sound again; the full-screen intent is purely additive |
| **An online first run was asked to turn on location** | The ladder ran GPS-first unconditionally, so a user with a working network was shown a permission prompt and a satellite wait to answer a question IP geolocation could have settled in under a second. Network first now, with the background watch upgrading the estimate |
| **A stored location was never re-checked** | `resolveOnboarding` returns early when a city exists and the watch only runs when one is missing, so the first fix an installation took was permanent. Moving cities kept the old prayer times forever, offline or on |
| **Prayer times stayed in the timezone you left** | `timeZoneId` was captured at fix time and frozen, while the phone updates its own zone on arrival |
| **Badges wrapped their own letters** | A `Row` child without `weight` gets the leftover space, and `Text` wraps rather than pushing back — so the badge, not its sibling, was what yielded |
| **"Turn on location" when location was already on** | `isLocationEnabled` reports the master toggle only. ColorOS can have it on with the GPS provider off, which offline yields no fix at all - so the app waited 90s on a switched-off receiver and said "no signal" |
| **`Missing '}'` and `Unclosed comment` from a scripted edit** | An insertion appended its own anchor twice, leaving a stray `/**` *inside* a KDoc. **Kotlin block comments nest**, unlike C, so the second opener swallowed the rest of the file. A bracket-balance check that resolves each `/*` to the first following `*/` cannot see this - it has to track depth |

### The meta-lesson

Two of the worst bugs above were **silent failures of my own edits** — a string replacement that
matched nothing because of a trailing space, and a conditional import block that added three of five
imports. Both would have been caught by asserting on every replacement. Every scripted edit in this
repo should assert its match count.

---

## Still outstanding

- **Tests.** None of the planned ones are written: `UserPreferencesTest`, `TasbihSeedTest` (the
  highest-value regression — seed, increment, seed again, assert counts survive),
  `PrayerLogToggleTest`, `AlertIdsTest` with pinned literals,
  `QiblaCalculatorTest`, and a DST test. Robolectric and Roborazzi are already configured at
  `sdk = [36]`. **CI runs only `assembleDebug`, so test code is not even compiled there.**
- **The Hisn al-Muslim translations are unreviewed** (see above) - a scholar's pass is still owed.
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

### Hisn al-Muslim: the entries to review first

The translating passes flagged these as idiomatic or ambiguous, where a meaning had to be chosen.
A reviewer should start here. Edit `tools/hisn/translations/` (or `tools/hisn/source/` for the
Arabic) and rerun `python3 tools/hisn/build.py`.

| Dua | The part in question | Why it's flagged |
| --- | --- | --- |
| Waking up: *"Al-hamdu lillāhil-ladhī aḥyānā ba'da mā amātanā wa ilayhin-nushūr"* | "النُّشُّورُ" | Vowel-mark typo in the GitHub source: a doubled shadda. It should be "النُّشُورُ". |
| Waking up: *"Lā ilāha illallāhu waḥdahu… rabbighfir lī"* | ending "أَوْ دَعَا اسْتُجِيبَ لَهُ…" | This ending is the hadith describing the reward ("if he then supplicates, it is answered…"). It isn't meant to be recited, so check that the translation doesn't read as part of the dua. |
| Opening the prayer: *"Allāhu akbar kabīrā… a'ūdhu billāhi minash-shayṭān min nafkhihi wa nafthihi wa hamzih"* | "نَفْخِهِ وَنَفْثِهِ وَهَمْزِهِ" | Literally "his blowing, spitting and pricking". The translation uses the narrators' explanation instead: pride, poetry, madness. |
| Fasting person invited to a meal: *"Idhā du'iya aḥadukum fal-yujib…"* | "فَلْيُصَلِّ" | Here it means "let him supplicate for the host", not "let him pray salah". |
| Against bad omens: *"Allāhumma lā ṭayra illā ṭayruk…"* | "لَا طَيْرَ إِلَّا طَيْرُكَ" | Literally "no bird except Your bird". It's an idiom about omens, so it was translated by meaning. |
| Traveller at dawn: *"Sammi'a sāmi'un biḥamdillāh…"* | "سَمَّعَ سَامِعٌ" | Old, compressed wording that scholars explain in different ways. |
| Spreading salām: *"Thalāthun man jama'ahunna faqad jama'al-īmān…"* | "الإِنْصَافُ مِنْ نَفْسِكَ" | A compressed phrase meaning "being fair even against yourself". |
| Between the Yemeni Corner and the Black Stone: *"Rabbanā ātinā fid-dunyā ḥasanah…"* | Spanish only | The Spanish adds "(201)", which the other languages don't have. It's correct (Al-Baqarah 2:201), but inconsistent. |

### The first-run state machine

`OnboardingState` says what is on screen; `LocatingPhase` carries the detail behind
`LOCATING`. Read them together or neither makes sense.

| State | What is on screen |
| --- | --- |
| `RESOLVING` | the acquisition screen; resolves within a frame |
| `LOCATING` | the acquisition screen, rendering the current `LocatingPhase`. Reached **only** when there is no network, or the IP rung failed - an online first run never enters it |
| `REQUIRED` | `WelcomeDialog` - language, then the city picker |
| `NOT_NEEDED` | the app |

| Phase | Shows | Offers |
| --- | --- | --- |
| `STARTING` | the animation alone - no text, no counter, no buttons | nothing |
| `REQUESTING_PERMISSION` | why location is needed, **no counter** | nothing; the system dialog is up |
| `SEARCHING` | the counter, the elapsed bar, the indoor hint | nothing; it is working |
| `LOCATION_SERVICES_OFF` | that the system toggle is off | the one-tap high-accuracy dialog |
| `GPS_PROVIDER_OFF` | that location is on but the *receiver* is not | the one-tap high-accuracy dialog |
| `PERMISSION_DENIED` | that rung 1 is unavailable and why it matters | app details settings |
| `NO_FIX` | that it is still listening in the background | Try again |

**Every terminal phase is a diagnosis.** The first version collapsed a refused permission, a
switched-off Location toggle and a genuine indoor no-fix into the same manual city picker, which
made all three indistinguishable from the app never having tried - which is exactly how it was
reported. Each phase now names the rung that failed and offers the action that fixes it.

Two things that are easy to break here. The **counter belongs only to `SEARCHING`** - showing
"0s / 90s" behind the permission dialog was the first thing that looked broken on the device. And
the **notification prompt is gated on `NOT_NEEDED`**, so it cannot land on top of the acquisition
screen or the welcome dialog; two system dialogs racing into one frame means one is dropped.

## UI rules that were learned the hard way

### A badge in a Row must never be the thing that yields

Compose gives a `Row` child that has no `weight` whatever space is left, and a `Text` with too
little space wraps rather than pushes back. So a badge beside an unweighted sibling wraps *its own
letters*: "NOW" rendered as `NO`/`W` in English and letter-by-letter vertically in Bengali, "LIVE"
as `LIV`/`E`. In the Qibla header the title `Column` had no weight, took the whole row, and crushed
the bearing chip into a sliver of wrapped characters that read on screen as a tall empty gap.

The rule, wherever a badge shares a row:

- the badge gets `maxLines = 1, softWrap = false`
- the flexible sibling gets `Modifier.weight(1f, fill = false)` and an ellipsis

**But that rule only chooses a loser.** In the prayer list it made the *name* yield instead:
"আসর" collapsed to "…" beside "পরবর্তী". Where the sibling is something that must never truncate
— a prayer name, the app name — do not share the row at all. The NOW/NEXT badge now sits on the
second line beside the Arabic name, and the header's location chip is icon-only. The schedule
popup follows the same layout.

**Bengali did not cause this.** Longer strings simply hit the squeeze at a smaller font scale than
English does, which is why it looked like a Bengali bug and was not.

### `MAX_FONT_SCALE = 1.3`

Android allows a system font scale of 2.0 and beyond, and this app's compact chrome cannot absorb
it — at 2.0 the header wrapped to two lines and pushed out its own tagline. `HaasiTheme` caps the
scale at 1.3 through `LocalDensity`. Not 1.0: that would discard accessibility rather than bound
it. Body text is unaffected in practice because it wraps freely; the cap exists for the chrome.

The same override also fits text to the screen: `responsiveFontFactor` is 1.0 at 393dp, shrinks in
proportion below that to a 0.85 floor, and never enlarges. Final scale is
`min(systemScale, 1.3) × widthFactor`. Only `sp` changes; `dp` spacing and touch targets do not.

### Localization is not done until there are no English literals in `ui/`

A sweep found about thirty — `ForbiddenPrayerTimesCard` did not even import
`LocalizationManager`. Grep for `text = "`, `Text("` and `contentDescription = "` before calling a
screen localized, and for `language == AppLanguage.BENGALI` two-way branches, which leave three
languages on English. `formatTime` pins its AM/PM marker to `Locale.ENGLISH` (with the device
default, a phone in another locale could emit an already-localized marker the replacements then
miss); digits go through `localizeDigits`. Dates too: the Hijri month and era, and the Gregorian
date, follow the *app* language, not the device locale.

### The moon is computed, not looked up

`data/astronomy/MoonCalculator.kt`, pure Kotlin like `PrayerCalculator`. The card used to read its
percentage from a table keyed on the Hijri day — 80% all of day 12, when the real figure was ~88%
and the moon was 42° below the horizon at the midday it was being read. The percentage is the lit
fraction of the disc (the phase), labelled "lit", and is paired with whether the moon is actually
up and when it next rises or sets. Rise/set use real coordinates only: while the location is the
Makkah placeholder, Makkah's moonrise would read as the user's. Checked against published values:
September 2026 new and full moons, and Dhaka moonrise on 23 Sep (15:52 published, 15:53 computed).

### The splash mark is not the launcher icon

`ic_launcher_foreground` keeps its artwork at 69dp on a 108dp canvas because an adaptive launcher
icon may be masked to a circle or a squircle. The splash screen masks nothing, so pointing
`windowSplashScreenAnimatedIcon` at it spent a third of the canvas protecting against nothing and
made the mark look small. `ic_splash_logo` fills the canvas; the launcher icon keeps its safe zone.

### The Qibla chip states a destination, not an origin

"278° Towards Kaaba, Makkah" beside a compass was read on a device as the app computing the bearing
*from* Makkah. It was not — 277.6° and 5,171 km are exactly right from 23.75°N, 90.40°E — but the
wording invited it. It reads "278° to the Kaaba" now, with the origin stated separately underneath.
Worth remembering the general shape: a label that names a place next to a number will be read as
that number's subject.

## Device reality (OPPO / ColorOS, the main test device)

Not fixable in code, and worth ruling out before debugging:

- `BOOT_COMPLETED` is blocked unless auto-launch is enabled in Startup Manager.
- **Sleep standby optimisation** is on by default and delays even exact alarms overnight — the most
  likely cause of a missed Fajr.
- Locking the app in Recents is the most effective user-side mitigation.
- A foreground service is **not** an escape hatch; ColorOS kills those too.

**Background running is the decisive one, and no API can set it.** ColorOS defaults every app to
"Smart background running", which is what silences a Fajr alarm; the alarm works reliably only once
the user picks **Run in Background** themselves, under Battery > App battery usage. Startup Manager
is the same story. There is no intent, no permission and no flag that changes either, so
`getOemBatteryHint` names the exact switches rather than gesturing at them - vague advice just
leaves the user hunting. Apps that appear to "just work" were either whitelisted by the user or
ship on an OEM allowlist; neither is something an app can grant itself.

What *can* be offered is the direct battery-optimisation exemption - one "Allow" rather than
finding this app in a list of every installed one. `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` is
restricted by Play to a short list of permitted use cases, of which "alarms or reminders as core
functionality" is one, and is what this app is; it holds `USE_EXACT_ALARM` for the same reason. Some
heavily skinned ROMs refuse the direct dialog, so it falls back to the full list.

Settings has a reliability panel reporting what is blocked - notifications, exact alarms,
full-screen alarm, battery - and a **Test alert in 1 minute** button that arms a real alarm through
the production receiver, PendingIntent and channel. If that arrives with the app closed, the
pipeline works and the problem is elsewhere.
