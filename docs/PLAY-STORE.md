# Publishing on Google Play

What the app needs before its first upload, and the answers to Play Console's forms. Keep this in
step with the code: if a network call or permission is added, this page and
`docs/site/haasi-al-layl/privacy/index.html` change with it.

## Before the first upload

- [x] **Application ID** — now `com.ahsanulhouqe.haasiallayl` (was `com.aistudio.haasiallayl.hall`, AI Studio's prefix).
      It can never change once published.
- [x] **Removed the unused Firebase dependencies** in `app/build.gradle.kts`: `firebase-bom`,
      `firebase-ai`, `firebase-appcheck-recaptcha`, `firebase-appcheck-debug`. No code uses them,
      and the App Check *debug* provider must not ship in a release build.
- [x] **Site published** (checked 2 October 2026, all HTTPS 200): the privacy policy at
      `https://www.ahsanulhouqe.com/haasi-al-layl/privacy/` and the guide at `…/guide/`, with
      localized versions such as `…/guide/es/`. The app links to both
      (`DeveloperContact.PRIVACY_POLICY`, `USER_GUIDE`); the privacy URL also goes into Play
      Console → App content → Privacy policy. See `docs/site/README.md`.
- [ ] **A signed release bundle (`.aab`).** Play accepts only Android App Bundles, signed with
      your upload key; CI currently builds a *debug APK* (`assembleDebug`). Steps:
      1. Create an upload key once, and back it up somewhere safe — losing it means asking Google
         to reset it:
         `keytool -genkeypair -v -keystore my-upload-key.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000`
      2. Add three repository secrets (GitHub → Settings → Secrets and variables → Actions):
         `UPLOAD_KEYSTORE_BASE64` (the output of `base64 -w0 my-upload-key.jks`),
         `UPLOAD_STORE_PASSWORD` and `UPLOAD_KEY_PASSWORD`. The key alias must be `upload`.
      3. Push to `build-apk-release`: CI then builds the signed `.aab` (`gradle bundleRelease`) and
         attaches it to the GitHub release beside the APK. Without the secrets it skips this step
         with a notice. Never commit the `.jks` file.
      **Pick one route for uploads.** If AI Studio's "Publish" is used for the first upload, its key
      becomes the upload key, and CI's bundles would be rejected (and vice versa) until the upload
      key is reset through Play support.
      4. Enrol in **Play App Signing** at the first upload (Google keeps the app-signing key; yours
         is only the upload key).
- [x] **Unit tests run in CI** on every push, after the APK (JDK 21, which Robolectric needs for
      SDK 36). First green run: build 55, all 40 tests. Their first run found a real parser bug
      (a repeated Hisn ID overwrote a translation) and a stale app-name assertion, both fixed.
- [ ] **A clean-install test** on a real phone with the new application ID: location disclosure →
      permission → correct town (the Panchagarh/Natore case), exact-alarm card → Allow, a prayer
      alarm, the 5-minute reminder.
- [ ] **Closed test first.** New personal developer accounts must run a closed test with at least
      12 testers for 14 days before production access. Recruit them early.
- [ ] `versionCode` bumped for every upload (it is 1 now; `versionName` is 1.0.0).

## Play Console: what else it asks for

- **Store listing:** app name (≤ 30 chars), short description (≤ 80), full description (≤ 4000);
  a 512×512 icon; a 1024×500 feature graphic; at least 2 phone screenshots (the guide's are
  usable). One listing per language you support.
- **App content:** privacy policy URL; ads — *no*; app access — *no login needed*; content rating
  questionnaire (no violence, no user-generated content, no data sharing beyond the table below);
  target audience — general, not designed for children; Data safety (below); permission
  declarations (below); government / news / health apps — *no*.
- **Category:** Lifestyle (where most prayer apps sit).
- **Accuracy disclosure, recommended:** the Hisn al-Muslim translations and the 99 Names' glosses
  are the app's own and unreviewed (see ENGINEERING-NOTES). Saying so in the description is better
  than a reviewer or user finding it.

## Data safety form

| Question | Answer |
| --- | --- |
| Does the app collect or share user data? | **Yes** (location and IP go to third-party services) |
| Is all data encrypted in transit? | **Yes** — every request is HTTPS |
| Can users request data deletion? | **Yes** — nothing is stored off the phone; uninstalling or clearing app data deletes it |

Data types:

| Data type | Collected / shared | Purpose | Optional? | Processed ephemerally? |
| --- | --- | --- | --- | --- |
| Location → **Precise location** | Shared (BigDataCloud, Open-Meteo) | App functionality | Yes (the app works with a city picked by hand) | Yes |
| Location → **Approximate location** | Shared (ipinfo.io, from the IP address, only when the device gives no location) | App functionality | Yes | Yes |
| App activity → **Other user-generated content** (city names typed in search) | Shared (Open-Meteo geocoding) | App functionality | Yes | Yes |

Not collected: personal info, financial info, health, messages, photos, audio, files, calendar,
contacts, app activity/history, web browsing, app info & performance, device or other IDs.
No data is used for advertising or analytics, and nothing is sold.

## Permission declarations

| Permission | What to say in Play Console |
| --- | --- |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Foreground only. Prayer times and the Qibla depend on where the user is. A prominent disclosure (`LocationDisclosureDialog`) is shown before the system prompt. |
| `SCHEDULE_EXACT_ALARM` | User-granted. Prayer alerts must ring at the prayer time; the user allows it from a card on Today. (The app deliberately does **not** use `USE_EXACT_ALARM`, which Play reserves for alarm-clock and calendar apps.) |
| `USE_FULL_SCREEN_INTENT` | The prayer alarm shows over the lock screen like an alarm clock. Falls back to a normal notification when refused. |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Alarms/reminders are the app's core function; some manufacturers' power managers kill scheduled alarms otherwise. Only offered from Settings' reliability panel, never forced. |
| `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK`, `VIBRATE` | Prayer alerts; re-arming alarms after a reboot. |

## Store listing (search)

Search ranking follows the title, short description, localized listings and quality signals:

- **Title (30 chars):** keywords, not just the name — e.g. *Haasi al-Layl: Prayer & Qibla*.
- **Short description (80 chars):** prayer times, azan alarm, Qibla, Tasbih, Hisn al-Muslim, 99 Names.
- **A listing per language.** Bengali matters most: নামাজের সময়, আজান, কিবলা, দোয়া, হিসনুল মুসলিম.
- **Screenshots** in Bengali and English: the alarm, the current-prayer panel, the Qibla.
- **Niche terms** the big apps do not own: "Hisn al-Muslim Bangla", "নামাজের সময় অফলাইন",
  "Tahajjud alarm", "sahih".
