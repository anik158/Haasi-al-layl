# Publishing on Google Play

What the app needs before its first upload, and the answers to Play Console's forms. Keep this in
step with the code: if a network call or permission is added, this page and
`docs/site/haasi-al-layl/privacy/index.html` change with it.

## Before the first upload

- [ ] **Application ID.** `com.aistudio.haasiallayl.hall` is AI Studio's prefix and can never
      change once published. Pick your own (e.g. `com.ahsanulhouqe.haasiallayl`) in
      `app/build.gradle.kts` first.
- [ ] **Remove the unused Firebase dependencies** in `app/build.gradle.kts`: `firebase-bom`,
      `firebase-ai`, `firebase-appcheck-recaptcha`, `firebase-appcheck-debug`. No code uses them,
      and the App Check *debug* provider must not ship in a release build.
- [ ] **Publish the site folder.** Upload `docs/site/haasi-al-layl/` to the root of
      ahsanulhouqe.com, so the policy is at `https://www.ahsanulhouqe.com/haasi-al-layl/privacy/`
      and the guide at `…/haasi-al-layl/guide/`. The app links to both
      (`DeveloperContact.PRIVACY_POLICY`, `USER_GUIDE`); the privacy URL also goes into Play
      Console → App content → Privacy policy. See `docs/site/README.md`.
- [ ] **Closed test first.** New personal developer accounts must run a closed test with at least
      12 testers for 14 days before production access.
- [ ] Release signing key and a `versionCode` bump per upload.

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
