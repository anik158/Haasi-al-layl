# Plan: finishing Sehri & Iftar

Status: **waiting for approval.** Nothing below is built yet.

## What already works (shipped)

- Today's **Sehri ends** (= Fajr) and **Iftar** (= Maghrib), with a countdown, offline.
- Forbidden / discouraged / recommended days, with checked references.
- Placement: under the prayer card in Ramadan, above Today's verse otherwise.

## What is missing — the "half-cooked" part

1. **Which days are Ramadan, Eid and Arafah is a calculation**: Umm al-Qura (`HijrahDate`) plus
   one fixed shift for every month (−1 for BD/IN/PK/NP/LK/MA, or the user's choice in Settings).
   It can be a day off from the moon sighting in the user's country, and the app cannot correct
   itself.
2. **No online source.** The app never asks Saudi Arabia's announced date.
3. **No Sehri alarm.** Nothing wakes the user for Sehri.

## How the date is actually decided (the facts the plan is built on)

- **Saudi Arabia:** the High Judiciary Council announces the start of Muharram, Ramadan, Shawwal
  and Dhul-Hijjah after a sighting. Aladhan publishes it (`calendarMethod=HJCoSA`, free, no key).
- **Turkey:** Diyanet calculates its calendar in advance. Aladhan publishes it (`DIYANET`).
- **Bangladesh:** the **National Moon Sighting Committee** (chaired by the religious affairs
  minister, hosted by the Islamic Foundation at Baitul Mukarram) meets on the evening a month can
  start. It reviews reports from district administrations, the Meteorological Department, SPARRSO
  and Islamic Foundation offices, then announces through a press release, TV and newspapers. It
  does this for ordinary months too (e.g. Jumada al-Ula), not only Ramadan and Eid. Newspapers
  print the Hijri date by counting days from the latest announcement. **There is no API or data
  feed.**
- In practice Bangladesh starts a month on the **same day as Saudi Arabia or one day later**.

## The plan — three steps, each tested before it reaches your phone

### Step 1 — Know when the month starts: Saudi Arabia online + the moon, no user question

No "was the moon sighted?" card. The date is worked out automatically, and every day where it
matters carries a warning.

**1a. Saudi Arabia is the anchor (online, cached).** Fetch Aladhan `calendarMethod=HJCoSA` (free,
no key): the High Judiciary Council's announced starts of Muharram, Ramadan, Shawwal and
Dhul-Hijjah; the other months follow Umm al-Qura. Cache it, so it keeps working offline; refresh
once a day and every evening a month can start. Checked: HJCoSA gives 1 Ramadan 1447 = Wed 18 Feb
2026 (Bangladesh began Thu 19 Feb — one day later).

**1b. Other countries: Saudi date + the moon at the user's location.** Countries fall in two
groups:

| Group | Countries (examples) | Month starts on |
| --- | --- | --- |
| Follow Saudi Arabia | SA, AE, QA, KW, BH, YE … | Saudi's date, as announced |
| Local sighting | BD, IN, PK, NP, LK, MA … | Saudi's date **or one day later**, decided by the moon (below) |

For a local-sighting country, on the evening before Saudi's 1st, `MoonCalculator` checks the new
crescent at the user's location after sunset — the moon's age, how long it stays up after the sun,
its height and its distance from the sun — with a standard visibility test (Odeh / Yallop):

- crescent **clearly visible** to the naked eye there → the month starts the same day as Saudi
  Arabia;
- otherwise → one day later (and never past day 30 of the old month).

`MoonCalculator` is accurate enough for this (moonrise for Dhaka came out one minute off a
published table). What no calculation can know is cloud, haze and the committee's decision, so the
result is a best estimate, not an announcement.

**1c. Offline with nothing cached:** Umm al-Qura plus the same moon test.

**1d. Settings keeps the manual adjustment** (−2…+2) as an escape hatch, but **per month**, not one
shift for the whole year.

**1e. Warnings — always, on the days that matter.** The Sehri & Iftar card and the fasting-day rules
show a note on:

- the days a month can start (29th, 30th) and the first two days of Ramadan;
- the last days of Ramadan and **Eid al-Fitr**;
- **Arafah, Eid al-Adha and the Tashreeq days** (11–13 Dhul-Hijjah);
- every **forbidden** day, and the day of doubt (30 Sha'ban).

Worded by source: *"Saudi Arabia announced … Bangladesh is expected to follow on …, based on the
moon. Confirm with your country's announcement."* — or *"calculated, may differ by a day"* when
offline with nothing cached.

**Tests first:**
- New-moon times and the visibility test against published tables.
- Past Bangladesh starts (from news archives) against "Saudi date + moon test", for Ramadan,
  Shawwal and Dhul-Hijjah of several years. The accuracy is written down here before shipping.
- A fake server for HJCoSA; offline uses the cache, or the calculation.
- Eid, Arafah and the forbidden days move with the resolved start; the warning appears on each.

### Step 2 — Sehri alarm

- A new setting: **Sehri reminder**, off by default, with "how long before Sehri ends":
  30 / 45 / 60 / 90 minutes.
- Rings like the prayer alarm (5 seconds), only on days the user is fasting: every day of
  Ramadan, plus days the user taps "I'm fasting tomorrow" on the card.
- No Iftar alarm of its own — Iftar is Maghrib, which already has the prayer alert.
- Test first: the alarm planner arms it at the right minute, only on fasting days, and never on a
  forbidden day.

### Not in this plan

- **A Bangladesh feed** — a small server job that reads the National Moon Sighting Committee's
  press release (reported by BSS / UNB) on sighting nights and publishes `bd.json`, which would
  override the estimate in 1b. Later, if wanted; best of all would be the Islamic Foundation
  publishing it themselves.
- **A "was the moon sighted?" question.** Dropped: the date comes from Saudi Arabia + the moon.

## What I need from you

- **Approve** steps 1 and 2.
- **Sehri reminder default:** off, with 60 minutes as the suggested lead time — OK?
- **"I'm fasting tomorrow" for voluntary fasts:** include it in step 2, or Ramadan-only alarm?

Nothing changes in the app until you say yes.
