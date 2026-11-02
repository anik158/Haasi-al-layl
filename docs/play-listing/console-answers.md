# Play Console — what to upload and what to answer

Work through this top to bottom. Everything the console asks for is here or in this folder.

## What only you can do

1. **Create the developer account** at <https://play.google.com/console/signup>: choose
   *Yourself* (personal account), pay the one-time US$25 fee, and verify your identity (a
   government ID and a phone number). Verification can take a few days.
2. **Re-upload the website folders.** `docs/site/haasi-al-layl/privacy/` and `…/guide/` changed
   (new feedback email, new screenshots). Upload both to ahsanulhouqe.com, and update the
   localized guides on the site by hand.
3. **Find 12 testers** for the closed test (see "Release" below). Start early; it is the slowest
   step.
4. **Keep the upload key safe** (see the next section).

## The upload key (`my-upload-key.jks`) in plain words

- A `.jks` file is a small locked box holding a secret key. Signing the `.aab` with it proves to
  Google that an upload came from you. The password opens the box.
- It sits in the project root, and git ignores it (`*.jks` in `.gitignore`), so it never ends up
  in a commit.
- **Back it up in two places** now (a cloud drive and a USB stick), and put the password in a
  password manager. Never put either in a chat, an email or the code.
- At the first upload, accept **Play App Signing**. Google then signs the app for users with its
  own key, and yours is only the *upload* key. If you lose it, Play support can reset it (it
  takes a few days). Without Play App Signing, a lost key would mean you could never update the
  app.
- Building a new `.aab` later, from the project root (Gradle 9.3.1 and a full JDK, e.g. JDK 23):

  ```bash
  KEYSTORE_PATH=$PWD/my-upload-key.jks STORE_PASSWORD='…' KEY_PASSWORD='…' gradle bundleRelease
  ```

  The file is `app/build/outputs/bundle/release/haasi-al-layl-release.aab`. Every upload needs a
  higher `versionCode` than the last (`app/build.gradle.kts`; a local build is 1).

## Create app

| Field | Answer |
| --- | --- |
| App name | Haasi al-Layl: Prayer & Qibla |
| Default language | English (United States) – en-US |
| App or game | App |
| Free or paid | Free (this cannot be changed to paid later) |
| Declarations | Tick both (Developer Program Policies, US export laws) |

## Store listing

Text: [`listing-en.md`](listing-en.md) as the default, plus translations under **Manage
translations → Add your own translations**: [`listing-bn.md`](listing-bn.md) (bn-BD),
[`listing-ar.md`](listing-ar.md) (ar), [`listing-es.md`](listing-es.md) (es-419 and es-ES) and
[`listing-pt.md`](listing-pt.md) (pt-BR and pt-PT). Play matches searches against the listing in
the searcher's language, so an untranslated locale only sees the English text. Graphics: `icon-512.png`, `feature-graphic-1024x500.png`, and the phone screenshots in
`screenshots/`.

| Field | Answer |
| --- | --- |
| Category | Lifestyle |
| Tags | Religion & spirituality (if offered), Lifestyle |
| Contact email | contact@ahsanulhouqe.com |
| Website | https://www.ahsanulhouqe.com/ |
| Phone | Leave empty |

## App content (Policy → App content)

| Section | Answer |
| --- | --- |
| Privacy policy | `https://www.ahsanulhouqe.com/haasi-al-layl/privacy/` |
| App access | All functionality is available without special access (no login) |
| Ads | No, my app does not contain ads |
| Content rating | See below |
| Target audience | **13–15, 16–17, 18 and over.** Do not tick any age under 13: that would put the app under the Families policy, with extra review. The app is still fine for children to use. |
| Appeal to children | No |
| News app | No |
| COVID-19 contact tracing | No |
| Data safety | See below |
| Government app | No |
| Financial features | My app doesn't provide any financial features |
| Health | No health features |
| Advertising ID | No, the app does not use an advertising ID |
| Full-screen intent (`USE_FULL_SCREEN_INTENT`) | Yes, it is needed. Use case: **Alarm** — "Prayer alarms ring at the prayer time and show over the lock screen like an alarm clock." |

`SCHEDULE_EXACT_ALARM` needs no form (only `USE_EXACT_ALARM` does, and the app does not use it).
Location is foreground only, so there is no background-location form.

### Content rating questionnaire (IARC)

- Email: contact@ahsanulhouqe.com
- Category: **All other app types** (it is not a game, social or communication app).
- Violence, fear, sexuality, profanity, drugs/alcohol/tobacco, gambling, crude humour: **No** to all.
- Does the app let users interact or exchange content? **No**
- Does it share the user's current physical location with other users? **No**
- Does it allow purchases of digital goods? **No**
- Is it a web browser or search engine? **No**

Expected rating: Everyone / PEGI 3 / IARC 3+.

### Data safety

Overview:

| Question | Answer |
| --- | --- |
| Does your app collect or share any of the required user data types? | **Yes** |
| Is all of the user data encrypted in transit? | **Yes** (every request is HTTPS) |
| Which ways can users request that their data is deleted? | Nothing is stored off the phone; uninstalling or clearing the app's data deletes it. (If a URL is required, use the privacy policy URL.) |

Data types to tick, and for each: **Shared** (not collected: nothing reaches the developer),
**processed ephemerally: Yes**, **optional: Yes**, purpose **App functionality**.

| Data type | Goes to | Why |
| --- | --- | --- |
| Location → Precise location | BigDataCloud | Coordinates → town name |
| Location → Approximate location | ipinfo.io | From the IP address, only when the phone gives no location |
| App activity → Other user-generated content | Open-Meteo | City names typed in search |

Everything else: **not collected**. No personal info, financial info, health, messages, photos,
audio, files, calendar, contacts, web history, app performance or device IDs. No advertising, no
analytics, nothing sold. (Aladhan receives only a Hijri month and year, not user data.)

## Release

New personal accounts cannot publish to production straight away:

1. **Testing → Internal testing** (optional, instant, up to 100 testers by email): create a release
   and upload the `.aab` to check that it installs.
2. **Testing → Closed testing → Create track** — add **at least 12 testers** (their Gmail
   addresses, or a Google Group). They must opt in from the link Play gives you and keep the app
   installed for **14 days in a row**.
3. Create a release on the closed track: accept **Play App Signing**, upload
   `haasi-al-layl-release.aab`, release name `1.0.0 (1)`, release notes e.g. *First release:
   prayer times and alarms, Qibla, Tasbih, Hisn al-Muslim, the 99 Names.* Then **Review → Start
   rollout**.
4. After the 14 days: **Dashboard → Apply for production**. Google asks a few questions about the
   test, then you can roll out to production.

Countries: start with all, or at least Bangladesh, India, Pakistan, the Gulf and the countries
where Spanish and Portuguese are spoken, since those listings exist.
