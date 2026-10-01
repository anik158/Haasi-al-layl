# Website files for ahsanulhouqe.com

Upload the `haasi-al-layl/` folder to the root of the site, as it is:

| Page | Address | Source |
| --- | --- | --- |
| Privacy policy | `https://www.ahsanulhouqe.com/haasi-al-layl/privacy/` | `haasi-al-layl/privacy/index.html` |
| User guide | `https://www.ahsanulhouqe.com/haasi-al-layl/guide/` | `haasi-al-layl/guide/index.html` |

The app opens both addresses (`DeveloperContact.PRIVACY_POLICY`, `DeveloperContact.USER_GUIDE`), so
they must not move. Both pages are plain HTML with no scripts to load, which is what Google's review
needs: a public, HTTPS page with no login, readable without JavaScript.

## Screenshots for the guide

Put these in `haasi-al-layl/guide/images/`, from the **current** build, in portrait. Until a file is
there, the guide shows a dashed slot saying what belongs in it.

| File | What to capture | Status |
| --- | --- | --- |
| `01-location-disclosure.png` | First launch: the "Why the app needs your location" dialog | added |
| `02-today.png` | Today, top of the screen, during a prayer's time (current + next prayer) | added |
| `03-alarm.png` | The alarm screen — Settings → "Test alert in 1 minute", then lock the phone | added |
| `04-reliability.png` | Settings → Notification reliability, ideally all rows ✓ | **needed** |
| `05-qibla.png` | The Qibla tab | **needed** |
| `02b-prayers.png` | Today's Prayers list | added |
| `06-duas.png` | A Hisn al-Muslim dua; the waking-up chapter's third entry ("Lā ilāha illallāhu waḥdahu…") has a "Virtue" box | added (chapter index) |
| `07-names.png` | The ﷲ99 tab | **needed** (the old one predates Bengali pronunciation) |

Before capturing, clear personal notifications from the status bar and use a city you are happy
to show.
